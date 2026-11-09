/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.gui.screen.Screen
 *  net.minecraft.client.render.RenderTickCounter
 *  net.minecraft.client.texture.AbstractTexture
 *  net.minecraft.text.MutableText
 *  net.minecraft.text.Text
 *  net.minecraft.util.Identifier
 */
package com.mp3player.client.gsmtc;

import com.mp3player.client.audio.AlbumArtExtractor;
import com.mp3player.client.audio.MusicPlayer;
import com.mp3player.client.config.ModConfig;
import com.mp3player.client.gsmtc.GsmtcManager;
import com.mp3player.client.gui.MainScreen;
import com.mp3player.client.gui.skija.SkijaEngine;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import com.mojang.renderpearl.backend.opengl.GlTexture;

public class GsmtcHudOverlay {
    private static final Identifier UNIFORM_FONT = Identifier.fromNamespaceAndPath((String)"minecraft", (String)"uniform");
    private static int lastPillX = 0;
    private static int lastPillY = 0;
    private static int lastPillW = 0;
    private static int lastPillH = 0;
    private static int lastBtnX = 0;
    private static int lastBtnY = 0;
    private static int lastBtnRadius = 0;
    private static int lastBarX = 0;
    private static int lastBarY = 0;
    private static int lastBarW = 0;

    private static long lastHudRenderTime = 0L;
    private static boolean hudTextureReady = false;
    private static String lastHudTitle = "";
    private static String lastHudArtist = "";
    private static boolean lastHudPlaying = false;
    private static boolean lastHudHover = false;
    private static boolean lastHudBtnHover = false;
    private static long lastHudPosSec = -1L;
    private static int lastHudBorderColor = 0;
    private static byte[] lastHudCover = null;
    private static float lastHudScale = 1.0f;

    public static void register() {
        HudElementRegistry.addLast(
            Identifier.fromNamespaceAndPath("mp3player", "hud_overlay"),
            GsmtcHudOverlay::render
        );
    }

    private static void render(GuiGraphicsExtractor ctx, DeltaTracker tickCounter) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null || (client.gui.hud != null && client.gui.hud.isHidden())) {
            lastPillW = 0;
            lastPillH = 0;
            lastBtnRadius = 0;
            return;
        }
        GsmtcManager manager = GsmtcManager.getInstance();
        MusicPlayer local = MusicPlayer.getInstance();
        int screenW = client.getWindow().getGuiScaledWidth();
        int screenH = client.getWindow().getGuiScaledHeight();
        if (screenW <= 0 || screenH <= 0 || client.getWindow().getWidth() <= 0 || client.getWindow().getHeight() <= 0) {
            lastPillW = 0;
            lastPillH = 0;
            lastBtnRadius = 0;
            return;
        }
        if (!manager.isHudEnabled()) {
            return;
        }
        double mouseX = -1.0;
        double mouseY = -1.0;
        if (client.gui.screen() != null) {
            double scale = client.getWindow().getGuiScale();
            mouseX = client.mouseHandler.xpos() / scale;
            mouseY = client.mouseHandler.ypos() / scale;
        }
        boolean isGsmtcPlaying = manager.isAvailable() && manager.hasTrack() && manager.isPlaying();
        boolean isLocalPlaying = local.isPlaying();
        if (!isGsmtcPlaying && !isLocalPlaying) {
            lastPillW = 0;
            lastPillH = 0;
            lastBtnRadius = 0;
            lastBarW = 0;
            hudTextureReady = false;
            return;
        }
        boolean hasGsmtc = isGsmtcPlaying || manager.isAvailable() && manager.hasTrack() && !isLocalPlaying;
        SkijaEngine skija = SkijaEngine.getInstance();
        if (skija.isAvailable()) {
            GsmtcHudOverlay.renderActiveCapsuleSkija(skija, ctx, manager, local, hasGsmtc, screenW, screenH, mouseX, mouseY);
        } else {
            GsmtcHudOverlay.renderActiveCapsule(ctx, manager, local, hasGsmtc, screenW, screenH, mouseX, mouseY);
        }
    }

    private static int getGsmtcCoverGlId() {
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

    private static int getLocalCoverGlId() {
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

    private static void renderActiveCapsuleSkija(SkijaEngine skija, GuiGraphicsExtractor ctx, GsmtcManager manager, MusicPlayer local, boolean hasGsmtc, int screenW, int screenH, double mouseX, double mouseY) {
        int dot;
        String title;
        int borderColor;
        int pillW = 205;
        int pillH = 24;
        int x = (screenW - pillW) / 2;
        int y = 8;
        lastPillX = x;
        lastPillY = y;
        lastPillW = pillW;
        lastPillH = pillH;
        boolean isHovered = GsmtcHudOverlay.isInside(mouseX, mouseY, x, y, pillW, pillH);
        String app = hasGsmtc ? manager.getSourceApp().toLowerCase() : "";
        boolean speaker = ModConfig.getInstance().speakerMode;
        borderColor = speaker ? 0xFFFFCC00 : 0xFF388BFD;
        String sourceLabel = speaker ? (hasGsmtc ? (app.contains("yandex") || app.contains("яндекс") ? "Колонка • Я.Музыка" : (app.contains("spotify") || app.contains("спотифай") ? "Колонка • Spotify" : "Колонка • Web")) : "Колонка") : (hasGsmtc ? (app.contains("yandex") || app.contains("яндекс") ? "Яндекс.Музыка" : (app.contains("spotify") || app.contains("спотифай") ? "Spotify" : "Web / App")) : "MP3 Плеер");
        title = hasGsmtc ? manager.getTitle() : local.getCurrentTrackName();
        if (title == null || title.isEmpty()) {
            title = "Без названия";
        }
        if ((dot = title.lastIndexOf('.')) > 0 && !hasGsmtc) {
            title = title.substring(0, dot);
        }
        String artist = hasGsmtc ? manager.getArtist() : "";
        boolean isPlaying = hasGsmtc ? manager.isPlaying() : local.isPlaying();
        String subText = artist.isEmpty() ? sourceLabel : artist;
        double progress = 0.0;
        if (hasGsmtc) {
            progress = manager.getProgressFraction();
        } else if (local.getDuration() > 0L) {
            progress = Math.max(0.0, Math.min(1.0, (double)local.getCurrentPosition() / (double)local.getDuration()));
        }
        int bg = isHovered ? 0xE614141E : 0xCC101018;

        float guiScale = (float) Minecraft.getInstance().getWindow().getGuiScale();
        int frameW = pillW + 16;
        int frameH = pillH + 16;

        int btnRadius = 8;
        int btnX = x + pillW - 13;
        int btnY = y + 12;
        lastBtnX = btnX;
        lastBtnY = btnY;
        lastBtnRadius = btnRadius;
        boolean hoverBtn = GsmtcHudOverlay.isInsideCircle(mouseX, mouseY, btnX, btnY, btnRadius + 3);
        long posSec = hasGsmtc ? (long)manager.getPosition() : local.getCurrentPosition() / 1000000L;
        byte[] coverBytes = hasGsmtc ? manager.getCoverBytes() : skija.getLocalCoverBytes();

        long now = System.currentTimeMillis();
        boolean stateChanged = !title.equals(lastHudTitle)
                || !artist.equals(lastHudArtist)
                || isPlaying != lastHudPlaying
                || isHovered != lastHudHover
                || hoverBtn != lastHudBtnHover
                || posSec != lastHudPosSec
                || borderColor != lastHudBorderColor
                || coverBytes != lastHudCover
                || Math.abs(guiScale - lastHudScale) > 0.01f;

        boolean inMainScreen = Minecraft.getInstance().gui != null && Minecraft.getInstance().gui.screen() instanceof MainScreen;
        boolean needRedraw;
        if (inMainScreen && hudTextureReady) {
            needRedraw = false;
        } else if (!hudTextureReady || stateChanged) {
            needRedraw = true;
        } else {
            needRedraw = false;
        }

        if (needRedraw) {
            skija.beginHudFrame(frameW, frameH, guiScale);

            float px = 8f;
            float py = 6f;
            skija.drawDropShadow(px, py, pillW, pillH, 12f, 10f, 1f, borderColor & 0x44FFFFFF);
            skija.drawRoundedRect(px, py, pillW, pillH, 12f, bg);
            skija.drawRoundedRectOutline(px, py, pillW, pillH, 12f, 1.2f, borderColor);

            skija.drawCover(coverBytes, px + 3.5f, py + 3.5f, 17f, 17f, 4f);

            float textX = px + 24f;
            float localBtnX = px + pillW - 13f;
            float localBtnY = py + 12f;

            float barsX = localBtnX - 28f;
            float timeX = barsX - 5f;
            String timeStr = GsmtcHudOverlay.formatTime(posSec);

            float timeW = skija.getTextWidth(timeStr, 8.5f, true);
            float maxTitleW = timeX - timeW - 5f - textX;
            String displayTitle = title;
            float titleW = skija.getTextWidth(displayTitle, 10f, true);
            if (titleW > maxTitleW) {
                int est = (int) Math.max(1, displayTitle.length() * (maxTitleW / titleW));
                displayTitle = displayTitle.substring(0, Math.min(displayTitle.length(), est));
                while (displayTitle.length() > 1 && skija.getTextWidth(displayTitle + "..", 10f, true) > maxTitleW) {
                    displayTitle = displayTitle.substring(0, displayTitle.length() - 1);
                }
                displayTitle = displayTitle + "..";
            }
            skija.drawText(displayTitle, textX, py + 8.5f, 10f, 0xFFFFFFFF, true, SkijaEngine.ALIGN_LEFT);

            String displaySub = subText;
            float subW = skija.getTextWidth(displaySub, 8f, false);
            if (subW > maxTitleW) {
                int est = (int) Math.max(1, displaySub.length() * (maxTitleW / subW));
                displaySub = displaySub.substring(0, Math.min(displaySub.length(), est));
                while (displaySub.length() > 1 && skija.getTextWidth(displaySub + "..", 8f, false) > maxTitleW) {
                    displaySub = displaySub.substring(0, displaySub.length() - 1);
                }
                displaySub = displaySub + "..";
            }
            skija.drawText(displaySub, textX, py + 16.5f, 8f, 0xFFA0A0B0, false, SkijaEngine.ALIGN_LEFT);
            skija.drawText(timeStr, timeX - timeW, py + 12.0f, 8.5f, 0xFFCCCCCC, true, SkijaEngine.ALIGN_LEFT);

            if (!isPlaying) {
                for (int i = 0; i < 4; ++i) {
                    skija.drawRoundedRect(barsX + (float)i * 2.8f, py + 14.5f, 1.6f, 2.5f, 0.8f, 0x44FFFFFF);
                }
            }

            skija.drawPlayPauseButton(localBtnX, localBtnY, 7.5f, isPlaying, hoverBtn, borderColor);

            float barX = textX;
            float barW = (float)pillW - 46f;
            float barY = py + pillH - 2.5f;
            lastBarX = (int)(x + (textX - px));
            lastBarY = (int)(y + pillH - 2.5f);
            lastBarW = (int)barW;
            skija.drawRoundedRect(barX, barY, barW, 2.0f, 1.0f, 0x33FFFFFF);
            if (progress > 0.001) {
                skija.drawRoundedRect(barX, barY, barW * (float)progress, 2.0f, 1.0f, borderColor);
            }

            skija.endHudFrame();

            lastHudRenderTime = now;
            hudTextureReady = true;
            lastHudTitle = title;
            lastHudArtist = artist;
            lastHudPlaying = isPlaying;
            lastHudHover = isHovered;
            lastHudBtnHover = hoverBtn;
            lastHudPosSec = posSec;
            lastHudBorderColor = borderColor;
            lastHudCover = coverBytes;
            lastHudScale = guiScale;
        }

        skija.renderHudToContext(ctx, x - 8, y - 6, frameW, frameH);

        // Fluid 60+ FPS hardware accelerated equalizer bars via Minecraft RenderPearl quad fill:
        if (isPlaying) {
            int screenBarsX = x + pillW - 13 - 28;
            int screenBarsY = y + 7;
            for (int i = 0; i < 4; ++i) {
                float wave = (float) Math.sin((double) now * 0.009 + (double) i * 1.3);
                int barH = (int) (Math.abs(wave) * 7.5f) + 2;
                int barTop = screenBarsY + 10 - barH;
                int bx = screenBarsX + (int)(i * 3);
                ctx.fill(bx, barTop, bx + 2, screenBarsY + 10, borderColor);
            }
        }
    }

    private static String formatTime(long seconds) {
        long m = seconds / 60L;
        long s = seconds % 60L;
        return String.format("%d:%02d", m, s);
    }

    public static void renderActiveCapsule(GuiGraphicsExtractor ctx, GsmtcManager manager, MusicPlayer local, boolean hasGsmtc, int screenW, int screenH, double mouseX, double mouseY) {
        boolean hoverBtn;
        int btnColor;
        boolean hasCover;
        Minecraft client = Minecraft.getInstance();
        int pillW = 205;
        int pillH = 24;
        int x = (screenW - pillW) / 2;
        int y = 8;
        lastPillX = x;
        lastPillY = y;
        lastPillW = pillW;
        lastPillH = pillH;
        boolean isHovered = GsmtcHudOverlay.isInside(mouseX, mouseY, x, y, pillW, pillH);
        int bgColor = isHovered ? -267119073 : -301068267;
        GsmtcHudOverlay.drawSmoothPill(ctx, x, y, pillW, pillH, bgColor);
        String app = hasGsmtc ? manager.getSourceApp().toLowerCase() : "";
        boolean speakerFallback = ModConfig.getInstance().speakerMode;
        int borderColor = speakerFallback ? -13312 : -13058568;
        GsmtcHudOverlay.drawSmoothPillBorder(ctx, x, y, pillW, pillH, borderColor);
        int artSize = 16;
        int artX = x + 4;
        int artY = y + 4;
        boolean bl = hasCover = hasGsmtc && manager.hasCover();
        if (hasCover) {
            ctx.blit(GsmtcManager.COVER_TEXTURE_ID, artX, artX + artSize, artY, artY + artSize, 0.0f, 1.0f, 0.0f, 1.0f);
            GsmtcHudOverlay.drawCircleOutline(ctx, artX + artSize / 2, artY + artSize / 2, artSize / 2, 0x44FFFFFF);
        } else {
            GsmtcHudOverlay.drawCircle(ctx, artX + artSize / 2, artY + artSize / 2, artSize / 2, -14934748);
            ctx.centeredText(client.font, Component.literal("♪"), artX + artSize / 2, artY + 4, -13312);
            GsmtcHudOverlay.drawCircleOutline(ctx, artX + artSize / 2, artY + artSize / 2, artSize / 2, 0x33FFFFFF);
        }
        String title = hasGsmtc ? manager.getTitle() : local.getCurrentTrackName();
        String artist = hasGsmtc ? manager.getArtist() : "";
        boolean isPlaying = hasGsmtc ? manager.isPlaying() : local.isPlaying();
        long posSec = hasGsmtc ? (long)manager.getPosition() : local.getCurrentPosition() / 1000000L;
        String timeStr = GsmtcHudOverlay.formatTime(posSec);
        int btnRadius = 7;
        int btnX = x + pillW - 13;
        int btnY = y + pillH / 2;
        lastBtnX = btnX;
        lastBtnY = btnY;
        lastBtnRadius = btnRadius;
        int barsX = btnX - 26;
        int timeX = barsX - 4;
        int timeW = client.font.width(timeStr);
        String fullInfo = artist.isEmpty() ? title : title + " \u00b7 " + artist;
        int maxTextW = timeX - timeW - (artX + artSize + 10);
        Object displayInfo = client.font.plainSubstrByWidth(fullInfo, Math.max(30, maxTextW));
        if (fullInfo.length() > ((String)displayInfo).length()) {
            displayInfo = client.font.plainSubstrByWidth(fullInfo, Math.max(20, maxTextW - 6)) + "..";
        }
        int textX = artX + artSize + 5;
        int textY = y + 5;
        Component renderedText = Component.literal((String)displayInfo);
        ctx.text(client.font, renderedText, textX, textY, -1, true);
        ctx.text(client.font, Component.literal(timeStr), timeX - timeW, textY, -3354404, true);
        if (isPlaying) {
            long now = System.currentTimeMillis();
            for (int i = 0; i < 3; ++i) {
                int bH = (int)(2.0 + (Math.sin((double)now * 0.01 + (double)i) * 0.5 + 0.5) * 6.0);
                ctx.fill(barsX + i * 3, y + pillH / 2 + 3 - bH, barsX + i * 3 + 2, y + pillH / 2 + 3, borderColor);
            }
        }
        int n = btnColor = (hoverBtn = GsmtcHudOverlay.isInsideCircle(mouseX, mouseY, btnX, btnY, btnRadius + 2)) ? -14702849 : -16739073;
        if (app.contains("spotify") || app.contains("\u0441\u043f\u043e\u0442\u0438\u0444\u0430\u0439")) {
            btnColor = hoverBtn ? -14298266 : -14829228;
        } else if (app.contains("yandex") || app.contains("\u044f\u043d\u0434\u0435\u043a\u0441")) {
            btnColor = hoverBtn ? -10496 : -13312;
        }
        GsmtcHudOverlay.drawCircle(ctx, btnX, btnY, btnRadius, btnColor);
        if (hoverBtn) {
            GsmtcHudOverlay.drawCircleOutline(ctx, btnX, btnY, btnRadius + 1, -1);
        }
        if (isPlaying) {
            ctx.fill(btnX - 2, btnY - 3, btnX - 1, btnY + 3, -1);
            ctx.fill(btnX + 1, btnY - 3, btnX + 2, btnY + 3, -1);
        } else {
            GsmtcHudOverlay.drawMiniPlayTriangle(ctx, btnX - 1, btnY);
        }
        double progress = 0.0;
        if (hasGsmtc) {
            progress = manager.getProgressFraction();
        } else if (local.getDuration() > 0L) {
            progress = Math.max(0.0, Math.min(1.0, (double)local.getCurrentPosition() / (double)local.getDuration()));
        }
        int barX = textX;
        int barW = pillW - (barX - x) - 26;
        int barY = y + pillH - 4;
        int barH = 2;
        lastBarX = barX;
        lastBarY = barY;
        lastBarW = barW;
        ctx.fill(barX, barY, barX + barW, barY + barH, 0x4433333D);
        int filledW = (int)((double)barW * progress);
        if (filledW > 0) {
            int fillCol = app.contains("spotify") || app.contains("\u0441\u043f\u043e\u0442\u0438\u0444\u0430\u0439") ? -14829228 : (app.contains("yandex") || app.contains("\u044f\u043d\u0434\u0435\u043a\u0441") ? -13312 : -16739073);
            ctx.fill(barX, barY, barX + filledW, barY + barH, fillCol);
        }
    }

    private static void renderStandbyCapsule(GuiGraphicsExtractor ctx, int screenW, int screenH, double mouseX, double mouseY) {
        Minecraft client = Minecraft.getInstance();
        int pillW = 145;
        int pillH = 22;
        int x = (screenW - pillW) / 2;
        int y = 8;
        lastPillX = x;
        lastPillY = y;
        lastPillW = pillW;
        lastPillH = pillH;
        boolean isHovered = GsmtcHudOverlay.isInside(mouseX, mouseY, x, y, pillW, pillH);
        GsmtcHudOverlay.drawSmoothPill(ctx, x, y, pillW, pillH, isHovered ? -401337317 : -871362539);
        GsmtcHudOverlay.drawSmoothPillBorder(ctx, x, y, pillW, pillH, isHovered ? 0x55FFFFFF : 0x28FFFFFF);
        int artSize = 14;
        int artX = x + 4;
        int artY = y + 4;
        GsmtcHudOverlay.drawCircle(ctx, artX + artSize / 2, artY + artSize / 2, artSize / 2, -14802904);
        ctx.centeredText(client.font, Component.literal("♪"), artX + artSize / 2, artY + 3, -13312);
        int textX = artX + artSize + 5;
        int textY = y + 6;
        Component text = Component.literal("MP3 Player · [P]");
        ctx.text(client.font, text, textX, textY, -3355444, true);
        int btnRadius = 6;
        int btnX = x + pillW - 12;
        int btnY = y + pillH / 2;
        lastBtnX = btnX;
        lastBtnY = btnY;
        lastBtnRadius = btnRadius;
        boolean hoverBtn = GsmtcHudOverlay.isInsideCircle(mouseX, mouseY, btnX, btnY, btnRadius + 2);
        GsmtcHudOverlay.drawCircle(ctx, btnX, btnY, btnRadius, hoverBtn ? -14702849 : -16739073);
        GsmtcHudOverlay.drawMiniPlayTriangle(ctx, btnX - 1, btnY);
    }

    public static boolean handleClick(double mouseX, double mouseY) {
        boolean hasLocal;
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null || (client.gui.hud != null && client.gui.hud.isHidden())) {
            return false;
        }
        if (lastPillW <= 0 || lastPillH <= 0) {
            return false;
        }
        GsmtcManager manager = GsmtcManager.getInstance();
        MusicPlayer local = MusicPlayer.getInstance();
        if (!manager.isHudEnabled()) {
            return false;
        }
        boolean hasGsmtc = manager.isAvailable() && manager.hasTrack();
        boolean bl = hasLocal = local.isPlaying() || local.getCurrentTrackName() != null && !local.getCurrentTrackName().equals("No track") && !local.getPlaylist().isEmpty();
        if (GsmtcHudOverlay.isInsideCircle(mouseX, mouseY, lastBtnX, lastBtnY, lastBtnRadius + 5)) {
            boolean isGsmtcPlaying = manager.isAvailable() && manager.hasTrack() && manager.isPlaying();
            boolean isLocalPlaying = local.isPlaying();
            if (isLocalPlaying && !isGsmtcPlaying) {
                local.pause();
            } else if (hasGsmtc) {
                manager.togglePlayPause();
            } else if (local.isPlaying()) {
                local.pause();
            } else {
                local.play();
            }
            return true;
        }
        if (GsmtcHudOverlay.isInside(mouseX, mouseY, lastBarX - 4, lastBarY - 4, lastBarW + 8, 8)) {
            double frac = Math.max(0.0, Math.min(1.0, (mouseX - (double)lastBarX) / (double)lastBarW));
            if (hasGsmtc) {
                manager.seek(manager.getDuration() * frac);
            } else if (local.getDuration() > 0L) {
                local.seekTo((long)((double)local.getDuration() * frac));
            }
            return true;
        }
        if (GsmtcHudOverlay.isInside(mouseX, mouseY, lastPillX, lastPillY, lastPillW, lastPillH)) {
            if (!(client.gui.screen() instanceof MainScreen)) {
                client.gui.setScreen((Screen)new MainScreen());
            }
            return true;
        }
        return false;
    }

    public static boolean isMouseOverHud(double mouseX, double mouseY) {
        GsmtcManager manager = GsmtcManager.getInstance();
        if (!manager.isHudEnabled()) {
            return false;
        }
        return GsmtcHudOverlay.isInside(mouseX, mouseY, lastPillX, lastPillY, lastPillW, lastPillH);
    }

    private static void drawSmoothPill(GuiGraphicsExtractor ctx, int x, int y, int w, int h, int color) {
        int r = h / 2;
        ctx.fill(x + r, y, x + w - r, y + h, color);
        GsmtcHudOverlay.drawCircle(ctx, x + r, y + r, r, color);
        GsmtcHudOverlay.drawCircle(ctx, x + w - r, y + r, r, color);
    }

    private static void drawSmoothPillBorder(GuiGraphicsExtractor ctx, int x, int y, int w, int h, int color) {
        int r = h / 2;
        ctx.fill(x + r, y, x + w - r, y + 1, color);
        ctx.fill(x + r, y + h - 1, x + w - r, y + h, color);
        GsmtcHudOverlay.drawCircleOutline(ctx, x + r, y + r, r, color);
        GsmtcHudOverlay.drawCircleOutline(ctx, x + w - r, y + r, r, color);
    }

    private static void drawCircle(GuiGraphicsExtractor ctx, int cx, int cy, int r, int color) {
        for (int dy = -r; dy <= r; ++dy) {
            int dx = (int)Math.sqrt(r * r - dy * dy);
            ctx.fill(cx - dx, cy + dy, cx + dx, cy + dy + 1, color);
        }
    }

    private static void drawCircleOutline(GuiGraphicsExtractor ctx, int cx, int cy, int r, int color) {
        for (int dy = -r; dy <= r; ++dy) {
            int dx = (int)Math.sqrt(r * r - dy * dy);
            ctx.fill(cx - dx, cy + dy, cx - dx + 1, cy + dy + 1, color);
            ctx.fill(cx + dx - 1, cy + dy, cx + dx, cy + dy + 1, color);
        }
    }

    private static void drawMiniPlayTriangle(GuiGraphicsExtractor ctx, int cx, int cy) {
        for (int i = 0; i <= 4; ++i) {
            ctx.fill(cx + i, cy - (4 - i), cx + i + 1, cy + (4 - i) + 1, -1);
        }
    }

    private static boolean isInside(double mx, double my, int x, int y, int w, int h) {
        return mx >= (double)x && mx < (double)(x + w) && my >= (double)y && my < (double)(y + h);
    }

    private static boolean isInsideCircle(double mx, double my, int cx, int cy, int r) {
        double dx = mx - (double)cx;
        double dy = my - (double)cy;
        return dx * dx + dy * dy <= (double)(r * r);
    }
}

