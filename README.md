# 🎵 MP3 Player 2.0.0 — Hardware-Accelerated Vector Music Experience in Minecraft

![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1%20--%201.21.11-brightgreen?style=for-the-badge&logo=minecraft)
![Fabric](https://img.shields.io/badge/Loader-Fabric-orange?style=for-the-badge)
![UI](https://img.shields.io/badge/UI%20Engine-NanoVG%20GL3-blue?style=for-the-badge)
![Version](https://img.shields.io/badge/Version-2.0.0-gold?style=for-the-badge)

**MP3 Player 2.0.0** — масштабное переосмысление музыкального мода для Minecraft (Fabric 1.21.1 — 1.21.11). В версии 2.0.0 интерфейс был полностью переписан с нуля на аппаратный векторный движок **NanoVG (OpenGL 3)**, добавлена система внешнего медиа-контроля (Windows GSMTC), динамический HUD-оверлей («Золотая капсула») и пространственное 3D-аудио.

---

## 📸 Скриншоты интерфейса 2.0.0

| Главное меню плеера и плейлист | Интеграция с внешними плеерами (GSMTC) |
| :---: | :---: |
| ![MP3 Player Главный экран](screenshots/player_main.jpg) | ![Webs / Apps Экран](screenshots/webs_apps.jpg) |

---

## ✨ Что нового в версии 2.0.0

### 🎨 Аппаратный векторный интерфейс (NanoVG GL3)
* **Премиальный дизайн:** Идеально сглаженные закругленные углы (antialiased rounded rectangles), радиальные и линейные градиенты, мягкие тени (box drop-shadows) и динамические эффекты наведения.
* **Кастомные шрифты и иконки:** Встроенные типографические гарнитуры и векторный шрифт **Material Icons** для всех элементов управления.
* **Плавные интерактивные анимации:** Анимированный эквалайзер в реальном времени, реактивные слайдеры громкости и радиуса вещания, плавное переключение между светлой и тёмной темами.

### 💊 Динамический HUD-оверлей («Золотая капсула»)
* В верхней части экрана во время воспроизведения отображается компактная плавающая капсула с обложкой трека, названием, исполнителем, таймлайном, динамическим эквалайзером и кнопкой быстрой паузы/воспроизведения.

### 🌐 Вкладка «Webs / Apps» (Интеграция с Windows GSMTC)
* Мод напрямую перехватывает воспроизведение системных медиаплееров через Windows Global System Media Transport Controls: **Spotify, Яндекс Музыка, VK Музыка, YouTube Music, браузеры (Chrome, Firefox, Edge)**.
* Автоматическое извлечение обложек альбомов в высоком разрешении, названий и состояния воспроизведения.

### 🔊 Режим «Колонка» (Пространственный 3D-звук)
* Трансляция музыки окружающим игрокам на сервере с реалистичным затуханием звука в зависимости от дистанции (настраиваемый радиус вещания с удобным слайдером прямо в интерфейсе).

### 💿 Кастомные виниловые пластинки & P2P передача
* Запись любого трека из библиотеки или сети на пластинку с синхронизацией в ванильном проигрывателе (Jukebox).
* Безопасная P2P-передача треков между игроками по сети через оптимизированное пакетирование (чанкинг).

---

## 🛠️ Архитектура и метод рендеринга: Direct Framebuffer + NanoVG Sandbox

Главная инженерная инновация версии 2.0.0 — отказ от ванильного растеризатора `DrawContext` / `GuiGraphics` в пользу низкоуровневого векторного движка **NanoVG GL3**, интегрированного напрямую в графический пайплайн Minecraft.

### 1. Direct Framebuffer Hooking (Прямой хук в сцену)
В современных версиях Minecraft (1.21+) игра использует изолированные текстуры кадрового буфера (`GlTexture` / `GpuTexture`). Чтобы NanoVG рисовал без оверхеда и мерцаний:
* Мод извлекает дескриптор нативной текстуры цвета через `MinecraftClient.getInstance().getFramebuffer().getColorAttachment().getGlId()`.
* Текстура монтируется в отдельный вспомогательный FBO (`helperFbo`) с буфером трафарета и глубины `GL_DEPTH24_STENCIL8`. NanoVG рисует векторные контуры и маски напрямую в текстуру основного экрана с полным сглаживанием.

```java
// Привязка цветового аттачмента Minecraft к вспомогательному FBO NanoVG
GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, this.helperFbo);
GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, glTexId, 0);
GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_STENCIL_ATTACHMENT, GL30.GL_RENDERBUFFER, this.helperRbo);
```

### 2. OpenGL State Sandboxing (Изоляция состояний)
Рендерер Minecraft (`GlCommandEncoder`) непрерывно меняет шейдерные программы, VAO, буферы вершин и индексов. Метод `beginFrame()` полностью сохраняет текущее состояние OpenGL, а `endFrame()` возвращает его с точностью до бита:
* Сохраняются и восстанавливаются: `GL_DRAW_FRAMEBUFFER_BINDING`, `GL_CURRENT_PROGRAM`, `GL_VERTEX_ARRAY_BINDING`, `GL_ARRAY_BUFFER_BINDING`, `GL_ELEMENT_ARRAY_BUFFER_BINDING`, `GL_BLEND`, уравнения блендинга, маски глубины и трафарета, Scissor/Viewport.

### 3. Решение конфликта Sampler Objects (Mipmapping Fix)
В Minecraft 1.21.11 движок привязывает **OpenGL Sampler Objects** (`glBindSampler(unit, sampler)`) с фильтром `GL_LINEAR_MIPMAP_LINEAR`. 
* **Проблема:** Атлас шрифтов и векторных значков NanoVG генерируется в реальном времени с одним уровнем детализации (без мипмап-пирамиды). Привязанный майнкрафтовский сэмплер переопределял параметры текстуры атласа, делая её неполной (*texture incomplete*). Видеокарта возвращала прозрачный цвет `(0, 0, 0, 0)`, из-за чего интерфейс рисовался, но текст и иконки были невидимыми.
* **Решение:** В `beginFrame()` мод временно отвязывает сэмплеры с текстурных юнитов (`GL33.glBindSampler(i, 0)`), а в `endFrame()` корректно восстанавливает их:

```java
// Разрешение конфликта со сэмплерами Minecraft 1.21.11+
for (int i = 0; i < 8; i++) {
    GL13.glActiveTexture(GL13.GL_TEXTURE0 + i);
    this.savedSamplers[i] = GL11.glGetInteger(GL33.GL_SAMPLER_BINDING);
    if (this.savedSamplers[i] != 0) {
        GL33.glBindSampler(i, 0);
    }
}
```

### 4. Изоляция Pixel Unpack Buffer & UBO
* При динамической растеризации глифов шрифтов из памяти NanoVG выполняет `glTexSubImage2D`. Если в Minecraft активен `GL_PIXEL_UNPACK_BUFFER`, видеокарта интерпретирует указатель как смещение внутри GPU-буфера. Мод гарантированно сбрасывает `glBindBuffer(GL_PIXEL_UNPACK_BUFFER, 0)` и выравнивание `GL_UNPACK_ALIGNMENT = 1`.
* Сохраняется и восстанавливается привязка Uniform Buffer Object (`GL31.GL_UNIFORM_BUFFER_BINDING`).

---

## ⌨️ Управление

* **Правый Shift (Right Shift):** Открыть / закрыть главное меню плеера.
* **Управление треками:** Перемотка на $\pm 10$ секунд, пауза/плей, следующий/предыдущий трек, слайдер громкости.
* **Режим колонки:** Кнопка «Колонка» в правом верхнем углу меню переключает режим между локальным наушником и 3D-вещанием для всех игроков.

---

## 📦 Сборка проекта

Проект собирается с помощью Gradle и Fabric Loom:

```bash
# Клонировать репозиторий и переключиться на ветку v2.0.0
git clone https://github.com/zabugorniyhuy-max/mp3player.git -b v2.0.0
cd mp3player

# Собрать jar-мод (требуется Java 21)
./gradlew build
```

Готовый файл мода появится в `build/libs/mp3player-1.21.11.jar`.
