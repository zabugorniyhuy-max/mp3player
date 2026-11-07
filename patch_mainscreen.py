import re

file_path = r'c:\Users\Владимир\Desktop\Projects\Моды отдельно\mp-3 player\mp3player-fabric\src\main\java\com\mp3player\client\gui\MainScreen.java'

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Add t method
t_method = """
    private String t(String en, String ru) {
        return ModConfig.getInstance().isEnglish ? en : ru;
    }
"""
if "private String t(" not in content:
    content = content.replace("protected void init() {", t_method + "\n    protected void init() {")

# Replace emojis and texts in render methods
replacements = [
    ('this.textRenderer.getWidth("MP3 Player")', 'this.textRenderer.getWidth(t("MP3 Player", "MP3 Плеер"))'),
    ('this.textRenderer.getWidth("⭐")', 'this.textRenderer.getWidth(t("Fav", "Избр"))'),
    ('drawTab(ctx, mouseX, mouseY, panelX + 10, panelY - 20, "MP3 Player", 0, "MP3 Player");',
     'drawTab(ctx, mouseX, mouseY, panelX + 10, panelY - 20, t("MP3 Player", "MP3 Плеер"), 0, t("MP3 Player", "MP3 Плеер"));'),
    ('drawTab(ctx, mouseX, mouseY, panelX + 10 + tab1W + 5, panelY - 20, "⭐", 1, "Любимые песни");',
     'drawTab(ctx, mouseX, mouseY, panelX + 10 + tab1W + 5, panelY - 20, t("Fav", "Избр"), 1, t("Favorites", "Любимые песни"));'),
    
    ('drawButton(ctx, mouseX, mouseY, panelX + 4, panelY + 4, 16, 14, darkTheme ? "☾" : "☀", 200);',
     'drawButton(ctx, mouseX, mouseY, panelX + 4, panelY + 4, 24, 14, darkTheme ? t("Drk", "Тем") : t("Lgt", "Св"), 200);\n        drawButton(ctx, mouseX, mouseY, panelX + 32, panelY + 4, 24, 14, ModConfig.getInstance().isEnglish ? "EN" : "RU", 204);'),
     
    ('drawButton(ctx, mouseX, mouseY, panelX + pw - 65, panelY + 24, 55, 14, "🌍 Колонка", 202, sm ? accent() : textSecondary());',
     'drawButton(ctx, mouseX, mouseY, panelX + pw - 65, panelY + 24, 55, 14, t("Speaker", "Колонка"), 202, sm ? accent() : textSecondary());'),
     
    ('drawButton(ctx, mouseX, mouseY, panelX + pw - 85, panelY + 24, 16, 14, mg ? "🔇" : "🔊", 203, mg ? 0xFFFF5555 : textSecondary());',
     'drawButton(ctx, mouseX, mouseY, panelX + pw - 90, panelY + 24, 20, 14, mg ? t("Mut", "Мут") : t("Vol", "Зв"), 203, mg ? 0xFFFF5555 : textSecondary());'),
     
    ('String title = currentTab == 0 ? "MP3 Player" : "Любимые песни";',
     'String title = currentTab == 0 ? t("MP3 Player", "MP3 Плеер") : t("Favorites", "Любимые песни");'),
     
    ('"Отправка файлов: "', 't("Uploading: ", "Отправка файлов: ")'),
    ('"Скачивание: "', 't("Downloading: ", "Скачивание: ")'),
    ('"Right Shift - закрыть"', 't("Right Shift - close", "Right Shift - закрыть")'),
    
    # renderTrackMenu
    ('String[] opts = {"♥ Нравится", "🔁 Повтор", "💿 Сделать пластинкой", "i Сведения", "📤 Поделиться", "✖ Удалить песню"};',
     'String[] opts = {t("★ Favorite", "★ Нравится"), t("O Repeat", "O Повтор"), t("O Make Disc", "O На пластинку"), t("i Info", "i Сведения"), t("^ Share", "^ Поделиться"), t("X Delete", "X Удалить")};'),
    ('int w = 135;', 'int w = 145;'),
    
    # renderLocalTab
    ('drawButton(ctx, mouseX, mouseY, px + 10, topY - 2, 20, 14, "🔄", 110);',
     'drawButton(ctx, mouseX, mouseY, px + 10, topY - 2, 24, 14, t("Upd", "Обн"), 110);'),
    ('drawButton(ctx, mouseX, mouseY, px + 35, topY - 2, 20, 14, "📁", 111);',
     'drawButton(ctx, mouseX, mouseY, px + 38, topY - 2, 24, 14, t("Dir", "Пап"), 111);'),
     
    ('"Треков: "', 't("Tracks: ", "Треков: ")'),
    ('"Конвертация MP3..."', 't("Converting MP3...", "Конвертация MP3...")'),
    ('"Добавьте .mp3 или .wav в папку mp3player"', 't("Add .mp3 or .wav to mp3player folder", "Добавьте .mp3 или .wav в папку mp3player")'),
    ('"MP3 конвертируются автоматически"', 't("MP3s are converted automatically", "MP3 конвертируются автоматически")'),
    
    ('"▶ "', '"> "'),
    ('"♥ "', '"★ "'),
    ('"⏸"', '"||"'),
    ('playIcon = current ? "⏸" : "▶";', 'playIcon = current ? "||" : ">";'),
    ('"⋮"', '"..."'),
    
    # renderPlayerBar
    ('"Колонка"', 't("Speaker", "Колонка")'),
    ('"Ничего не играет"', 't("Nothing playing", "Ничего не играет")'),
    ('"Дальность"', 't("Range", "Дальность")'),
    ('"Громкость"', 't("Vol", "Громкость")')
]

for old, new in replacements:
    content = content.replace(old, new)

# Add mouseClick logic for EN/RU toggle
click_theme_logic = """        // Тема
        if (isInside(mx, my, px + 4, py + 4, 24, 14)) {
            playClickSound();
            darkTheme = !darkTheme;
            ModConfig.getInstance().darkTheme = darkTheme;
            ModConfig.getInstance().save();
            return true;
        }
        
        // Кнопка языка
        if (isInside(mx, my, px + 32, py + 4, 24, 14)) {
            playClickSound();
            ModConfig.getInstance().isEnglish = !ModConfig.getInstance().isEnglish;
            ModConfig.getInstance().save();
            return true;
        }"""
        
content = re.sub(r'// Тема\s*if \(isInside\(mx, my, px \+ 4, py \+ 4, 16, 14\)\) \{.*?return true;\s*\}', click_theme_logic, content, flags=re.DOTALL)

# Fix hover inside mouseClicked
content = content.replace('int tab2W = this.textRenderer.getWidth("⭐") + 20;', 'int tab2W = this.textRenderer.getWidth(t("Fav", "Избр")) + 20;')

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("MainScreen patched successfully")
