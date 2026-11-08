package com.mp3player.client.gui.skija;

import com.mojang.blaze3d.platform.NativeImage;
import com.mp3player.MP3PlayerClient;
import io.github.humbleui.skija.*;
import io.github.humbleui.skija.svg.SVGDOM;
import io.github.humbleui.types.RRect;
import io.github.humbleui.types.Rect;
import java.io.InputStream;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

public class SkijaEngine {
    private static SkijaEngine instance;

    public static final Identifier SKIJA_TEXTURE_ID = Identifier.fromNamespaceAndPath("mp3player", "skija_ui");

    public static final int ALIGN_LEFT = 17;
    public static final int ALIGN_CENTER = 18;
    public static final int ALIGN_RIGHT = 20;

    public static final String ICON_PLAY = "\ue037";
    public static final String ICON_PAUSE = "\ue034";
    public static final String ICON_SKIP_NEXT = "\ue044";
    public static final String ICON_SKIP_PREV = "\ue045";
    public static final String ICON_FAST_FORWARD = "\ue01f";
    public static final String ICON_FAST_REWIND = "\ue020";
    public static final String ICON_VOLUME_UP = "\ue050";
    public static final String ICON_VOLUME_OFF = "\ue04f";
    public static final String ICON_VOLUME_MUTE = "\ue04e";
    public static final String ICON_REFRESH = "\ue5d5";
    public static final String ICON_FOLDER = "\ue2c7";
    public static final String ICON_FOLDER_OPEN = "\ue2c8";
    public static final String ICON_DELETE = "\ue872";
    public static final String ICON_HEART = "\ue87d";
    public static final String ICON_HEART_BORDER = "\ue87e";
    public static final String ICON_DARK_MODE = "\ue51c";
    public static final String ICON_LIGHT_MODE = "\ue518";
    public static final String ICON_MORE_VERT = "\ue5d4";
    public static final String ICON_CLOSE = "\ue5cd";
    public static final String ICON_MUSIC = "\ue405";
    public static final String ICON_SETTINGS = "\ue8b8";
    public static final String ICON_REPEAT = "\ue040";
    public static final String ICON_SHARE = "\ue80d";
    public static final String ICON_INFO = "\ue88e";
    public static final String ICON_ALBUM = "\ue019";

    private boolean available = false;
    private boolean darkTheme = true;

    private int bufferWidth = 0;
    private int bufferHeight = 0;
    private NativeImage nativeImage;
    private DynamicTexture dynamicTexture;
    private Surface surface;
    private Canvas canvas;
    private int scissorSaveCount = 0;

    private Typeface regularTypeface;
    private Typeface boldTypeface;
    private Typeface iconTypeface;
    private Font regularFont;
    private Font boldFont;
    private Font iconFont;

    private volatile byte[] gsmtcCoverBytes;
    private volatile byte[] localCoverBytes;

    public static synchronized SkijaEngine getInstance() {
        if (instance == null) {
            instance = new SkijaEngine();
        }
        return instance;
    }

    private SkijaEngine() {
        init();
    }

    private void init() {
        try {
            MP3PlayerClient.LOGGER.info("[Skija] Initializing Skija vector graphics engine...");
            loadFonts();
            this.available = true;
            MP3PlayerClient.LOGGER.info("[Skija] Skija vector engine initialized successfully!");
        } catch (Throwable t) {
            MP3PlayerClient.LOGGER.error("[Skija] Failed to initialize Skija: {}", t.getMessage(), t);
            this.available = false;
        }
    }

    private void loadFonts() {
        FontMgr fontMgr = FontMgr.getDefault();
        try (InputStream is = getClass().getResourceAsStream("/assets/mp3player/fonts/font.ttf")) {
            if (is != null) {
                try (Data data = Data.makeFromBytes(is.readAllBytes())) {
                    this.regularTypeface = fontMgr.makeFromData(data);
                    if (this.regularTypeface != null) {
                        this.regularFont = new Font(this.regularTypeface, 12f);
                    }
                }
            }
        } catch (Throwable ignored) {}

        try (InputStream is = getClass().getResourceAsStream("/assets/mp3player/fonts/font-bold.ttf")) {
            if (is != null) {
                try (Data data = Data.makeFromBytes(is.readAllBytes())) {
                    this.boldTypeface = fontMgr.makeFromData(data);
                    if (this.boldTypeface != null) {
                        this.boldFont = new Font(this.boldTypeface, 12f);
                    }
                }
            }
        } catch (Throwable ignored) {}

        try (InputStream is = getClass().getResourceAsStream("/assets/mp3player/fonts/material-icons.ttf")) {
            if (is != null) {
                try (Data data = Data.makeFromBytes(is.readAllBytes())) {
                    this.iconTypeface = fontMgr.makeFromData(data);
                    if (this.iconTypeface != null) {
                        this.iconFont = new Font(this.iconTypeface, 16f);
                    }
                }
            }
        } catch (Throwable ignored) {}

        if (this.regularFont == null) {
            this.regularTypeface = fontMgr.matchFamilyStyle(null, FontStyle.NORMAL);
            this.regularFont = new Font(this.regularTypeface, 12f);
        }
        if (this.boldFont == null) {
            this.boldTypeface = this.regularTypeface;
            this.boldFont = new Font(this.boldTypeface, 12f);
        }
        if (this.iconFont == null) {
            this.iconFont = this.regularFont;
        }
    }

    public boolean isAvailable() {
        return this.available;
    }

    public void setDarkTheme(boolean dark) {
        this.darkTheme = dark;
    }

    public boolean isDarkTheme() {
        return this.darkTheme;
    }

    public synchronized void beginFrame(int width, int height) {
        if (!this.available || width <= 0 || height <= 0) {
            return;
        }

        this.scissorSaveCount = 0;

        if (this.surface == null || this.bufferWidth != width || this.bufferHeight != height) {
            destroySurface();
            this.bufferWidth = width;
            this.bufferHeight = height;

            try {
                this.nativeImage = new NativeImage(NativeImage.Format.RGBA, width, height, false);
                ImageInfo info = new ImageInfo(width, height, ColorType.RGBA_8888, ColorAlphaType.PREMUL);
                this.surface = Surface.wrapPixels(info, this.nativeImage.getPointer(), (long) width * 4);
                this.canvas = this.surface.getCanvas();

                this.dynamicTexture = new DynamicTexture(() -> "skija_ui", this.nativeImage);
                Minecraft.getInstance().getTextureManager().register(SKIJA_TEXTURE_ID, this.dynamicTexture);
            } catch (Throwable t) {
                MP3PlayerClient.LOGGER.error("[Skija] Error allocating surface: {}", t.getMessage());
                this.canvas = null;
                return;
            }
        }

        if (this.canvas != null) {
            this.canvas.clear(0x00000000);
        }
    }

    public synchronized void endFrame() {
        if (this.surface == null || this.canvas == null || this.dynamicTexture == null) {
            return;
        }
        try {
            while (this.scissorSaveCount > 0) {
                this.canvas.restore();
                this.scissorSaveCount--;
            }
            this.dynamicTexture.upload();
        } catch (Throwable t) {
            MP3PlayerClient.LOGGER.error("[Skija] Error ending frame: {}", t.getMessage());
        }
    }

    public void renderToContext(GuiGraphicsExtractor ctx, int screenX, int screenY, int width, int height) {
        if (!this.available || this.dynamicTexture == null) {
            return;
        }
        ctx.blit(SKIJA_TEXTURE_ID, screenX, screenX + width, screenY, screenY + height, 0.0f, 1.0f, 0.0f, 1.0f);
    }

    public Canvas getCanvas() {
        return this.canvas;
    }

    // ==========================================
    // High-level Vector Drawing Utilities
    // ==========================================

    public void drawRoundedRect(float x, float y, float w, float h, float radius, int color) {
        if (this.canvas == null) return;
        try (Paint paint = new Paint()) {
            paint.setColor(color);
            paint.setAntiAlias(true);
            this.canvas.drawRRect(RRect.makeXYWH(x, y, w, h, radius), paint);
        }
    }

    public void drawRoundedRectOutline(float x, float y, float w, float h, float radius, float strokeWidth, int color) {
        if (this.canvas == null || strokeWidth <= 0) return;
        try (Paint stroke = new Paint()) {
            stroke.setColor(color);
            stroke.setMode(PaintMode.STROKE);
            stroke.setStrokeWidth(strokeWidth);
            stroke.setAntiAlias(true);
            float off = strokeWidth / 2f;
            this.canvas.drawRRect(RRect.makeXYWH(x + off, y + off, w - strokeWidth, h - strokeWidth, Math.max(0, radius - off)), stroke);
        }
    }

    public void drawRoundedRectStroke(float x, float y, float w, float h, float radius, int fillColor, int strokeColor, float strokeWidth) {
        if (this.canvas == null) return;
        try (Paint fill = new Paint(); Paint stroke = new Paint()) {
            fill.setColor(fillColor);
            fill.setAntiAlias(true);
            this.canvas.drawRRect(RRect.makeXYWH(x, y, w, h, radius), fill);

            if (strokeColor != 0 && strokeWidth > 0) {
                stroke.setColor(strokeColor);
                stroke.setMode(PaintMode.STROKE);
                stroke.setStrokeWidth(strokeWidth);
                stroke.setAntiAlias(true);
                float off = strokeWidth / 2f;
                this.canvas.drawRRect(RRect.makeXYWH(x + off, y + off, w - strokeWidth, h - strokeWidth, Math.max(0, radius - off)), stroke);
            }
        }
    }

    public void drawCard(float x, float y, float w, float h, float radius, int bgColor, int borderColor, float shadowBlur) {
        if (this.canvas == null) return;
        if (shadowBlur > 0) {
            try (Paint shadow = new Paint()) {
                shadow.setColor(0x44000000);
                shadow.setMaskFilter(MaskFilter.makeBlur(FilterBlurMode.NORMAL, shadowBlur));
                this.canvas.drawRRect(RRect.makeXYWH(x, y + 2, w, h, radius), shadow);
            }
        }
        drawRoundedRectStroke(x, y, w, h, radius, bgColor, borderColor, 1.0f);
    }

    public void drawDropShadow(float x, float y, float w, float h, float radius, float blur, float spread, int color) {
        if (this.canvas == null || blur <= 0) return;
        try (Paint shadow = new Paint()) {
            shadow.setColor(color);
            shadow.setMaskFilter(MaskFilter.makeBlur(FilterBlurMode.NORMAL, blur / 2f));
            this.canvas.drawRRect(RRect.makeXYWH(x - spread, y - spread + 2f, w + spread * 2f, h + spread * 2f, radius), shadow);
        }
    }

    public void drawGradientRect(float x, float y, float w, float h, float radius, int topColor, int bottomColor) {
        if (this.canvas == null) return;
        try (Paint paint = new Paint();
             Shader shader = Shader.makeLinearGradient(x, y, x, y + h, new int[]{topColor, bottomColor})) {
            paint.setShader(shader);
            paint.setAntiAlias(true);
            this.canvas.drawRRect(RRect.makeXYWH(x, y, w, h, radius), paint);
        }
    }

    public void drawCircle(float cx, float cy, float radius, int color) {
        if (this.canvas == null) return;
        try (Paint paint = new Paint()) {
            paint.setColor(color);
            paint.setAntiAlias(true);
            this.canvas.drawCircle(cx, cy, radius, paint);
        }
    }

    public void drawCircleOutline(float cx, float cy, float radius, float strokeWidth, int color) {
        if (this.canvas == null || strokeWidth <= 0) return;
        try (Paint stroke = new Paint()) {
            stroke.setColor(color);
            stroke.setMode(PaintMode.STROKE);
            stroke.setStrokeWidth(strokeWidth);
            stroke.setAntiAlias(true);
            this.canvas.drawCircle(cx, cy, radius, stroke);
        }
    }

    public void drawCircleStroke(float cx, float cy, float radius, int fillColor, int strokeColor, float strokeWidth) {
        if (this.canvas == null) return;
        try (Paint fill = new Paint(); Paint stroke = new Paint()) {
            fill.setColor(fillColor);
            fill.setAntiAlias(true);
            this.canvas.drawCircle(cx, cy, radius, fill);

            if (strokeColor != 0 && strokeWidth > 0) {
                stroke.setColor(strokeColor);
                stroke.setMode(PaintMode.STROKE);
                stroke.setStrokeWidth(strokeWidth);
                stroke.setAntiAlias(true);
                this.canvas.drawCircle(cx, cy, radius - strokeWidth / 2f, stroke);
            }
        }
    }

    public void drawTab(float x, float y, float w, float h, String text, boolean active, boolean hover, int accent) {
        if (this.canvas == null) return;
        int bg = active ? (darkTheme ? 0xFF2A2A38 : 0xFFFFFFFF) : (hover ? (darkTheme ? 0xFF242430 : 0xFFE8E8EE) : 0x00000000);
        if (bg != 0) {
            drawRoundedRect(x, y, w, h, 6f, bg);
        }
        int textCol = active ? accent : (hover ? (darkTheme ? 0xFFFFFFFF : 0xFF111111) : (darkTheme ? 0xFF9999AA : 0xFF666677));
        drawCenteredText(text, x + w / 2f, y + h / 2f + 4f, 12f, textCol, active);
        if (active) {
            try (Paint line = new Paint()) {
                line.setColor(accent);
                line.setStrokeWidth(2.5f);
                line.setAntiAlias(true);
                this.canvas.drawLine(x + 8, y + h - 1.5f, x + w - 8, y + h - 1.5f, line);
            }
        }
    }

    public void drawTabBase(float x, float y, float w, float h, boolean active, boolean hover, int accent) {
        if (this.canvas == null) return;
        int bg = active ? (darkTheme ? 0xFF2A2A38 : 0xFFFFFFFF) : (hover ? (darkTheme ? 0xFF242430 : 0xFFE8E8EE) : 0x00000000);
        if (bg != 0) {
            drawRoundedRect(x, y, w, h, 6f, bg);
        }
    }

    public void drawButtonBase(float x, float y, float w, float h, boolean hover, boolean pressed, int color) {
        if (this.canvas == null) return;
        int bg = pressed ? (color & 0x66FFFFFF) : (hover ? (color & 0x33FFFFFF) : (darkTheme ? 0xFF242430 : 0xFFE0E0EE));
        drawRoundedRect(x, y, w, h, 5f, bg);
    }

    public void drawButton(float x, float y, float w, float h, String text, boolean hover, boolean pressed, int activeColor, int normalColor) {
        if (this.canvas == null) return;
        int bg = pressed ? (activeColor & 0x55FFFFFF) : (hover ? (darkTheme ? 0xFF2A2A38 : 0xFFE0E0EE) : (darkTheme ? 0xFF1C1C26 : 0xFFEDEDF5));
        int border = hover ? activeColor : (darkTheme ? 0xFF353545 : 0xFFD0D0DD);
        drawRoundedRectStroke(x, y, w, h, 6f, bg, border, 1f);
        int textCol = hover ? (darkTheme ? 0xFFFFFFFF : 0xFF111111) : normalColor;
        drawCenteredText(text, x + w / 2f, y + h / 2f + 4f, 11f, textCol, hover);
    }

    public void drawSlider(float x, float y, float w, float h, float progress, int activeColor, int trackColor, int knobColor) {
        if (this.canvas == null) return;
        float r = h / 2f;
        drawRoundedRect(x, y, w, h, r, trackColor);
        float fillW = Math.max(0f, Math.min(w, w * progress));
        if (fillW > 0) {
            drawRoundedRect(x, y, fillW, h, r, activeColor);
        }
        float knobX = x + fillW;
        drawCircle(knobX, y + r, r + 2.5f, knobColor);
    }

    public void drawSlider(float x, float y, float w, float h, float progress, int activeColor, boolean hovered) {
        if (this.canvas == null) return;
        float r = h * 0.5f;
        int trackBg = this.darkTheme ? 0xFF242436 : 0xFFE2E6F0;
        drawRoundedRect(x, y, w, h, r, trackBg);
        float fillW = Math.max(h, Math.min(w, w * progress));
        if (fillW > 0.0f) {
            drawRoundedRect(x, y, fillW, h, r, activeColor);
        }
        float thumbX = x + Math.max(r, Math.min(w - r, w * progress));
        float thumbY = y + r;
        float thumbR = r + 2.0f;
        drawDropShadow(thumbX - thumbR, thumbY - thumbR, thumbR * 2.0f, thumbR * 2.0f, thumbR, 4.0f, 0.5f, 0x44000000);
        drawCircle(thumbX, thumbY, thumbR, 0xFFFFFFFF);
        drawCircleOutline(thumbX, thumbY, thumbR, 1.2f, activeColor);
    }

    public void drawProgressBar(float x, float y, float w, float h, float progress, boolean hover, int activeColor) {
        drawSlider(x, y, w, h, progress, activeColor, darkTheme ? 0xFF242432 : 0xFFD8DDE6, hover ? 0xFFFFFFFF : activeColor);
    }

    public void drawText(String text, float x, float y, float fontSize, int color, boolean bold) {
        if (this.canvas == null || text == null || text.isEmpty()) return;
        try (Paint paint = new Paint();
             Font font = new Font(bold ? this.boldTypeface : this.regularTypeface, fontSize)) {
            paint.setColor(color);
            paint.setAntiAlias(true);
            this.canvas.drawString(text, x, y, font, paint);
        }
    }

    public void drawText(String text, float x, float y, float fontSize, int color, boolean bold, int align) {
        if (this.canvas == null || text == null || text.isEmpty()) return;
        if (align == ALIGN_CENTER) {
            drawCenteredText(text, x, y, fontSize, color, bold);
        } else if (align == ALIGN_RIGHT) {
            float w = getTextWidth(text, fontSize, bold);
            drawText(text, x - w, y, fontSize, color, bold);
        } else {
            drawText(text, x, y, fontSize, color, bold);
        }
    }

    public void drawCenteredText(String text, float cx, float y, float fontSize, int color, boolean bold) {
        if (this.canvas == null || text == null || text.isEmpty()) return;
        try (Paint paint = new Paint();
             Font font = new Font(bold ? this.boldTypeface : this.regularTypeface, fontSize)) {
            paint.setColor(color);
            paint.setAntiAlias(true);
            Rect bounds = font.measureText(text, paint);
            float x = cx - bounds.getWidth() / 2f;
            this.canvas.drawString(text, x, y, font, paint);
        }
    }

    public void drawBoundedText(String text, float x, float y, float size, int color, boolean bold, float maxW) {
        if (this.canvas == null || text == null || text.isEmpty()) return;
        float tw = this.getTextWidth(text, size, bold);
        if (tw <= maxW) {
            this.drawText(text, x, y, size, color, bold, ALIGN_LEFT);
            return;
        }
        String truncated = text;
        while (truncated.length() > 3 && this.getTextWidth(truncated + "…", size, bold) > maxW) {
            truncated = truncated.substring(0, truncated.length() - 1);
        }
        this.drawText(truncated + "…", x, y, size, color, bold, ALIGN_LEFT);
    }

    public float getTextWidth(String text, float fontSize, boolean bold) {
        if (text == null || text.isEmpty()) return 0f;
        try (Paint p = new Paint();
             Font f = new Font(bold ? this.boldTypeface : this.regularTypeface, fontSize)) {
            return f.measureTextWidth(text, p);
        }
    }

    // ==========================================
    // Vector Icon Drawing
    // ==========================================

    public void drawIcon(String glyph, float x, float y, float size, int color) {
        if (this.canvas == null || glyph == null || glyph.isEmpty()) return;
        try (Paint paint = new Paint();
             Font font = new Font(this.iconTypeface, size)) {
            paint.setColor(color);
            paint.setAntiAlias(true);
            this.canvas.drawString(glyph, x, y, font, paint);
        }
    }

    public void drawIcon(String glyph, float x, float y, float size, int color, int align) {
        if (align == ALIGN_CENTER) {
            drawCenteredIcon(glyph, x, y, size, color);
        } else {
            drawIcon(glyph, x, y, size, color);
        }
    }

    public void drawCenteredIcon(String glyph, float cx, float cy, float size, int color) {
        if (this.canvas == null || glyph == null || glyph.isEmpty()) return;
        try (Paint paint = new Paint();
             Font font = new Font(this.iconTypeface, size)) {
            paint.setColor(color);
            paint.setAntiAlias(true);
            Rect bounds = font.measureText(glyph, paint);
            float x = cx - bounds.getWidth() / 2f;
            float y = cy + bounds.getHeight() / 2f;
            this.canvas.drawString(glyph, x, y, font, paint);
        }
    }

    public void drawPlayIcon(float cx, float cy, float radius, int color) {
        if (this.canvas == null) return;
        try (PathBuilder pb = new PathBuilder()) {
            float off = radius * 0.1f;
            pb.moveTo(cx - radius * 0.45f + off, cy - radius * 0.6f);
            pb.lineTo(cx + radius * 0.65f + off, cy);
            pb.lineTo(cx - radius * 0.45f + off, cy + radius * 0.6f);
            pb.closePath();
            try (Path path = pb.build(); Paint paint = new Paint()) {
                paint.setColor(color);
                paint.setAntiAlias(true);
                this.canvas.drawPath(path, paint);
            }
        }
    }

    public void drawPauseIcon(float cx, float cy, float radius, int color) {
        if (this.canvas == null) return;
        float barW = radius * 0.35f;
        float barH = radius * 1.2f;
        float gap = radius * 0.3f;
        drawRoundedRect(cx - gap / 2f - barW, cy - barH / 2f, barW, barH, 2f, color);
        drawRoundedRect(cx + gap / 2f, cy - barH / 2f, barW, barH, 2f, color);
    }

    public void drawSkipIcon(float cx, float cy, float radius, boolean forward, int color) {
        if (this.canvas == null) return;
        float dir = forward ? 1f : -1f;
        try (PathBuilder pb = new PathBuilder()) {
            float triSize = radius * 0.6f;
            pb.moveTo(cx - triSize * dir, cy - triSize);
            pb.lineTo(cx, cy);
            pb.lineTo(cx - triSize * dir, cy + triSize);
            pb.closePath();
            try (Path path = pb.build(); Paint paint = new Paint()) {
                paint.setColor(color);
                paint.setAntiAlias(true);
                this.canvas.drawPath(path, paint);
            }
        }
        float barW = radius * 0.25f;
        float barH = radius * 1.2f;
        float barX = forward ? cx + radius * 0.15f : cx - radius * 0.15f - barW;
        drawRoundedRect(barX, cy - barH / 2f, barW, barH, 1.5f, color);
    }

    public void drawPlayPauseButton(float cx, float cy, float radius, boolean isPlaying, boolean hover, int color) {
        if (this.canvas == null) return;
        int bg = hover ? (color & 0x44FFFFFF) : (color & 0x22FFFFFF);
        drawCircleStroke(cx, cy, radius, bg, color, 1.2f);
        if (isPlaying) {
            drawCenteredIcon(ICON_PAUSE, cx, cy + 0.5f, radius * 1.3f, color);
        } else {
            drawCenteredIcon(ICON_PLAY, cx + 0.5f, cy + 0.5f, radius * 1.3f, color);
        }
    }

    public void drawMiniPlayIcon(float cx, float cy, float size, int color) {
        drawCenteredIcon(ICON_PLAY, cx + 0.5f, cy + 0.5f, size * 2.3f, color);
    }

    public void drawMiniPauseIcon(float cx, float cy, float size, int color) {
        drawCenteredIcon(ICON_PAUSE, cx, cy + 0.5f, size * 2.3f, color);
    }

    public void drawSkipButton(float cx, float cy, float radius, boolean isForward, boolean isHovered) {
        int iconCol = isHovered ? (this.darkTheme ? 0xFFFFFFFF : 0xFF0F1416) : (this.darkTheme ? 0xFFA0A5B4 : 0xFF646E7B);
        drawCenteredIcon(isForward ? ICON_SKIP_NEXT : ICON_SKIP_PREV, cx, cy + 0.5f, radius * 1.6f, iconCol);
    }

    public void drawHeartIcon(float cx, float cy, float size, boolean isFav, boolean isHovered) {
        int color = isFav ? 0xFFFF4C53 : (isHovered ? 0xFFFFFFFF : (this.darkTheme ? 0x88888899 : 0xFF646E7B));
        drawCenteredIcon(isFav ? ICON_HEART : ICON_HEART_BORDER, cx, cy + 0.5f, size * 1.4f, color);
    }

    public void drawHeart(float cx, float cy, float size, int color, boolean filled) {
        if (this.canvas == null) return;
        float s = size * 0.5f;
        try (PathBuilder pb = new PathBuilder()) {
            pb.moveTo(cx, cy + s * 0.7f);
            pb.cubicTo(cx - s * 1.2f, cy - s * 0.3f, cx - s * 1.2f, cy - s * 1.1f, cx, cy - s * 0.5f);
            pb.cubicTo(cx + s * 1.2f, cy - s * 1.1f, cx + s * 1.2f, cy - s * 0.3f, cx, cy + s * 0.7f);
            pb.closePath();
            try (Path path = pb.build(); Paint paint = new Paint()) {
                paint.setColor(color);
                paint.setAntiAlias(true);
                if (!filled) {
                    paint.setMode(PaintMode.STROKE);
                    paint.setStrokeWidth(1.5f);
                }
                this.canvas.drawPath(path, paint);
            }
        }
    }

    public void drawGearIcon(float cx, float cy, float radius, int color) {
        drawCenteredIcon(ICON_SETTINGS, cx, cy + 0.5f, radius * 2.2f, color);
    }

    public void drawMoonSunIcon(float cx, float cy, float radius, boolean dark, int color) {
        drawCenteredIcon(dark ? ICON_DARK_MODE : ICON_LIGHT_MODE, cx, cy, radius * 2.2f, color);
    }

    public void drawSpeakerIcon(float cx, float cy, float radius, boolean muted, int color) {
        drawCenteredIcon(muted ? ICON_VOLUME_MUTE : ICON_VOLUME_UP, cx, cy, radius * 2.0f, color);
    }

    public void drawRefreshIcon(float cx, float cy, float radius, int color) {
        drawCenteredIcon(ICON_REFRESH, cx, cy + 0.5f, radius * 2.3f, color);
    }

    public void drawFolderIcon(float cx, float cy, float w, float h, int color) {
        drawCenteredIcon(ICON_FOLDER, cx, cy + 0.5f, Math.max(w, h) * 1.25f, color);
    }

    public void drawCrossIcon(float cx, float cy, float size, int color) {
        drawCenteredIcon(ICON_DELETE, cx, cy + 0.5f, size * 1.55f, color);
    }

    public void drawDotsIcon(float cx, float cy, float dotRadius, int color) {
        drawCenteredIcon(ICON_MORE_VERT, cx, cy + 0.5f, 13.0f, color);
    }

    public void drawMiniDisc(float cx, float cy, float radius, int vinylColor, int centerColor) {
        drawCircle(cx, cy, radius, vinylColor);
        drawCircleOutline(cx, cy, radius * 0.65f, 0.7f, 0x22FFFFFF);
        drawCircle(cx, cy, radius * 0.35f, centerColor);
        drawCircle(cx, cy, radius * 0.12f, 0xFF101016);
    }

    public void drawToggleSwitch(float x, float y, float w, float h, boolean active, boolean hovered) {
        if (this.canvas == null) return;
        float r = h * 0.5f;
        int trackCol = active ? 0xFF388BFD : (this.darkTheme ? 0xFF2A2A38 : 0xFFD1D5DB);
        if (hovered && !active) {
            trackCol = this.darkTheme ? 0xFF3D3D50 : 0xFF9CA3AF;
        }
        drawRoundedRect(x, y, w, h, r, trackCol);
        if (active) {
            drawDropShadow(x, y, w, h, r, 4.0f, 0.5f, 0x33388BFD);
        }
        float knobR = r - 2.0f;
        float knobX = active ? x + w - r : x + r;
        float knobY = y + r;
        drawCircle(knobX, knobY, knobR, 0xFFFFFFFF);
        drawCircleOutline(knobX, knobY, knobR, 0.8f, 0x22000000);
    }

    public void drawTogglePill(float x, float y, float w, float h, String text, boolean active, boolean hovered, int activeColor) {
        if (this.canvas == null) return;
        float radius = Math.min(h * 0.5f, 7.0f);
        int bg;
        int outline;
        int textCol;
        if (active) {
            bg = (activeColor & 0x00FFFFFF) | (this.darkTheme ? 0x28000000 : 0x20000000);
            outline = activeColor;
            textCol = this.darkTheme ? 0xFFFFFFFF : 0xFF0F1416;
        } else {
            bg = this.darkTheme ? (hovered ? 0xFF282836 : 0xFF1C1C28) : (hovered ? 0xFFE2E6F0 : 0xFFEEEFF6);
            outline = hovered ? 0x6690B4F8 : (this.darkTheme ? 0x2A555566 : 0x33CCCCCC);
            textCol = this.darkTheme ? 0xFF949AA8 : 0xFF646E7B;
        }
        drawRoundedRect(x, y, w, h, radius, bg);
        drawRoundedRectOutline(x, y, w, h, radius, active ? 1.2f : 0.8f, outline);
        if (active || hovered) {
            int glowCol = active ? activeColor : 0x6690B4F8;
            drawDropShadow(x, y, w, h, radius, hovered ? 7.0f : 4.0f, 0.5f, glowCol & 0x44FFFFFF);
        }
        drawCenteredText(text, x + w * 0.5f, y + h * 0.5f + 3.5f, h * 0.52f, textCol, active || hovered);
    }

    public void drawBadge(float x, float y, float h, String text, int color) {
        if (this.canvas == null) return;
        float textW = getTextWidth(text, 7.8f, true);
        float badgeW = textW + 12f;
        drawRoundedRect(x, y, badgeW, h, h / 2f, color & 0x33FFFFFF);
        drawRoundedRectOutline(x, y, badgeW, h, h / 2f, 1f, color);
        drawCenteredText(text, x + badgeW / 2f, y + h / 2f + 3f, 7.8f, color, true);
    }

    public void drawSourceIcon(float cx, float cy, float radius, String app) {
        if (this.canvas == null) return;
        int col = 0xFF388BFD;
        if (app.contains("yandex") || app.contains("яндекс")) col = 0xFFFF3333;
        else if (app.contains("spotify") || app.contains("спотифай")) col = 0xFF1DB954;
        drawCircle(cx, cy, radius, col);
    }

    public void drawEqualizerBars(float x, float y, float w, float h, int color, long timeMs) {
        if (this.canvas == null) return;
        int bars = 4;
        float barW = (w - (bars - 1) * 2f) / bars;
        for (int i = 0; i < bars; i++) {
            float phase = (float) Math.sin((timeMs / 120.0) + i * 1.3) * 0.5f + 0.5f;
            float barH = Math.max(2f, h * phase);
            drawRoundedRect(x + i * (barW + 2f), y + h - barH, barW, barH, 1f, color);
        }
    }

    // ==========================================
    // Clipping and Scissor Utilities
    // ==========================================

    public void clipRect(float x, float y, float w, float h) {
        if (this.canvas != null) {
            this.canvas.clipRect(Rect.makeXYWH(x, y, w, h), true);
        }
    }

    public void clipRRect(float x, float y, float w, float h, float radius) {
        if (this.canvas != null) {
            this.canvas.clipRRect(RRect.makeXYWH(x, y, w, h, radius), true);
        }
    }

    public void clipPath(Path path) {
        if (this.canvas != null && path != null) {
            this.canvas.clipPath(path, ClipMode.INTERSECT, true);
        }
    }

    public void scissor(float x, float y, float w, float h) {
        if (this.canvas != null) {
            this.canvas.save();
            this.scissorSaveCount++;
            this.canvas.clipRect(Rect.makeXYWH(x, y, w, h), true);
        }
    }

    public void drawScissor(float x, float y, float w, float h) {
        scissor(x, y, w, h);
    }

    public void resetScissor() {
        if (this.canvas != null && this.scissorSaveCount > 0) {
            this.canvas.restore();
            this.scissorSaveCount--;
        }
    }

    public void save() {
        if (this.canvas != null) {
            this.canvas.save();
        }
    }

    public void restore() {
        if (this.canvas != null) {
            this.canvas.restore();
        }
    }

    // ==========================================
    // Album Art & Advanced Vector/SVG Graphics
    // ==========================================

    public void drawGsmtcCover(int dummy, float x, float y, float size, float radius) {
        drawCover(this.gsmtcCoverBytes, x, y, size, size, radius);
    }

    public void drawLocalCover(int dummy, float x, float y, float size, float radius) {
        drawCover(this.localCoverBytes, x, y, size, size, radius);
    }

    public void drawCover(byte[] imageBytes, float x, float y, float w, float h, float radius) {
        if (this.canvas == null) return;
        if (imageBytes != null && imageBytes.length > 0) {
            try (Image img = Image.makeFromEncoded(imageBytes)) {
                this.canvas.save();
                this.canvas.clipRRect(RRect.makeXYWH(x, y, w, h, radius), true);
                this.canvas.drawImageRect(img, Rect.makeXYWH(x, y, w, h));
                this.canvas.restore();
                return;
            } catch (Throwable ignored) {}
        }
        drawCard(x, y, w, h, radius, darkTheme ? 0xFF2A2A38 : 0xFFE0E0EB, 0x00000000, 0f);
        drawCenteredIcon(ICON_MUSIC, x + w / 2f, y + h / 2f, w * 0.4f, darkTheme ? 0xFF555566 : 0xFFAAAAAA);
    }

    public void drawSvg(byte[] svgBytes, float x, float y, float w, float h) {
        if (this.canvas == null || svgBytes == null || svgBytes.length == 0) return;
        try (Data data = Data.makeFromBytes(svgBytes);
             SVGDOM dom = new SVGDOM(data)) {
            dom.setContainerSize(w, h);
            this.canvas.save();
            this.canvas.translate(x, y);
            dom.render(this.canvas);
            this.canvas.restore();
        } catch (Throwable t) {
            MP3PlayerClient.LOGGER.error("[Skija] Error rendering SVG: {}", t.getMessage());
        }
    }

    public void drawBezierCurve(Path path, int color, float strokeWidth) {
        if (this.canvas == null || path == null) return;
        try (Paint paint = new Paint()) {
            paint.setColor(color);
            paint.setMode(PaintMode.STROKE);
            paint.setStrokeWidth(strokeWidth);
            paint.setAntiAlias(true);
            this.canvas.drawPath(path, paint);
        }
    }

    public void updateGsmtcCoverImage(byte[] bytes) {
        this.gsmtcCoverBytes = bytes;
    }

    public void updateLocalCoverImage(byte[] bytes) {
        this.localCoverBytes = bytes;
    }

    public byte[] getGsmtcCoverBytes() {
        return this.gsmtcCoverBytes;
    }

    public byte[] getLocalCoverBytes() {
        return this.localCoverBytes;
    }

    private void destroySurface() {
        if (this.surface != null) {
            try { this.surface.close(); } catch (Throwable ignored) {}
            this.surface = null;
        }
        if (this.nativeImage != null) {
            try { this.nativeImage.close(); } catch (Throwable ignored) {}
            this.nativeImage = null;
        }
        if (this.dynamicTexture != null) {
            try { this.dynamicTexture.close(); } catch (Throwable ignored) {}
            this.dynamicTexture = null;
        }
        this.canvas = null;
    }

    public synchronized void destroy() {
        destroySurface();
        if (this.regularFont != null) {
            try { this.regularFont.close(); } catch (Throwable ignored) {}
        }
        if (this.boldFont != null) {
            try { this.boldFont.close(); } catch (Throwable ignored) {}
        }
        if (this.iconFont != null) {
            try { this.iconFont.close(); } catch (Throwable ignored) {}
        }
        if (this.regularTypeface != null) {
            try { this.regularTypeface.close(); } catch (Throwable ignored) {}
        }
        if (this.boldTypeface != null) {
            try { this.boldTypeface.close(); } catch (Throwable ignored) {}
        }
        if (this.iconTypeface != null) {
            try { this.iconTypeface.close(); } catch (Throwable ignored) {}
        }
        this.available = false;
    }
}
