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
    public static final Identifier SKIJA_HUD_TEXTURE_ID = Identifier.fromNamespaceAndPath("mp3player", "skija_hud");

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
    private Canvas uiCanvas;

    private int hudBufferWidth = 0;
    private int hudBufferHeight = 0;
    private NativeImage hudNativeImage;
    private DynamicTexture hudDynamicTexture;
    private Surface hudSurface;
    private Canvas hudCanvas;

    private Canvas canvas;
    private int scissorSaveCount = 0;

    private Typeface regularTypeface;
    private Typeface boldTypeface;
    private Typeface iconTypeface;
    private Font regularFont;
    private Font boldFont;
    private Font iconFont;

    // Reusable Paint instances to eliminate per-frame native JNI allocations
    private final Paint fillPaint = new Paint();
    private final Paint strokePaint = new Paint();
    private final Paint textPaint = new Paint();
    private final Paint shadowPaint = new Paint();
    private final Paint imagePaint = new Paint();

    // Caches to avoid recreating fonts every frame
    private final java.util.Map<Integer, Font> regularFontCache = new java.util.HashMap<>();
    private final java.util.Map<Integer, Font> boldFontCache = new java.util.HashMap<>();
    private final java.util.Map<Integer, Font> iconFontCache = new java.util.HashMap<>();

    private volatile byte[] gsmtcCoverBytes;
    private volatile byte[] localCoverBytes;
    private volatile Image gsmtcCoverImage;
    private volatile Image localCoverImage;

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
            this.fillPaint.setAntiAlias(true);
            this.strokePaint.setAntiAlias(true);
            this.strokePaint.setMode(PaintMode.STROKE);
            this.shadowPaint.setAntiAlias(true);
            this.shadowPaint.setMode(PaintMode.FILL);
            this.textPaint.setAntiAlias(true);
            this.imagePaint.setAntiAlias(true);
            loadFonts();
            this.available = true;
            MP3PlayerClient.LOGGER.info("[Skija] Skija vector engine initialized successfully!");
        } catch (Throwable t) {
            MP3PlayerClient.LOGGER.error("[Skija] Failed to initialize Skija: {}", t.getMessage(), t);
            this.available = false;
        }
    }

    private Font getFont(Typeface tf, float fontSize, boolean bold, boolean icon) {
        if (tf == null) tf = this.regularTypeface;
        int key = Math.round(fontSize * 10f);
        java.util.Map<Integer, Font> cache = icon ? iconFontCache : (bold ? boldFontCache : regularFontCache);
        Font f = cache.get(key);
        if (f == null) {
            f = new Font(tf, fontSize);
            f.setSubpixel(true);
            f.setEdging(FontEdging.SUBPIXEL_ANTI_ALIAS);
            cache.put(key, f);
        }
        return f;
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
            this.regularTypeface = fontMgr.matchFamilyStyle("Arial", FontStyle.NORMAL);
            if (this.regularTypeface != null) {
                this.regularFont = new Font(this.regularTypeface, 12f);
            }
        }
        if (this.boldFont == null) {
            this.boldTypeface = fontMgr.matchFamilyStyle("Arial", FontStyle.BOLD);
            if (this.boldTypeface != null) {
                this.boldFont = new Font(this.boldTypeface, 12f);
            } else if (this.regularTypeface != null) {
                this.boldTypeface = this.regularTypeface;
                this.boldFont = this.regularFont;
            }
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

    public synchronized void beginFrame(int width, int height, float scale, float offsetX, float offsetY) {
        if (!this.available || width <= 0 || height <= 0) {
            return;
        }

        this.scissorSaveCount = 0;
        float actualScale = Math.max(1.0f, scale);
        int physicalWidth = Math.max(1, (int) Math.ceil((float) width * actualScale));
        int physicalHeight = Math.max(1, (int) Math.ceil((float) height * actualScale));

        if (this.surface == null || this.bufferWidth != physicalWidth || this.bufferHeight != physicalHeight) {
            destroySurface();
            this.bufferWidth = physicalWidth;
            this.bufferHeight = physicalHeight;

            try {
                this.nativeImage = new NativeImage(NativeImage.Format.RGBA, physicalWidth, physicalHeight, false);
                ImageInfo info = new ImageInfo(physicalWidth, physicalHeight, ColorType.RGBA_8888, ColorAlphaType.PREMUL);
                this.surface = Surface.wrapPixels(info, this.nativeImage.getPointer(), (long) physicalWidth * 4);
                this.uiCanvas = this.surface.getCanvas();

                this.dynamicTexture = new DynamicTexture(() -> "skija_ui", this.nativeImage);
                Minecraft.getInstance().getTextureManager().register(SKIJA_TEXTURE_ID, this.dynamicTexture);
            } catch (Throwable t) {
                MP3PlayerClient.LOGGER.error("[Skija] Error allocating surface: {}", t.getMessage());
                this.uiCanvas = null;
                this.canvas = null;
                return;
            }
        }

        this.canvas = this.uiCanvas;
        if (this.canvas != null) {
            this.canvas.clear(0x00000000);
            this.canvas.resetMatrix();
            this.canvas.scale(actualScale, actualScale);
            if (offsetX != 0f || offsetY != 0f) {
                this.canvas.translate(offsetX, offsetY);
            }
            this.canvas.save();
        }
    }

    public synchronized void beginFrame(int width, int height, float scale) {
        beginFrame(width, height, scale, 0f, 0f);
    }

    public synchronized void beginFrame(int width, int height) {
        float scale = 1.0f;
        try {
            scale = (float) Minecraft.getInstance().getWindow().getGuiScale();
        } catch (Throwable ignored) {}
        beginFrame(width, height, scale);
    }

    public synchronized void endFrame() {
        if (this.surface == null || this.uiCanvas == null || this.dynamicTexture == null) {
            return;
        }
        try {
            while (this.scissorSaveCount > 0) {
                this.uiCanvas.restore();
                this.scissorSaveCount--;
            }
            this.uiCanvas.restore();
            this.dynamicTexture.upload();
        } catch (Throwable t) {
            MP3PlayerClient.LOGGER.error("[Skija] Error ending frame: {}", t.getMessage());
        }
    }

    public void renderToContext(GuiGraphicsExtractor ctx, int screenX, int screenY, int width, int height) {
        if (!this.available || this.dynamicTexture == null) {
            return;
        }
        ctx.blit(SKIJA_TEXTURE_ID, screenX, screenY, screenX + width, screenY + height, 0.0f, 1.0f, 0.0f, 1.0f);
    }

    public synchronized void beginHudFrame(int width, int height, float scale) {
        if (!this.available || width <= 0 || height <= 0) {
            return;
        }

        this.scissorSaveCount = 0;
        float actualScale = Math.max(1.0f, scale);
        int physicalWidth = Math.max(1, (int) Math.ceil((float) width * actualScale));
        int physicalHeight = Math.max(1, (int) Math.ceil((float) height * actualScale));

        if (this.hudSurface == null || this.hudBufferWidth != physicalWidth || this.hudBufferHeight != physicalHeight) {
            destroyHudSurface();
            this.hudBufferWidth = physicalWidth;
            this.hudBufferHeight = physicalHeight;

            try {
                this.hudNativeImage = new NativeImage(NativeImage.Format.RGBA, physicalWidth, physicalHeight, false);
                ImageInfo info = new ImageInfo(physicalWidth, physicalHeight, ColorType.RGBA_8888, ColorAlphaType.PREMUL);
                this.hudSurface = Surface.wrapPixels(info, this.hudNativeImage.getPointer(), (long) physicalWidth * 4);
                this.hudCanvas = this.hudSurface.getCanvas();

                this.hudDynamicTexture = new DynamicTexture(() -> "skija_hud", this.hudNativeImage);
                Minecraft.getInstance().getTextureManager().register(SKIJA_HUD_TEXTURE_ID, this.hudDynamicTexture);
            } catch (Throwable t) {
                MP3PlayerClient.LOGGER.error("[Skija] Error allocating HUD surface: {}", t.getMessage());
                this.hudCanvas = null;
                this.canvas = null;
                return;
            }
        }

        this.canvas = this.hudCanvas;
        if (this.canvas != null) {
            this.canvas.clear(0x00000000);
            this.canvas.resetMatrix();
            this.canvas.scale(actualScale, actualScale);
            this.canvas.save();
        }
    }

    public synchronized void beginHudFrame(int width, int height) {
        float scale = 1.0f;
        try {
            scale = (float) Minecraft.getInstance().getWindow().getGuiScale();
        } catch (Throwable ignored) {}
        beginHudFrame(width, height, scale);
    }

    public synchronized void endHudFrame() {
        if (this.hudSurface == null || this.hudCanvas == null || this.hudDynamicTexture == null) {
            return;
        }
        try {
            while (this.scissorSaveCount > 0) {
                this.hudCanvas.restore();
                this.scissorSaveCount--;
            }
            this.hudCanvas.restore();
            this.hudDynamicTexture.upload();
        } catch (Throwable t) {
            MP3PlayerClient.LOGGER.error("[Skija] Error ending HUD frame: {}", t.getMessage());
        }
    }

    public void renderHudToContext(GuiGraphicsExtractor ctx, int screenX, int screenY, int width, int height) {
        if (!this.available || this.hudDynamicTexture == null) {
            return;
        }
        ctx.blit(SKIJA_HUD_TEXTURE_ID, screenX, screenY, screenX + width, screenY + height, 0.0f, 1.0f, 0.0f, 1.0f);
    }

    public Canvas getCanvas() {
        return this.canvas;
    }

    // ==========================================
    // High-level Vector Drawing Utilities
    // ==========================================

    public void drawRoundedRect(float x, float y, float w, float h, float radius, int color) {
        if (this.canvas == null) return;
        fillPaint.setColor(color);
        this.canvas.drawRRect(RRect.makeXYWH(x, y, w, h, radius), fillPaint);
    }

    public void drawRoundedRectOutline(float x, float y, float w, float h, float radius, float strokeWidth, int color) {
        if (this.canvas == null || strokeWidth <= 0) return;
        strokePaint.setColor(color);
        strokePaint.setStrokeWidth(strokeWidth);
        float off = strokeWidth / 2f;
        this.canvas.drawRRect(RRect.makeXYWH(x + off, y + off, w - strokeWidth, h - strokeWidth, Math.max(0, radius - off)), strokePaint);
    }

    public void drawRoundedRectStroke(float x, float y, float w, float h, float radius, int fillColor, int strokeColor, float strokeWidth) {
        if (this.canvas == null) return;
        fillPaint.setColor(fillColor);
        this.canvas.drawRRect(RRect.makeXYWH(x, y, w, h, radius), fillPaint);

        if (strokeColor != 0 && strokeWidth > 0) {
            strokePaint.setColor(strokeColor);
            strokePaint.setStrokeWidth(strokeWidth);
            float off = strokeWidth / 2f;
            this.canvas.drawRRect(RRect.makeXYWH(x + off, y + off, w - strokeWidth, h - strokeWidth, Math.max(0, radius - off)), strokePaint);
        }
    }

    public void drawCard(float x, float y, float w, float h, float radius, int bgColor, int borderColor, float shadowBlur) {
        if (this.canvas == null) return;
        if (shadowBlur > 0) {
            drawDropShadow(x, y + 2, w, h, radius, shadowBlur, 0f, 0x44000000);
        }
        drawRoundedRectStroke(x, y, w, h, radius, bgColor, borderColor, 1.0f);
    }

    public void drawDropShadow(float x, float y, float w, float h, float radius, float blur, float spread, int color) {
        if (this.canvas == null || blur <= 0f || ((color >>> 24) == 0)) return;

        int alpha = (color >>> 24) & 0xFF;
        int rgb = color & 0x00FFFFFF;

        // Ultra-fast multi-layer analytic soft shadow without CPU Gaussian convolution.
        // Replaces CPU software MaskFilter blur which was taking ~150ms per frame.
        float totalSpread = spread + blur * 0.7f;
        float yOffset = Math.max(1.0f, blur * 0.18f);

        // Layer 4 (Outer ambient halo - soft 12% opacity)
        int a4 = (int) (alpha * 0.12f);
        if (a4 > 0) {
            shadowPaint.setColor((a4 << 24) | rgb);
            float ext = totalSpread;
            float r = Math.max(0f, radius + ext * 0.75f);
            this.canvas.drawRRect(RRect.makeXYWH(x - ext, y - ext + yOffset, w + ext * 2f, h + ext * 2f, r), shadowPaint);
        }

        // Layer 3 (Penumbra - 22% opacity)
        int a3 = (int) (alpha * 0.22f);
        if (a3 > 0) {
            shadowPaint.setColor((a3 << 24) | rgb);
            float ext = totalSpread * 0.65f;
            float r = Math.max(0f, radius + ext * 0.55f);
            this.canvas.drawRRect(RRect.makeXYWH(x - ext, y - ext + yOffset, w + ext * 2f, h + ext * 2f, r), shadowPaint);
        }

        // Layer 2 (Umbra - 36% opacity)
        int a2 = (int) (alpha * 0.36f);
        if (a2 > 0) {
            shadowPaint.setColor((a2 << 24) | rgb);
            float ext = totalSpread * 0.35f;
            float r = Math.max(0f, radius + ext * 0.35f);
            this.canvas.drawRRect(RRect.makeXYWH(x - ext, y - ext + yOffset, w + ext * 2f, h + ext * 2f, r), shadowPaint);
        }

        // Layer 1 (Contact core - 52% opacity)
        int a1 = (int) (alpha * 0.52f);
        if (a1 > 0) {
            shadowPaint.setColor((a1 << 24) | rgb);
            float ext = totalSpread * 0.12f;
            float r = Math.max(0f, radius + ext * 0.15f);
            this.canvas.drawRRect(RRect.makeXYWH(x - ext, y - ext + yOffset, w + ext * 2f, h + ext * 2f, r), shadowPaint);
        }
    }

    public void drawGradientRect(float x, float y, float w, float h, float radius, int topColor, int bottomColor) {
        if (this.canvas == null) return;
        try (Shader shader = Shader.makeLinearGradient(x, y, x, y + h, new int[]{topColor, bottomColor})) {
            fillPaint.setShader(shader);
            this.canvas.drawRRect(RRect.makeXYWH(x, y, w, h, radius), fillPaint);
            fillPaint.setShader(null);
        }
    }

    public void drawCircle(float cx, float cy, float radius, int color) {
        if (this.canvas == null) return;
        fillPaint.setColor(color);
        this.canvas.drawCircle(cx, cy, radius, fillPaint);
    }

    public void drawCircleOutline(float cx, float cy, float radius, float strokeWidth, int color) {
        if (this.canvas == null || strokeWidth <= 0) return;
        strokePaint.setColor(color);
        strokePaint.setStrokeWidth(strokeWidth);
        this.canvas.drawCircle(cx, cy, radius, strokePaint);
    }

    public void drawCircleStroke(float cx, float cy, float radius, int fillColor, int strokeColor, float strokeWidth) {
        if (this.canvas == null) return;
        fillPaint.setColor(fillColor);
        this.canvas.drawCircle(cx, cy, radius, fillPaint);

        if (strokeColor != 0 && strokeWidth > 0) {
            strokePaint.setColor(strokeColor);
            strokePaint.setStrokeWidth(strokeWidth);
            this.canvas.drawCircle(cx, cy, radius - strokeWidth / 2f, strokePaint);
        }
    }

    public void drawTab(float x, float y, float w, float h, String text, boolean isSelected, boolean isHovered, int accentColor) {
        if (this.canvas == null) return;
        drawTabBase(x, y, w, h, isSelected, isHovered, accentColor);
        int textCol = isSelected ? accentColor : (isHovered ? (this.darkTheme ? 0xFFFFFFFF : 0xFF0F1416) : (this.darkTheme ? 0xFF9E9EB8 : 0xFF646E7B));
        drawText(text, x + w * 0.5f, y + h * 0.5f, 12.0f, textCol, isSelected, ALIGN_CENTER);
    }

    public void drawTabBase(float x, float y, float w, float h, boolean isSelected, boolean isHovered, int accentColor) {
        if (this.canvas == null) return;
        float radius = 7.0f;
        int bg = 0;
        if (this.darkTheme) {
            bg = isSelected ? 0xFF28283C : (isHovered ? 0xFF202030 : 0);
        } else {
            bg = isSelected ? 0xFFFFFFFF : (isHovered ? 0xFFE2E6F0 : 0);
        }
        if (bg != 0) {
            drawRoundedRect(x, y, w, h, radius, bg);
            if (!this.darkTheme && isSelected) {
                drawRoundedRectOutline(x, y, w, h, radius, 1.0f, 0x33CCCCCC);
            }
        }
        if (isSelected) {
            drawRoundedRect(x + 4.0f, y + h - 2.5f, w - 8.0f, 2.5f, 1.5f, accentColor);
            drawDropShadow(x + 4.0f, y + h - 2.5f, w - 8.0f, 2.5f, 1.5f, 6.0f, 1.0f, accentColor & 0x77FFFFFF);
        }
    }

    public void drawButtonBase(float x, float y, float w, float h, boolean isHovered, boolean isPressed, int accentColor) {
        if (this.canvas == null) return;
        float radius = Math.min(h * 0.5f, 7.0f);
        int bg;
        int outline;
        if (this.darkTheme) {
            bg = isPressed ? 0xFF14141F : (isHovered ? 0xFF2E2E42 : 0xFF20202E);
            outline = isHovered && !isPressed ? accentColor : 0x33444466;
        } else {
            bg = isPressed ? 0xFFCBCBD5 : (isHovered ? 0xFFE2E6F0 : 0xFFF1F3F9);
            outline = isHovered && !isPressed ? accentColor : 0x44CCCCCC;
        }
        drawRoundedRect(x, y, w, h, radius, bg);
        drawRoundedRectOutline(x, y, w, h, radius, 1.0f, outline);
        if (isHovered && !isPressed) {
            drawDropShadow(x, y, w, h, radius, 6.0f, 1.0f, accentColor & 0x44FFFFFF);
        }
    }

    public void drawButton(float x, float y, float w, float h, String text, boolean isHovered, boolean isPressed, int accentColor, int textColor) {
        if (this.canvas == null) return;
        drawButtonBase(x, y, w, h, isHovered, isPressed, accentColor);
        int textCol = isHovered ? (accentColor | 0xFF000000) : (this.darkTheme ? textColor : 0xFF0F1416);
        drawText(text, x + w * 0.5f, y + h * 0.5f + 1.0f, h * 0.55f, textCol, isHovered, ALIGN_CENTER);
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

    public void drawProgressBar(float x, float y, float w, float h, float progress, boolean isHovered, int accentColor) {
        if (this.canvas == null) return;
        progress = Math.max(0.0f, Math.min(1.0f, progress));
        float barH = isHovered ? h + 2.0f : h;
        float curY = y - (barH - h) * 0.5f;
        int trackBg = this.darkTheme ? 0x44FFFFFF : 0xFFCBCBD5;
        drawRoundedRect(x, curY, w, barH, barH * 0.5f, trackBg);
        if (progress > 0.001f) {
            float fillW = w * progress;
            drawRoundedRect(x, curY, fillW, barH, barH * 0.5f, accentColor);
            if (isHovered) {
                float knobX = x + fillW;
                float knobY = curY + barH * 0.5f;
                drawCircle(knobX, knobY, barH * 1.5f, 0xFFFFFFFF);
                drawDropShadow(knobX - 4.0f, knobY - 4.0f, 8.0f, 8.0f, 4.0f, 8.0f, 2.0f, accentColor);
            }
        }
    }

    public void drawText(String text, float x, float y, float fontSize, int color, boolean bold) {
        drawText(text, x, y, fontSize, color, bold, ALIGN_LEFT);
    }

    public void drawText(String text, float x, float y, float fontSize, int color, boolean bold, int align) {
        if (this.canvas == null || text == null || text.isEmpty()) return;
        Font font = getFont(bold ? this.boldTypeface : this.regularTypeface, fontSize, bold, false);
        textPaint.setColor(color);
        Rect bounds = font.measureText(text, textPaint);
        float baselineY = y - (bounds.getTop() + bounds.getBottom()) / 2f;
        float startX;
        if (align == ALIGN_CENTER) {
            startX = x - (bounds.getLeft() + bounds.getRight()) / 2f;
        } else if (align == ALIGN_RIGHT) {
            startX = x - bounds.getRight();
        } else {
            startX = x - bounds.getLeft();
        }
        this.canvas.drawString(text, startX, baselineY, font, textPaint);
    }

    public void drawCenteredText(String text, float cx, float y, float fontSize, int color, boolean bold) {
        drawText(text, cx, y, fontSize, color, bold, ALIGN_CENTER);
    }

    public void drawBoundedText(String text, float x, float y, float size, int color, boolean bold, float maxW) {
        if (this.canvas == null || text == null || text.isEmpty()) return;
        float tw = this.getTextWidth(text, size, bold);
        if (tw <= maxW) {
            this.drawText(text, x, y, size, color, bold, ALIGN_LEFT);
            return;
        }
        int targetLen = (int) Math.max(1, text.length() * (maxW / tw));
        String truncated = text.substring(0, Math.min(text.length(), targetLen));
        while (truncated.length() > 1 && this.getTextWidth(truncated + "…", size, bold) > maxW) {
            truncated = truncated.substring(0, truncated.length() - 1);
        }
        this.drawText(truncated + "…", x, y, size, color, bold, ALIGN_LEFT);
    }

    public float getTextWidth(String text, float fontSize, boolean bold) {
        if (text == null || text.isEmpty()) return 0f;
        Font font = getFont(bold ? this.boldTypeface : this.regularTypeface, fontSize, bold, false);
        return font.measureTextWidth(text, textPaint);
    }

    // ==========================================
    // Vector Icon Drawing
    // ==========================================

    public void drawIcon(String glyph, float x, float y, float size, int color) {
        if (this.canvas == null || glyph == null || glyph.isEmpty()) return;
        Font font = getFont(this.iconTypeface != null ? this.iconTypeface : this.regularTypeface, size, false, true);
        textPaint.setColor(color);
        this.canvas.drawString(glyph, x, y, font, textPaint);
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
        Font font = getFont(this.iconTypeface != null ? this.iconTypeface : this.regularTypeface, size, false, true);
        textPaint.setColor(color);
        Rect bounds = font.measureText(glyph, textPaint);
        float x = cx - (bounds.getLeft() + bounds.getRight()) / 2f;
        float y = cy - (bounds.getTop() + bounds.getBottom()) / 2f;
        this.canvas.drawString(glyph, x, y, font, textPaint);
    }

    public void drawPlayIcon(float cx, float cy, float radius, int color) {
        if (this.canvas == null) return;
        try (PathBuilder pb = new PathBuilder()) {
            float off = radius * 0.1f;
            pb.moveTo(cx - radius * 0.45f + off, cy - radius * 0.6f);
            pb.lineTo(cx + radius * 0.65f + off, cy);
            pb.lineTo(cx - radius * 0.45f + off, cy + radius * 0.6f);
            pb.closePath();
            try (Path path = pb.build()) {
                fillPaint.setColor(color);
                this.canvas.drawPath(path, fillPaint);
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
            try (Path path = pb.build()) {
                fillPaint.setColor(color);
                this.canvas.drawPath(path, fillPaint);
            }
        }
        float barW = radius * 0.25f;
        float barH = radius * 1.2f;
        float barX = forward ? cx + radius * 0.15f : cx - radius * 0.15f - barW;
        drawRoundedRect(barX, cy - barH / 2f, barW, barH, 1.5f, color);
    }

    public void drawPlayPauseButton(float cx, float cy, float radius, boolean isPlaying, boolean isHovered, int accentColor) {
        if (this.canvas == null) return;
        drawDropShadow(cx - radius, cy - radius, radius * 2.0f, radius * 2.0f, radius, isHovered ? 12.0f : 6.0f, 1.0f, accentColor & 0x66FFFFFF);
        int col = isHovered ? (accentColor | 0x222222) : accentColor;
        drawCircle(cx, cy, radius, col);
        float iconSize = radius * 1.55f;
        float offX = isPlaying ? 0.0f : radius * 0.08f;
        drawCenteredIcon(isPlaying ? ICON_PAUSE : ICON_PLAY, cx + offX, cy, iconSize, 0xFFFFFFFF);
    }

    public void drawMiniPlayIcon(float cx, float cy, float size, int color) {
        drawCenteredIcon(ICON_PLAY, cx + 0.5f, cy + 0.5f, size * 2.3f, color);
    }

    public void drawMiniPauseIcon(float cx, float cy, float size, int color) {
        drawCenteredIcon(ICON_PAUSE, cx, cy + 0.5f, size * 2.3f, color);
    }

    public void drawSkipButton(float cx, float cy, float radius, boolean isForward, boolean isHovered) {
        int iconCol = isHovered ? (this.darkTheme ? 0xFFFFFFFF : 0xFF0F1416) : (this.darkTheme ? 0xFFA0A5B4 : 0xFF646E7B);
        drawCenteredIcon(isForward ? ICON_SKIP_NEXT : ICON_SKIP_PREV, cx, cy, radius * 1.6f, iconCol);
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
            try (Path path = pb.build()) {
                if (filled) {
                    fillPaint.setColor(color);
                    this.canvas.drawPath(path, fillPaint);
                } else {
                    strokePaint.setColor(color);
                    strokePaint.setStrokeWidth(1.5f);
                    this.canvas.drawPath(path, strokePaint);
                }
            }
        }
    }

    public void drawGearIcon(float cx, float cy, float radius, int color) {
        drawCenteredIcon(ICON_SETTINGS, cx, cy, radius * 2.2f, color);
    }

    public void drawMoonSunIcon(float cx, float cy, float radius, boolean dark, int color) {
        drawCenteredIcon(dark ? ICON_DARK_MODE : ICON_LIGHT_MODE, cx, cy, radius * 2.2f, color);
    }

    public void drawSpeakerIcon(float cx, float cy, float radius, boolean muted, int color) {
        drawCenteredIcon(muted ? ICON_VOLUME_OFF : ICON_VOLUME_UP, cx, cy, radius * 2.0f, muted ? 0xFFFF5555 : color);
    }

    public void drawRefreshIcon(float cx, float cy, float radius, int color) {
        drawCenteredIcon(ICON_REFRESH, cx, cy, radius * 2.3f, color);
    }

    public void drawFolderIcon(float cx, float cy, float w, float h, int color) {
        drawCenteredIcon(ICON_FOLDER, cx, cy, Math.max(w, h) * 1.25f, color);
    }

    public void drawCrossIcon(float cx, float cy, float size, int color) {
        drawCenteredIcon(ICON_CLOSE, cx, cy, size * 1.55f, color);
    }

    public void drawDotsIcon(float cx, float cy, float dotRadius, int color) {
        drawCenteredIcon(ICON_MORE_VERT, cx, cy, 13.0f, color);
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

    public void drawBadge(float x, float y, float h, String text, int badgeColor) {
        if (this.canvas == null || text == null || text.isEmpty()) return;
        float textW = getTextWidth(text, h * 0.65f, true);
        float pad = 6.0f;
        float w = textW + pad * 2.0f;
        float radius = h * 0.5f;
        drawRoundedRect(x, y, w, h, radius, (badgeColor & 0x00FFFFFF) | 0x28000000);
        drawRoundedRectOutline(x, y, w, h, radius, 1.0f, badgeColor);
        drawDropShadow(x, y, w, h, radius, 6.0f, 0.5f, badgeColor & 0x44FFFFFF);
        drawText(text, x + w * 0.5f, y + h * 0.5f + 0.5f, h * 0.65f, badgeColor, true, ALIGN_CENTER);
    }

    public void drawSourceIcon(float cx, float cy, float radius, String sourceApp) {
        if (this.canvas == null) return;
        String s = sourceApp == null ? "" : sourceApp.toLowerCase();
        if (s.contains("spotify") || s.contains("спотифай")) {
            drawCircle(cx, cy, radius, 0xFF1DB954);
            drawDropShadow(cx - radius, cy - radius, radius * 2.0f, radius * 2.0f, radius, 4.0f, 0.5f, 0x441DB954);
            for (int i = 0; i < 3; ++i) {
                float r = radius * (0.35f + (float)i * 0.22f);
                float yOff = cy - radius * 0.1f + (float)i * radius * 0.22f;
                drawCircleOutline(cx, yOff, r, 0.8f, 0xFFFFFFFF);
            }
        } else if (s.contains("yandex") || s.contains("яндекс")) {
            drawCircle(cx, cy, radius, 0xFFFFCC00);
            drawDropShadow(cx - radius, cy - radius, radius * 2.0f, radius * 2.0f, radius, 4.0f, 0.5f, 0x44FFCC00);
            drawCenteredIcon(ICON_MUSIC, cx, cy, radius * 1.3f, 0xFF000000);
        } else {
            drawCircle(cx, cy, radius, 0xFF0078D7);
            drawDropShadow(cx - radius, cy - radius, radius * 2.0f, radius * 2.0f, radius, 4.0f, 0.5f, 0x440078D7);
            drawCircleOutline(cx, cy, radius * 0.6f, 0.8f, 0xFFFFFFFF);
        }
    }

    public void drawEqualizerBars(float x, float y, float w, float h, int color, long timeMs) {
        if (this.canvas == null) return;
        int bars = 4;
        float barW = (w - (bars - 1) * 2f) / bars;
        for (int i = 0; i < bars; i++) {
            double wave = Math.sin((double)timeMs * 0.008 + (double)i * 1.3);
            float pct = (float)(0.25 + 0.75 * Math.abs(wave));
            float barH = h * pct;
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
        Image img = null;
        if (imageBytes != null && imageBytes.length > 0) {
            if (imageBytes == this.gsmtcCoverBytes) {
                if (this.gsmtcCoverImage == null) {
                    try { this.gsmtcCoverImage = Image.makeFromEncoded(imageBytes); } catch (Throwable ignored) {}
                }
                img = this.gsmtcCoverImage;
            } else if (imageBytes == this.localCoverBytes) {
                if (this.localCoverImage == null) {
                    try { this.localCoverImage = Image.makeFromEncoded(imageBytes); } catch (Throwable ignored) {}
                }
                img = this.localCoverImage;
            } else {
                try { img = Image.makeFromEncoded(imageBytes); } catch (Throwable ignored) {}
            }
        }
        if (img != null) {
            try {
                this.canvas.save();
                this.canvas.clipRRect(RRect.makeXYWH(x, y, w, h, radius), true);
                Rect src = Rect.makeWH(img.getWidth(), img.getHeight());
                Rect dst = Rect.makeXYWH(x, y, w, h);
                this.canvas.drawImageRect(img, src, dst, SamplingMode.LINEAR, null, true);
                this.canvas.restore();
                if (imageBytes != this.gsmtcCoverBytes && imageBytes != this.localCoverBytes) {
                    img.close();
                }
                return;
            } catch (Throwable ignored) {}
        }
        drawCard(x, y, w, h, radius, darkTheme ? 0xFF2A2A38 : 0xFFE0E0EB, 0x00000000, 0f);
        drawCenteredIcon(ICON_MUSIC, x + w / 2f, y + h / 2f, w * 0.45f, darkTheme ? 0xFF555566 : 0xFFAAAAAA);
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
        strokePaint.setColor(color);
        strokePaint.setStrokeWidth(strokeWidth);
        this.canvas.drawPath(path, strokePaint);
    }

    public synchronized void updateGsmtcCoverImage(byte[] bytes) {
        this.gsmtcCoverBytes = bytes;
        if (this.gsmtcCoverImage != null) {
            try { this.gsmtcCoverImage.close(); } catch (Throwable ignored) {}
            this.gsmtcCoverImage = null;
        }
        if (bytes != null && bytes.length > 0) {
            try {
                this.gsmtcCoverImage = Image.makeFromEncoded(bytes);
            } catch (Throwable ignored) {}
        }
    }

    public synchronized void updateLocalCoverImage(byte[] bytes) {
        this.localCoverBytes = bytes;
        if (this.localCoverImage != null) {
            try { this.localCoverImage.close(); } catch (Throwable ignored) {}
            this.localCoverImage = null;
        }
        if (bytes != null && bytes.length > 0) {
            try {
                this.localCoverImage = Image.makeFromEncoded(bytes);
            } catch (Throwable ignored) {}
        }
    }

    public byte[] getGsmtcCoverBytes() {
        return this.gsmtcCoverBytes;
    }

    public byte[] getLocalCoverBytes() {
        return this.localCoverBytes;
    }

    public Image getGsmtcCoverImage() {
        return this.gsmtcCoverImage;
    }

    public Image getLocalCoverImage() {
        return this.localCoverImage;
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
        this.uiCanvas = null;
        this.canvas = null;
    }

    private void destroyHudSurface() {
        if (this.hudSurface != null) {
            try { this.hudSurface.close(); } catch (Throwable ignored) {}
            this.hudSurface = null;
        }
        if (this.hudNativeImage != null) {
            try { this.hudNativeImage.close(); } catch (Throwable ignored) {}
            this.hudNativeImage = null;
        }
        if (this.hudDynamicTexture != null) {
            try { this.hudDynamicTexture.close(); } catch (Throwable ignored) {}
            this.hudDynamicTexture = null;
        }
        this.hudCanvas = null;
        this.canvas = null;
    }

    public synchronized void destroy() {
        destroySurface();
        destroyHudSurface();
        if (this.gsmtcCoverImage != null) {
            try { this.gsmtcCoverImage.close(); } catch (Throwable ignored) {}
            this.gsmtcCoverImage = null;
        }
        if (this.localCoverImage != null) {
            try { this.localCoverImage.close(); } catch (Throwable ignored) {}
            this.localCoverImage = null;
        }
        for (Font f : regularFontCache.values()) {
            try { f.close(); } catch (Throwable ignored) {}
        }
        regularFontCache.clear();
        for (Font f : boldFontCache.values()) {
            try { f.close(); } catch (Throwable ignored) {}
        }
        boldFontCache.clear();
        for (Font f : iconFontCache.values()) {
            try { f.close(); } catch (Throwable ignored) {}
        }
        iconFontCache.clear();

        try { fillPaint.close(); } catch (Throwable ignored) {}
        try { strokePaint.close(); } catch (Throwable ignored) {}
        try { textPaint.close(); } catch (Throwable ignored) {}
        try { shadowPaint.close(); } catch (Throwable ignored) {}
        try { imagePaint.close(); } catch (Throwable ignored) {}

        if (this.regularFont != null) {
            try { this.regularFont.close(); } catch (Throwable ignored) {}
            this.regularFont = null;
        }
        if (this.boldFont != null) {
            try { this.boldFont.close(); } catch (Throwable ignored) {}
            this.boldFont = null;
        }
        if (this.iconFont != null) {
            try { this.iconFont.close(); } catch (Throwable ignored) {}
            this.iconFont = null;
        }
        if (this.regularTypeface != null) {
            try { this.regularTypeface.close(); } catch (Throwable ignored) {}
            this.regularTypeface = null;
        }
        if (this.boldTypeface != null) {
            try { this.boldTypeface.close(); } catch (Throwable ignored) {}
            this.boldTypeface = null;
        }
        if (this.iconTypeface != null) {
            try { this.iconTypeface.close(); } catch (Throwable ignored) {}
            this.iconTypeface = null;
        }
        this.available = false;
    }
}
