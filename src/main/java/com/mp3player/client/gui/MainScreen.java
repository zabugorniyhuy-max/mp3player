package com.mp3player.client.gui;

import com.mp3player.client.ModSounds;
import com.mp3player.client.audio.MusicPlayer;
import com.mp3player.client.config.ModConfig;
import com.mp3player.client.network.FileReceiverManager;
import com.mp3player.client.network.FileSenderManager;
import com.mp3player.network.ClearDiscPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.text.Text;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class MainScreen extends Screen {
    // Вкладки: 0 = MP3 Player (локальные файлы), 1 = Любимые песни
    private int currentTab = 0;
    
    // Анимация
    private float animationProgress = 0f;
    private boolean isClosing = false;
    private static final float ANIMATION_SPEED = 0.12f;
    
    // Тема
    private static boolean darkTheme = true;
    
    // Плееры
    private final MusicPlayer localPlayer = MusicPlayer.getInstance();
    private final File musicFolder;
    
    // UI
    private Set<Integer> pressedButtons = new HashSet<>();
    private boolean draggingVolume = false;
    private boolean draggingRange = false;
    private boolean draggingProgress = false;
    private int localTrackScroll = 0;
    private int favTrackScroll = 0;
    
    // Меню трека
    private int trackMenuIndex = -1;
    private int trackMenuX = 0;
    private int trackMenuY = 0;
    
    public MainScreen() {
        super(Text.literal("Music Player"));
        darkTheme = ModConfig.getInstance().darkTheme;
        currentTab = ModConfig.getInstance().currentTab;
        localPlayer.setVolume(ModConfig.getInstance().musicVolume);
        
        musicFolder = new File(MinecraftClient.getInstance().runDirectory, "mp3player");
        if (!musicFolder.exists()) musicFolder.mkdirs();
        localPlayer.loadMusicFolder(musicFolder);
    }

    @Override
    protected void init() {
    }
    
    private void playClickSound() {
        if (this.client != null) {
            this.client.getSoundManager().play(PositionedSoundInstance.master(ModSounds.KNOCK, 1.0f));
        }
    }
    
    // Цвета
    private int bgPanel() { return darkTheme ? 0xEE1a1a1a : 0xFFe8e8e8; }
    private int bgDark() { return darkTheme ? 0xFF151515 : 0xFFffffff; }
    private int bgButton() { return darkTheme ? 0xFF2a2a2a : 0xFF9a9a9a; }
    private int bgHover() { return darkTheme ? 0xFF3a3a3a : 0xFF7a7a7a; }
    private int textPrimary() { return darkTheme ? 0xFFFFFFFF : 0xFF1a1a1a; }
    private int textSecondary() { return darkTheme ? 0xFF888888 : 0xFF3a3a3a; }
    private int accent() { return 0xFFFFCC00; }
    private int border() { return darkTheme ? 0xFF3d3d3d : 0xFF8a8a8a; }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, this.width, this.height, 0x44000000);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        if (isClosing) {
            animationProgress -= ANIMATION_SPEED;
            if (animationProgress <= 0) { this.close(); return; }
        } else if (animationProgress < 1f) {
            animationProgress = Math.min(1f, animationProgress + ANIMATION_SPEED);
        }
        
        float eased = easeOutCubic(animationProgress);
        int panelW = 320, panelH = 220;
        int centerX = this.width / 2;
        int targetY = this.height / 2 - panelH / 2;
        int panelY = (int) (this.height + 20 + (targetY - this.height - 20) * eased);
        int panelX = centerX - panelW / 2;
        
        // Вкладки сверху
        int tab1W = this.textRenderer.getWidth("MP3 Player") + 20;
        drawTab(ctx, mouseX, mouseY, panelX + 10, panelY - 20, "MP3 Player", 0, "MP3 Player");
        drawTab(ctx, mouseX, mouseY, panelX + 10 + tab1W + 5, panelY - 20, "⭐", 1, "Любимые песни");
        
        // Главная панель
        ctx.fill(panelX, panelY, panelX + panelW, panelY + panelH, bgPanel());
        ctx.fill(panelX, panelY, panelX + panelW, panelY + 2, accent());
        
        // Кнопка темы
        drawButton(ctx, mouseX, mouseY, panelX + 4, panelY + 4, 16, 14, darkTheme ? "☾" : "☀", 200);
        
        // Кнопка Hitmo
        drawButton(ctx, mouseX, mouseY, panelX + panelW - 50, panelY + 6, 40, 14, "Hitmo", 201);
        
        // Кнопка Колонка
        boolean sm = ModConfig.getInstance().speakerMode;
        drawButton(ctx, mouseX, mouseY, panelX + panelW - 65, panelY + 24, 55, 14, "🌍 Колонка", 202, sm ? accent() : textSecondary());
        if (sm) ctx.fill(panelX + panelW - 65, panelY + 36, panelX + panelW - 10, panelY + 38, accent());
        
        // Кнопка Мут Глобальный
        boolean mg = ModConfig.getInstance().muteGlobal;
        drawButton(ctx, mouseX, mouseY, panelX + panelW - 85, panelY + 24, 16, 14, mg ? "🔇" : "🔊", 203, mg ? 0xFFFF5555 : textSecondary());
        
        // Заголовок
        String title = currentTab == 0 ? "MP3 Player" : "Любимые песни";
        DrawUtils.drawText(ctx, this.textRenderer, title, panelX + 25, panelY + 10, accent(), true);
        
        if (currentTab == 0) {
            renderLocalTab(ctx, mouseX, mouseY, panelX, panelY, panelW, panelH, false);
        } else {
            renderLocalTab(ctx, mouseX, mouseY, panelX, panelY, panelW, panelH, true);
        }
        
        // Плеер внизу
        renderPlayerBar(ctx, mouseX, mouseY, panelX, panelY + panelH - 55, panelW);
        
        // Рендер всплывающего меню поверх всего
        if (trackMenuIndex != -1) {
            renderTrackMenu(ctx, mouseX, mouseY);
        }
        
        int uploads = FileSenderManager.getInstance().getUploadsCount();
        int downloads = FileReceiverManager.getInstance().getDownloadsCount();
        if (uploads > 0) {
            DrawUtils.drawText(ctx, this.textRenderer, "Отправка файлов: " + uploads, panelX + panelW - 120, panelY - 12, accent(), true);
        }
        if (downloads > 0) {
            DrawUtils.drawText(ctx, this.textRenderer, "Скачивание: " + downloads, panelX + panelW - 120, panelY - 24, 0xFF55FF55, true);
        }
        
        ctx.drawCenteredTextWithShadow(this.textRenderer, "Right Shift - закрыть", centerX, panelY + panelH + 5, textSecondary());
        super.render(ctx, mouseX, mouseY, delta);
    }
    
    private void renderTrackMenu(DrawContext ctx, int mouseX, int mouseY) {
        int w = 135;
        int h = 84;
        ctx.fill(trackMenuX - 1, trackMenuY - 1, trackMenuX + w + 1, trackMenuY + h + 1, border());
        ctx.fill(trackMenuX, trackMenuY, trackMenuX + w, trackMenuY + h, bgDark());
        
        String[] opts = {"♥ Нравится", "🔁 Повтор", "💿 Сделать пластинкой", "i Сведения", "📤 Поделиться", "✖ Удалить песню"};
        for (int i = 0; i < 6; i++) {
            int y = trackMenuY + i * 14;
            boolean hover = mouseX >= trackMenuX && mouseX < trackMenuX + w && mouseY >= y && mouseY < y + 14;
            if (hover) ctx.fill(trackMenuX, y, trackMenuX + w, y + 14, bgHover());
            int color = textPrimary();
            if (i == 1 && localPlayer.isRepeat()) color = accent();
            String icon = opts[i].substring(0, opts[i].indexOf(' '));
            String text = opts[i].substring(opts[i].indexOf(' ') + 1);
            int iconW = this.textRenderer.getWidth(icon);
            DrawUtils.drawText(ctx, this.textRenderer, icon, trackMenuX + 10 - iconW / 2, y + 3, color, false);
            DrawUtils.drawText(ctx, this.textRenderer, text, trackMenuX + 20, y + 3, color, false);
        }
    }
    
    private void renderLocalTab(DrawContext ctx, int mouseX, int mouseY, int px, int py, int pw, int ph, boolean showFavorites) {
        int topY = py + 22;
        
        var playlist = showFavorites ? localPlayer.getFavorites() : localPlayer.getPlaylist();
        
        // Кнопки
        drawButton(ctx, mouseX, mouseY, px + 10, topY - 2, 20, 14, "🔄", 110);
        drawButton(ctx, mouseX, mouseY, px + 35, topY - 2, 20, 14, "📁", 111);
        DrawUtils.drawText(ctx, this.textRenderer, "Треков: " + playlist.size(), px + 65, topY, textSecondary(), false);
        
        if (localPlayer.isConverting()) {
            DrawUtils.drawText(ctx, this.textRenderer, "Конвертация MP3...", px + 140, topY, accent(), false);
        }
        
        // Список треков
        int listY = topY + 18, listH = ph - 90;
        ctx.fill(px + 9, listY - 1, px + pw - 9, listY + listH + 1, border());
        ctx.fill(px + 10, listY, px + pw - 10, listY + listH, bgDark());
        
        if (playlist.isEmpty()) {
            ctx.drawCenteredTextWithShadow(this.textRenderer, "Добавьте .mp3 или .wav в папку mp3player", px + pw / 2, listY + listH / 2 - 10, textSecondary());
            ctx.drawCenteredTextWithShadow(this.textRenderer, "MP3 конвертируются автоматически", px + pw / 2, listY + listH / 2 + 5, textSecondary());
        } else {
            int tY = listY + 2;
            int scroll = showFavorites ? favTrackScroll : localTrackScroll;
            for (int i = scroll; i < Math.min(playlist.size(), scroll + 10); i++) {
                File f = playlist.get(i);
                boolean hover = mouseX >= px + 10 && mouseX < px + pw - 10 && mouseY >= tY && mouseY < tY + 12;
                boolean current = f.equals(localPlayer.getPlaylist().isEmpty() ? null : localPlayer.getPlaylist().get(localPlayer.getCurrentIndex())) && localPlayer.isPlaying();
                boolean isFav = localPlayer.getFavorites().contains(f);
                if (hover) ctx.fill(px + 10, tY, px + pw - 10, tY + 12, bgHover());
                if (current) ctx.fill(px + 10, tY, px + 13, tY + 12, accent());
                String name = f.getName();
                int dot = name.lastIndexOf('.');
                if (dot > 0) name = name.substring(0, dot);
                if (name.length() > 30) name = name.substring(0, 28) + "..";
                
                String prefix = current ? "▶ " : (isFav ? "♥ " : "  ");
                DrawUtils.drawText(ctx, this.textRenderer, prefix + name, px + 14, tY + 2, current ? accent() : textPrimary(), false);
                
                // Кнопка плей и 3 точки
                boolean hoverPlay = isInside(mouseX, mouseY, px + pw - 40, tY, 15, 12);
                boolean hoverDots = isInside(mouseX, mouseY, px + pw - 20, tY, 15, 12);
                String playIcon = current ? "⏸" : "▶";
                DrawUtils.drawText(ctx, this.textRenderer, playIcon, px + pw - 38, tY + 2, hoverPlay ? accent() : textSecondary(), false);
                DrawUtils.drawText(ctx, this.textRenderer, "⋮", px + pw - 18, tY + 2, hoverDots ? accent() : textSecondary(), false);
                
                tY += 12;
            }
        }
    }


    private void renderPlayerBar(DrawContext ctx, int mouseX, int mouseY, int px, int py, int pw) {
        boolean sm = ModConfig.getInstance().speakerMode;
        if (sm) {
            ctx.fill(px - 1, py - 1, px + pw + 1, py + 56, accent());
            ctx.fill(px, py, px + pw, py + 55, bgDark());
        } else {
            ctx.fill(px, py, px + pw, py + 55, bgButton());
            ctx.fill(px, py, px + pw, py + 1, border());
        }
        
        int centerX = px + pw / 2;
        boolean localActive = localPlayer.isPlaying() || localPlayer.getCurrentPosition() > 0;
        
        // Название трека
        String trackInfo = "Ничего не играет";
        long dur = 0, pos = 0;
        boolean playing = false;
        float vol = localPlayer.getVolume();
        
        if (localActive) {
            trackInfo = localPlayer.getCurrentTrackName();
            dur = localPlayer.getDuration();
            pos = localPlayer.getCurrentPosition();
            playing = localPlayer.isPlaying();
            vol = localPlayer.getVolume();
        }
        
        if (sm) {
            DrawUtils.drawText(ctx, this.textRenderer, "Колонка", px + 4, py + 4, accent(), true);
            if (trackInfo.length() > 30) trackInfo = trackInfo.substring(0, 28) + "..";
            ctx.drawCenteredTextWithShadow(this.textRenderer, trackInfo, centerX + 15, py + 4, textPrimary());
        } else {
            if (trackInfo.length() > 40) trackInfo = trackInfo.substring(0, 38) + "..";
            ctx.drawCenteredTextWithShadow(this.textRenderer, trackInfo, centerX, py + 4, textPrimary());
        }
        
        // Прогресс
        int barX = px + 50, barY = py + 16, barW = pw - 100;
        ctx.fill(barX, barY, barX + barW, barY + 4, 0xFF555555);
        if (dur > 0) {
            int prog = (int) ((pos * barW) / dur);
            ctx.fill(barX, barY, barX + prog, barY + 4, accent());
        }
        DrawUtils.drawText(ctx, this.textRenderer, formatTime(pos / 1000) + "/" + formatTime(dur / 1000), barX + barW + 4, barY - 2, textSecondary(), false);
        
        // Кнопки
        int btnY = py + 32, btnS = 16, gap = 4;
        int btnW_10 = this.textRenderer.getWidth("<-10") + 8;
        int totalW = btnS * 3 + btnW_10 * 2 + gap * 4;
        int btnX = centerX - totalW / 2;
        if (sm) btnX += 10;
        
        drawButton(ctx, mouseX, mouseY, btnX, btnY, btnS, btnS, "⏮", 50);
        drawButton(ctx, mouseX, mouseY, btnX + btnS + gap, btnY, btnW_10, btnS, "<-10", 51);
        drawButton(ctx, mouseX, mouseY, btnX + btnS + gap + btnW_10 + gap, btnY, btnS, btnS, playing ? "⏸" : "▶", 52, accent());
        drawButton(ctx, mouseX, mouseY, btnX + btnS*2 + gap*2 + btnW_10 + gap, btnY, btnW_10, btnS, "+10>", 53);
        drawButton(ctx, mouseX, mouseY, btnX + btnS*2 + gap*2 + btnW_10*2 + gap*2, btnY, btnS, btnS, "⏭", 54);
        
        // Громкость и дальность
        if (sm) {
            int rangeX = px + 5;
            DrawUtils.drawText(ctx, this.textRenderer, "Радиус", rangeX, btnY - 5, textSecondary(), false);
            ctx.fill(rangeX, btnY + 5, rangeX + 45, btnY + 9, bgDark());
            int maxRange = 64, minRange = 8;
            float rProgress = (ModConfig.getInstance().speakerRange - minRange) / (float)(maxRange - minRange);
            ctx.fill(rangeX, btnY + 5, rangeX + (int)(rProgress * 45), btnY + 9, accent());
            
            int volX = px + pw - 60;
            DrawUtils.drawText(ctx, this.textRenderer, "🔊", volX, btnY - 5, textSecondary(), false);
            ctx.fill(volX, btnY + 5, volX + 45, btnY + 9, bgDark());
            ctx.fill(volX, btnY + 5, volX + (int)(vol * 45), btnY + 9, accent());
        } else {
            int volX = px + pw - 70;
            DrawUtils.drawText(ctx, this.textRenderer, "🔊", volX - 10, btnY + 3, textSecondary(), false);
            ctx.fill(volX, btnY + 5, volX + 55, btnY + 9, bgDark());
            ctx.fill(volX, btnY + 5, volX + (int)(vol * 55), btnY + 9, accent());
        }
    }
    
    private void drawTab(DrawContext ctx, int mouseX, int mouseY, int x, int y, String label, int idx, String tooltip) {
        int w = this.textRenderer.getWidth(label) + 20;
        boolean hover = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + 20;
        boolean sel = currentTab == idx;
        ctx.fill(x, y, x + w, y + 20, sel ? bgPanel() : (hover ? bgHover() : bgButton()));
        if (sel) ctx.fill(x, y, x + w, y + 2, accent());
        ctx.drawCenteredTextWithShadow(this.textRenderer, label, x + w / 2, y + 6, sel ? accent() : textSecondary());
    }
    
    private void drawButton(DrawContext ctx, int mouseX, int mouseY, int x, int y, int w, int h, String icon, int id) {
        drawButton(ctx, mouseX, mouseY, x, y, w, h, icon, id, textSecondary());
    }
    
    private void drawButton(DrawContext ctx, int mouseX, int mouseY, int x, int y, int w, int h, String icon, int id, int color) {
        boolean hover = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
        boolean pressed = pressedButtons.contains(id);
        ctx.fill(x, y, x + w, y + h, pressed ? 0xFF555555 : (hover ? bgHover() : bgButton()));
        if (hover && !pressed) ctx.fill(x, y + h - 2, x + w, y + h, accent());
        DrawUtils.drawText(ctx, this.textRenderer, icon, x + w / 2 - this.textRenderer.getWidth(icon) / 2, y + (h - 8) / 2, hover ? accent() : color, false);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        // Если клик вне меню - закрыть его
        if (trackMenuIndex != -1) {
            if (!isInside(mx, my, trackMenuX, trackMenuY, 135, 84)) {
                trackMenuIndex = -1;
            } else {
                handleMenuClick(mx, my);
                return true;
            }
        }
    
        float eased = easeOutCubic(animationProgress);
        int pw = 320, ph = 220;
        int px = this.width / 2 - pw / 2;
        int py = (int) (this.height + 20 + (this.height / 2 - ph / 2 - this.height - 20) * eased);
        
        // Вкладки сверху
        int tab1W = this.textRenderer.getWidth("MP3 Player") + 20;
        int tab2W = this.textRenderer.getWidth("⭐") + 20;
        if (isInside(mx, my, px + 10, py - 20, tab1W, 20)) { playClickSound(); currentTab = 0; ModConfig.getInstance().currentTab = currentTab; ModConfig.getInstance().save(); return true; }
        if (isInside(mx, my, px + 10 + tab1W + 5, py - 20, tab2W, 20)) { playClickSound(); currentTab = 1; ModConfig.getInstance().currentTab = currentTab; ModConfig.getInstance().save(); return true; }
        
        // Тема
        if (isInside(mx, my, px + 4, py + 4, 16, 14)) {
            playClickSound();
            darkTheme = !darkTheme;
            ModConfig.getInstance().darkTheme = darkTheme;
            ModConfig.getInstance().save();
            return true;
        }
        
        // Hitmo кнопка
        if (isInside(mx, my, px + pw - 50, py + 6, 40, 14)) {
            playClickSound();
            try {
                net.minecraft.util.Util.getOperatingSystem().open("https://rus.hitmoz.org/");
            } catch (Exception e) {}
            return true;
        }
        
        // Колонка кнопка
        if (isInside(mx, my, px + pw - 65, py + 24, 55, 14)) {
            playClickSound();
            ModConfig.getInstance().speakerMode = !ModConfig.getInstance().speakerMode;
            ModConfig.getInstance().save();
            if (ModConfig.getInstance().speakerMode) {
                if (localPlayer.isPlaying()) {
                    localPlayer.seekTo(0);
                }
                localPlayer.broadcastState();
            } else {
                ClientPlayNetworking.send(new com.mp3player.network.GlobalPlayPayload("", 0, 0, 0f));
            }
            return true;
        }
        
        // Мут глобальный
        if (isInside(mx, my, px + pw - 85, py + 24, 16, 14)) {
            playClickSound();
            ModConfig.getInstance().muteGlobal = !ModConfig.getInstance().muteGlobal;
            ModConfig.getInstance().save();
            if (ModConfig.getInstance().muteGlobal) {
                com.mp3player.MP3PlayerClient.getActiveJukeboxes().values().forEach(com.mp3player.client.audio.JukeboxAudioPlayer::stop);
                com.mp3player.MP3PlayerClient.getActiveJukeboxes().clear();
                com.mp3player.client.audio.GlobalAudioTracker.getInstance().stopAll();
                localPlayer.pause();
            }
            return true;
        }
        
        if (currentTab == 0) return handleLocalClick(mx, my, px, py, pw, ph, false);
        else return handleLocalClick(mx, my, px, py, pw, ph, true);
    }
    
    private void handleMenuClick(double mx, double my) {
        playClickSound();
        int option = (int) (my - trackMenuY) / 14;
        File trackFile = null;

        var playlist = currentTab == 1 ? localPlayer.getFavorites() : localPlayer.getPlaylist();
        if (trackMenuIndex >= 0 && trackMenuIndex < playlist.size()) {
            trackFile = playlist.get(trackMenuIndex);
        }
        
        if (option == 0) { // Нравится
            if (trackFile != null) {
                if (localPlayer.getFavorites().contains(trackFile)) {
                    localPlayer.getFavorites().remove(trackFile);
                    ModConfig.getInstance().favorites.remove(trackFile.getName());
                } else {
                    localPlayer.getFavorites().add(trackFile);
                    if (!ModConfig.getInstance().favorites.contains(trackFile.getName())) {
                        ModConfig.getInstance().favorites.add(trackFile.getName());
                    }
                }
                ModConfig.getInstance().save();
            }
        } else if (option == 1) { // Повтор
            localPlayer.toggleRepeat();
        } else if (option == 2) { // Пластинка
            // Открыть окно пластинок
            if (this.client != null) {
                this.client.setScreen(new MusicDiscScreen(this, trackFile));
            }
        } else if (option == 3) { // Сведения
            if (trackFile != null) {
                try {
                    String q = java.net.URLEncoder.encode(trackFile.getName().replace(".mp3", "").replace(".wav", ""), "UTF-8");
                    net.minecraft.util.Util.getOperatingSystem().open("https://www.google.com/search?q=" + q);
                } catch (Exception e) {}
            }
        } else if (option == 4) { // Поделиться
            if (trackFile != null) {
                if (this.client != null) {
                    // Запускаем перерисовку экрана чтобы меню закрылось
this.client.setScreen(new ShareScreen(this, trackFile));
                }
            }
        } else if (option == 5) { // Удалить песню
            if (trackFile != null) {
                if (localPlayer.getFavorites().contains(trackFile)) {
                    localPlayer.getFavorites().remove(trackFile);
                    ModConfig.getInstance().favorites.remove(trackFile.getName());
                    ModConfig.getInstance().save();
                }
                
                // Останавливаем плеер, если этот файл играет
                if (localPlayer.getCurrentTrackName().equals(trackFile.getName().replace(".mp3", "").replace(".wav", ""))) {
                    localPlayer.stop();
                }
                com.mp3player.MP3PlayerClient.getActiveJukeboxes().values().forEach(p -> p.stop());
                com.mp3player.client.audio.GlobalAudioTracker.getInstance().stopAll();
                
                System.gc(); // Форсируем GC чтобы освободить локи файла на винде
                
                // Моментально удаляем из списка, чтобы не ждать Windows
                localPlayer.getPlaylist().remove(trackFile);
                if (localPlayer.getFavorites().contains(trackFile)) {
                    localPlayer.getFavorites().remove(trackFile);
                }
                
                try {
                    Thread.sleep(50); // Даем время на закрытие потоков
                    
                    // Если удаляем WAV из папки converted, нужно удалить оригинальный MP3 из родительской папки
                    if (trackFile.getParentFile() != null && trackFile.getParentFile().getName().equals("converted")) {
                        File mainFolder = trackFile.getParentFile().getParentFile();
                        String nameWithoutExt = trackFile.getName().replace(".wav", "");
                        File mp3File = new File(mainFolder, nameWithoutExt + ".mp3");
                        if (mp3File.exists()) {
                            mp3File.deleteOnExit();
                            mp3File.delete();
                        }
                    }
                    
                    if (trackFile.exists()) {
                        trackFile.deleteOnExit(); // Если не удалится сейчас, удалится при выходе
                        boolean deleted = trackFile.delete();
                        if (!deleted) {
                            com.mp3player.MP3PlayerClient.LOGGER.error("Failed to delete " + trackFile.getName());
                        }
                    }
                } catch (Exception e) {}
                
                // Больше не делаем refreshPlaylist(), так как файл удален из памяти локально
                // И если файл еще заблокирован, refresh его бы снова вернул в список!
            }
        }
        trackMenuIndex = -1;
    }
    
    
    private boolean handleLocalClick(double mx, double my, int px, int py, int pw, int ph, boolean showFavorites) {
        int topY = py + 22;
        
        // Обновить
        if (isInside(mx, my, px + 10, topY - 2, 20, 14)) {
            playClickSound();
            localPlayer.refreshPlaylist();
            return true;
        }
        
        // Открыть папку
        if (isInside(mx, my, px + 35, topY - 2, 20, 14)) {
            playClickSound();
            openFolder();
            return true;
        }
        
        // Треки
        int listY = topY + 18;
        var playlist = showFavorites ? localPlayer.getFavorites() : localPlayer.getPlaylist();
        int tY = listY + 2;
        int scroll = showFavorites ? favTrackScroll : localTrackScroll;
        for (int i = scroll; i < Math.min(playlist.size(), scroll + 10); i++) {
            if (isInside(mx, my, px + 10, tY, pw - 20, 12)) {
                // Если клик по 3 точкам
                if (isInside(mx, my, px + pw - 20, tY, 15, 12)) {
                    playClickSound();
                    trackMenuIndex = i;
                    
                    int menuW = 135;
                    if (mx + menuW > this.width) {
                        trackMenuX = (int) mx - menuW;
                    } else {
                        trackMenuX = (int) mx;
                    }
                    trackMenuY = (int) my;
                    
                    // Если всё еще не влезает (вдруг mx - menuW < 0)
                    if (trackMenuX < 0) trackMenuX = 0;
                    if (trackMenuY + 84 > this.height) trackMenuY = this.height - 84;
                    
                    return true;
                }
                // Если клик по плей
                if (isInside(mx, my, px + pw - 40, tY, 15, 12)) {
                    playClickSound();
                    
                    File f = playlist.get(i);
                    int realIndex = localPlayer.getPlaylist().indexOf(f);
                    if (realIndex >= 0) {
                        if (localPlayer.getCurrentIndex() == realIndex && localPlayer.isPlaying()) {
                            localPlayer.pause();
                        } else if (localPlayer.getCurrentIndex() == realIndex && !localPlayer.isPlaying()) {
                            localPlayer.play();
                        } else {
                            localPlayer.playTrack(realIndex);
                        }
                    }
                    return true;
                }
            }
            tY += 12;
        }
        
        return handlePlayerClick(mx, my, px, py + ph - 55, pw);
    }
    
    private boolean handlePlayerClick(double mx, double my, int px, int py, int pw) {
        boolean sm = ModConfig.getInstance().speakerMode;
        int centerX = px + pw / 2;
        int btnY = py + 32, btnS = 16, gap = 4;
        int btnW_10 = this.textRenderer.getWidth("<-10") + 8;
        int totalW = btnS * 3 + btnW_10 * 2 + gap * 4;
        int btnX = centerX - totalW / 2;
        if (sm) btnX += 10;
        
        // Previous
        if (isInside(mx, my, btnX, btnY, btnS, btnS)) {
            playClickSound();
            localPlayer.previous();
            return true;
        }
        // Rewind
        if (isInside(mx, my, btnX + btnS + gap, btnY, btnW_10, btnS)) {
            playClickSound();
            localPlayer.seekRelative(-10_000_000);
            return true;
        }
        // Play/Pause
        if (isInside(mx, my, btnX + btnS + gap + btnW_10 + gap, btnY, btnS, btnS)) {
            playClickSound();
            if (localPlayer.isPlaying()) localPlayer.pause();
            else localPlayer.play();
            return true;
        }
        // Forward
        if (isInside(mx, my, btnX + btnS*2 + gap*2 + btnW_10 + gap, btnY, btnW_10, btnS)) {
            playClickSound();
            localPlayer.seekRelative(10_000_000);
            return true;
        }
        // Next
        if (isInside(mx, my, btnX + btnS*2 + gap*2 + btnW_10*2 + gap*2, btnY, btnS, btnS)) {
            playClickSound();
            localPlayer.next();
            return true;
        }
        
        // Громкость
        if (sm) {
            int rangeX = px + 5;
            if (isInside(mx, my, rangeX, btnY, 45, 14)) {
                draggingRange = true;
                float rProgress = (float) Math.max(0, Math.min(1, (mx - rangeX) / 45.0));
                ModConfig.getInstance().speakerRange = 8 + (int)(rProgress * (64 - 8));
                ModConfig.getInstance().save();
                localPlayer.broadcastState();
                return true;
            }
            int volX = px + pw - 60;
            if (isInside(mx, my, volX, btnY, 45, 14)) {
                draggingVolume = true;
                float vol = (float) Math.max(0, Math.min(1, (mx - volX) / 45.0));
                localPlayer.setVolume(vol);
                ModConfig.getInstance().musicVolume = vol;
                ModConfig.getInstance().save();
                return true;
            }
        } else {
            int volX = px + pw - 70;
            if (isInside(mx, my, volX - 5, btnY, 65, 14)) {
                draggingVolume = true;
                float vol = (float) Math.max(0, Math.min(1, (mx - volX) / 55.0));
                localPlayer.setVolume(vol);
                ModConfig.getInstance().musicVolume = vol;
                ModConfig.getInstance().save();
                return true;
            }
        }
        
        // Клик по прогресс бару
        int barX = px + 50, barY = py + 16, barW = pw - 100;
        if (isInside(mx, my, barX, barY - 2, barW, 8)) {
            draggingProgress = true;
            float prog = (float) Math.max(0, Math.min(1, (mx - barX) / barW));
            long dur = localPlayer.getDuration();
            if (dur > 0) {
                localPlayer.seekTo((long) (dur * prog));
                if (sm) localPlayer.broadcastState();
            }
            return true;
        }
        
        return false;
    }
    
    private void openFolder() {
        new Thread(() -> {
            try { new ProcessBuilder("cmd", "/c", "start", "", musicFolder.getAbsolutePath()).start(); }
            catch (Exception e) { e.printStackTrace(); }
        }).start();
    }
    
    @Override
    public boolean mouseReleased(double mx, double my, int btn) {
        pressedButtons.clear();
        if (draggingRange || draggingVolume) ModConfig.getInstance().save();
        draggingVolume = false;
        draggingRange = false;
        draggingProgress = false;
        return super.mouseReleased(mx, my, btn);
    }
    
    @Override
    public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
        boolean sm = ModConfig.getInstance().speakerMode;
        int pw = 320;
        int px = this.width / 2 - pw / 2;
        
        if (draggingVolume) {
            if (sm) {
                int volX = px + pw - 60;
                float vol = (float) Math.max(0, Math.min(1, (mx - volX) / 45.0));
                localPlayer.setVolume(vol);
                ModConfig.getInstance().musicVolume = vol;
            } else {
                int volX = px + pw - 70;
                float vol = (float) Math.max(0, Math.min(1, (mx - volX) / 55.0));
                localPlayer.setVolume(vol);
                ModConfig.getInstance().musicVolume = vol;
            }
            return true;
        }
        if (draggingRange) {
            int rangeX = px + 5;
            float rProgress = (float) Math.max(0, Math.min(1, (mx - rangeX) / 45.0));
            ModConfig.getInstance().speakerRange = 8 + (int)(rProgress * (64 - 8));
            localPlayer.broadcastState();
            return true;
        }
        if (draggingProgress) {
            int barX = px + 50, barW = pw - 100;
            float prog = (float) Math.max(0, Math.min(1, (mx - barX) / barW));
            long dur = localPlayer.getDuration();
            if (dur > 0) {
                localPlayer.seekTo((long) (dur * prog));
                if (sm) localPlayer.broadcastState();
            }
            return true;
        }
        return super.mouseDragged(mx, my, btn, dx, dy);
    }
    
    @Override
    public boolean mouseScrolled(double mx, double my, double horizontalAmount, double verticalAmount) {
        if (currentTab == 0) {
            int max = Math.max(0, localPlayer.getPlaylist().size() - 10);
            localTrackScroll = Math.max(0, Math.min(max, localTrackScroll - (int) verticalAmount));
        } else if (currentTab == 1) {
            int max = Math.max(0, localPlayer.getFavorites().size() - 10);
            favTrackScroll = Math.max(0, Math.min(max, favTrackScroll - (int) verticalAmount));
        }
        return true;
    }
    
    @Override
    public void mouseMoved(double mx, double my) {
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (com.mp3player.client.KeyInputHandler.openMenuKeyBinding.matchesKey(keyCode, scanCode)) {
            startClosing();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        return super.charTyped(chr, modifiers);
    }
    
    private boolean isInside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }
    
    private float easeOutCubic(float x) { return 1f - (float) Math.pow(1 - x, 3); }
    private String formatTime(long ms) { long s = ms / 1000; return String.format("%d:%02d", s / 60, s % 60); }
    
    public void startClosing() { if (!isClosing) isClosing = true; }
    
    @Override
    public void close() {
        if (!isClosing && animationProgress > 0) startClosing();
        else super.close();
    }

    @Override
    public boolean shouldPause() { return false; }
}
