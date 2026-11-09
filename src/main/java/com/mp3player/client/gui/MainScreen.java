/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.gui.screen.Screen
 *  net.minecraft.client.sound.PositionedSoundInstance
 *  net.minecraft.client.sound.SoundInstance
 *  net.minecraft.client.texture.AbstractTexture
 *  net.minecraft.sound.SoundEvent
 *  net.minecraft.text.Text
 *  net.minecraft.util.Util
 */
package com.mp3player.client.gui;

import com.mp3player.client.ModSounds;
import com.mp3player.client.audio.AlbumArtExtractor;
import com.mp3player.client.audio.AudioConverter;
import com.mp3player.client.audio.MusicPlayer;
import com.mp3player.client.config.ModConfig;
import com.mp3player.client.gsmtc.GsmtcHudOverlay;
import com.mp3player.client.gsmtc.GsmtcManager;
import com.mp3player.client.gui.DrawUtils;
import com.mp3player.client.gui.MusicDiscScreen;
import com.mp3player.client.gui.ShareScreen;
import com.mp3player.client.gui.skija.SkijaEngine;
import com.mp3player.client.network.FileReceiverManager;
import com.mp3player.client.network.FileSenderManager;
import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import com.mojang.renderpearl.backend.opengl.GlTexture;

public class MainScreen
extends Screen {
    public static final int PANEL_W = 340;
    public static final int PANEL_H = 196;
    private int currentTab = 0;
    private float animationProgress = 1.0f;
    private boolean isClosing = false;
    private boolean loggedFirstRender = false;
    private static final float ANIMATION_SPEED = 0.12f;
    private static boolean darkTheme = true;
    private final MusicPlayer localPlayer = MusicPlayer.getInstance();
    private final GsmtcManager gsmtc = GsmtcManager.getInstance();
    private final File musicFolder;
    private Set<Integer> pressedButtons = new HashSet<Integer>();
    private boolean draggingVolume = false;
    private boolean draggingRange = false;
    private boolean draggingProgress = false;
    private boolean draggingGsmtcSeek = false;
    private boolean draggingGsmtcVolume = false;
    private float draggedGsmtcSeekFraction = -1.0f;
    private float draggedLocalSeekFraction = -1.0f;
    private String downloadStatus = "";
    private long downloadStatusTimestamp = 0L;
    private boolean isDownloadingTrack = false;
    private int localTrackScroll = 0;
    private int favTrackScroll = 0;
    private int settingsScroll = 0;
    private int maxSettingsScroll = 0;
    private int trackMenuIndex = -1;
    private int trackMenuX = 0;
    private int trackMenuY = 0;
    private final long openedTimeMs = System.currentTimeMillis();

    private long lastSkijaRenderTime = 0L;
    private int lastRenderMouseX = -9999;
    private int lastRenderMouseY = -9999;
    private int lastRenderPanelX = -9999;
    private int lastRenderPanelY = -9999;
    private int lastRenderTab = -1;
    private boolean lastRenderDarkTheme = true;
    private boolean lastRenderPlaying = false;
    private long lastRenderPosSec = -1L;
    private String lastRenderTitle = "";
    private int lastTrackMenuIndex = -1;
    private boolean skijaTextureReady = false;
    private boolean forceSkijaRedraw = true;

    public MainScreen() {
        super((Component)Component.literal((String)"Music Player"));
        darkTheme = ModConfig.getInstance().darkTheme;
        SkijaEngine.getInstance().setDarkTheme(darkTheme);
        this.currentTab = ModConfig.getInstance().currentTab;
        if (this.currentTab < 0 || this.currentTab > 3) {
            this.currentTab = 0;
        }
        this.localPlayer.setVolume(ModConfig.getInstance().musicVolume);
        this.musicFolder = new File(Minecraft.getInstance().gameDirectory, "mp3player");
        if (!this.musicFolder.exists()) {
            this.musicFolder.mkdirs();
        }
        this.localPlayer.loadMusicFolder(this.musicFolder);
        this.gsmtc.setBrowserVisible(false);
    }

    @Override
    protected void init() {
        super.init();
        this.skijaTextureReady = false;
        this.forceSkijaRedraw = true;
        com.mp3player.MP3PlayerClient.LOGGER.info("[MainScreen] init() called: width=" + this.width + ", height=" + this.height);
    }

    private void playClickSound() {
        if (this.minecraft != null) {
            this.minecraft.getSoundManager().play((SoundInstance)SimpleSoundInstance.forUI(ModSounds.KNOCK, 1.0f));
        }
    }

    private int bgPanel() {
        return darkTheme ? -267184609 : -117900548;
    }

    private int bgDark() {
        return darkTheme ? -15197404 : -1;
    }

    private int bgButton() {
        return darkTheme ? -14671058 : -920071;
    }

    private int bgHover() {
        return darkTheme ? -14012867 : -1906448;
    }

    private int textPrimary() {
        return darkTheme ? -1 : -15788246;
    }

    private int textSecondary() {
        return darkTheme ? -7035976 : -10193781;
    }

    private int accent() {
        return -19712;
    }

    private int border() {
        return darkTheme ? -14013126 : -3418655;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0x66000000);
    }

    private void drawModernTab(GuiGraphicsExtractor ctx, int mouseX, int mouseY, int x, int y, int w, int h, String label, boolean selected, int accentColor) {
        boolean hover = this.isInside(mouseX, mouseY, x, y, w, h);
        int bg = selected ? this.bgPanel() : (hover ? this.bgHover() : this.bgButton());
        ctx.fill(x, y, x + w, y + h, this.border());
        ctx.fill(x + 1, y + 1, x + w - 1, y + h, bg);
        if (selected) {
            ctx.fill(x + 2, y, x + w - 2, y + 2, accentColor);
        }
        int col = selected ? accentColor : (hover ? accentColor : this.textSecondary());
        ctx.centeredText(this.font, Component.literal(label), x + w / 2, y + (h - 8) / 2, col);
    }

    private void drawModernButton(GuiGraphicsExtractor ctx, int mouseX, int mouseY, int x, int y, int w, int h, String text, boolean active, int activeColor, int normalColor) {
        boolean hover = this.isInside(mouseX, mouseY, x, y, w, h);
        int bg = active ? this.bgHover() : (hover ? this.bgHover() : this.bgButton());
        int border = active ? activeColor : (hover ? this.accent() : this.border());
        ctx.fill(x, y, x + w, y + h, border);
        ctx.fill(x + 1, y + 1, x + w - 1, y + h - 1, bg);
        int col = active ? activeColor : (hover ? this.accent() : normalColor);
        ctx.centeredText(this.font, Component.literal(text), x + w / 2, y + (h - 8) / 2, col);
    }

    private void drawModernCard(GuiGraphicsExtractor ctx, int x, int y, int w, int h, int bg, int border) {
        ctx.fill(x - 1, y + h, x + w + 1, y + h + 2, 0x33000000);
        ctx.fill(x, y, x + w, y + h, border);
        ctx.fill(x + 1, y + 1, x + w - 1, y + h - 1, bg);
    }

    private void drawModernProgressBar(GuiGraphicsExtractor ctx, int x, int y, int w, int h, float progress, boolean hover, int activeColor) {
        int bg = darkTheme ? 0xFF1E2028 : 0xFFD8DDE6;
        ctx.fill(x, y, x + w, y + h, bg);
        int fillW = (int) ((float)w * Math.max(0.0f, Math.min(1.0f, progress)));
        if (fillW > 0) {
            ctx.fill(x, y, x + fillW, y + h, activeColor);
        }
        if (hover) {
            int knobX = Math.max(x, Math.min(x + w - 3, x + fillW - 1));
            ctx.fill(knobX, y - 1, knobX + 3, y + h + 1, 0xFFFFFFFF);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        if (this.width <= 0 || this.height <= 0 || this.minecraft == null || this.minecraft.getWindow().getWidth() <= 0 || this.minecraft.getWindow().getHeight() <= 0) {
            return;
        }
        if (this.isClosing) {
            this.animationProgress -= 0.12f;
            if (this.animationProgress <= 0.0f) {
                this.onClose();
                return;
            }
        } else if (this.animationProgress < 1.0f) {
            this.animationProgress = Math.min(1.0f, this.animationProgress + 0.12f);
        }
        float eased = this.easeOutCubic(this.animationProgress);
        int panelW = 340;
        int panelH = 196;
        int centerX = this.width / 2;
        int targetY = this.height / 2 - panelH / 2;
        int panelY = (int)((float)(this.height + 20) + (float)(targetY - this.height - 20) * eased);
        int panelX = centerX - panelW / 2;

        SkijaEngine skija = SkijaEngine.getInstance();
        if (skija.isAvailable() && ModConfig.getInstance().useSkija) {
            float guiScale = (float) this.minecraft.getWindow().getGuiScale();
            skija.setDarkTheme(darkTheme);

            int pad = 24;
            int originX = panelX - pad;
            int originY = panelY - 28;
            int boundsW = panelW + pad * 2;
            int boundsH = panelH + 28 + pad;

            long now = System.currentTimeMillis();
            boolean isGsmtc = this.currentTab == 0 && this.gsmtc.isAvailable() && this.gsmtc.hasTrack();
            boolean isPlaying = isGsmtc ? this.gsmtc.isPlaying() : this.localPlayer.isPlaying();
            long posSec = isGsmtc ? (long) this.gsmtc.getPosition() : this.localPlayer.getCurrentPosition() / 1000000L;
            String curTitle = isGsmtc ? this.gsmtc.getTitle() : this.localPlayer.getCurrentTrackName();
            if (curTitle == null) curTitle = "";

            boolean mouseMoved = (mouseX != this.lastRenderMouseX || mouseY != this.lastRenderMouseY);
            boolean panelMoved = (panelX != this.lastRenderPanelX || panelY != this.lastRenderPanelY);
            boolean isAnimating = this.animationProgress < 1.0f || this.isClosing;
            boolean isDragging = this.draggingProgress || this.draggingVolume || this.draggingRange || this.draggingGsmtcSeek || this.draggingGsmtcVolume;
            boolean tabChanged = (this.currentTab != this.lastRenderTab);
            boolean themeChanged = (darkTheme != this.lastRenderDarkTheme);
            boolean playChanged = (isPlaying != this.lastRenderPlaying);
            boolean titleChanged = !curTitle.equals(this.lastRenderTitle);
            boolean menuChanged = (this.trackMenuIndex != this.lastTrackMenuIndex);
            boolean posChanged = (posSec != this.lastRenderPosSec);

            boolean isGpu = skija.isGpuMode();
            boolean stateChanged = this.forceSkijaRedraw
                    || !this.skijaTextureReady
                    || isGpu
                    || mouseMoved
                    || panelMoved
                    || isAnimating
                    || isDragging
                    || tabChanged
                    || themeChanged
                    || playChanged
                    || titleChanged
                    || menuChanged
                    || posChanged;

            if (stateChanged) {
                this.forceSkijaRedraw = false;
                this.lastSkijaRenderTime = now;
                this.lastRenderMouseX = mouseX;
                this.lastRenderMouseY = mouseY;
                this.lastRenderPanelX = panelX;
                this.lastRenderPanelY = panelY;
                this.lastRenderTab = this.currentTab;
                this.lastRenderDarkTheme = darkTheme;
                this.lastRenderPlaying = isPlaying;
                this.lastRenderPosSec = posSec;
                this.lastRenderTitle = curTitle;
                this.lastTrackMenuIndex = this.trackMenuIndex;

                skija.beginFrame(boundsW, boundsH, guiScale, -originX, -originY);
                this.renderScreenSkija(skija, mouseX, mouseY, panelX, panelY, panelW, panelH);
                skija.endFrame();
                this.skijaTextureReady = true;
            }

            skija.renderToContext(context, originX, originY, boundsW, boundsH);
            super.extractRenderState(context, mouseX, mouseY, delta);
            return;
        }

        int tab0W = 96;
        int tab1W = 86;
        int tab2W = 80;
        int tab3W = 30;
        int tab0X = panelX + 10;
        int tab1X = tab0X + tab0W + 4;
        int tab2X = tab1X + tab1W + 4;
        int tab3X = tab2X + tab2W + 4;

        this.drawModernTab(context, mouseX, mouseY, tab0X, panelY - 22, tab0W, 22, "Webs / Apps", this.currentTab == 0, this.accent());
        this.drawModernTab(context, mouseX, mouseY, tab1X, panelY - 22, tab1W, 22, DrawUtils.t("MP3 Player", "MP3 Плеер"), this.currentTab == 1, this.accent());
        this.drawModernTab(context, mouseX, mouseY, tab2X, panelY - 22, tab2W, 22, DrawUtils.t("Favorites", "Избранное"), this.currentTab == 2, this.accent());
        this.drawModernTab(context, mouseX, mouseY, tab3X, panelY - 22, tab3W, 22, "⚙", this.currentTab == 3, this.accent());

        this.drawModernCard(context, panelX, panelY, panelW, panelH, this.bgPanel(), this.border());
        context.fill(panelX + 12, panelY, panelX + panelW - 12, panelY + 2, this.accent());

        this.drawModernButton(context, mouseX, mouseY, panelX + 8, panelY + 6, 22, 18, darkTheme ? "D" : "L", false, this.accent(), this.textSecondary());
        this.drawModernButton(context, mouseX, mouseY, panelX + 34, panelY + 6, 26, 18, ModConfig.getInstance().isEnglish ? "EN" : "RU", false, this.accent(), this.textSecondary());

        String title = this.currentTab == 0 ? "Webs / Apps" : (this.currentTab == 1 ? DrawUtils.t("MP3 Player", "MP3 Плеер") : (this.currentTab == 2 ? DrawUtils.t("Favorites", "Любимые песни") : DrawUtils.t("Settings & Help", "Настройки и Справка")));
        context.centeredText(this.font, Component.literal(title), centerX, panelY + 10, this.accent());

        boolean mg = ModConfig.getInstance().muteGlobal;
        this.drawModernButton(context, mouseX, mouseY, panelX + panelW - 90, panelY + 6, 22, 18, mg ? "✕" : "♪", mg, 0xFFFF5555, mg ? 0xFFFF5555 : this.textSecondary());

        boolean sm = ModConfig.getInstance().speakerMode;
        this.drawModernButton(context, mouseX, mouseY, panelX + panelW - 64, panelY + 6, 56, 18, DrawUtils.t("Speaker", "Колонка"), sm, this.accent(), sm ? this.accent() : this.textSecondary());
        if (sm) {
            context.fill(panelX + panelW - 64, panelY + 22, panelX + panelW - 8, panelY + 24, this.accent());
        }

        if (this.currentTab == 0) {
            this.renderOnlineTab(context, mouseX, mouseY, panelX, panelY, panelW, panelH);
        } else if (this.currentTab == 1) {
            this.renderLocalTab(context, mouseX, mouseY, panelX, panelY, panelW, panelH, false);
            this.renderPlayerBar(context, mouseX, mouseY, panelX + 8, panelY + panelH - 70, panelW - 16);
        } else if (this.currentTab == 2) {
            this.renderLocalTab(context, mouseX, mouseY, panelX, panelY, panelW, panelH, true);
            this.renderPlayerBar(context, mouseX, mouseY, panelX + 8, panelY + panelH - 70, panelW - 16);
        } else if (this.currentTab == 3) {
            this.renderSettingsTab(context, mouseX, mouseY, panelX, panelY, panelW, panelH);
        }

        if (this.trackMenuIndex != -1) {
            this.renderTrackMenu(context, mouseX, mouseY);
        }

        int uploads = FileSenderManager.getInstance().getUploadsCount();
        int downloads = FileReceiverManager.getInstance().getDownloadsCount();
        if (uploads > 0) {
            DrawUtils.drawText(context, this.font, DrawUtils.t("Uploading: ", "Отправка файлов: ") + uploads, panelX + panelW - 120, panelY - 12, this.accent(), true);
        }
        if (downloads > 0) {
            DrawUtils.drawText(context, this.font, DrawUtils.t("Downloading: ", "Скачивание: ") + downloads, panelX + panelW - 120, panelY - 24, 0xFF55FF55, true);
        }
        super.extractRenderState(context, mouseX, mouseY, delta);
    }

    private void renderTrackMenu(GuiGraphicsExtractor ctx, int mouseX, int mouseY) {
        int w = 135;
        int h = 84;
        ctx.fill(this.trackMenuX - 1, this.trackMenuY - 1, this.trackMenuX + w + 1, this.trackMenuY + h + 1, this.border());
        ctx.fill(this.trackMenuX, this.trackMenuY, this.trackMenuX + w, this.trackMenuY + h, this.bgDark());
        String[] opts = new String[]{DrawUtils.t("Fav: Favorite", "\u0418\u0437\u0431: \u041d\u0440\u0430\u0432\u0438\u0442\u0441\u044f"), DrawUtils.t("Rep: Repeat", "\u041f\u043e\u0432: \u041f\u043e\u0432\u0442\u043e\u0440"), DrawUtils.t("Dsc: Make Disc", "\u0414\u0441\u043a: \u041f\u043b\u0430\u0441\u0442\u0438\u043d\u043a\u0430"), DrawUtils.t("i Info", "i \u0421\u0432\u0435\u0434\u0435\u043d\u0438\u044f"), DrawUtils.t("Shr: Share", "\u041f\u043e\u0434: \u041f\u043e\u0434\u0435\u043b\u0438\u0442\u044c\u0441\u044f"), DrawUtils.t("Del: Delete", "\u0423\u0434\u0430: \u0423\u0434\u0430\u043b\u0438\u0442\u044c")};
        List<File> playlist = this.currentTab == 2 ? this.localPlayer.getFavorites() : this.localPlayer.getPlaylist();
        File menuTrack = this.trackMenuIndex >= 0 && this.trackMenuIndex < playlist.size() ? playlist.get(this.trackMenuIndex) : null;
        boolean isFav = menuTrack != null && this.localPlayer.getFavorites().contains(menuTrack);
        for (int i = 0; i < 6; ++i) {
            boolean hover;
            int y = this.trackMenuY + i * 14;
            boolean bl = hover = mouseX >= this.trackMenuX && mouseX < this.trackMenuX + w && mouseY >= y && mouseY < y + 14;
            if (hover) {
                ctx.fill(this.trackMenuX, y, this.trackMenuX + w, y + 14, this.bgHover());
            }
            int color = this.textPrimary();
            if (i == 0 && isFav) {
                color = -1096636;
            }
            if (i == 5) {
                color = -1096636;
            }
            DrawUtils.drawText(ctx, this.font, opts[i], this.trackMenuX + 6, y + 3, color, false);
        }
    }

    private int getGsmtcCoverGlId() {
        try {
            AbstractTexture tex = Minecraft.getInstance().getTextureManager().getTexture(GsmtcManager.COVER_TEXTURE_ID);
            if (tex != null && tex.getTexture() instanceof GlTexture glTex) {
                return glTex.glId();
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return -1;
    }

    private int getLocalCoverGlId() {
        try {
            AbstractTexture tex = Minecraft.getInstance().getTextureManager().getTexture(AlbumArtExtractor.LOCAL_COVER_TEXTURE_ID);
            if (tex != null && tex.getTexture() instanceof GlTexture glTex) {
                return glTex.glId();
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return -1;
    }

    private void renderScreenSkija(SkijaEngine skija, int mouseX, int mouseY, int panelX, int panelY, int panelW, int panelH) {
        boolean sm = ModConfig.getInstance().speakerMode;
        boolean mg = ModConfig.getInstance().muteGlobal;
        int centerX = panelX + panelW / 2;
        int tab0W = 96;
        int tab1W = 86;
        int tab2W = 80;
        int tab3W = 30;
        int tab0X = panelX + 10;
        int tab1X = tab0X + tab0W + 4;
        int tab2X = tab1X + tab1W + 4;
        int tab3X = tab2X + tab2W + 4;
        boolean hoverTab0 = this.isInside(mouseX, mouseY, tab0X, panelY - 22, tab0W, 22);
        boolean hoverTab1 = this.isInside(mouseX, mouseY, tab1X, panelY - 22, tab1W, 22);
        boolean hoverTab2 = this.isInside(mouseX, mouseY, tab2X, panelY - 22, tab2W, 22);
        boolean hoverTab3 = this.isInside(mouseX, mouseY, tab3X, panelY - 22, tab3W, 22);
        skija.drawTab(tab0X, panelY - 22, tab0W, 22.0f, "Webs / Apps", this.currentTab == 0, hoverTab0, this.accent());
        skija.drawTab(tab1X, panelY - 22, tab1W, 22.0f, DrawUtils.t("MP3 Player", "MP3 \u041f\u043b\u0435\u0435\u0440"), this.currentTab == 1, hoverTab1, this.accent());
        skija.drawTab(tab2X, panelY - 22, tab2W, 22.0f, DrawUtils.t("Favorites", "\u0418\u0437\u0431\u0440\u0430\u043d\u043d\u043e\u0435"), this.currentTab == 2, hoverTab2, this.accent());
        skija.drawTabBase(tab3X, panelY - 22, tab3W, 22.0f, this.currentTab == 3, hoverTab3, this.accent());
        skija.drawGearIcon((float)tab3X + (float)tab3W * 0.5f, (float)panelY - 11.0f, 6.0f, this.currentTab == 3 ? this.accent() : (hoverTab3 ? (darkTheme ? -1 : -15788246) : this.textSecondary()));
        skija.drawDropShadow(panelX, panelY, panelW, panelH, 12.0f, 24.0f, 4.0f, 0x66000000);
        skija.drawRoundedRect(panelX, panelY, panelW, panelH, 12.0f, this.bgPanel());
        skija.drawRoundedRectOutline(panelX, panelY, panelW, panelH, 12.0f, 1.0f, this.border());
        skija.drawRoundedRect(panelX + 12, panelY, panelW - 24, 2.0f, 1.0f, this.accent());
        boolean hoverTheme = this.isInside(mouseX, mouseY, panelX + 8, panelY + 6, 22, 18);
        skija.drawButtonBase(panelX + 8, panelY + 6, 22.0f, 18.0f, hoverTheme, this.pressedButtons.contains(200), this.accent());
        skija.drawMoonSunIcon(panelX + 19, panelY + 15, 5.0f, darkTheme, hoverTheme ? this.accent() : this.textSecondary());
        boolean hoverLang = this.isInside(mouseX, mouseY, panelX + 34, panelY + 6, 26, 18);
        skija.drawButton(panelX + 34, panelY + 6, 26.0f, 18.0f, ModConfig.getInstance().isEnglish ? "EN" : "RU", hoverLang, this.pressedButtons.contains(204), this.accent(), this.textSecondary());
        String title = this.currentTab == 0 ? "Webs / Apps" : (this.currentTab == 1 ? DrawUtils.t("MP3 Player", "MP3 \u041f\u043b\u0435\u0435\u0440") : (this.currentTab == 2 ? DrawUtils.t("Favorites", "\u041b\u044e\u0431\u0438\u043c\u044b\u0435 \u043f\u0435\u0441\u043d\u0438") : DrawUtils.t("Settings & Help", "\u041d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0438 \u0438 \u0421\u043f\u0440\u0430\u0432\u043a\u0430")));
        skija.drawText(title, panelX + 66, panelY + 15, 12.0f, this.accent(), true, 17);
        boolean hoverMute = this.isInside(mouseX, mouseY, panelX + panelW - 90, panelY + 6, 22, 18);
        skija.drawButtonBase(panelX + panelW - 90, panelY + 6, 22.0f, 18.0f, hoverMute, this.pressedButtons.contains(203), mg ? -43691 : this.accent());
        skija.drawSpeakerIcon(panelX + panelW - 79, panelY + 15, 9.0f, mg, mg ? -43691 : (hoverMute ? this.accent() : this.textSecondary()));
        int speakerCol = sm ? -19712 : this.textSecondary();
        boolean hoverSpeaker = this.isInside(mouseX, mouseY, panelX + panelW - 64, panelY + 6, 56, 18);
        skija.drawButton(panelX + panelW - 64, panelY + 6, 56.0f, 18.0f, DrawUtils.t("Speaker", "\u041a\u043e\u043b\u043e\u043d\u043a\u0430"), hoverSpeaker, this.pressedButtons.contains(202), -19712, speakerCol);
        if (sm) {
            skija.drawRoundedRect(panelX + panelW - 60, panelY + 22, 48.0f, 2.0f, 1.0f, -19712);
        }
        if (this.currentTab == 0) {
            this.renderOnlineTabSkija(skija, mouseX, mouseY, panelX, panelY, panelW, panelH);
        } else if (this.currentTab == 1 || this.currentTab == 2) {
            this.renderLocalTabSkija(skija, mouseX, mouseY, panelX, panelY, panelW, panelH, this.currentTab == 2);
            this.renderPlayerBarSkija(skija, mouseX, mouseY, panelX + 8, panelY + panelH - 70, panelW - 16);
        } else if (this.currentTab == 3) {
            this.renderSettingsTabSkija(skija, mouseX, mouseY, panelX, panelY, panelW, panelH);
        }
        if (this.trackMenuIndex != -1) {
            this.renderTrackMenuSkija(skija, mouseX, mouseY);
        }
        int uploads = FileSenderManager.getInstance().getUploadsCount();
        int downloads = FileReceiverManager.getInstance().getDownloadsCount();
        if (uploads > 0) {
            skija.drawText(DrawUtils.t("Uploading: ", "\u041e\u0442\u043f\u0440\u0430\u0432\u043a\u0430 \u0444\u0430\u0439\u043b\u043e\u0432: ") + uploads, panelX + panelW - 10, panelY - 10, 10.0f, this.accent(), true, 20);
        }
        if (downloads > 0) {
            skija.drawText(DrawUtils.t("Downloading: ", "\u0421\u043a\u0430\u0447\u0438\u0432\u0430\u043d\u0438\u0435: ") + downloads, panelX + panelW - 10, panelY - 22, 10.0f, -11141291, true, 20);
        }
    }

    private void renderOnlineTabSkija(SkijaEngine skija, int mouseX, int mouseY, int px, int py, int pw, int ph) {
        int cx;
        float prog;
        Object displayTitle;
        String title;
        String statusText;
        String sourceName;
        boolean isSpotify;
        boolean hasTrack = this.gsmtc.isAvailable() && this.gsmtc.hasTrack();
        boolean playing = this.gsmtc.isPlaying();
        boolean sm = ModConfig.getInstance().speakerMode;
        String sourceApp = hasTrack ? this.gsmtc.getSourceApp().toLowerCase() : "";
        boolean isYandex = hasTrack && (sourceApp.contains("yandex") || sourceApp.contains("\u044f\u043d\u0434\u0435\u043a\u0441"));
        boolean bl = isSpotify = hasTrack && (sourceApp.contains("spotify") || sourceApp.contains("\u0441\u043f\u043e\u0442\u0438\u0444\u0430\u0439"));
        String rawSourceName = isYandex ? "\u042f\u043d\u0434\u0435\u043a\u0441.\u041c\u0443\u0437\u044b\u043a\u0430" : (isSpotify ? "Spotify" : (hasTrack ? "Windows / Web" : DrawUtils.t("Not Connected", "\u041d\u0435 \u043f\u043e\u0434\u043a\u043b\u044e\u0447\u0435\u043d\u043e")));
        String string = sourceName = sm ? DrawUtils.t("Speaker \u2022 ", "\u041a\u043e\u043b\u043e\u043d\u043a\u0430 \u2022 ") + rawSourceName : rawSourceName;
        int sourceColor = sm ? -19712 : (isYandex ? -13312 : (isSpotify ? -14829228 : -16739073));
        int cardX = px + 8;
        int cardY = py + 42;
        int cardW = pw - 16;
        int cardH = 74;
        skija.drawDropShadow(cardX, cardY, cardW, cardH, 8.0f, 12.0f, 1.0f, sourceColor & 0x33FFFFFF);
        skija.drawRoundedRect(cardX, cardY, cardW, cardH, 8.0f, this.bgDark());
        skija.drawRoundedRectOutline(cardX, cardY, cardW, cardH, 8.0f, 1.0f, hasTrack ? sourceColor : this.border());
        int coverGlId = hasTrack && this.gsmtc.hasCover() ? this.getGsmtcCoverGlId() : -1;
        skija.drawGsmtcCover(coverGlId, cardX + 8, cardY + 8, 58.0f, 6.0f);
        int infoX = cardX + 74;
        float maxTextW = cardW - 82;
        skija.drawSourceIcon(infoX + 6, cardY + 15, 6.0f, sourceApp);
        skija.drawBadge(infoX + 16, cardY + 9, 12.0f, sourceName, sourceColor);
        float badgeW = skija.getTextWidth(sourceName, 7.8f, true) + 26.0f;
        statusText = hasTrack ? (playing ? "\u25cf " + DrawUtils.t("Playing", "\u0418\u0433\u0440\u0430\u0435\u0442") : "II " + DrawUtils.t("Paused", "\u041f\u0430\u0443\u0437\u0430")) : DrawUtils.t("Waiting for music...", "\u041e\u0436\u0438\u0434\u0430\u043d\u0438\u0435 \u0442\u0440\u0435\u043a\u0430...");
        int statusColor = hasTrack ? (playing ? -11141291 : this.textSecondary()) : this.textSecondary();
        skija.drawText(statusText, (float)infoX + badgeW + 6.0f, cardY + 15, 8.5f, statusColor, false, 17);
        String string2 = title = hasTrack ? this.gsmtc.getTitle() : DrawUtils.t("Turn on music in browser or app", "\u0412\u043a\u043b\u044e\u0447\u0438\u0442\u0435 \u043c\u0443\u0437\u044b\u043a\u0443 \u0432 \u0431\u0440\u0430\u0443\u0437\u0435\u0440\u0435 \u0438\u043b\u0438 \u043f\u0440\u0438\u043b\u043e\u0436\u0435\u043d\u0438\u0438");
        if (title.isEmpty()) {
            title = DrawUtils.t("No title", "\u0411\u0435\u0437 \u043d\u0430\u0437\u0432\u0430\u043d\u0438\u044f");
        }
        float fullTitleW = skija.getTextWidth((String)(displayTitle = title), 10.5f, true);
        if (fullTitleW > maxTextW) {
            int est = (int) Math.max(1, title.length() * (maxTextW / fullTitleW));
            displayTitle = title.substring(0, Math.min(title.length(), est));
            while (((String)displayTitle).length() > 1 && skija.getTextWidth((String)displayTitle + "..", 10.5f, true) > maxTextW) {
                displayTitle = ((String)displayTitle).substring(0, ((String)displayTitle).length() - 1);
            }
            displayTitle = (String)displayTitle + "..";
        }
        skija.drawText((String)displayTitle, infoX, cardY + 32, 10.5f, this.textPrimary(), true, 17);
        String artist = hasTrack ? this.gsmtc.getArtist() : "";
        String album = hasTrack ? this.gsmtc.getAlbum() : "";
        String sub = artist;
        if (!album.isEmpty() && !album.equalsIgnoreCase(title)) {
            String string3 = sub = artist.isEmpty() ? album : artist + " \u2014 " + album;
        }
        if (!sub.isEmpty()) {
            Object displaySub = sub;
            float fullSubW = skija.getTextWidth((String)displaySub, 8.5f, false);
            if (fullSubW > maxTextW) {
                int est = (int) Math.max(1, sub.length() * (maxTextW / fullSubW));
                displaySub = sub.substring(0, Math.min(sub.length(), est));
                while (((String)displaySub).length() > 1 && skija.getTextWidth((String)displaySub + "..", 8.5f, false) > maxTextW) {
                    displaySub = ((String)displaySub).substring(0, ((String)displaySub).length() - 1);
                }
                displaySub = (String)displaySub + "..";
            }
            skija.drawText((String)displaySub, infoX, cardY + 48, 8.5f, this.textSecondary(), false, 17);
        }
        long curSec = (long)this.gsmtc.getPosition();
        long durSec = (long)this.gsmtc.getDuration();
        float f = prog = durSec > 0L ? (float)Math.max(0.0, Math.min(1.0, (double)curSec / (double)durSec)) : 0.0f;
        if (this.draggingGsmtcSeek && this.draggedGsmtcSeekFraction >= 0.0f) {
            prog = this.draggedGsmtcSeekFraction;
            curSec = (long)((float)durSec * prog);
        }
        int timeY = py + 120;
        skija.drawText(this.formatTime(curSec), px + 12, timeY, 8.5f, this.textSecondary(), false, 17);
        skija.drawText(this.formatTime(durSec), px + pw - 12, timeY, 8.5f, this.textSecondary(), false, 20);
        int sbX = px + 44;
        int sbW = pw - 88;
        int sbY = timeY - 1;
        boolean hoverSeek = this.isInside(mouseX, mouseY, sbX, sbY - 4, sbW, 10);
        skija.drawProgressBar(sbX, sbY, sbW, 3.5f, prog, hoverSeek, sourceColor);
        int btnY = py + 140;
        int btnS = 16;
        int btnW_10 = 20;
        int playCx = cx = px + pw / 2;
        int playCy = btnY + 9;
        int btnRewX = playCx - 14 - btnW_10;
        int btnPrevX = btnRewX - 4 - btnS;
        int dlBtnW = 54;
        int dlBtnH = 18;
        int dlBtnX = btnPrevX - 6 - dlBtnW;
        int dlBtnY = btnY;
        int btnFwdX = playCx + 14;
        int btnNextX = btnFwdX + btnW_10 + 4;
        int volIconX = btnNextX + btnS + 8;
        int volBarX = volIconX + 12;
        int volBarW = 46;
        boolean hoverDl = this.isInside(mouseX, mouseY, dlBtnX, dlBtnY, dlBtnW, dlBtnH);
        skija.drawButtonBase(dlBtnX, dlBtnY, dlBtnW, dlBtnH, hoverDl, this.pressedButtons.contains(301), this.accent());
        if (hoverDl) {
            skija.drawDropShadow(dlBtnX, dlBtnY, dlBtnW, dlBtnH, 4.0f, 8.0f, 1.0f, this.accent() & 0x44FFFFFF);
        }
        String dlText = DrawUtils.t("Add", "\u0414\u043e\u0431\u0430\u0432\u0438\u0442\u044c");
        if (!this.downloadStatus.isEmpty() && System.currentTimeMillis() - this.downloadStatusTimestamp < 4000L) {
            dlText = this.downloadStatus;
        }
        skija.drawText(dlText, dlBtnX + dlBtnW / 2, dlBtnY + dlBtnH / 2 + 1, 7.8f, hoverDl ? -1 : this.accent(), true, 18);
        boolean hoverPrev = this.isInside(mouseX, mouseY, btnPrevX, btnY, btnS, btnS);
        skija.drawButtonBase(btnPrevX, btnY, btnS, btnS, hoverPrev, false, this.accent());
        skija.drawSkipButton(btnPrevX + 8, btnY + 8, 7.0f, false, hoverPrev);
        boolean hoverRew = this.isInside(mouseX, mouseY, btnRewX, btnY, btnW_10, btnS);
        skija.drawButton(btnRewX, btnY, btnW_10, btnS, "-10", hoverRew, false, this.accent(), this.textSecondary());
        boolean hoverPlay = this.isInsideCircle(mouseX, mouseY, playCx, playCy, 12);
        skija.drawPlayPauseButton(playCx, playCy, 11.0f, playing, hoverPlay, sourceColor);
        boolean hoverFwd = this.isInside(mouseX, mouseY, btnFwdX, btnY, btnW_10, btnS);
        skija.drawButton(btnFwdX, btnY, btnW_10, btnS, "+10", hoverFwd, false, this.accent(), this.textSecondary());
        boolean hoverNext = this.isInside(mouseX, mouseY, btnNextX, btnY, btnS, btnS);
        skija.drawButtonBase(btnNextX, btnY, btnS, btnS, hoverNext, false, this.accent());
        skija.drawSkipButton(btnNextX + 8, btnY + 8, 7.0f, true, hoverNext);
        float extVol = this.gsmtc.getVolume();
        boolean hoverVol = this.isInside(mouseX, mouseY, volBarX - 4, btnY - 2, volBarW + 8, 16);
        skija.drawSpeakerIcon(volIconX, btnY + 8, 8.0f, extVol <= 0.01f, this.textSecondary());
        skija.drawProgressBar(volBarX, btnY + 8, volBarW, 3.5f, extVol, hoverVol, sourceColor);
        skija.drawText((int)(extVol * 100.0f) + "%", volBarX + volBarW + 12, btnY + 8, 7.2f, this.textSecondary(), false, 18);
        if (sm) {
            int auxY = py + 164;
            int rangeBoxW = 150;
            int rangeBoxX = cx - rangeBoxW / 2;
            boolean hoverRange = this.isInside(mouseX, mouseY, rangeBoxX, auxY - 2, rangeBoxW, 18);
            skija.drawDropShadow(rangeBoxX, auxY - 2, rangeBoxW, 18.0f, 9.0f, 6.0f, 0.5f, 587182848);
            skija.drawRoundedRect(rangeBoxX, auxY - 2, rangeBoxW, 18.0f, 9.0f, this.bgDark());
            skija.drawRoundedRectOutline(rangeBoxX, auxY - 2, rangeBoxW, 18.0f, 9.0f, 1.0f, 1157608192);
            skija.drawSpeakerIcon(rangeBoxX + 10, auxY + 7, 7.0f, false, -19712);
            String rangeStr = ModConfig.getInstance().speakerRange + "\u043c";
            skija.drawText(rangeStr, rangeBoxX + 22, auxY + 7, 9.5f, -19712, true, 17);
            int sliderX = rangeBoxX + 54;
            int sliderW = rangeBoxW - 64;
            float rProgress = (float)(ModConfig.getInstance().speakerRange - 8) / 56.0f;
            boolean hoverSlider = this.isInside(mouseX, mouseY, sliderX - 4, auxY - 2, sliderW + 8, 18);
            skija.drawSlider(sliderX, auxY + 4, sliderW, 6.0f, rProgress, -19712, hoverSlider);
        }
    }

    private void renderLocalTabSkija(SkijaEngine skija, int mouseX, int mouseY, int px, int py, int pw, int ph, boolean showFavorites) {
        List<File> playlist = showFavorites ? this.localPlayer.getFavorites() : this.localPlayer.getPlaylist();
        boolean hoverRefresh = this.isInside(mouseX, mouseY, px + 8, py + 28, 22, 16);
        skija.drawButtonBase(px + 8, py + 28, 22.0f, 16.0f, hoverRefresh, this.pressedButtons.contains(110), this.accent());
        skija.drawRefreshIcon(px + 19, py + 36, 4.5f, hoverRefresh ? this.accent() : this.textSecondary());
        boolean hoverFolder = this.isInside(mouseX, mouseY, px + 34, py + 28, 22, 16);
        skija.drawButtonBase(px + 34, py + 28, 22.0f, 16.0f, hoverFolder, this.pressedButtons.contains(111), this.accent());
        skija.drawFolderIcon(px + 45, py + 36, 11.0f, 9.0f, hoverFolder ? this.accent() : this.textSecondary());
        skija.drawText(DrawUtils.t("Tracks: ", "\u0422\u0440\u0435\u043a\u043e\u0432: ") + playlist.size(), px + 62, py + 36, 10.0f, this.textSecondary(), false, 17);
        if (this.localPlayer.isConverting()) {
            skija.drawText(DrawUtils.t("Converting MP3...", "\u041a\u043e\u043d\u0432\u0435\u0440\u0442\u0430\u0446\u0438\u044f MP3..."), px + 130, py + 36, 9.5f, this.accent(), true, 17);
        }
        int listX = px + 8;
        int listY = py + 48;
        int listW = pw - 16;
        int listH = 114;
        skija.drawRoundedRect(listX, listY, listW, listH, 6.0f, this.bgDark());
        skija.drawRoundedRectOutline(listX, listY, listW, listH, 6.0f, 1.0f, this.border());
        if (playlist.isEmpty()) {
            skija.drawText(DrawUtils.t("Add .mp3 or .wav to mp3player folder", "\u0417\u0430\u043a\u0438\u043d\u044c\u0442\u0435 .mp3 \u0438\u043b\u0438 .wav \u0432 \u043f\u0430\u043f\u043a\u0443 mp3player"), listX + listW / 2, listY + listH / 2 - 8, 10.0f, this.textSecondary(), false, 18);
            skija.drawText(DrawUtils.t("MP3s are converted automatically", "MP3 \u043a\u043e\u043d\u0432\u0435\u0440\u0442\u0438\u0440\u0443\u044e\u0442\u0441\u044f \u0430\u0432\u0442\u043e\u043c\u0430\u0442\u0438\u0447\u0435\u0441\u043a\u0438"), listX + listW / 2, listY + listH / 2 + 8, 9.0f, this.textSecondary(), false, 18);
        } else {
            int scroll;
            for (int i = scroll = showFavorites ? this.favTrackScroll : this.localTrackScroll; i < Math.min(playlist.size(), scroll + 7); ++i) {
                int itemY = listY + 3 + (i - scroll) * 16;
                int itemX = listX + 2;
                int itemW = listW - 4;
                int itemH = 15;
                File f = playlist.get(i);
                boolean hover = this.isInside(mouseX, mouseY, itemX, itemY, itemW, itemH);
                boolean current = f.equals(this.localPlayer.getPlaylist().isEmpty() ? null : this.localPlayer.getPlaylist().get(this.localPlayer.getCurrentIndex())) && this.localPlayer.isPlaying();
                boolean isFav = this.localPlayer.getFavorites().contains(f);
                if (hover) {
                    skija.drawRoundedRect(itemX, itemY, itemW, itemH, 4.0f, this.bgHover());
                }
                if (current) {
                    skija.drawRoundedRect(itemX, itemY, 3.0f, itemH, 1.5f, this.accent());
                }
                float iconCx = (float)itemX + 9.0f;
                float iconCy = (float)itemY + (float)itemH * 0.5f;
                if (current) {
                    skija.drawMiniDisc(iconCx, iconCy, 5.5f, -15197662, this.accent());
                    skija.drawMiniPlayIcon(iconCx + 0.5f, iconCy, 2.5f, -1);
                } else {
                    skija.drawMiniDisc(iconCx, iconCy, 5.0f, -14671058, isFav ? -46226 : -10193781);
                    if (isFav) {
                        skija.drawHeartIcon(iconCx + 8.5f, iconCy, 5.0f, true, false);
                    }
                }
                String name = f.getName();
                int dot = name.lastIndexOf(46);
                if (dot > 0) {
                    name = name.substring(0, dot);
                }
                float maxNameW = itemW - 80;
                Object displayName = name;
                if (skija.getTextWidth((String)displayName, 9.5f, current) > maxNameW) {
                    while (((String)displayName).length() > 3 && skija.getTextWidth((String)displayName + "..", 9.5f, current) > maxNameW) {
                        displayName = ((String)displayName).substring(0, ((String)displayName).length() - 1);
                    }
                    displayName = (String)displayName + "..";
                }
                int col = current ? this.accent() : this.textPrimary();
                float nameStartX = isFav && !current ? (float)(itemX + 26) : (float)(itemX + 19);
                skija.drawText((String)displayName, nameStartX, (float)itemY + (float)itemH * 0.5f + 0.5f, 9.5f, col, current, 17);
                boolean hoverPlay = this.isInside(mouseX, mouseY, itemX + itemW - 60, itemY + 1, 16, 13);
                boolean hoverDel = this.isInside(mouseX, mouseY, itemX + itemW - 40, itemY + 1, 16, 13);
                boolean hoverDots = this.isInside(mouseX, mouseY, itemX + itemW - 20, itemY + 1, 16, 13);
                if (hoverPlay) {
                    skija.drawRoundedRect(itemX + itemW - 60, itemY + 1, 16.0f, 13.0f, 3.0f, 0x33FFFFFF);
                }
                if (hoverDel) {
                    skija.drawRoundedRect(itemX + itemW - 40, itemY + 1, 16.0f, 13.0f, 3.0f, 0x33FF5555);
                }
                if (hoverDots) {
                    skija.drawRoundedRect(itemX + itemW - 20, itemY + 1, 16.0f, 13.0f, 3.0f, 0x33FFFFFF);
                }
                if (current) {
                    skija.drawMiniPauseIcon(itemX + itemW - 52, (float)itemY + (float)itemH * 0.5f, 3.0f, hoverPlay ? this.accent() : this.textSecondary());
                } else {
                    skija.drawMiniPlayIcon(itemX + itemW - 52, (float)itemY + (float)itemH * 0.5f, 3.5f, hoverPlay ? this.accent() : this.textSecondary());
                }
                skija.drawCrossIcon(itemX + itemW - 32, (float)itemY + (float)itemH * 0.5f, 6.0f, hoverDel ? -43691 : this.textSecondary());
                skija.drawDotsIcon(itemX + itemW - 12, (float)itemY + (float)itemH * 0.5f, 1.2f, hoverDots ? this.accent() : this.textSecondary());
            }
        }
    }

    private void renderPlayerBarSkija(SkijaEngine skija, int mouseX, int mouseY, int barX, int barY, int barW) {
        boolean sm = ModConfig.getInstance().speakerMode;
        int outlineColor = sm ? -19712 : this.accent();
        String badge = sm ? DrawUtils.t("Speaker", "\u041a\u043e\u043b\u043e\u043d\u043a\u0430") : DrawUtils.t("Local", "MP3");
        int badgeColor = outlineColor;
        int barH = 68;
        skija.drawDropShadow(barX, barY, barW, barH, 8.0f, 12.0f, 1.0f, outlineColor & 0x44FFFFFF);
        skija.drawRoundedRect(barX, barY, barW, barH, 8.0f, this.bgDark());
        skija.drawRoundedRectOutline(barX, barY, barW, barH, 8.0f, 1.0f, outlineColor);
        String trackInfo = this.localPlayer.getCurrentTrackName();
        long durMicros = this.localPlayer.getDuration();
        long posMicros = this.localPlayer.getCurrentPosition();
        long posSec = posMicros / 1000000L;
        long durSec = durMicros / 1000000L;
        float prog = durMicros > 0L ? (float)Math.max(0.0, Math.min(1.0, (double)posMicros / (double)durMicros)) : 0.0f;
        boolean playing = this.localPlayer.isPlaying();
        float vol = this.localPlayer.getVolume();
        int coverGlId = this.getLocalCoverGlId();
        skija.drawLocalCover(coverGlId, barX + 6, barY + 6, 34.0f, 5.0f);
        float badgeW = skija.getTextWidth(badge, 7.8f, true) + 12.0f;
        skija.drawBadge(barX + 46, barY + 6, 12.0f, badge, badgeColor);
        float maxTitleW = (float)(barW - 46) - badgeW - 12.0f;
        Object displayTitle = trackInfo;
        if (skija.getTextWidth((String)displayTitle, 10.0f, true) > maxTitleW) {
            while (((String)displayTitle).length() > 3 && skija.getTextWidth((String)displayTitle + "..", 10.0f, true) > maxTitleW) {
                displayTitle = ((String)displayTitle).substring(0, ((String)displayTitle).length() - 1);
            }
            displayTitle = (String)displayTitle + "..";
        }
        skija.drawText((String)displayTitle, (float)(barX + 46) + badgeW + 6.0f, barY + 12, 10.0f, this.textPrimary(), true, 17);
        int sbX = barX + 46;
        int sbW = barW - 126;
        int sbY = barY + 28;
        boolean hoverSeek = this.isInside(mouseX, mouseY, sbX, sbY - 4, sbW, 10);
        skija.drawProgressBar(sbX, sbY, sbW, 3.0f, prog, hoverSeek, outlineColor);
        String timerText = this.formatTime(posSec) + " / " + this.formatTime(durSec);
        skija.drawText(timerText, sbX + sbW + 6, sbY + 1, 8.0f, this.textSecondary(), false, 17);
        int ctrlCenter = barX + barW / 2;
        int btnY = barY + 46;
        int btnS = 16;
        int btnW_10 = 22;
        int btnPrevX = ctrlCenter - 61;
        int btnRewX = ctrlCenter - 39;
        int playCx = ctrlCenter;
        int playCy = btnY + 8;
        int btnFwdX = ctrlCenter + 17;
        int btnNextX = ctrlCenter + 45;
        boolean hoverPrev = this.isInside(mouseX, mouseY, btnPrevX, btnY, btnS, btnS);
        skija.drawButtonBase(btnPrevX, btnY, btnS, btnS, hoverPrev, false, this.accent());
        skija.drawSkipButton(btnPrevX + 8, btnY + 8, 7.0f, false, hoverPrev);
        boolean hoverRew = this.isInside(mouseX, mouseY, btnRewX, btnY, btnW_10, btnS);
        skija.drawButton(btnRewX, btnY, btnW_10, btnS, "-10", hoverRew, false, this.accent(), this.textSecondary());
        boolean hoverPlay = this.isInsideCircle(mouseX, mouseY, playCx, playCy, 10);
        skija.drawPlayPauseButton(playCx, playCy, 9.5f, playing, hoverPlay, outlineColor);
        boolean hoverFwd = this.isInside(mouseX, mouseY, btnFwdX, btnY, btnW_10, btnS);
        skija.drawButton(btnFwdX, btnY, btnW_10, btnS, "+10", hoverFwd, false, this.accent(), this.textSecondary());
        boolean hoverNext = this.isInside(mouseX, mouseY, btnNextX, btnY, btnS, btnS);
        skija.drawButtonBase(btnNextX, btnY, btnS, btnS, hoverNext, false, this.accent());
        skija.drawSkipButton(btnNextX + 8, btnY + 8, 7.0f, true, hoverNext);
        if (sm) {
            int rangeX = barX + 6;
            skija.drawText(DrawUtils.t("Range", "\u0420\u0430\u0434\u0438\u0443\u0441"), rangeX, btnY + 1, 7.0f, this.textSecondary(), false, 17);
            int maxRange = 64;
            int minRange = 8;
            float rProgress = (float)(ModConfig.getInstance().speakerRange - minRange) / (float)(maxRange - minRange);
            skija.drawProgressBar(rangeX, btnY + 8, 34.0f, 3.0f, rProgress, false, this.accent());
        }
        int volIconX = barX + barW - 54;
        int volBarX = barX + barW - 44;
        boolean hoverVol = this.isInside(mouseX, mouseY, volBarX - 2, btnY + 5, 44, 8);
        skija.drawSpeakerIcon(volIconX, btnY + 8, 9.0f, vol <= 0.001f, this.textSecondary());
        skija.drawProgressBar(volBarX, btnY + 7, 40.0f, 3.0f, vol, hoverVol, this.accent());
    }

    private void renderTrackMenuSkija(SkijaEngine skija, int mouseX, int mouseY) {
        int w = 135;
        int h = 90;
        skija.drawDropShadow(this.trackMenuX, this.trackMenuY, w, h, 8.0f, 16.0f, 2.0f, 0x66000000);
        skija.drawRoundedRect(this.trackMenuX, this.trackMenuY, w, h, 8.0f, this.bgDark());
        skija.drawRoundedRectOutline(this.trackMenuX, this.trackMenuY, w, h, 8.0f, 1.0f, this.border());
        String[] labels = new String[]{DrawUtils.t("Favorite", "\u041d\u0440\u0430\u0432\u0438\u0442\u0441\u044f"), DrawUtils.t("Repeat", "\u041f\u043e\u0432\u0442\u043e\u0440"), DrawUtils.t("Make Disc", "\u041f\u043b\u0430\u0441\u0442\u0438\u043d\u043a\u0430"), DrawUtils.t("Info", "\u0421\u0432\u0435\u0434\u0435\u043d\u0438\u044f"), DrawUtils.t("Share", "\u041f\u043e\u0434\u0435\u043b\u0438\u0442\u044c\u0441\u044f"), DrawUtils.t("Delete", "\u0423\u0434\u0430\u043b\u0438\u0442\u044c")};
        for (int i = 0; i < 6; ++i) {
            int color;
            int y = this.trackMenuY + 3 + i * 14;
            boolean hover = this.isInside(mouseX, mouseY, this.trackMenuX + 2, y, w - 4, 14);
            if (hover) {
                skija.drawRoundedRect(this.trackMenuX + 2, y, w - 4, 14.0f, 4.0f, this.bgHover());
            }
            color = (i == 1 && this.localPlayer.isRepeat()) ? this.accent() : (hover ? this.accent() : this.textPrimary());
            if (i == 5) {
                color = hover ? -43691 : -2271915;
            }
            String menuIcon = switch (i) {
                case 0 -> {
                    if (this.localPlayer.isFavorite()) {
                        yield "\ue87d";
                    }
                    yield "\ue87e";
                }
                case 1 -> "\ue040";
                case 2 -> "\ue019";
                case 3 -> "\ue88e";
                case 4 -> "\ue80d";
                case 5 -> "\ue872";
                default -> "";
            };
            skija.drawIcon(menuIcon, this.trackMenuX + 11, (float)y + 7.5f, 10.5f, color, 18);
            skija.drawText(labels[i], this.trackMenuX + 22, y + 7, 9.0f, color, hover, 17);
        }
    }

    private void renderSettingsTabSkija(SkijaEngine skija, int mouseX, int mouseY, int px, int py, int pw, int ph) {
        int sx = px + 8;
        int sy = py + 30;
        int sw = pw - 16;
        int sh = ph - 36;
        skija.drawDropShadow(sx, sy, sw, sh, 8.0f, 12.0f, 1.0f, 0x44000000);
        skija.drawRoundedRect(sx, sy, sw, sh, 10.0f, this.bgDark());
        skija.drawRoundedRectOutline(sx, sy, sw, sh, 10.0f, 1.0f, this.border());
        skija.scissor(sx + 2, sy + 2, sw - 4, sh - 4);
        int cy = sy + 8 - this.settingsScroll;
        int cardX = sx + 8;
        int cardW = sw - 16;
        skija.drawText(DrawUtils.t("APPEARANCE & INTERFACE", "\u0412\u041d\u0415\u0428\u041d\u0418\u0419 \u0412\u0418\u0414 \u0418 \u0418\u041d\u0422\u0415\u0420\u0424\u0415\u0419\u0421"), sx + 12, cy + 6, 8.5f, this.accent(), true, 17);
        int card1Y = cy + 18;
        int card1H = 80;
        skija.drawRoundedRect(cardX, card1Y, cardW, card1H, 8.0f, this.bgPanel());
        skija.drawRoundedRectOutline(cardX, card1Y, cardW, card1H, 8.0f, 0.8f, this.border());
        skija.drawMoonSunIcon(cardX + 16, card1Y + 13, 5.5f, darkTheme, this.accent());
        skija.drawText(DrawUtils.t("Theme Mode", "\u0422\u0435\u043c\u0430 \u043e\u0444\u043e\u0440\u043c\u043b\u0435\u043d\u0438\u044f"), cardX + 28, card1Y + 13, 8.5f, this.textPrimary(), false, 17);
        int pillThemeW = 84;
        int pillThemeH = 18;
        int pillThemeX = cardX + cardW - pillThemeW - 10;
        int pillThemeY = card1Y + 4;
        boolean hoverTheme = this.isInside(mouseX, mouseY, pillThemeX, pillThemeY, pillThemeW, pillThemeH);
        String themeText = darkTheme ? DrawUtils.t("Dark", "\u0422\u0451\u043c\u043d\u0430\u044f") : DrawUtils.t("Light", "\u0421\u0432\u0435\u0442\u043b\u0430\u044f");
        skija.drawButton(pillThemeX, pillThemeY, pillThemeW, pillThemeH, themeText, hoverTheme, false, this.accent(), this.textPrimary());
        skija.drawRoundedRect(cardX + 10, card1Y + 26, cardW - 20, 0.6f, 0.3f, this.border());
        skija.drawText(DrawUtils.t("Interface Language", "\u042f\u0437\u044b\u043a \u0438\u043d\u0442\u0435\u0440\u0444\u0435\u0439\u0441\u0430"), cardX + 14, card1Y + 39, 8.5f, this.textPrimary(), false, 17);
        int pillLangW = 84;
        int pillLangH = 18;
        int pillLangX = cardX + cardW - pillLangW - 10;
        int pillLangY = card1Y + 30;
        boolean hoverLang = this.isInside(mouseX, mouseY, pillLangX, pillLangY, pillLangW, pillLangH);
        String langText = ModConfig.getInstance().isEnglish ? "English" : "\u0420\u0443\u0441\u0441\u043a\u0438\u0439";
        skija.drawButton(pillLangX, pillLangY, pillLangW, pillLangH, langText, hoverLang, false, -13058568, this.textPrimary());
        skija.drawRoundedRect(cardX + 10, card1Y + 52, cardW - 20, 0.6f, 0.3f, this.border());
        skija.drawText(DrawUtils.t("Dynamic Island HUD", "\u041e\u0432\u0435\u0440\u043b\u0435\u0439 Dynamic Island"), cardX + 14, card1Y + 65, 8.5f, this.textPrimary(), false, 17);
        int pillHudW = 84;
        int pillHudH = 18;
        int pillHudX = cardX + cardW - pillHudW - 10;
        int pillHudY = card1Y + 56;
        boolean hudActive = this.gsmtc.isHudEnabled();
        String hudText = hudActive ? DrawUtils.t("ON", "\u0412\u041a\u041b") : DrawUtils.t("OFF", "\u0412\u042b\u041a\u041b");
        boolean hoverHud = this.isInside(mouseX, mouseY, pillHudX, pillHudY, pillHudW, pillHudH);
        skija.drawTogglePill(pillHudX, pillHudY, pillHudW, pillHudH, hudText, hudActive, hoverHud, -15681151);
        int sec2Y = cy + 108;
        skija.drawText(DrawUtils.t("AUDIO & 3D SPEAKER", "\u0417\u0412\u0423\u041a \u0418 3D \u041a\u041e\u041b\u041e\u041d\u041a\u0410"), sx + 12, sec2Y + 6, 8.5f, -19712, true, 17);
        int card2Y = sec2Y + 18;
        int card2H = 92;
        skija.drawRoundedRect(cardX, card2Y, cardW, card2H, 8.0f, this.bgPanel());
        skija.drawRoundedRectOutline(cardX, card2Y, cardW, card2H, 8.0f, 0.8f, this.border());
        int btnW = (cardW - 28) / 2;
        int btnH = 20;
        int btn1X = cardX + 10;
        int btn1Y = card2Y + 6;
        int btn2X = cardX + 18 + btnW;
        int btn2Y = card2Y + 6;
        boolean soundOn = !ModConfig.getInstance().muteGlobal;
        String soundText = soundOn ? DrawUtils.t("Sound: ON", "\u0417\u0432\u0443\u043a: \u0412\u041a\u041b") : DrawUtils.t("Sound: OFF", "\u0417\u0432\u0443\u043a: \u0412\u042b\u041a\u041b");
        boolean hoverSound = this.isInside(mouseX, mouseY, btn1X, btn1Y, btnW, btnH);
        skija.drawTogglePill(btn1X, btn1Y, btnW, btnH, soundText, soundOn, hoverSound, this.accent());
        boolean spkOn = ModConfig.getInstance().speakerMode;
        String spkText = spkOn ? DrawUtils.t("3D: ON", "3D: \u0412\u041a\u041b") : DrawUtils.t("3D: OFF", "3D: \u0412\u042b\u041a\u041b");
        boolean hoverSpk = this.isInside(mouseX, mouseY, btn2X, btn2Y, btnW, btnH);
        skija.drawTogglePill(btn2X, btn2Y, btnW, btnH, spkText, spkOn, hoverSpk, -19712);
        skija.drawRoundedRect(cardX + 10, card2Y + 31, cardW - 20, 0.6f, 0.3f, this.border());
        skija.drawSpeakerIcon(cardX + 16, card2Y + 45, 6.5f, false, -19712);
        skija.drawText(DrawUtils.t("3D Sound Radius", "\u0420\u0430\u0434\u0438\u0443\u0441 3D \u0437\u0432\u0443\u043a\u0430"), cardX + 26, card2Y + 45, 8.5f, -19712, false, 17);
        int slRangeW = 108;
        int slRangeH = 6;
        int badgeW = 34;
        int badgeH = 16;
        int badgeX = cardX + cardW - badgeW - 10;
        int badgeY = card2Y + 37;
        int slRangeX = badgeX - slRangeW - 8;
        int slRangeY = card2Y + 42;
        skija.drawRoundedRect(badgeX, badgeY, badgeW, badgeH, 4.0f, 587182848);
        skija.drawRoundedRectOutline(badgeX, badgeY, badgeW, badgeH, 4.0f, 0.8f, -19712);
        skija.drawText(ModConfig.getInstance().speakerRange + "\u043c", (float)badgeX + (float)badgeW * 0.5f, (float)badgeY + (float)badgeH * 0.5f + 0.5f, 7.5f, -19712, true, 18);
        float rProg = (float)(ModConfig.getInstance().speakerRange - 8) / 56.0f;
        boolean hoverRange = this.isInside(mouseX, mouseY, slRangeX - 4, slRangeY - 5, slRangeW + 8, 16);
        skija.drawSlider(slRangeX, slRangeY, slRangeW, slRangeH, rProg, -19712, hoverRange);
        skija.drawRoundedRect(cardX + 10, card2Y + 61, cardW - 20, 0.6f, 0.3f, this.border());
        float curVol = this.localPlayer.getVolume();
        skija.drawSpeakerIcon(cardX + 16, card2Y + 75, 6.5f, curVol <= 0.001f, this.accent());
        skija.drawText(DrawUtils.t("MP3 Music Volume", "\u0413\u0440\u043e\u043c\u043a\u043e\u0441\u0442\u044c MP3"), cardX + 26, card2Y + 75, 8.5f, this.accent(), false, 17);
        int slVolW = 108;
        int slVolH = 6;
        int badgeVolX = cardX + cardW - badgeW - 10;
        int badgeVolY = card2Y + 67;
        int slVolX = badgeVolX - slVolW - 8;
        int slVolY = card2Y + 72;
        skija.drawRoundedRect(badgeVolX, badgeVolY, badgeW, badgeH, 4.0f, this.accent() & 0xFFFFFF | 0x22000000);
        skija.drawRoundedRectOutline(badgeVolX, badgeVolY, badgeW, badgeH, 4.0f, 0.8f, this.accent());
        skija.drawText((int)(curVol * 100.0f) + "%", (float)badgeVolX + (float)badgeW * 0.5f, (float)badgeVolY + (float)badgeH * 0.5f + 0.5f, 7.5f, this.accent(), true, 18);
        boolean hoverVol = this.isInside(mouseX, mouseY, slVolX - 4, slVolY - 5, slVolW + 8, 16);
        skija.drawSlider(slVolX, slVolY, slVolW, slVolH, curVol, this.accent(), hoverVol);
        int sec3Y = cy + 228;
        skija.drawText(DrawUtils.t("USER GUIDE & TAB MANUAL", "\u041f\u041e\u041b\u041d\u041e\u0415 \u0420\u0423\u041a\u041e\u0412\u041e\u0414\u0421\u0422\u0412\u041e \u041f\u041e \u0412\u041a\u041b\u0410\u0414\u041a\u0410\u041c"), sx + 12, sec3Y + 6, 8.5f, this.accent(), true, 17);
        String[][] guideItems = new String[][]{{"Webs / Apps", DrawUtils.t("Streaming", "\u0421\u0442\u0440\u0438\u043c\u0438\u043d\u0433"), DrawUtils.t("Stream music from Yandex Music, Spotify, browsers and Windows.", "\u041c\u0443\u0437\u044b\u043a\u0430 \u0438\u0437 \u042f\u043d\u0434\u0435\u043a\u0441 \u041c\u0443\u0437\u044b\u043a\u0438, Spotify, \u0431\u0440\u0430\u0443\u0437\u0435\u0440\u043e\u0432 \u0438 \u041f\u041a."), DrawUtils.t("Sync: Play/Pause, -10s/+10s seek, and volume control.", "\u0421\u0438\u043d\u0445\u0440\u043e\u043d\u0438\u0437\u0430\u0446\u0438\u044f: \u041f\u043b\u0435\u0439/\u041f\u0430\u0443\u0437\u0430, \u043f\u0435\u0440\u0435\u043c\u043e\u0442\u043a\u0430 -10/+10\u0441 \u0438 \u0437\u0432\u0443\u043a.")}, {DrawUtils.t("MP3 Player", "MP3 \u041f\u043b\u0435\u0435\u0440"), DrawUtils.t("Local Files", "\u041b\u043e\u043a\u0430\u043b\u044c\u043d\u044b\u0435"), DrawUtils.t("Local tracks from .minecraft/mp3player folder (MP3 & WAV).", "\u041c\u0443\u0437\u044b\u043a\u0430 \u0438\u0437 \u043f\u0430\u043f\u043a\u0438 .minecraft/mp3player (MP3 \u0438 WAV)."), DrawUtils.t("Button 'DIR' opens folder, button 'R' refreshes the list.", "\u041a\u043d\u043e\u043f\u043a\u0430 'DIR' \u043e\u0442\u043a\u0440\u044b\u0432\u0430\u0435\u0442 \u043f\u0430\u043f\u043a\u0443, \u043a\u043d\u043e\u043f\u043a\u0430 'R' \u043e\u0431\u043d\u043e\u0432\u043b\u044f\u0435\u0442 \u0441\u043f\u0438\u0441\u043e\u043a.")}, {DrawUtils.t("Favorites", "\u0418\u0437\u0431\u0440\u0430\u043d\u043d\u043e\u0435"), DrawUtils.t("Favorites", "\u041f\u043b\u0435\u0439\u043b\u0438\u0441\u0442"), DrawUtils.t("Quick access playlist for your most favorite music tracks.", "\u0411\u044b\u0441\u0442\u0440\u044b\u0439 \u043f\u043b\u0435\u0439\u043b\u0438\u0441\u0442 \u0434\u043b\u044f \u0432\u0430\u0448\u0438\u0445 \u0441\u0430\u043c\u044b\u0445 \u043b\u044e\u0431\u0438\u043c\u044b\u0445 \u043f\u0435\u0441\u0435\u043d."), DrawUtils.t("Click '...' next to any track and select heart to favorite.", "\u041d\u0430\u0436\u043c\u0438\u0442\u0435 '...' \u0443 \u043b\u044e\u0431\u043e\u0433\u043e \u0442\u0440\u0435\u043a\u0430 \u0438 \u0432\u044b\u0431\u0435\u0440\u0438\u0442\u0435 \u0441\u0435\u0440\u0434\u0435\u0447\u043a\u043e.")}, {DrawUtils.t("Add", "\u0414\u043e\u0431\u0430\u0432\u0438\u0442\u044c"), DrawUtils.t("In Browser", "\u0412 \u0431\u0440\u0430\u0443\u0437\u0435\u0440\u0435"), DrawUtils.t("Opens your browser with the song title and artist.", "\u041e\u0442\u043a\u0440\u044b\u0432\u0430\u0435\u0442 \u0431\u0440\u0430\u0443\u0437\u0435\u0440 \u0441 \u043d\u0430\u0437\u0432\u0430\u043d\u0438\u0435\u043c \u0438 \u0430\u0432\u0442\u043e\u0440\u043e\u043c \u0438\u0433\u0440\u0430\u044e\u0449\u0435\u0439 \u043f\u0435\u0441\u043d\u0438."), DrawUtils.t("Quickly find and download your favorite tracks directly.", "\u041f\u043e\u0437\u0432\u043e\u043b\u044f\u0435\u0442 \u0431\u044b\u0441\u0442\u0440\u043e \u043d\u0430\u0439\u0442\u0438 \u0438 \u0441\u043a\u0430\u0447\u0430\u0442\u044c \u043f\u043e\u043d\u0440\u0430\u0432\u0438\u0432\u0448\u0443\u044e\u0441\u044f \u043f\u0435\u0441\u043d\u044e.")}, {DrawUtils.t("3D Speaker", "3D \u041a\u043e\u043b\u043e\u043d\u043a\u0430"), DrawUtils.t("3D Spatial", "3D \u0417\u0432\u0443\u043a"), DrawUtils.t("Broadcasts 3D positional audio and floating hologram to all players.", "\u041f\u043e\u0437\u0438\u0446\u0438\u043e\u043d\u043d\u044b\u0439 3D-\u0437\u0432\u0443\u043a \u0432\u043e\u043a\u0440\u0443\u0433 \u0432\u0430\u0441 \u0438 \u0433\u043e\u043b\u043e\u0433\u0440\u0430\u043c\u043c\u0430 \u043d\u0430\u0434 \u0433\u043e\u043b\u043e\u0432\u043e\u0439."), DrawUtils.t("Adjust hearing distance (8m to 64m) with range slider above.", "\u0414\u0430\u043b\u044c\u043d\u043e\u0441\u0442\u044c \u0441\u043b\u044b\u0448\u0438\u043c\u043e\u0441\u0442\u0438 (8-64\u043c) \u043c\u0435\u043d\u044f\u0435\u0442\u0441\u044f \u043f\u043e\u043b\u0437\u0443\u043d\u043a\u043e\u043c \u0432\u044b\u0448\u0435.")}, {DrawUtils.t("Vinyl Discs", "\u041f\u043b\u0430\u0441\u0442\u0438\u043d\u043a\u0438"), DrawUtils.t("Vinyl Discs", "\u041f\u043b\u0430\u0441\u0442\u0438\u043d\u043a\u0438"), DrawUtils.t("Burn any song onto music discs (Track menu '...' -> Disc).", "\u0417\u0430\u043f\u0438\u0441\u044b\u0432\u0430\u0439\u0442\u0435 \u043f\u0435\u0441\u043d\u0438 \u043d\u0430 \u043f\u043b\u0430\u0441\u0442\u0438\u043d\u043a\u0438 (\u043c\u0435\u043d\u044e '...' -> \u041f\u043b\u0430\u0441\u0442\u0438\u043d\u043a\u0430)."), DrawUtils.t("Playing in jukebox renders rotating 3D vinyl and floating hologram!", "\u0412 \u043f\u0440\u043e\u0438\u0433\u0440\u044b\u0432\u0430\u0442\u0435\u043b\u0435 \u043a\u0440\u0443\u0442\u0438\u0442 3D-\u0434\u0438\u0441\u043a \u0438 \u043f\u0430\u0440\u0438\u0442 \u0433\u043e\u043b\u043e\u0433\u0440\u0430\u043c\u043c\u0430!")}, {"Dynamic Island", DrawUtils.t("Overlay HUD", "\u041e\u0432\u0435\u0440\u043b\u0435\u0439"), DrawUtils.t("Compact HUD overlay at screen top with cover, seconds, and equalizer.", "\u041e\u0432\u0435\u0440\u043b\u0435\u0439 \u0432\u0432\u0435\u0440\u0445\u0443 \u044d\u043a\u0440\u0430\u043d\u0430 \u0441 \u043e\u0431\u043b\u043e\u0436\u043a\u043e\u0439, \u0442\u0430\u0439\u043c\u0435\u0440\u043e\u043c \u0438 \u044d\u043a\u0432\u0430\u043b\u0430\u0439\u0437\u0435\u0440\u043e\u043c."), DrawUtils.t("Clickable directly in-game for instant playback controls.", "\u041a\u043b\u0438\u043a\u0430\u0431\u0435\u043b\u0435\u043d \u043f\u0440\u044f\u043c\u043e \u0432 \u0438\u0433\u0440\u0435 \u0434\u043b\u044f \u0443\u043f\u0440\u0430\u0432\u043b\u0435\u043d\u0438\u044f \u0431\u0435\u0437 \u043e\u0442\u043a\u0440\u044b\u0442\u0438\u044f \u043c\u0435\u043d\u044e.")}};
        int[] badgeColors = new int[]{-16739073, -13058568, -46226, -15681151, -19712, -5745161, -15817239};
        int card3Y = sec3Y + 18;
        int itemH = 44;
        int card3H = 4 + guideItems.length * itemH;
        skija.drawRoundedRect(cardX, card3Y, cardW, card3H, 8.0f, this.bgPanel());
        skija.drawRoundedRectOutline(cardX, card3Y, cardW, card3H, 8.0f, 0.8f, this.border());
        float maxTextW = cardW - 20;
        for (int i = 0; i < guideItems.length; ++i) {
            String[] g = guideItems[i];
            int itemY = card3Y + 3 + i * itemH;
            int bCol = badgeColors[i % badgeColors.length];
            int gBadgeW = 96;
            int gBadgeH = 15;
            int badgeItemX = cardX + 8;
            int badgeItemY = itemY + 3;
            skija.drawRoundedRect(badgeItemX, badgeItemY, gBadgeW, gBadgeH, 7.5f, bCol & 0xFFFFFF | 0x22000000);
            skija.drawRoundedRectOutline(badgeItemX, badgeItemY, gBadgeW, gBadgeH, 7.5f, 0.8f, bCol);
            skija.drawText(g[0], (float)badgeItemX + (float)gBadgeW * 0.5f, (float)badgeItemY + (float)gBadgeH * 0.5f + 0.5f, 7.5f, bCol, true, 18);
            skija.drawText(g[1], cardX + cardW - 10, itemY + 10, 7.0f, this.textSecondary(), false, 20);
            skija.drawBoundedText(g[2], cardX + 10, itemY + 22, 7.5f, this.textPrimary(), false, maxTextW);
            skija.drawBoundedText(g[3], cardX + 10, itemY + 33, 6.8f, this.textSecondary(), false, maxTextW);
            if (i >= guideItems.length - 1) continue;
            skija.drawRoundedRect(cardX + 10, itemY + itemH - 1, cardW - 20, 0.5f, 0.25f, this.border());
        }
        skija.resetScissor();
        int totalContentH = card3Y + card3H + 16 - cy;
        this.maxSettingsScroll = Math.max(0, totalContentH - sh);
        this.settingsScroll = Math.max(0, Math.min(this.maxSettingsScroll, this.settingsScroll));
        if (this.maxSettingsScroll > 0) {
            float scrollFrac = (float)this.settingsScroll / (float)this.maxSettingsScroll;
            float trackH = sh - 10;
            float scrollbarH = Math.max(24.0f, trackH * ((float)sh / (float)(sh + this.maxSettingsScroll)));
            float scrollbarY = (float)(sy + 5) + (trackH - scrollbarH) * scrollFrac;
            float scrollbarX = sx + sw - 6;
            skija.drawRoundedRect(scrollbarX, scrollbarY, 3.0f, scrollbarH, 1.5f, 0x66FFFFFF);
            skija.drawDropShadow(scrollbarX, scrollbarY, 3.0f, scrollbarH, 1.5f, 4.0f, 0.5f, 0x33000000);
        }
    }

    private boolean handleSettingsTabClick(double mx, double my, int px, int py, int pw, int ph) {
        int sx = px + 8;
        int sy = py + 30;
        int sw = pw - 16;
        int sh = ph - 36;
        int cardX = sx + 8;
        int cardW = sw - 16;
        int pillThemeW = 84;
        int pillThemeX = cardX + cardW - pillThemeW - 10;
        int cy = sy + 8 - this.settingsScroll;
        int card1Y = cy + 18;
        int pillThemeY = card1Y + 4;
        int pillThemeH = 18;
        if (this.isInside(mx, my, pillThemeX, pillThemeY, pillThemeW, pillThemeH)) {
            this.playClickSound();
            darkTheme = !darkTheme;
            SkijaEngine.getInstance().setDarkTheme(darkTheme);
            ModConfig.getInstance().darkTheme = darkTheme;
            ModConfig.getInstance().save();
            return true;
        }
        int pillLangW = 84;
        int pillLangX = cardX + cardW - pillLangW - 10;
        int pillLangY = card1Y + 30;
        int pillLangH = 18;
        if (this.isInside(mx, my, pillLangX, pillLangY, pillLangW, pillLangH)) {
            this.playClickSound();
            ModConfig.getInstance().isEnglish = !ModConfig.getInstance().isEnglish;
            ModConfig.getInstance().save();
            return true;
        }
        int pillHudW = 84;
        int pillHudX = cardX + cardW - pillHudW - 10;
        int pillHudY = card1Y + 56;
        int pillHudH = 18;
        if (this.isInside(mx, my, pillHudX, pillHudY, pillHudW, pillHudH)) {
            this.playClickSound();
            this.gsmtc.setHudEnabled(!this.gsmtc.isHudEnabled());
            return true;
        }
        int btn1X = cardX + 10;
        int sec2Y = cy + 108;
        int card2Y = sec2Y + 18;
        int btn1Y = card2Y + 6;
        int btnW = (cardW - 28) / 2;
        int btnH = 20;
        if (this.isInside(mx, my, btn1X, btn1Y, btnW, btnH)) {
            this.playClickSound();
            ModConfig.getInstance().muteGlobal = !ModConfig.getInstance().muteGlobal;
            ModConfig.getInstance().save();
            return true;
        }
        int btn2X = cardX + 18 + btnW;
        int btn2Y = card2Y + 6;
        if (this.isInside(mx, my, btn2X, btn2Y, btnW, btnH)) {
            this.playClickSound();
            ModConfig.getInstance().speakerMode = !ModConfig.getInstance().speakerMode;
            ModConfig.getInstance().save();
            if (ModConfig.getInstance().speakerMode) {
                this.localPlayer.broadcastState();
            } else {
                this.localPlayer.broadcastStop();
            }
            return true;
        }
        int badgeW = 34;
        int badgeX = cardX + cardW - badgeW - 10;
        int slRangeW = 108;
        int slRangeX = badgeX - slRangeW - 8;
        int slRangeY = card2Y + 42;
        if (this.isInside(mx, my, slRangeX - 4, slRangeY - 5, slRangeW + 8, 16)) {
            this.draggingRange = true;
            float rProg = (float)Math.max(0.0, Math.min(1.0, (mx - (double)slRangeX) / (double)slRangeW));
            ModConfig.getInstance().speakerRange = 8 + (int)(rProg * 56.0f);
            ModConfig.getInstance().save();
            this.localPlayer.broadcastState();
            return true;
        }
        int badgeVolX = cardX + cardW - badgeW - 10;
        int slVolW = 108;
        int slVolX = badgeVolX - slVolW - 8;
        int slVolY = card2Y + 72;
        if (this.isInside(mx, my, slVolX - 4, slVolY - 5, slVolW + 8, 16)) {
            this.draggingVolume = true;
            float vol = (float)Math.max(0.0, Math.min(1.0, (mx - (double)slVolX) / (double)slVolW));
            this.localPlayer.setVolume(vol);
            ModConfig.getInstance().musicVolume = vol;
            ModConfig.getInstance().save();
            return true;
        }
        return false;
    }

    private void downloadCurrentTrack() {
        String[] parts;
        String cleanedRawArtist;
        String rawTitle = "";
        String rawArtist = "";
        if (this.gsmtc.isAvailable() && this.gsmtc.hasTrack()) {
            rawTitle = this.gsmtc.getTitle();
            rawArtist = this.gsmtc.getArtist();
        } else if (this.localPlayer.isPlaying()) {
            String full = this.localPlayer.getCurrentTrackName();
            if (full.contains(" - ")) {
                String[] p = full.split(" - ", 2);
                rawArtist = p[0].trim();
                rawTitle = p[1].replace(".wav", "").replace(".mp3", "").trim();
            } else {
                rawTitle = full.replace(".wav", "").replace(".mp3", "").trim();
            }
        }
        if (rawTitle.isEmpty() || rawTitle.equalsIgnoreCase("No track") || rawTitle.equalsIgnoreCase("Nothing playing")) {
            this.downloadStatus = DrawUtils.t("Turn on song first", "\u0412\u043a\u043b\u044e\u0447\u0438\u0442\u0435 \u0442\u0440\u0435\u043a");
            this.downloadStatusTimestamp = System.currentTimeMillis();
            return;
        }
        String cleanedRawTitle = rawTitle.replaceAll("(?i)\\s*[-|\u2014\u2013]\\s*(?:Spotify|\u042f\u043d\u0434\u0435\u043a\u0441\\s*\u041c\u0443\u0437\u044b\u043a\u0430|\u042f\u043d\u0434\u0435\u043a\u0441\\.\u041c\u0443\u0437\u044b\u043a\u0430|YouTube\\s*Music|YouTube|SoundCloud|VK\\s*\u041c\u0443\u0437\u044b\u043a\u0430|VK|Apple\\s*Music)", "").replaceAll("(?i)\\s*[-\u2013\u2014]\\s*(?:(?:\\d{4}\\s+)?Remaster(?:ed)?(?:\\s+\\d{4})?|Live(?:\\s+.*)?|Radio\\s+Edit|Single\\s+Version|Album\\s+Version|Original\\s+Mix|Acoustic(?:\\s+Version)?|Deluxe(?:\\s+Edition)?|Bonus\\s+Track).*$", "").replaceAll("(?i)\\s*\\((?:Official\\s*(?:Music\\s*)?Video|Official\\s*Audio|Lyrics|Lyric\\s*Video|Visualizer|Audio|HQ|HD|4K|(?:\\d{4}\\s+)?Remaster(?:ed)?(?:\\s+\\d{4})?|Single\\s+Version|Album\\s+Version|Radio\\s+Edit|Original\\s+Mix|Deluxe\\s+Edition|Bonus\\s+Track|feat\\.[^)]*)\\)", "").replaceAll("(?i)\\s*\\[(?:Official\\s*(?:Music\\s*)?Video|Official\\s*Audio|Lyrics|Lyric\\s*Video|Visualizer|Audio|HQ|HD|4K|(?:\\d{4}\\s+)?Remaster(?:ed)?(?:\\s+\\d{4})?|Single\\s+Version|Album\\s+Version|Radio\\s+Edit|Original\\s+Mix|Deluxe\\s+Edition|Bonus\\s+Track|feat\\.[^\\]]*)\\]", "").trim();
        String string = cleanedRawArtist = rawArtist != null ? rawArtist.trim() : "";
        if (cleanedRawArtist.equalsIgnoreCase("Spotify") || cleanedRawArtist.equalsIgnoreCase("Google Chrome") || cleanedRawArtist.equalsIgnoreCase("Yandex") || cleanedRawArtist.equalsIgnoreCase("Firefox") || cleanedRawArtist.equalsIgnoreCase("msedge") || cleanedRawArtist.equalsIgnoreCase("Unknown") || cleanedRawArtist.equalsIgnoreCase("\u041d\u0435\u0438\u0437\u0432\u0435\u0441\u0442\u0435\u043d")) {
            cleanedRawArtist = "";
        }
        if (cleanedRawArtist.isEmpty() && (cleanedRawTitle.contains(" - ") || cleanedRawTitle.contains(" \u2014 ") || cleanedRawTitle.contains(" \u2013 ")) && (parts = cleanedRawTitle.split("\\s+[-\u2014\u2013]\\s+", 2)).length == 2) {
            cleanedRawArtist = parts[0].trim();
            cleanedRawTitle = parts[1].trim();
        }
        if (!cleanedRawArtist.isEmpty()) {
            String p1 = cleanedRawArtist.toLowerCase() + " - ";
            String p2 = cleanedRawArtist.toLowerCase() + " \u2014 ";
            String p3 = cleanedRawArtist.toLowerCase() + " \u2013 ";
            if (cleanedRawTitle.toLowerCase().startsWith(p1)) {
                cleanedRawTitle = cleanedRawTitle.substring(p1.length()).trim();
            } else if (cleanedRawTitle.toLowerCase().startsWith(p2)) {
                cleanedRawTitle = cleanedRawTitle.substring(p2.length()).trim();
            } else if (cleanedRawTitle.toLowerCase().startsWith(p3)) {
                cleanedRawTitle = cleanedRawTitle.substring(p3.length()).trim();
            }
        }
        String finalArtist = cleanedRawArtist;
        String finalTitle = cleanedRawTitle;
        String searchQuery = !finalArtist.isEmpty() ? finalArtist + " - " + finalTitle : finalTitle;
        try {
            String encoded = URLEncoder.encode(searchQuery, StandardCharsets.UTF_8);
            try {
                if (java.awt.Desktop.isDesktopSupported() && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.BROWSE)) {
                    java.awt.Desktop.getDesktop().browse(new java.net.URI("https://www.google.com/search?q=" + encoded));
                } else {
                    new ProcessBuilder("cmd", "/c", "start", "https://www.google.com/search?q=" + encoded).start();
                }
            } catch (Throwable t2) {
                new ProcessBuilder("cmd", "/c", "start", "https://www.google.com/search?q=" + encoded).start();
            }
            this.downloadStatus = DrawUtils.t("Opened!", "\u041e\u0442\u043a\u0440\u044b\u0442\u043e!");
            this.downloadStatusTimestamp = System.currentTimeMillis();
            this.playClickSound();
        }
        catch (Throwable t) {
            this.downloadStatus = DrawUtils.t("Error", "\u041e\u0448\u0438\u0431\u043a\u0430");
            this.downloadStatusTimestamp = System.currentTimeMillis();
        }
    }

    private boolean handleOnlineTabClick(double mx, double my, int px, int py, int pw, int ph) {
        int sliderW;
        int auxY;
        int rangeBoxW;
        int rangeBoxX;
        int sliderX;
        int cx;
        boolean sm = ModConfig.getInstance().speakerMode;
        int btnY = py + 140;
        int btnS = 16;
        int btnW_10 = 20;
        int playCx = cx = px + pw / 2;
        int playCy = btnY + 9;
        int btnRewX = playCx - 14 - btnW_10;
        int btnPrevX = btnRewX - 4 - btnS;
        int dlBtnW = 54;
        int dlBtnH = 18;
        int dlBtnX = btnPrevX - 6 - dlBtnW;
        int dlBtnY = btnY;
        int btnFwdX = playCx + 14;
        int btnNextX = btnFwdX + btnW_10 + 4;
        int volIconX = btnNextX + btnS + 8;
        int volBarX = volIconX + 12;
        int volBarW = 46;
        if (this.isInside(mx, my, dlBtnX, dlBtnY, dlBtnW, dlBtnH)) {
            this.playClickSound();
            this.downloadCurrentTrack();
            return true;
        }
        int sbX = px + 44;
        int timeY = py + 120;
        int sbW = pw - 88;
        if (this.isInside(mx, my, sbX, timeY - 5, sbW, 12)) {
            this.draggingGsmtcSeek = true;
            this.draggedGsmtcSeekFraction = (float)Math.max(0.0, Math.min(1.0, (mx - (double)sbX) / (double)sbW));
            if (this.gsmtc.getDuration() > 0.0) {
                this.gsmtc.seek(this.gsmtc.getDuration() * (double)this.draggedGsmtcSeekFraction);
                if (sm) {
                    this.localPlayer.broadcastState();
                }
            }
            return true;
        }
        if (this.isInside(mx, my, btnPrevX, btnY, btnS, btnS)) {
            this.playClickSound();
            this.gsmtc.previous();
            if (sm) {
                this.localPlayer.broadcastState();
            }
            return true;
        }
        if (this.isInside(mx, my, btnRewX, btnY, btnW_10, btnS)) {
            this.playClickSound();
            this.gsmtc.seekRelative(-10.0);
            if (sm) {
                this.localPlayer.broadcastState();
            }
            return true;
        }
        if (this.isInsideCircle(mx, my, playCx, playCy, 13)) {
            this.playClickSound();
            if (this.localPlayer.isPlaying()) {
                this.localPlayer.pause();
            }
            this.gsmtc.togglePlayPause();
            if (sm) {
                this.localPlayer.broadcastState();
            }
            return true;
        }
        if (this.isInside(mx, my, btnFwdX, btnY, btnW_10, btnS)) {
            this.playClickSound();
            this.gsmtc.seekRelative(10.0);
            if (sm) {
                this.localPlayer.broadcastState();
            }
            return true;
        }
        if (this.isInside(mx, my, btnNextX, btnY, btnS, btnS)) {
            this.playClickSound();
            this.gsmtc.next();
            if (sm) {
                this.localPlayer.broadcastState();
            }
            return true;
        }
        if (this.isInside(mx, my, volBarX - 4, btnY - 2, volBarW + 8, 16)) {
            this.draggingGsmtcVolume = true;
            float vol = (float)Math.max(0.0, Math.min(1.0, (mx - (double)volBarX) / (double)volBarW));
            this.gsmtc.setVolume(vol);
            if (sm) {
                this.localPlayer.broadcastState();
            }
            return true;
        }
        if (sm && this.isInside(mx, my, (sliderX = (rangeBoxX = cx - (rangeBoxW = 150) / 2) + 54) - 4, (auxY = py + 164) - 2, (sliderW = rangeBoxW - 64) + 8, 18)) {
            this.draggingRange = true;
            float rProg = (float)Math.max(0.0, Math.min(1.0, (mx - (double)sliderX) / (double)sliderW));
            ModConfig.getInstance().speakerRange = 8 + (int)(rProg * 56.0f);
            ModConfig.getInstance().save();
            this.localPlayer.broadcastState();
            return true;
        }
        return false;
    }

    private void renderOnlineTab(GuiGraphicsExtractor ctx, int mouseX, int mouseY, int px, int py, int pw, int ph) {
        boolean isSpotify;
        boolean hasTrack = this.gsmtc.isAvailable() && this.gsmtc.hasTrack();
        boolean playing = this.gsmtc.isPlaying();
        boolean sm = ModConfig.getInstance().speakerMode;
        String sourceApp = hasTrack ? this.gsmtc.getSourceApp().toLowerCase() : "";
        boolean isYandex = hasTrack && (sourceApp.contains("yandex") || sourceApp.contains("яндекс"));
        isSpotify = hasTrack && (sourceApp.contains("spotify") || sourceApp.contains("спотифай"));
        String rawSourceName = isYandex ? "Яндекс.Музыка" : (isSpotify ? "Spotify" : (hasTrack ? "Windows / Web" : DrawUtils.t("Not Connected", "Не подключено")));
        String sourceName = sm ? DrawUtils.t("Speaker • ", "Колонка • ") + rawSourceName : rawSourceName;
        int sourceColor = sm ? -19712 : (isYandex ? -13312 : (isSpotify ? -14829228 : -16739073));

        int cardX = px + 8;
        int cardY = py + 42;
        int cardW = pw - 16;
        int cardH = 74;
        this.drawModernCard(ctx, cardX, cardY, cardW, cardH, this.bgDark(), hasTrack ? sourceColor : this.border());

        int coverSize = 58;
        int coverX = cardX + 8;
        int coverY = cardY + 8;
        if (hasTrack && this.gsmtc.hasCover()) {
            ctx.blit(GsmtcManager.COVER_TEXTURE_ID, coverX, coverX + coverSize, coverY, coverY + coverSize, 0.0f, 1.0f, 0.0f, 1.0f);
        } else {
            ctx.fill(coverX, coverY, coverX + coverSize, coverY + coverSize, this.bgButton());
            ctx.centeredText(this.font, Component.literal("♪"), coverX + coverSize / 2, coverY + coverSize / 2 - 4, this.accent());
        }

        int infoX = cardX + 74;
        int badgeW = this.font.width(sourceName) + 10;
        this.drawModernButton(ctx, mouseX, mouseY, infoX, cardY + 8, badgeW, 14, sourceName, true, sourceColor, sourceColor);

        String statusText = hasTrack ? (playing ? "● " + DrawUtils.t("Playing", "Играет") : "II " + DrawUtils.t("Paused", "Пауза")) : DrawUtils.t("Waiting for music...", "Ожидание трека...");
        int statusColor = hasTrack ? (playing ? 0xFF55FF55 : this.textSecondary()) : this.textSecondary();
        DrawUtils.drawText(ctx, this.font, statusText, infoX + badgeW + 6, cardY + 11, statusColor, false);

        String title = hasTrack ? this.gsmtc.getTitle() : DrawUtils.t("Turn on music in browser or app", "Включите музыку в браузере или приложении");
        if (title.isEmpty()) title = DrawUtils.t("No title", "Без названия");
        if (this.font.width(title) > cardW - 84) {
            while (title.length() > 3 && this.font.width(title + "..") > cardW - 84) {
                title = title.substring(0, title.length() - 1);
            }
            title = title + "..";
        }
        DrawUtils.drawText(ctx, this.font, title, infoX, cardY + 28, this.textPrimary(), true);

        String artist = hasTrack ? this.gsmtc.getArtist() : "";
        String album = hasTrack ? this.gsmtc.getAlbum() : "";
        String sub = artist;
        if (!album.isEmpty() && !album.equalsIgnoreCase(title)) {
            sub = artist.isEmpty() ? album : artist + " — " + album;
        }
        if (!sub.isEmpty()) {
            if (this.font.width(sub) > cardW - 84) {
                while (sub.length() > 3 && this.font.width(sub + "..") > cardW - 84) {
                    sub = sub.substring(0, sub.length() - 1);
                }
                sub = sub + "..";
            }
            DrawUtils.drawText(ctx, this.font, sub, infoX, cardY + 44, this.textSecondary(), false);
        }

        long curSec = (long)this.gsmtc.getPosition();
        long durSec = (long)this.gsmtc.getDuration();
        float prog = durSec > 0L ? (float)Math.max(0.0, Math.min(1.0, (double)curSec / (double)durSec)) : 0.0f;
        if (this.draggingGsmtcSeek && this.draggedGsmtcSeekFraction >= 0.0f) {
            prog = this.draggedGsmtcSeekFraction;
            curSec = (long)((float)durSec * prog);
        }

        int timeY = py + 120;
        DrawUtils.drawText(ctx, this.font, this.formatTime(curSec), px + 12, timeY, this.textSecondary(), false);
        DrawUtils.drawText(ctx, this.font, this.formatTime(durSec), px + pw - 30, timeY, this.textSecondary(), false);

        int sbX = px + 44;
        int sbW = pw - 88;
        int sbY = timeY + 2;
        boolean hoverSeek = this.isInside(mouseX, mouseY, sbX, sbY - 4, sbW, 10);
        this.drawModernProgressBar(ctx, sbX, sbY, sbW, 4, prog, hoverSeek, sourceColor);

        int btnY = py + 140;
        int btnS = 16;
        int btnW_10 = 20;
        int playCx = px + pw / 2;
        int btnRewX = playCx - 14 - btnW_10;
        int btnPrevX = btnRewX - 4 - btnS;
        int dlBtnW = 54;
        int dlBtnH = 18;
        int dlBtnX = btnPrevX - 6 - dlBtnW;
        int dlBtnY = btnY;
        int btnFwdX = playCx + 14;
        int btnNextX = btnFwdX + btnW_10 + 4;
        int volIconX = btnNextX + btnS + 8;
        int volBarX = volIconX + 12;
        int volBarW = 46;

        String dlText = DrawUtils.t("Add", "Добавить");
        if (!this.downloadStatus.isEmpty() && System.currentTimeMillis() - this.downloadStatusTimestamp < 4000L) {
            dlText = this.downloadStatus;
        }
        this.drawModernButton(ctx, mouseX, mouseY, dlBtnX, dlBtnY, dlBtnW, dlBtnH, dlText, false, this.accent(), this.accent());

        this.drawModernButton(ctx, mouseX, mouseY, btnPrevX, btnY, btnS, btnS, "⏮", false, this.accent(), this.textSecondary());
        this.drawModernButton(ctx, mouseX, mouseY, btnRewX, btnY, btnW_10, btnS, "-10", false, this.accent(), this.textSecondary());

        this.drawModernButton(ctx, mouseX, mouseY, playCx - 10, btnY - 2, 20, 20, playing ? "⏸" : "▶", playing, sourceColor, sourceColor);

        this.drawModernButton(ctx, mouseX, mouseY, btnFwdX, btnY, btnW_10, btnS, "+10", false, this.accent(), this.textSecondary());
        this.drawModernButton(ctx, mouseX, mouseY, btnNextX, btnY, btnS, btnS, "⏭", false, this.accent(), this.textSecondary());

        float extVol = this.gsmtc.getVolume();
        boolean hoverVol = this.isInside(mouseX, mouseY, volBarX - 4, btnY - 2, volBarW + 8, 16);
        DrawUtils.drawText(ctx, this.font, extVol <= 0.01f ? "🔇" : "🔊", volIconX, btnY + 4, this.textSecondary(), false);
        this.drawModernProgressBar(ctx, volBarX, btnY + 7, volBarW, 4, extVol, hoverVol, sourceColor);
        DrawUtils.drawText(ctx, this.font, (int)(extVol * 100.0f) + "%", volBarX + volBarW + 4, btnY + 4, this.textSecondary(), false);

        if (sm) {
            int auxY = py + 164;
            int rangeBoxW = 150;
            int rangeBoxX = playCx - rangeBoxW / 2;
            this.drawModernCard(ctx, rangeBoxX, auxY - 2, rangeBoxW, 18, this.bgDark(), this.accent());
            String rangeStr = ModConfig.getInstance().speakerRange + "m";
            DrawUtils.drawText(ctx, this.font, "🔊 " + rangeStr, rangeBoxX + 6, auxY + 3, this.accent(), false);
            int sliderX = rangeBoxX + 54;
            int sliderW = rangeBoxW - 60;
            float rProgress = (float)(ModConfig.getInstance().speakerRange - 8) / 56.0f;
            boolean hoverSlider = this.isInside(mouseX, mouseY, sliderX - 4, auxY - 2, sliderW + 8, 18);
            this.drawModernProgressBar(ctx, sliderX, auxY + 6, sliderW, 4, rProgress, hoverSlider, this.accent());
        }
    }

    private void renderSettingsTab(GuiGraphicsExtractor ctx, int mouseX, int mouseY, int px, int py, int pw, int ph) {
        int sx = px + 8;
        int sy = py + 30;
        int sw = pw - 16;
        int sh = ph - 36;
        this.drawModernCard(ctx, sx, sy, sw, sh, this.bgDark(), this.border());

        int cy = sy + 8 - this.settingsScroll;
        int cardX = sx + 8;
        int cardW = sw - 16;

        ctx.enableScissor(sx + 2, sy + 2, sx + sw - 2, sy + sh - 2);

        DrawUtils.drawText(ctx, this.font, DrawUtils.t("APPEARANCE & INTERFACE", "ВНЕШНИЙ ВИД И ИНТЕРФЕЙС"), sx + 12, cy + 6, this.accent(), true);
        int card1Y = cy + 18;
        int card1H = 80;
        this.drawModernCard(ctx, cardX, card1Y, cardW, card1H, this.bgPanel(), this.border());

        DrawUtils.drawText(ctx, this.font, DrawUtils.t("Theme Mode", "Тема оформления"), cardX + 14, card1Y + 9, this.textPrimary(), false);
        int pillThemeW = 84;
        int pillThemeH = 18;
        int pillThemeX = cardX + cardW - pillThemeW - 10;
        int pillThemeY = card1Y + 4;
        String themeText = darkTheme ? DrawUtils.t("Dark", "Тёмная") : DrawUtils.t("Light", "Светлая");
        this.drawModernButton(ctx, mouseX, mouseY, pillThemeX, pillThemeY, pillThemeW, pillThemeH, themeText, false, this.accent(), this.textPrimary());

        ctx.fill(cardX + 10, card1Y + 26, cardX + cardW - 10, card1Y + 27, this.border());

        DrawUtils.drawText(ctx, this.font, DrawUtils.t("Interface Language", "Язык интерфейса"), cardX + 14, card1Y + 35, this.textPrimary(), false);
        int pillLangW = 84;
        int pillLangH = 18;
        int pillLangX = cardX + cardW - pillLangW - 10;
        int pillLangY = card1Y + 30;
        String langText = ModConfig.getInstance().isEnglish ? "English" : "Русский";
        this.drawModernButton(ctx, mouseX, mouseY, pillLangX, pillLangY, pillLangW, pillLangH, langText, false, this.accent(), this.textPrimary());

        ctx.fill(cardX + 10, card1Y + 52, cardX + cardW - 10, card1Y + 53, this.border());

        DrawUtils.drawText(ctx, this.font, DrawUtils.t("Dynamic Island HUD", "Оверлей Dynamic Island"), cardX + 14, card1Y + 61, this.textPrimary(), false);
        int pillHudW = 84;
        int pillHudH = 18;
        int pillHudX = cardX + cardW - pillHudW - 10;
        int pillHudY = card1Y + 56;
        boolean hudActive = this.gsmtc.isHudEnabled();
        String hudText = hudActive ? DrawUtils.t("ON", "ВКЛ") : DrawUtils.t("OFF", "ВЫКЛ");
        this.drawModernButton(ctx, mouseX, mouseY, pillHudX, pillHudY, pillHudW, pillHudH, hudText, hudActive, 0xFF00AAFF, hudActive ? 0xFF00AAFF : this.textSecondary());

        int sec2Y = cy + 108;
        DrawUtils.drawText(ctx, this.font, DrawUtils.t("AUDIO & 3D SPEAKER", "ЗВУК И 3D КОЛОНКА"), sx + 12, sec2Y + 6, -19712, true);
        int card2Y = sec2Y + 18;
        int card2H = 92;
        this.drawModernCard(ctx, cardX, card2Y, cardW, card2H, this.bgPanel(), this.border());

        int btnW = (cardW - 28) / 2;
        int btnH = 20;
        int btn1X = cardX + 10;
        int btn1Y = card2Y + 6;
        int btn2X = cardX + 18 + btnW;
        int btn2Y = card2Y + 6;

        boolean soundOn = !ModConfig.getInstance().muteGlobal;
        String soundText = soundOn ? DrawUtils.t("Sound: ON", "Звук: ВКЛ") : DrawUtils.t("Sound: OFF", "Звук: ВЫКЛ");
        this.drawModernButton(ctx, mouseX, mouseY, btn1X, btn1Y, btnW, btnH, soundText, soundOn, this.accent(), soundOn ? this.accent() : this.textSecondary());

        boolean spkOn = ModConfig.getInstance().speakerMode;
        String spkText = spkOn ? DrawUtils.t("3D: ON", "3D: ВКЛ") : DrawUtils.t("3D: OFF", "3D: ВЫКЛ");
        this.drawModernButton(ctx, mouseX, mouseY, btn2X, btn2Y, btnW, btnH, spkText, spkOn, -19712, spkOn ? -19712 : this.textSecondary());

        ctx.fill(cardX + 10, card2Y + 31, cardX + cardW - 10, card2Y + 32, this.border());

        DrawUtils.drawText(ctx, this.font, DrawUtils.t("3D Sound Radius", "Радиус 3D звука"), cardX + 14, card2Y + 41, -19712, false);
        int slRangeW = 108;
        int badgeW = 34;
        int badgeH = 16;
        int badgeX = cardX + cardW - badgeW - 10;
        int badgeY = card2Y + 37;
        int slRangeX = badgeX - slRangeW - 8;
        int slRangeY = card2Y + 42;
        this.drawModernButton(ctx, mouseX, mouseY, badgeX, badgeY, badgeW, badgeH, ModConfig.getInstance().speakerRange + "m", true, -19712, -19712);
        float rProg = (float)(ModConfig.getInstance().speakerRange - 8) / 56.0f;
        boolean hoverRange = this.isInside(mouseX, mouseY, slRangeX - 4, slRangeY - 5, slRangeW + 8, 16);
        this.drawModernProgressBar(ctx, slRangeX, slRangeY, slRangeW, 6, rProg, hoverRange, -19712);

        ctx.fill(cardX + 10, card2Y + 61, cardX + cardW - 10, card2Y + 62, this.border());

        float curVol = this.localPlayer.getVolume();
        DrawUtils.drawText(ctx, this.font, DrawUtils.t("MP3 Music Volume", "Громкость MP3"), cardX + 14, card2Y + 71, this.accent(), false);
        int slVolW = 108;
        int badgeVolX = cardX + cardW - badgeW - 10;
        int badgeVolY = card2Y + 67;
        int slVolX = badgeVolX - slVolW - 8;
        int slVolY = card2Y + 72;
        this.drawModernButton(ctx, mouseX, mouseY, badgeVolX, badgeVolY, badgeW, badgeH, (int)(curVol * 100.0f) + "%", true, this.accent(), this.accent());
        boolean hoverVol = this.isInside(mouseX, mouseY, slVolX - 4, slVolY - 5, slVolW + 8, 16);
        this.drawModernProgressBar(ctx, slVolX, slVolY, slVolW, 6, curVol, hoverVol, this.accent());

        ctx.disableScissor();

        int totalContentH = card2Y + card2H + 16 - cy;
        this.maxSettingsScroll = Math.max(0, totalContentH - sh);
        this.settingsScroll = Math.max(0, Math.min(this.maxSettingsScroll, this.settingsScroll));
        if (this.maxSettingsScroll > 0) {
            float scrollFrac = (float)this.settingsScroll / (float)this.maxSettingsScroll;
            float trackH = sh - 10;
            float scrollbarH = Math.max(24.0f, trackH * ((float)sh / (float)(sh + this.maxSettingsScroll)));
            float scrollbarY = (float)(sy + 5) + (trackH - scrollbarH) * scrollFrac;
            float scrollbarX = sx + sw - 6;
            ctx.fill((int)scrollbarX, (int)scrollbarY, (int)scrollbarX + 3, (int)(scrollbarY + scrollbarH), 0x66FFFFFF);
        }
    }

    private void renderLocalTab(GuiGraphicsExtractor ctx, int mouseX, int mouseY, int px, int py, int pw, int ph, boolean showFavorites) {
        List<File> playlist = showFavorites ? this.localPlayer.getFavorites() : this.localPlayer.getPlaylist();
        this.drawModernButton(ctx, mouseX, mouseY, px + 8, py + 28, 22, 16, "⟳", this.pressedButtons.contains(110), this.accent(), this.textSecondary());
        this.drawModernButton(ctx, mouseX, mouseY, px + 34, py + 28, 22, 16, "📁", this.pressedButtons.contains(111), this.accent(), this.textSecondary());
        DrawUtils.drawText(ctx, this.font, DrawUtils.t("Tracks: ", "Треков: ") + playlist.size(), px + 62, py + 32, this.textSecondary(), false);
        if (this.localPlayer.isConverting()) {
            DrawUtils.drawText(ctx, this.font, DrawUtils.t("Converting MP3...", "Конвертация MP3..."), px + 130, py + 32, this.accent(), true);
        }

        int listX = px + 8;
        int listY = py + 48;
        int listW = pw - 16;
        int listH = 74;
        this.drawModernCard(ctx, listX, listY, listW, listH, this.bgDark(), this.border());

        if (playlist.isEmpty()) {
            ctx.centeredText(this.font, Component.literal(DrawUtils.t("Add .mp3 or .wav to mp3player folder", "Закиньте .mp3 или .wav в папку mp3player")), listX + listW / 2, listY + listH / 2 - 8, this.textSecondary());
            ctx.centeredText(this.font, Component.literal(DrawUtils.t("MP3s are converted automatically", "MP3 конвертируются автоматически")), listX + listW / 2, listY + listH / 2 + 6, this.textSecondary());
        } else {
            int scroll = showFavorites ? this.favTrackScroll : this.localTrackScroll;
            for (int i = scroll; i < Math.min(playlist.size(), scroll + 4); ++i) {
                int itemY = listY + 3 + (i - scroll) * 17;
                int itemX = listX + 2;
                int itemW = listW - 4;
                int itemH = 15;
                File f = playlist.get(i);
                boolean hover = this.isInside(mouseX, mouseY, itemX, itemY, itemW, itemH);
                boolean current = f.equals(this.localPlayer.getPlaylist().isEmpty() ? null : this.localPlayer.getPlaylist().get(this.localPlayer.getCurrentIndex())) && this.localPlayer.isPlaying();
                boolean isFav = this.localPlayer.getFavorites().contains(f);

                if (hover) {
                    ctx.fill(itemX, itemY, itemX + itemW, itemY + itemH, this.bgHover());
                }
                if (current) {
                    ctx.fill(itemX, itemY, itemX + 3, itemY + itemH, this.accent());
                }

                String playIcon = current ? "▶" : (isFav ? "★" : "♪");
                int iconColor = current ? this.accent() : (isFav ? 0xFFFFAA00 : this.textSecondary());
                DrawUtils.drawText(ctx, this.font, playIcon, itemX + 6, itemY + 4, iconColor, false);

                String name = f.getName();
                int dot = name.lastIndexOf('.');
                if (dot > 0) name = name.substring(0, dot);
                if (this.font.width(name) > itemW - 68) {
                    while (name.length() > 3 && this.font.width(name + "..") > itemW - 68) {
                        name = name.substring(0, name.length() - 1);
                    }
                    name = name + "..";
                }
                DrawUtils.drawText(ctx, this.font, name, itemX + 18, itemY + 4, current ? this.accent() : this.textPrimary(), false);

                boolean hoverDel = this.isInside(mouseX, mouseY, itemX + itemW - 40, itemY + 1, 16, 13);
                ctx.fill(itemX + itemW - 40, itemY + 1, itemX + itemW - 24, itemY + 14, hoverDel ? this.bgButton() : 0);
                DrawUtils.drawText(ctx, this.font, "✖", itemX + itemW - 36, itemY + 3, hoverDel ? 0xFFFF5555 : this.textSecondary(), false);

                boolean hoverDots = this.isInside(mouseX, mouseY, itemX + itemW - 20, itemY + 1, 16, 13);
                ctx.fill(itemX + itemW - 20, itemY + 1, itemX + itemW - 4, itemY + 14, hoverDots ? this.bgButton() : 0);
                DrawUtils.drawText(ctx, this.font, "⋮", itemX + itemW - 14, itemY + 3, hoverDots ? this.accent() : this.textSecondary(), false);
            }
        }
    }

    private void renderPlayerBar(GuiGraphicsExtractor ctx, int mouseX, int mouseY, int barX, int barY, int barW) {
        boolean sm = ModConfig.getInstance().speakerMode;
        int outlineColor = sm ? -19712 : this.accent();
        String badge = sm ? DrawUtils.t("Speaker", "Колонка") : DrawUtils.t("Local", "MP3");
        int badgeColor = outlineColor;
        int barH = 68;

        this.drawModernCard(ctx, barX, barY, barW, barH, this.bgDark(), outlineColor);

        String trackInfo = this.localPlayer.getCurrentTrackName();
        long durMicros = this.localPlayer.getDuration();
        long posMicros = this.localPlayer.getCurrentPosition();
        long posSec = posMicros / 1000000L;
        long durSec = durMicros / 1000000L;
        float prog = durMicros > 0L ? (float)Math.max(0.0, Math.min(1.0, (double)posMicros / (double)durMicros)) : 0.0f;
        boolean playing = this.localPlayer.isPlaying();
        float vol = this.localPlayer.getVolume();

        int coverX = barX + 6;
        int coverY = barY + 6;
        int coverSize = 34;
        ctx.fill(coverX, coverY, coverX + coverSize, coverY + coverSize, this.bgButton());
        ctx.centeredText(this.font, Component.literal("♪"), coverX + coverSize / 2, coverY + coverSize / 2 - 4, this.accent());

        int badgeW = this.font.width(badge) + 8;
        this.drawModernButton(ctx, mouseX, mouseY, barX + 46, barY + 6, badgeW, 12, badge, true, badgeColor, badgeColor);

        int maxTitleW = barW - 46 - badgeW - 16;
        String displayTitle = trackInfo;
        if (this.font.width(displayTitle) > maxTitleW) {
            while (displayTitle.length() > 3 && this.font.width(displayTitle + "..") > maxTitleW) {
                displayTitle = displayTitle.substring(0, displayTitle.length() - 1);
            }
            displayTitle = displayTitle + "..";
        }
        DrawUtils.drawText(ctx, this.font, displayTitle, barX + 46 + badgeW + 6, barY + 8, this.textPrimary(), true);

        int sbX = barX + 46;
        int sbW = barW - 126;
        int sbY = barY + 28;
        boolean hoverSeek = this.isInside(mouseX, mouseY, sbX, sbY - 4, sbW, 10);
        this.drawModernProgressBar(ctx, sbX, sbY, sbW, 3, prog, hoverSeek, outlineColor);

        String timerText = this.formatTime(posSec) + " / " + this.formatTime(durSec);
        DrawUtils.drawText(ctx, this.font, timerText, sbX + sbW + 6, sbY - 2, this.textSecondary(), false);

        int ctrlCenter = barX + barW / 2;
        int btnY = barY + 46;
        int btnS = 16;
        int btnW_10 = 22;
        int btnPrevX = ctrlCenter - 61;
        int btnRewX = ctrlCenter - 39;
        int playCx = ctrlCenter;
        int btnFwdX = ctrlCenter + 17;
        int btnNextX = ctrlCenter + 45;

        this.drawModernButton(ctx, mouseX, mouseY, btnPrevX, btnY, btnS, btnS, "⏮", false, this.accent(), this.textSecondary());
        this.drawModernButton(ctx, mouseX, mouseY, btnRewX, btnY, btnW_10, btnS, "-10", false, this.accent(), this.textSecondary());

        this.drawModernButton(ctx, mouseX, mouseY, playCx - 10, btnY - 2, 20, 20, playing ? "⏸" : "▶", playing, outlineColor, outlineColor);

        this.drawModernButton(ctx, mouseX, mouseY, btnFwdX, btnY, btnW_10, btnS, "+10", false, this.accent(), this.textSecondary());
        this.drawModernButton(ctx, mouseX, mouseY, btnNextX, btnY, btnS, btnS, "⏭", false, this.accent(), this.textSecondary());

        if (sm) {
            int rangeX = barX + 6;
            DrawUtils.drawText(ctx, this.font, DrawUtils.t("Range", "Радиус"), rangeX, btnY - 1, this.textSecondary(), false);
            int maxRange = 64;
            int minRange = 8;
            float rProgress = (float)(ModConfig.getInstance().speakerRange - minRange) / (float)(maxRange - minRange);
            this.drawModernProgressBar(ctx, rangeX, btnY + 8, 34, 3, rProgress, false, this.accent());
        }

        int volIconX = barX + barW - 54;
        int volBarX = barX + barW - 44;
        boolean hoverVol = this.isInside(mouseX, mouseY, volBarX - 2, btnY + 5, 44, 8);
        DrawUtils.drawText(ctx, this.font, vol <= 0.001f ? "🔇" : "🔊", volIconX, btnY + 4, this.textSecondary(), false);
        this.drawModernProgressBar(ctx, volBarX, btnY + 7, 40, 3, vol, hoverVol, this.accent());
    }

    private void drawTab(GuiGraphicsExtractor ctx, int mouseX, int mouseY, int x, int y, String label, int idx, String tooltip) {
        boolean sel;
        int w = this.font.width(label) + 20;
        boolean hover = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + 20;
        boolean bl = sel = this.currentTab == idx;
        ctx.fill(x, y, x + w, y + 20, sel ? this.bgPanel() : (hover ? this.bgHover() : this.bgButton()));
        if (sel) {
            ctx.fill(x, y, x + w, y + 2, this.accent());
        }
        ctx.centeredText(this.font, (Component)Component.literal((String)label), x + w / 2, y + 6, sel ? this.accent() : this.textSecondary());
    }

    private void drawButton(GuiGraphicsExtractor ctx, int mouseX, int mouseY, int x, int y, int w, int h, String icon, int id) {
        this.drawButton(ctx, mouseX, mouseY, x, y, w, h, icon, id, this.textSecondary());
    }

    private void drawButton(GuiGraphicsExtractor ctx, int mouseX, int mouseY, int x, int y, int w, int h, String icon, int id, int color) {
        boolean hover = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
        boolean pressed = this.pressedButtons.contains(id);
        ctx.fill(x, y, x + w, y + h, pressed ? -11184811 : (hover ? this.bgHover() : this.bgButton()));
        if (hover && !pressed) {
            ctx.fill(x, y + h - 2, x + w, y + h, this.accent());
        }
        DrawUtils.drawText(ctx, this.font, icon, x + w / 2 - this.font.width(icon) / 2, y + (h - 8) / 2, hover ? this.accent() : color, false);
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent click, boolean bl) {
        this.forceSkijaRedraw = true;
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();
        if (GsmtcHudOverlay.handleClick(mouseX, mouseY)) {
            return true;
        }
        if (this.trackMenuIndex != -1) {
            if (!this.isInside(mouseX, mouseY, this.trackMenuX, this.trackMenuY, 135, 84)) {
                this.trackMenuIndex = -1;
            } else {
                this.handleMenuClick(mouseX, mouseY);
                return true;
            }
        }
        float eased = this.easeOutCubic(this.animationProgress);
        int pw = 340;
        int ph = 196;
        int px = this.width / 2 - pw / 2;
        int py = (int)((float)(this.height + 20) + (float)(this.height / 2 - ph / 2 - this.height - 20) * eased);
        int tab0W = 96;
        int tab1W = 86;
        int tab2W = 80;
        int tab3W = 30;
        int tab0X = px + 10;
        int tab1X = tab0X + tab0W + 4;
        int tab2X = tab1X + tab1W + 4;
        int tab3X = tab2X + tab2W + 4;
        if (this.isInside(mouseX, mouseY, tab0X, py - 22, tab0W, 22)) {
            this.playClickSound();
            this.currentTab = 0;
            ModConfig.getInstance().currentTab = 0;
            ModConfig.getInstance().save();
            return true;
        }
        if (this.isInside(mouseX, mouseY, tab1X, py - 22, tab1W, 22)) {
            this.playClickSound();
            this.currentTab = 1;
            ModConfig.getInstance().currentTab = 1;
            ModConfig.getInstance().save();
            return true;
        }
        if (this.isInside(mouseX, mouseY, tab2X, py - 22, tab2W, 22)) {
            this.playClickSound();
            this.currentTab = 2;
            ModConfig.getInstance().currentTab = 2;
            ModConfig.getInstance().save();
            return true;
        }
        if (this.isInside(mouseX, mouseY, tab3X, py - 22, tab3W, 22)) {
            this.playClickSound();
            this.currentTab = 3;
            ModConfig.getInstance().currentTab = 3;
            ModConfig.getInstance().save();
            return true;
        }
        if (this.isInside(mouseX, mouseY, px + 8, py + 6, 22, 18)) {
            this.playClickSound();
            darkTheme = !darkTheme;
            SkijaEngine.getInstance().setDarkTheme(darkTheme);
            ModConfig.getInstance().darkTheme = darkTheme;
            ModConfig.getInstance().save();
            return true;
        }
        if (this.isInside(mouseX, mouseY, px + 34, py + 6, 26, 18)) {
            this.playClickSound();
            ModConfig.getInstance().isEnglish = !ModConfig.getInstance().isEnglish;
            ModConfig.getInstance().save();
            return true;
        }
        if (this.isInside(mouseX, mouseY, px + pw - 90, py + 6, 22, 18)) {
            this.playClickSound();
            ModConfig.getInstance().muteGlobal = !ModConfig.getInstance().muteGlobal;
            ModConfig.getInstance().save();
            return true;
        }
        if (this.isInside(mouseX, mouseY, px + pw - 64, py + 6, 56, 18)) {
            this.playClickSound();
            ModConfig.getInstance().speakerMode = !ModConfig.getInstance().speakerMode;
            ModConfig.getInstance().save();
            if (ModConfig.getInstance().speakerMode) {
                this.localPlayer.broadcastState();
            } else {
                this.localPlayer.broadcastStop();
            }
            return true;
        }
        if (this.currentTab == 0) {
            return this.handleOnlineTabClick(mouseX, mouseY, px, py, pw, ph);
        }
        if (this.currentTab == 1) {
            return this.handleLocalClick(mouseX, mouseY, px, py, pw, ph, false);
        }
        if (this.currentTab == 2) {
            return this.handleLocalClick(mouseX, mouseY, px, py, pw, ph, true);
        }
        if (this.currentTab == 3) {
            return this.handleSettingsTabClick(mouseX, mouseY, px, py, pw, ph);
        }
        return super.mouseClicked(click, bl);
    }

    private void handleMenuClick(double mx, double my) {
        int option = (int)((my - (double)this.trackMenuY) / 14.0);
        List<File> playlist = this.currentTab == 2 ? this.localPlayer.getFavorites() : this.localPlayer.getPlaylist();
        File trackFile = null;
        if (this.trackMenuIndex >= 0 && this.trackMenuIndex < playlist.size()) {
            trackFile = playlist.get(this.trackMenuIndex);
        }
        if (option == 0) {
            if (trackFile != null) {
                if (this.localPlayer.getFavorites().contains(trackFile)) {
                    this.localPlayer.getFavorites().remove(trackFile);
                    ModConfig.getInstance().favorites.remove(trackFile.getName());
                } else {
                    this.localPlayer.getFavorites().add(trackFile);
                    if (!ModConfig.getInstance().favorites.contains(trackFile.getName())) {
                        ModConfig.getInstance().favorites.add(trackFile.getName());
                    }
                }
                ModConfig.getInstance().save();
            }
        } else if (option == 1) {
            this.localPlayer.toggleRepeat();
        } else if (option == 2) {
            if (this.minecraft != null && trackFile != null) {
                this.minecraft.gui.setScreen((Screen)new MusicDiscScreen(this, trackFile));
            }
        } else if (option == 3) {
            if (trackFile != null) {
                try {
                    String q = URLEncoder.encode(trackFile.getName().replace(".mp3", "").replace(".wav", ""), StandardCharsets.UTF_8);
                    try {
                        if (java.awt.Desktop.isDesktopSupported() && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.BROWSE)) {
                            java.awt.Desktop.getDesktop().browse(new java.net.URI("https://www.google.com/search?q=" + q));
                        } else {
                            new ProcessBuilder("cmd", "/c", "start", "https://www.google.com/search?q=" + q).start();
                        }
                    } catch (Throwable t2) {
                        new ProcessBuilder("cmd", "/c", "start", "https://www.google.com/search?q=" + q).start();
                    }
                }
                catch (Exception exception) {}
            }
        } else if (option == 4) {
            if (trackFile != null && this.minecraft != null) {
                this.minecraft.gui.setScreen((Screen)new ShareScreen(this, trackFile));
            }
        } else if (option == 5) {
            this.deleteTrackFile(trackFile);
        }
        this.trackMenuIndex = -1;
    }

    private void deleteTrackFile(File trackFile) {
        if (trackFile == null) {
            return;
        }
        try {
            if (this.localPlayer.isPlaying()) {
                File currentPlaying;
                File file = currentPlaying = this.localPlayer.getPlaylist().isEmpty() ? null : this.localPlayer.getPlaylist().get(this.localPlayer.getCurrentIndex());
                if (trackFile.equals(currentPlaying)) {
                    this.localPlayer.stop();
                }
            }
            if (trackFile.exists()) {
                trackFile.delete();
                this.localPlayer.refreshPlaylist();
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private boolean handleLocalClick(double mx, double my, int px, int py, int pw, int ph, boolean showFavorites) {
        int scroll;
        if (this.isInside(mx, my, px + 8, py + 28, 22, 16)) {
            this.playClickSound();
            this.localPlayer.refreshPlaylist();
            return true;
        }
        if (this.isInside(mx, my, px + 34, py + 28, 22, 16)) {
            this.playClickSound();
            this.openFolder();
            return true;
        }
        int listX = px + 8;
        int listY = py + 48;
        int listW = pw - 16;
        List<File> playlist = showFavorites ? this.localPlayer.getFavorites() : this.localPlayer.getPlaylist();
        for (int i = scroll = showFavorites ? this.favTrackScroll : this.localTrackScroll; i < Math.min(playlist.size(), scroll + 7); ++i) {
            int itemX = listX + 2;
            int itemY = listY + 3 + (i - scroll) * 16;
            int itemW = listW - 4;
            int itemH = 15;
            if (!this.isInside(mx, my, itemX, itemY, itemW, itemH)) continue;
            if (this.isInside(mx, my, itemX + itemW - 20, itemY + 1, 16, 13)) {
                this.playClickSound();
                this.trackMenuIndex = i;
                int menuW = 135;
                this.trackMenuX = mx + (double)menuW > (double)this.width ? (int)mx - menuW : (int)mx;
                this.trackMenuY = (int)my;
                if (this.trackMenuX < 0) {
                    this.trackMenuX = 0;
                }
                if (this.trackMenuY + 90 > this.height) {
                    this.trackMenuY = this.height - 90;
                }
                return true;
            }
            if (this.isInside(mx, my, itemX + itemW - 40, itemY + 1, 16, 13)) {
                this.playClickSound();
                File f = playlist.get(i);
                this.deleteTrackFile(f);
                return true;
            }
            this.playClickSound();
            File f = playlist.get(i);
            int realIndex = this.localPlayer.getPlaylist().indexOf(f);
            if (realIndex >= 0) {
                if (this.localPlayer.getCurrentIndex() == realIndex && this.localPlayer.isPlaying()) {
                    this.localPlayer.pause();
                } else if (this.localPlayer.getCurrentIndex() == realIndex && !this.localPlayer.isPlaying()) {
                    this.localPlayer.play();
                } else {
                    this.localPlayer.playTrack(realIndex);
                }
            }
            return true;
        }
        return this.handlePlayerClick(mx, my, px + 8, py + ph - 72, pw - 16);
    }

    private boolean handlePlayerClick(double mx, double my, int barX, int barY, int barW) {
        int rangeX;
        boolean sm = ModConfig.getInstance().speakerMode;
        int sbX = barX + 46;
        int sbY = barY + 28;
        int sbW = barW - 126;
        if (this.isInside(mx, my, sbX, sbY - 5, sbW, 10)) {
            this.draggingProgress = true;
            this.draggedLocalSeekFraction = (float)Math.max(0.0, Math.min(1.0, (mx - (double)sbX) / (double)sbW));
            long dur = this.localPlayer.getDuration();
            if (dur > 0L) {
                this.localPlayer.seekTo((long)((float)dur * this.draggedLocalSeekFraction));
                if (sm) {
                    this.localPlayer.broadcastState();
                }
            }
            return true;
        }
        int ctrlCenter = barX + barW / 2;
        int btnY = barY + 46;
        int btnS = 16;
        int btnW_10 = 22;
        int btnPrevX = ctrlCenter - 61;
        int btnRewX = ctrlCenter - 39;
        int playCx = ctrlCenter;
        int playCy = btnY + 8;
        int btnFwdX = ctrlCenter + 17;
        int btnNextX = ctrlCenter + 45;
        if (this.isInside(mx, my, btnPrevX, btnY, btnS, btnS)) {
            this.playClickSound();
            this.localPlayer.previous();
            return true;
        }
        if (this.isInside(mx, my, btnRewX, btnY, btnW_10, btnS)) {
            this.playClickSound();
            this.localPlayer.seekRelative(-10000000L);
            return true;
        }
        if (this.isInsideCircle(mx, my, playCx, playCy, 12)) {
            this.playClickSound();
            if (this.localPlayer.isPlaying()) {
                this.localPlayer.pause();
            } else {
                this.localPlayer.play();
            }
            return true;
        }
        if (this.isInside(mx, my, btnFwdX, btnY, btnW_10, btnS)) {
            this.playClickSound();
            this.localPlayer.seekRelative(10000000L);
            return true;
        }
        if (this.isInside(mx, my, btnNextX, btnY, btnS, btnS)) {
            this.playClickSound();
            this.localPlayer.next();
            return true;
        }
        if (sm && this.isInside(mx, my, rangeX = barX + 6, btnY, 34, 16)) {
            this.draggingRange = true;
            float rProg = (float)Math.max(0.0, Math.min(1.0, (mx - (double)rangeX) / 34.0));
            ModConfig.getInstance().speakerRange = 8 + (int)(rProg * 56.0f);
            ModConfig.getInstance().save();
            this.localPlayer.broadcastState();
            return true;
        }
        int volBarX = barX + barW - 44;
        if (this.isInside(mx, my, volBarX - 10, btnY, 52, 16)) {
            this.draggingVolume = true;
            float vol = (float)Math.max(0.0, Math.min(1.0, (mx - (double)volBarX) / 40.0));
            this.localPlayer.setVolume(vol);
            ModConfig.getInstance().musicVolume = vol;
            ModConfig.getInstance().save();
            return true;
        }
        return false;
    }

    private void openFolder() {
        new Thread(() -> {
            try {
                File dir = AudioConverter.getConvertedFolder();
                if (!dir.exists()) {
                    dir = this.musicFolder;
                }
                new ProcessBuilder("cmd", "/c", "start", "", dir.getAbsolutePath()).start();
            }
            catch (Exception exception) {
                // empty catch block
            }
        }).start();
    }

    @Override
    public boolean mouseReleased(net.minecraft.client.input.MouseButtonEvent click) {
        this.forceSkijaRedraw = true;
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();
        this.pressedButtons.clear();
        if (this.draggingRange || this.draggingVolume || this.draggingGsmtcVolume) {
            ModConfig.getInstance().save();
        }
        if (this.draggingGsmtcSeek) {
            if (this.draggedGsmtcSeekFraction >= 0.0f && this.gsmtc.getDuration() > 0.0) {
                this.gsmtc.seek(this.gsmtc.getDuration() * (double)this.draggedGsmtcSeekFraction);
            }
            this.draggingGsmtcSeek = false;
            this.draggedGsmtcSeekFraction = -1.0f;
        }
        if (this.draggingProgress) {
            if (this.draggedLocalSeekFraction >= 0.0f && this.localPlayer.getDuration() > 0L) {
                this.localPlayer.seekTo((long)((float)this.localPlayer.getDuration() * this.draggedLocalSeekFraction));
                if (ModConfig.getInstance().speakerMode) {
                    this.localPlayer.broadcastState();
                }
            }
            this.draggingProgress = false;
            this.draggedLocalSeekFraction = -1.0f;
        }
        this.draggingVolume = false;
        this.draggingRange = false;
        this.draggingGsmtcVolume = false;
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseDragged(net.minecraft.client.input.MouseButtonEvent click, double deltaX, double deltaY) {
        this.forceSkijaRedraw = true;
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();
        float eased = this.easeOutCubic(this.animationProgress);
        int pw = 340;
        int ph = 196;
        int px = this.width / 2 - pw / 2;
        int py = (int)((float)(this.height + 20) + (float)(this.height / 2 - ph / 2 - this.height - 20) * eased);
        int barX = px + 8;
        int barY = py + ph - 70;
        int barW = pw - 16;
        if (this.draggingGsmtcVolume) {
            int cx = px + pw / 2;
            int volBarX = cx + 74;
            int volBarW = 46;
            float vol = (float)Math.max(0.0, Math.min(1.0, (mouseX - (double)volBarX) / (double)volBarW));
            this.gsmtc.setVolume(vol);
            if (ModConfig.getInstance().speakerMode) {
                this.localPlayer.broadcastState();
            }
            return true;
        }
        if (this.draggingGsmtcSeek) {
            int sbX = px + 44;
            int sbW = pw - 88;
            this.draggedGsmtcSeekFraction = (float)Math.max(0.0, Math.min(1.0, (mouseX - (double)sbX) / (double)sbW));
            return true;
        }
        if (this.draggingVolume) {
            if (this.currentTab == 3) {
                int sx = px + 8;
                int sw = pw - 16;
                int cardX = sx + 8;
                int cardW = sw - 16;
                int slVolW = 108;
                int badgeW = 34;
                int badgeVolX = cardX + cardW - badgeW - 10;
                int slVolX = badgeVolX - slVolW - 8;
                float vol = (float)Math.max(0.0, Math.min(1.0, (mouseX - (double)slVolX) / (double)slVolW));
                this.localPlayer.setVolume(vol);
                ModConfig.getInstance().musicVolume = vol;
                return true;
            }
            int volBarX = barX + barW - 44;
            float vol = (float)Math.max(0.0, Math.min(1.0, (mouseX - (double)volBarX) / 40.0));
            this.localPlayer.setVolume(vol);
            ModConfig.getInstance().musicVolume = vol;
            return true;
        }
        if (this.draggingRange) {
            float rProgress;
            if (this.currentTab == 0) {
                int cx = px + pw / 2;
                int rangeBoxW = 150;
                int rangeBoxX = cx - rangeBoxW / 2;
                int sliderX = rangeBoxX + 54;
                int sliderW = rangeBoxW - 64;
                rProgress = (float)Math.max(0.0, Math.min(1.0, (mouseX - (double)sliderX) / (double)sliderW));
            } else if (this.currentTab == 3) {
                int sx = px + 8;
                int sw = pw - 16;
                int cardX = sx + 8;
                int cardW = sw - 16;
                int slRangeW = 108;
                int badgeW = 34;
                int badgeX = cardX + cardW - badgeW - 10;
                int slRangeX = badgeX - slRangeW - 8;
                rProgress = (float)Math.max(0.0, Math.min(1.0, (mouseX - (double)slRangeX) / (double)slRangeW));
            } else {
                int rangeX = barX + 6;
                rProgress = (float)Math.max(0.0, Math.min(1.0, (mouseX - (double)rangeX) / 34.0));
            }
            ModConfig.getInstance().speakerRange = 8 + (int)(rProgress * 56.0f);
            this.localPlayer.broadcastState();
            return true;
        }
        if (this.draggingProgress) {
            int sbX = barX + 44;
            int sbW = barW - 124;
            this.draggedLocalSeekFraction = (float)Math.max(0.0, Math.min(1.0, (mouseX - (double)sbX) / (double)sbW));
            return true;
        }
        return super.mouseDragged(click, deltaX, deltaY);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        this.forceSkijaRedraw = true;
        if (this.currentTab == 1) {
            int max = Math.max(0, this.localPlayer.getPlaylist().size() - 4);
            this.localTrackScroll = Math.max(0, Math.min(max, this.localTrackScroll - (int)verticalAmount));
        } else if (this.currentTab == 2) {
            int max = Math.max(0, this.localPlayer.getFavorites().size() - 4);
            this.favTrackScroll = Math.max(0, Math.min(max, this.favTrackScroll - (int)verticalAmount));
        } else if (this.currentTab == 3) {
            this.settingsScroll = Math.max(0, Math.min(this.maxSettingsScroll, this.settingsScroll - (int)(verticalAmount * 24.0)));
        }
        return true;
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyEvent input) {
        this.forceSkijaRedraw = true;
        int keyCode = input.key();
        if (keyCode == 256) {
            this.startClosing();
            return true;
        }
        if ((keyCode == 344 || (com.mp3player.client.KeyInputHandler.openMenuKeyBinding != null && com.mp3player.client.KeyInputHandler.openMenuKeyBinding.matches(input))) 
                && System.currentTimeMillis() - this.openedTimeMs > 350L) {
            this.startClosing();
            return true;
        }
        return super.keyPressed(input);
    }

    private boolean isInside(double mx, double my, int x, int y, int w, int h) {
        return mx >= (double)x && mx < (double)(x + w) && my >= (double)y && my < (double)(y + h);
    }

    private boolean isInsideCircle(double mx, double my, int cx, int cy, int r) {
        double dx = mx - (double)cx;
        double dy = my - (double)cy;
        return dx * dx + dy * dy <= (double)(r * r);
    }

    private float easeOutCubic(float x) {
        return 1.0f - (float)Math.pow(1.0f - x, 3.0);
    }

    private String formatTime(long s) {
        if (s < 0L) {
            s = 0L;
        }
        return String.format("%d:%02d", s / 60L, s % 60L);
    }

    public void startClosing() {
        com.mp3player.MP3PlayerClient.LOGGER.info("[MainScreen] startClosing() called, closing screen!");
        this.onClose();
    }

    @Override
    public void onClose() {
        com.mp3player.MP3PlayerClient.LOGGER.info("[MainScreen] close() called");
        super.onClose();
    }

    @Override
    public void removed() {
        com.mp3player.MP3PlayerClient.LOGGER.info("[MainScreen] removed() called");
        super.removed();
    }

    public boolean isPauseScreen() {
        return false;
    }
}

