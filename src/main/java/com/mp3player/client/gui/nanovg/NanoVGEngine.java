/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.client.MinecraftClient
 *  org.lwjgl.opengl.GL11
 *  org.lwjgl.opengl.GL13
 *  org.lwjgl.opengl.GL15
 *  org.lwjgl.opengl.GL20
 *  org.lwjgl.opengl.GL30
 */
package com.mp3player.client.gui.nanovg;

import com.mojang.blaze3d.systems.RenderSystem;
import java.io.File;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import net.minecraft.client.Minecraft;
import org.lwjgl.BufferUtils;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.nanovg.NanoVGGL3;
import org.lwjgl.opengl.ARBClipControl;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL21;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL33;

public class NanoVGEngine {
    public static final int ALIGN_LEFT = 17;
    public static final int ALIGN_CENTER = 18;
    public static final int ALIGN_RIGHT = 20;
    public static final int ALIGN_TOP_LEFT = 9;
    public static final int ALIGN_BOTTOM_LEFT = 33;
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
    private static NanoVGEngine instance;
    private long vg = 0L;
    private boolean initialized = false;
    private boolean failed = false;
    private int regularFont = -1;
    private int boldFont = -1;
    private int iconFont = -1;
    private boolean darkTheme = true;
    private int gsmtcCoverImage = 0;
    private int localCoverImage = 0;
    private ByteBuffer gsmtcByteBuffer = null;
    private ByteBuffer localByteBuffer = null;
    private volatile byte[] pendingGsmtcCoverBytes = null;
    private volatile boolean pendingGsmtcCoverUpdate = false;
    private volatile byte[] pendingLocalCoverBytes = null;
    private volatile boolean pendingLocalCoverUpdate = false;
    private final NVGColor colorA = NVGColor.create();
    private final NVGColor colorB = NVGColor.create();
    private final NVGPaint paint = NVGPaint.create();
    private ByteBuffer fontBuffer;
    private ByteBuffer boldFontBuffer;
    private ByteBuffer iconFontBuffer;
    private final IntBuffer intBuf = BufferUtils.createIntBuffer(16);
    private final ByteBuffer byteBuf = BufferUtils.createByteBuffer(16);
    private boolean loggedFirstFrame = false;
    private int savedProgram;
    private int savedVao;
    private int savedArrayBuffer;
    private int savedElementArrayBuffer;
    private int savedActiveTexture;
    private int savedTexture2D;
    private boolean savedBlend;
    private int savedBlendSrcRgb;
    private int savedBlendDstRgb;
    private int savedBlendSrcAlpha;
    private int savedBlendDstAlpha;
    private int savedBlendEquationRgb;
    private int savedBlendEquationAlpha;
    private boolean savedDepthTest;
    private boolean savedDepthMask;
    private int savedDepthFunc;
    private boolean savedStencilTest;
    private int savedStencilMask;
    private int savedStencilFunc;
    private int savedStencilRef;
    private int savedStencilValueMask;
    private int savedStencilFail;
    private int savedStencilPassDepthFail;
    private int savedStencilPassDepthPass;
    private int savedDrawFbo;
    private int savedReadFbo;
    private boolean savedCullFace;
    private boolean savedScissorTest;
    private int savedScissorX;
    private int savedScissorY;
    private int savedScissorW;
    private int savedScissorH;
    private int savedViewportX;
    private int savedViewportY;
    private int savedViewportW;
    private int savedViewportH;
    private boolean savedColorMaskR;
    private boolean savedColorMaskG;
    private boolean savedColorMaskB;
    private boolean savedColorMaskA;
    private int helperFbo = 0;
    private int helperRbo = 0;
    private int helperWidth = 0;
    private int helperHeight = 0;
    private int activeTargetFbo = 0;
    private final int[] savedSamplers = new int[8];
    private int savedPixelUnpackBuffer = 0;
    private int savedUnpackAlignment = 4;
    private int savedUnpackRowLength = 0;
    private int savedUnpackSkipPixels = 0;
    private int savedUnpackSkipRows = 0;
    private int savedUbo0 = 0;
    private int savedClipOrigin = 0;
    private int savedClipDepthMode = 0;
    private boolean loggedFirstText = false;

    public void setDarkTheme(boolean dark) {
        this.darkTheme = dark;
    }

    public boolean isDarkTheme() {
        return this.darkTheme;
    }

    public void updateCoverImage(byte[] imageBytes) {
        this.updateGsmtcCoverImage(imageBytes);
    }

    public void updateGsmtcCoverImage(byte[] imageBytes) {
        this.pendingGsmtcCoverBytes = imageBytes;
        this.pendingGsmtcCoverUpdate = true;
    }

    public void updateLocalCoverImage(byte[] imageBytes) {
        this.pendingLocalCoverBytes = imageBytes;
        this.pendingLocalCoverUpdate = true;
    }

    private void applyPendingGsmtcCover(byte[] imageBytes) {
        if (this.vg == 0L) {
            return;
        }
        if (this.gsmtcCoverImage > 0) {
            NanoVG.nvgDeleteImage(this.vg, this.gsmtcCoverImage);
            this.gsmtcCoverImage = 0;
        }
        if (imageBytes != null && imageBytes.length > 0) {
            try {
                this.gsmtcByteBuffer = BufferUtils.createByteBuffer(imageBytes.length);
                this.gsmtcByteBuffer.put(imageBytes).flip();
                this.gsmtcCoverImage = NanoVG.nvgCreateImageMem(this.vg, 0, this.gsmtcByteBuffer);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
    }

    private void applyPendingLocalCover(byte[] imageBytes) {
        if (this.vg == 0L) {
            return;
        }
        if (this.localCoverImage > 0) {
            NanoVG.nvgDeleteImage(this.vg, this.localCoverImage);
            this.localCoverImage = 0;
        }
        if (imageBytes != null && imageBytes.length > 0) {
            try {
                this.localByteBuffer = BufferUtils.createByteBuffer(imageBytes.length);
                this.localByteBuffer.put(imageBytes).flip();
                this.localCoverImage = NanoVG.nvgCreateImageMem(this.vg, 0, this.localByteBuffer);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
    }

    public boolean hasCoverImage() {
        return this.gsmtcCoverImage > 0 || this.localCoverImage > 0;
    }

    public boolean hasGsmtcCover() {
        return this.gsmtcCoverImage > 0;
    }

    public boolean hasLocalCover() {
        return this.localCoverImage > 0;
    }

    public static synchronized NanoVGEngine getInstance() {
        if (instance == null) {
            instance = new NanoVGEngine();
        }
        return instance;
    }

    private NanoVGEngine() {
    }

    public boolean init() {
        if (this.initialized) {
            return true;
        }
        if (this.failed) {
            return false;
        }
        if (!RenderSystem.isOnRenderThread()) {
            return false;
        }
        try {
            com.mp3player.MP3PlayerClient.LOGGER.info("[NanoVG] Initializing NanoVG renderer...");
            boolean hasClipControl = false;
            int initClipOrigin = 0;
            int initClipDepth = 0;
            try {
                hasClipControl = GL.getCapabilities().GL_ARB_clip_control;
                if (hasClipControl) {
                    initClipOrigin = GL11.glGetInteger(ARBClipControl.GL_CLIP_ORIGIN);
                    initClipDepth = GL11.glGetInteger(ARBClipControl.GL_CLIP_DEPTH_MODE);
                    ARBClipControl.glClipControl(ARBClipControl.GL_LOWER_LEFT, ARBClipControl.GL_NEGATIVE_ONE_TO_ONE);
                }
            } catch (Throwable ignored) {}

            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            GL33.glBindSampler(0, 0);
            GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, 0);
            GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
            GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH, 0);
            byte[] bytes;
            InputStream is2;
            try {
                this.vg = NanoVGGL3.nvgCreate(1);
                if (this.vg == 0L) {
                    this.vg = NanoVGGL3.nvgCreate(0);
                }
            } finally {
                if (hasClipControl && initClipOrigin != 0 && initClipDepth != 0) {
                    try {
                        ARBClipControl.glClipControl(initClipOrigin, initClipDepth);
                    } catch (Throwable ignored) {}
                }
            }
            if (this.vg == 0L) {
                com.mp3player.MP3PlayerClient.LOGGER.error("[NanoVG] nvgCreate failed! Returned 0L");
                this.failed = true;
                return false;
            }
            com.mp3player.MP3PlayerClient.LOGGER.info("[NanoVG] Created context vg = " + this.vg);
            try {
                is2 = this.getClass().getResourceAsStream("/assets/mp3player/fonts/font.ttf");
                try {
                    if (is2 != null) {
                        bytes = is2.readAllBytes();
                        this.fontBuffer = BufferUtils.createByteBuffer(bytes.length);
                        this.fontBuffer.put(bytes).flip();
                        this.regularFont = NanoVG.nvgCreateFontMem(this.vg, "CustomFont", this.fontBuffer, false);
                    }
                }
                finally {
                    if (is2 != null) {
                        is2.close();
                    }
                }
            }
            catch (Throwable t1) {
                com.mp3player.MP3PlayerClient.LOGGER.warn("[NanoVG] Could not load bundled font.ttf: " + t1.getMessage());
            }
            try {
                is2 = this.getClass().getResourceAsStream("/assets/mp3player/fonts/font-bold.ttf");
                try {
                    if (is2 != null) {
                        bytes = is2.readAllBytes();
                        this.boldFontBuffer = BufferUtils.createByteBuffer(bytes.length);
                        this.boldFontBuffer.put(bytes).flip();
                        this.boldFont = NanoVG.nvgCreateFontMem(this.vg, "CustomFont-Bold", this.boldFontBuffer, false);
                    }
                }
                finally {
                    if (is2 != null) {
                        is2.close();
                    }
                }
            }
            catch (Throwable is3) {
                com.mp3player.MP3PlayerClient.LOGGER.warn("[NanoVG] Could not load bundled font-bold.ttf: " + is3.getMessage());
            }
            try {
                is2 = this.getClass().getResourceAsStream("/assets/mp3player/fonts/material-icons.ttf");
                try {
                    if (is2 != null) {
                        bytes = is2.readAllBytes();
                        this.iconFontBuffer = BufferUtils.createByteBuffer(bytes.length);
                        this.iconFontBuffer.put(bytes).flip();
                        this.iconFont = NanoVG.nvgCreateFontMem(this.vg, "MaterialIcons", this.iconFontBuffer, false);
                    }
                }
                finally {
                    if (is2 != null) {
                        is2.close();
                    }
                }
            }
            catch (Throwable is4) {
                com.mp3player.MP3PlayerClient.LOGGER.warn("[NanoVG] Could not load bundled material-icons.ttf: " + is4.getMessage());
            }
            if (this.regularFont == -1) {
                this.regularFont = this.loadSystemFont("SegoeUI", "C:/Windows/Fonts/segoeui.ttf");
            }
            if (this.regularFont == -1) {
                this.regularFont = this.loadSystemFont("Arial", "C:/Windows/Fonts/arial.ttf");
            }
            if (this.boldFont == -1) {
                this.boldFont = this.loadSystemFont("SegoeUI-Bold", "C:/Windows/Fonts/segoeuib.ttf");
            }
            if (this.boldFont == -1) {
                this.boldFont = this.loadSystemFont("Arial-Bold", "C:/Windows/Fonts/arialbd.ttf");
            }
            if (this.iconFont == -1) {
                this.iconFont = this.loadSystemFont("SegoeIcons", "C:/Windows/Fonts/SegoeIcons.ttf");
            }
            com.mp3player.MP3PlayerClient.LOGGER.info("[NanoVG] Loaded fonts: regular=" + this.regularFont + ", bold=" + this.boldFont + ", icons=" + this.iconFont);
            this.initialized = true;
            return true;
        }
        catch (Throwable t) {
            com.mp3player.MP3PlayerClient.LOGGER.error("[NanoVG] Exception during NanoVG initialization:", t);
            this.failed = true;
            return false;
        }
    }

    private int loadSystemFont(String name, String path) {
        try {
            File f = new File(path);
            if (f.exists()) {
                return NanoVG.nvgCreateFont(this.vg, name, f.getAbsolutePath());
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return -1;
    }

    public boolean isAvailable() {
        if (!this.initialized && !this.failed && RenderSystem.isOnRenderThread()) {
            this.init();
        }
        return this.initialized && this.vg != 0L;
    }

    private int getTargetFramebuffer() {
        return this.savedDrawFbo;
    }

    public void beginFrame(float width, float height, float pixelRatio) {
        byte[] bytes;
        if (!this.isAvailable() || width <= 0.0f || height <= 0.0f || pixelRatio <= 0.0f) {
            return;
        }
        if (this.pendingGsmtcCoverUpdate) {
            this.pendingGsmtcCoverUpdate = false;
            bytes = this.pendingGsmtcCoverBytes;
            this.pendingGsmtcCoverBytes = null;
            this.applyPendingGsmtcCover(bytes);
        }
        if (this.pendingLocalCoverUpdate) {
            this.pendingLocalCoverUpdate = false;
            bytes = this.pendingLocalCoverBytes;
            this.pendingLocalCoverBytes = null;
            this.applyPendingLocalCover(bytes);
        }
        this.savedDrawFbo = GL11.glGetInteger((int)36006);
        this.savedReadFbo = GL11.glGetInteger((int)36010);
        this.savedProgram = GL11.glGetInteger((int)35725);
        this.savedVao = GL11.glGetInteger((int)34229);
        this.savedArrayBuffer = GL11.glGetInteger((int)34964);
        this.savedElementArrayBuffer = GL11.glGetInteger((int)34965);
        this.savedActiveTexture = GL11.glGetInteger((int)34016);
        this.savedTexture2D = GL11.glGetInteger((int)32873);
        this.savedBlend = GL11.glIsEnabled((int)3042);
        this.savedBlendSrcRgb = GL11.glGetInteger((int)32969);
        this.savedBlendDstRgb = GL11.glGetInteger((int)32968);
        this.savedBlendSrcAlpha = GL11.glGetInteger((int)32971);
        this.savedBlendDstAlpha = GL11.glGetInteger((int)32970);
        this.savedBlendEquationRgb = GL11.glGetInteger((int)32777);
        this.savedBlendEquationAlpha = GL11.glGetInteger((int)34877);
        this.savedDepthTest = GL11.glIsEnabled((int)2929);
        this.savedDepthMask = GL11.glGetBoolean((int)2930);
        this.savedDepthFunc = GL11.glGetInteger((int)2932);
        this.savedStencilTest = GL11.glIsEnabled((int)2960);
        this.savedStencilMask = GL11.glGetInteger((int)2968);
        this.savedStencilFunc = GL11.glGetInteger((int)2962);
        this.savedStencilRef = GL11.glGetInteger((int)2967);
        this.savedStencilValueMask = GL11.glGetInteger((int)2963);
        this.savedStencilFail = GL11.glGetInteger((int)2964);
        this.savedStencilPassDepthFail = GL11.glGetInteger((int)2965);
        this.savedStencilPassDepthPass = GL11.glGetInteger((int)2966);
        this.savedCullFace = GL11.glIsEnabled((int)2884);
        this.savedScissorTest = GL11.glIsEnabled((int)3089);
        this.intBuf.clear();
        GL11.glGetIntegerv((int)3088, (IntBuffer)this.intBuf);
        this.savedScissorX = this.intBuf.get(0);
        this.savedScissorY = this.intBuf.get(1);
        this.savedScissorW = this.intBuf.get(2);
        this.savedScissorH = this.intBuf.get(3);
        this.intBuf.clear();
        GL11.glGetIntegerv((int)2978, (IntBuffer)this.intBuf);
        this.savedViewportX = this.intBuf.get(0);
        this.savedViewportY = this.intBuf.get(1);
        this.savedViewportW = this.intBuf.get(2);
        this.savedViewportH = this.intBuf.get(3);
        this.byteBuf.clear();
        GL11.glGetBooleanv((int)3107, (ByteBuffer)this.byteBuf);
        this.savedColorMaskR = this.byteBuf.get(0) != 0;
        this.savedColorMaskG = this.byteBuf.get(1) != 0;
        this.savedColorMaskB = this.byteBuf.get(2) != 0;
        this.savedColorMaskA = this.byteBuf.get(3) != 0;

        // Unbind samplers on texture units 0..7 so Minecraft's mipmapped samplers don't break NanoVG's non-mipmapped font atlas
        for (int i = 0; i < 8; i++) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + i);
            this.savedSamplers[i] = GL11.glGetInteger(GL33.GL_SAMPLER_BINDING);
            if (this.savedSamplers[i] != 0) {
                GL33.glBindSampler(i, 0);
            }
        }
        GL13.glActiveTexture(GL13.GL_TEXTURE0);

        // Unbind pixel unpack buffer so font glyph uploads use CPU memory pointer
        this.savedPixelUnpackBuffer = GL11.glGetInteger(GL21.GL_PIXEL_UNPACK_BUFFER_BINDING);
        if (this.savedPixelUnpackBuffer != 0) {
            GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, 0);
        }

        // Reset pixel store unpack alignment and strides
        this.savedUnpackAlignment = GL11.glGetInteger(GL11.GL_UNPACK_ALIGNMENT);
        this.savedUnpackRowLength = GL11.glGetInteger(GL11.GL_UNPACK_ROW_LENGTH);
        this.savedUnpackSkipPixels = GL11.glGetInteger(GL11.GL_UNPACK_SKIP_PIXELS);
        this.savedUnpackSkipRows = GL11.glGetInteger(GL11.GL_UNPACK_SKIP_ROWS);
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
        GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH, 0);
        GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_PIXELS, 0);
        GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_ROWS, 0);

        // Save UBO index 0
        this.intBuf.clear();
        GL30.glGetIntegeri_v(GL31.GL_UNIFORM_BUFFER_BINDING, 0, this.intBuf);
        this.savedUbo0 = this.intBuf.get(0);

        // Save and switch clip control for NanoVG
        try {
            if (GL.getCapabilities().GL_ARB_clip_control) {
                this.savedClipOrigin = GL11.glGetInteger(ARBClipControl.GL_CLIP_ORIGIN);
                this.savedClipDepthMode = GL11.glGetInteger(ARBClipControl.GL_CLIP_DEPTH_MODE);
                ARBClipControl.glClipControl(ARBClipControl.GL_LOWER_LEFT, ARBClipControl.GL_NEGATIVE_ONE_TO_ONE);
            }
        } catch (Throwable ignored) {}

        this.activeTargetFbo = this.getTargetFramebuffer();
        if (this.activeTargetFbo != 0 && this.activeTargetFbo != this.savedDrawFbo) {
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, this.activeTargetFbo);
        }

        if (!this.loggedFirstFrame) {
            this.loggedFirstFrame = true;
            com.mp3player.MP3PlayerClient.LOGGER.info("[NanoVG] beginFrame: width=" + width + ", height=" + height + ", scale=" + pixelRatio + ", savedFBO=" + this.savedDrawFbo + ", activeFBO=" + this.activeTargetFbo + ", savedSampler0=" + this.savedSamplers[0] + ", savedUnpackBuf=" + this.savedPixelUnpackBuffer + ", clipOrigin=" + this.savedClipOrigin + ", clipDepth=" + this.savedClipDepthMode);
        }
        NanoVG.nvgBeginFrame(this.vg, width, height, pixelRatio);
    }

    public void endFrame() {
        if (!this.isAvailable()) {
            return;
        }
        NanoVG.nvgEndFrame(this.vg);

        // Restore clip control
        try {
            if (this.savedClipOrigin != 0 && this.savedClipDepthMode != 0) {
                ARBClipControl.glClipControl(this.savedClipOrigin, this.savedClipDepthMode);
            }
        } catch (Throwable ignored) {}

        // Restore UBO index 0
        if (this.savedUbo0 != 0) {
            GL30.glBindBufferBase(GL31.GL_UNIFORM_BUFFER, 0, this.savedUbo0);
        } else {
            GL30.glBindBufferBase(GL31.GL_UNIFORM_BUFFER, 0, 0);
        }

        // Restore pixel unpack parameters
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, this.savedUnpackAlignment);
        GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH, this.savedUnpackRowLength);
        GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_PIXELS, this.savedUnpackSkipPixels);
        GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_ROWS, this.savedUnpackSkipRows);

        // Restore pixel unpack buffer
        if (this.savedPixelUnpackBuffer != 0) {
            GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, this.savedPixelUnpackBuffer);
        }

        // Restore samplers
        for (int i = 0; i < 8; i++) {
            if (this.savedSamplers[i] != 0) {
                GL33.glBindSampler(i, this.savedSamplers[i]);
            }
        }

        GL30.glBindFramebuffer(36009, this.savedDrawFbo);
        GL30.glBindFramebuffer(36008, this.savedReadFbo);
        GL20.glUseProgram((int)this.savedProgram);
        GL30.glBindVertexArray((int)this.savedVao);
        GL15.glBindBuffer((int)34962, (int)this.savedArrayBuffer);
        GL15.glBindBuffer((int)34963, (int)this.savedElementArrayBuffer);
        GL13.glActiveTexture((int)this.savedActiveTexture);
        GL11.glBindTexture((int)3553, (int)this.savedTexture2D);
        if (this.savedBlend) {
            GL11.glEnable((int)3042);
        } else {
            GL11.glDisable((int)3042);
        }
        GL20.glBlendFuncSeparate((int)this.savedBlendSrcRgb, (int)this.savedBlendDstRgb, (int)this.savedBlendSrcAlpha, (int)this.savedBlendDstAlpha);
        GL20.glBlendEquationSeparate((int)this.savedBlendEquationRgb, (int)this.savedBlendEquationAlpha);
        if (this.savedDepthTest) {
            GL11.glEnable((int)2929);
        } else {
            GL11.glDisable((int)2929);
        }
        GL11.glDepthMask((boolean)this.savedDepthMask);
        GL11.glDepthFunc((int)this.savedDepthFunc);
        if (this.savedStencilTest) {
            GL11.glEnable((int)2960);
        } else {
            GL11.glDisable((int)2960);
        }
        GL11.glStencilMask((int)this.savedStencilMask);
        GL11.glStencilFunc((int)this.savedStencilFunc, (int)this.savedStencilRef, (int)this.savedStencilValueMask);
        GL11.glStencilOp((int)this.savedStencilFail, (int)this.savedStencilPassDepthFail, (int)this.savedStencilPassDepthPass);
        if (this.savedCullFace) {
            GL11.glEnable((int)2884);
        } else {
            GL11.glDisable((int)2884);
        }
        if (this.savedScissorTest) {
            GL11.glEnable((int)3089);
        } else {
            GL11.glDisable((int)3089);
        }
        GL11.glScissor((int)this.savedScissorX, (int)this.savedScissorY, (int)this.savedScissorW, (int)this.savedScissorH);
        GL11.glViewport((int)this.savedViewportX, (int)this.savedViewportY, (int)this.savedViewportW, (int)this.savedViewportH);
        GL11.glColorMask((boolean)this.savedColorMaskR, (boolean)this.savedColorMaskG, (boolean)this.savedColorMaskB, (boolean)this.savedColorMaskA);
    }

    private NVGColor rgba(int color, NVGColor out) {
        float a = (float)(color >> 24 & 0xFF) / 255.0f;
        float r = (float)(color >> 16 & 0xFF) / 255.0f;
        float g = (float)(color >> 8 & 0xFF) / 255.0f;
        float b = (float)(color & 0xFF) / 255.0f;
        return NanoVG.nvgRGBAf(r, g, b, a, out);
    }

    public void drawRoundedRect(float x, float y, float w, float h, float radius, int color) {
        if (!this.isAvailable()) {
            return;
        }
        NanoVG.nvgBeginPath(this.vg);
        NanoVG.nvgRoundedRect(this.vg, x, y, w, h, radius);
        NanoVG.nvgFillColor(this.vg, this.rgba(color, this.colorA));
        NanoVG.nvgFill(this.vg);
    }

    public void drawRoundedRectOutline(float x, float y, float w, float h, float radius, float strokeWidth, int color) {
        if (!this.isAvailable()) {
            return;
        }
        NanoVG.nvgBeginPath(this.vg);
        NanoVG.nvgRoundedRect(this.vg, x + strokeWidth * 0.5f, y + strokeWidth * 0.5f, w - strokeWidth, h - strokeWidth, radius);
        NanoVG.nvgStrokeWidth(this.vg, strokeWidth);
        NanoVG.nvgStrokeColor(this.vg, this.rgba(color, this.colorA));
        NanoVG.nvgStroke(this.vg);
    }

    public void drawRoundedGradient(float x, float y, float w, float h, float radius, int colTop, int colBot) {
        if (!this.isAvailable()) {
            return;
        }
        NanoVG.nvgBeginPath(this.vg);
        NanoVG.nvgRoundedRect(this.vg, x, y, w, h, radius);
        NanoVG.nvgLinearGradient(this.vg, x, y, x, y + h, this.rgba(colTop, this.colorA), this.rgba(colBot, this.colorB), this.paint);
        NanoVG.nvgFillPaint(this.vg, this.paint);
        NanoVG.nvgFill(this.vg);
    }

    public void drawDropShadow(float x, float y, float w, float h, float radius, float blur, float spread, int shadowColor) {
        if (!this.isAvailable()) {
            return;
        }
        NanoVG.nvgBeginPath(this.vg);
        NanoVG.nvgBoxGradient(this.vg, x - spread, y - spread + 2.0f, w + spread * 2.0f, h + spread * 2.0f, radius, blur, this.rgba(shadowColor, this.colorA), this.rgba(0, this.colorB), this.paint);
        NanoVG.nvgRect(this.vg, x - blur - spread, y - blur - spread, w + (blur + spread) * 2.0f, h + (blur + spread) * 2.0f);
        NanoVG.nvgFillPaint(this.vg, this.paint);
        NanoVG.nvgFill(this.vg);
    }

    public void drawCircle(float cx, float cy, float radius, int color) {
        if (!this.isAvailable()) {
            return;
        }
        NanoVG.nvgBeginPath(this.vg);
        NanoVG.nvgCircle(this.vg, cx, cy, radius);
        NanoVG.nvgFillColor(this.vg, this.rgba(color, this.colorA));
        NanoVG.nvgFill(this.vg);
    }

    public void drawCircleOutline(float cx, float cy, float radius, float strokeWidth, int color) {
        if (!this.isAvailable()) {
            return;
        }
        NanoVG.nvgBeginPath(this.vg);
        NanoVG.nvgCircle(this.vg, cx, cy, radius);
        NanoVG.nvgStrokeWidth(this.vg, strokeWidth);
        NanoVG.nvgStrokeColor(this.vg, this.rgba(color, this.colorA));
        NanoVG.nvgStroke(this.vg);
    }

    public void drawImageTexture(int glTextureId, float x, float y, float w, float h, float radius) {
        if (!this.isAvailable() || glTextureId <= 0) {
            return;
        }
        try {
            int img = NanoVGGL3.nvglCreateImageFromHandle(this.vg, glTextureId, (int)w, (int)h, 65536);
            if (img == 0) {
                return;
            }
            NanoVG.nvgImagePattern(this.vg, x, y, w, h, 0.0f, img, 1.0f, this.paint);
            NanoVG.nvgBeginPath(this.vg);
            if (radius > 0.0f) {
                NanoVG.nvgRoundedRect(this.vg, x, y, w, h, radius);
            } else {
                NanoVG.nvgRect(this.vg, x, y, w, h);
            }
            NanoVG.nvgFillPaint(this.vg, this.paint);
            NanoVG.nvgFill(this.vg);
            NanoVG.nvgDeleteImage(this.vg, img);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    public void drawCover(int glTextureId, float x, float y, float size, float radius) {
        this.drawGsmtcCover(glTextureId, x, y, size, radius);
    }

    public void drawGsmtcCover(int glTextureId, float x, float y, float size, float radius) {
        if (!this.isAvailable()) {
            return;
        }
        if (this.gsmtcCoverImage > 0) {
            NanoVG.nvgImagePattern(this.vg, x, y, size, size, 0.0f, this.gsmtcCoverImage, 1.0f, this.paint);
            NanoVG.nvgBeginPath(this.vg);
            if (radius > 0.0f) {
                NanoVG.nvgRoundedRect(this.vg, x, y, size, size, radius);
            } else {
                NanoVG.nvgRect(this.vg, x, y, size, size);
            }
            NanoVG.nvgFillPaint(this.vg, this.paint);
            NanoVG.nvgFill(this.vg);
            this.drawRoundedRectOutline(x, y, size, size, radius, 1.0f, this.darkTheme ? 0x44FFFFFF : 0x22000000);
            return;
        }
        if (glTextureId > 0) {
            this.drawImageTexture(glTextureId, x, y, size, size, radius);
            this.drawRoundedRectOutline(x, y, size, size, radius, 1.0f, this.darkTheme ? 0x44FFFFFF : 0x22000000);
            return;
        }
        this.drawVinylFallback(x, y, size, radius);
    }

    public void drawLocalCover(int glTextureId, float x, float y, float size, float radius) {
        if (!this.isAvailable()) {
            return;
        }
        if (this.localCoverImage > 0) {
            NanoVG.nvgImagePattern(this.vg, x, y, size, size, 0.0f, this.localCoverImage, 1.0f, this.paint);
            NanoVG.nvgBeginPath(this.vg);
            if (radius > 0.0f) {
                NanoVG.nvgRoundedRect(this.vg, x, y, size, size, radius);
            } else {
                NanoVG.nvgRect(this.vg, x, y, size, size);
            }
            NanoVG.nvgFillPaint(this.vg, this.paint);
            NanoVG.nvgFill(this.vg);
            this.drawRoundedRectOutline(x, y, size, size, radius, 1.0f, this.darkTheme ? 0x44FFFFFF : 0x22000000);
            return;
        }
        if (glTextureId > 0) {
            this.drawImageTexture(glTextureId, x, y, size, size, radius);
            this.drawRoundedRectOutline(x, y, size, size, radius, 1.0f, this.darkTheme ? 0x44FFFFFF : 0x22000000);
            return;
        }
        this.drawVinylFallback(x, y, size, radius);
    }

    public void drawVinylFallback(float x, float y, float size, float radius) {
        if (!this.isAvailable()) {
            return;
        }
        int bgTop = this.darkTheme ? -14013126 : -1906448;
        int bgBot = this.darkTheme ? -15197662 : -3418655;
        this.drawRoundedGradient(x, y, size, size, radius, bgTop, bgBot);
        this.drawRoundedRectOutline(x, y, size, size, radius, 1.0f, this.darkTheme ? 0x33FFFFFF : 0x22000000);
        this.drawCircle(x + size * 0.5f, y + size * 0.5f, size * 0.32f, this.darkTheme ? -15724009 : -7035976);
        this.drawCircleOutline(x + size * 0.5f, y + size * 0.5f, size * 0.32f, 1.0f, this.darkTheme ? 0x44FFFFFF : 0x33000000);
        this.drawMusicNoteIcon(x + size * 0.52f, y + size * 0.48f, size * 0.38f, -13312);
    }

    public void drawIcon(String glyph, float x, float y, float size, int color, int align) {
        if (!this.isAvailable() || glyph == null || glyph.isEmpty()) {
            return;
        }
        NanoVG.nvgFontSize(this.vg, size);
        if (this.iconFont != -1) {
            NanoVG.nvgFontFace(this.vg, "MaterialIcons");
            NanoVG.nvgFontFaceId(this.vg, this.iconFont);
        } else if (this.regularFont != -1) {
            NanoVG.nvgFontFace(this.vg, "CustomFont");
            NanoVG.nvgFontFaceId(this.vg, this.regularFont);
        }
        NanoVG.nvgTextAlign(this.vg, align);
        NanoVG.nvgFillColor(this.vg, this.rgba(color, this.colorA));
        NanoVG.nvgText(this.vg, x, y, glyph);
    }

    public void drawMusicNoteIcon(float cx, float cy, float size, int color) {
        if (!this.isAvailable()) {
            return;
        }
        if (this.iconFont != -1) {
            this.drawIcon(ICON_MUSIC, cx, cy + 0.5f, size * 1.15f, color, 18);
            return;
        }
        NanoVG.nvgBeginPath(this.vg);
        NanoVG.nvgCircle(this.vg, cx - size * 0.22f, cy + size * 0.25f, size * 0.24f);
        NanoVG.nvgFillColor(this.vg, this.rgba(color, this.colorA));
        NanoVG.nvgFill(this.vg);
        NanoVG.nvgBeginPath(this.vg);
        NanoVG.nvgMoveTo(this.vg, cx - size * 0.04f, cy + size * 0.25f);
        NanoVG.nvgLineTo(this.vg, cx - size * 0.04f, cy - size * 0.42f);
        NanoVG.nvgStrokeWidth(this.vg, 1.4f);
        NanoVG.nvgStrokeColor(this.vg, this.rgba(color, this.colorA));
        NanoVG.nvgStroke(this.vg);
        NanoVG.nvgBeginPath(this.vg);
        NanoVG.nvgMoveTo(this.vg, cx - size * 0.04f, cy - size * 0.42f);
        NanoVG.nvgBezierTo(this.vg, cx + size * 0.28f, cy - size * 0.28f, cx + size * 0.18f, cy - size * 0.08f, cx + size * 0.08f, cy);
        NanoVG.nvgStrokeWidth(this.vg, 1.3f);
        NanoVG.nvgStrokeColor(this.vg, this.rgba(color, this.colorA));
        NanoVG.nvgStroke(this.vg);
    }

    public void drawButtonBase(float x, float y, float w, float h, boolean isHovered, boolean isPressed, int accentColor) {
        int outline;
        int bg;
        if (!this.isAvailable()) {
            return;
        }
        float radius = Math.min(h * 0.5f, 7.0f);
        if (this.darkTheme) {
            bg = isPressed ? -15460833 : (isHovered ? -13749694 : -14671058);
            outline = isHovered && !isPressed ? accentColor : 860116326;
        } else {
            bg = isPressed ? -3418655 : (isHovered ? -1906448 : -920071);
            outline = isHovered && !isPressed ? accentColor : 1150591928;
        }
        this.drawRoundedRect(x, y, w, h, radius, bg);
        this.drawRoundedRectOutline(x, y, w, h, radius, 1.0f, outline);
        if (isHovered && !isPressed) {
            this.drawDropShadow(x, y, w, h, radius, 6.0f, 1.0f, accentColor & 0x44FFFFFF);
        }
    }

    public void drawMoonSunIcon(float cx, float cy, float radius, boolean isDark, int color) {
        if (!this.isAvailable()) {
            return;
        }
        if (this.iconFont != -1) {
            this.drawIcon(isDark ? ICON_DARK_MODE : ICON_LIGHT_MODE, cx, cy + 0.5f, radius * 2.2f, color, 18);
            return;
        }
        if (isDark) {
            NanoVG.nvgBeginPath(this.vg);
            NanoVG.nvgCircle(this.vg, cx, cy, radius);
            NanoVG.nvgFillColor(this.vg, this.rgba(color, this.colorA));
            NanoVG.nvgFill(this.vg);
            NanoVG.nvgBeginPath(this.vg);
            NanoVG.nvgCircle(this.vg, cx + radius * 0.45f, cy - radius * 0.3f, radius * 0.85f);
            int cutColor = this.darkTheme ? -14671058 : -920071;
            NanoVG.nvgFillColor(this.vg, this.rgba(cutColor, this.colorA));
            NanoVG.nvgFill(this.vg);
        } else {
            this.drawCircle(cx, cy, radius * 0.48f, color);
            int rays = 8;
            for (int i = 0; i < rays; ++i) {
                double angle = (double)i * (Math.PI * 2 / (double)rays);
                float r1 = radius * 0.65f;
                float r2 = radius * 0.98f;
                float x1 = cx + (float)Math.cos(angle) * r1;
                float y1 = cy + (float)Math.sin(angle) * r1;
                float x2 = cx + (float)Math.cos(angle) * r2;
                float y2 = cy + (float)Math.sin(angle) * r2;
                NanoVG.nvgBeginPath(this.vg);
                NanoVG.nvgMoveTo(this.vg, x1, y1);
                NanoVG.nvgLineTo(this.vg, x2, y2);
                NanoVG.nvgStrokeWidth(this.vg, 1.3f);
                NanoVG.nvgStrokeColor(this.vg, this.rgba(color, this.colorA));
                NanoVG.nvgStroke(this.vg);
            }
        }
    }

    public void drawRefreshIcon(float cx, float cy, float radius, int color) {
        if (!this.isAvailable()) {
            return;
        }
        if (this.iconFont != -1) {
            this.drawIcon(ICON_REFRESH, cx, cy + 0.5f, radius * 2.3f, color, 18);
            return;
        }
        NanoVG.nvgBeginPath(this.vg);
        NanoVG.nvgArc(this.vg, cx, cy, radius, -2.4f, 1.8f, 2);
        NanoVG.nvgStrokeWidth(this.vg, 1.4f);
        NanoVG.nvgStrokeColor(this.vg, this.rgba(color, this.colorA));
        NanoVG.nvgStroke(this.vg);
        float ax = cx + (float)Math.cos(1.8f) * radius;
        float ay = cy + (float)Math.sin(1.8f) * radius;
        NanoVG.nvgBeginPath(this.vg);
        NanoVG.nvgMoveTo(this.vg, ax, ay);
        NanoVG.nvgLineTo(this.vg, ax - 3.2f, ay - 2.0f);
        NanoVG.nvgLineTo(this.vg, ax + 0.8f, ay - 3.5f);
        NanoVG.nvgClosePath(this.vg);
        NanoVG.nvgFillColor(this.vg, this.rgba(color, this.colorA));
        NanoVG.nvgFill(this.vg);
    }

    public void drawFolderIcon(float cx, float cy, float w, float h, int color) {
        if (!this.isAvailable()) {
            return;
        }
        if (this.iconFont != -1) {
            this.drawIcon(ICON_FOLDER, cx, cy + 0.5f, Math.max(w, h) * 1.25f, color, 18);
            return;
        }
        float halfW = w * 0.5f;
        float halfH = h * 0.5f;
        NanoVG.nvgBeginPath(this.vg);
        NanoVG.nvgMoveTo(this.vg, cx - halfW, cy - halfH * 0.35f);
        NanoVG.nvgLineTo(this.vg, cx - halfW * 0.2f, cy - halfH * 0.35f);
        NanoVG.nvgLineTo(this.vg, cx, cy - halfH * 0.05f);
        NanoVG.nvgLineTo(this.vg, cx + halfW, cy - halfH * 0.05f);
        NanoVG.nvgLineTo(this.vg, cx + halfW, cy + halfH);
        NanoVG.nvgLineTo(this.vg, cx - halfW, cy + halfH);
        NanoVG.nvgClosePath(this.vg);
        NanoVG.nvgFillColor(this.vg, this.rgba(color & 0xFFFFFF | 0x2E000000, this.colorA));
        NanoVG.nvgFill(this.vg);
        NanoVG.nvgStrokeWidth(this.vg, 1.3f);
        NanoVG.nvgStrokeColor(this.vg, this.rgba(color, this.colorA));
        NanoVG.nvgStroke(this.vg);
    }

    public void drawCrossIcon(float cx, float cy, float size, int color) {
        if (!this.isAvailable()) {
            return;
        }
        if (this.iconFont != -1) {
            this.drawIcon(ICON_DELETE, cx, cy + 0.5f, size * 1.55f, color, 18);
            return;
        }
        float half = size * 0.5f;
        NanoVG.nvgBeginPath(this.vg);
        NanoVG.nvgMoveTo(this.vg, cx - half, cy - half);
        NanoVG.nvgLineTo(this.vg, cx + half, cy + half);
        NanoVG.nvgMoveTo(this.vg, cx + half, cy - half);
        NanoVG.nvgLineTo(this.vg, cx - half, cy + half);
        NanoVG.nvgStrokeWidth(this.vg, 1.4f);
        NanoVG.nvgStrokeColor(this.vg, this.rgba(color, this.colorA));
        NanoVG.nvgStroke(this.vg);
    }

    public void drawDotsIcon(float cx, float cy, float dotRadius, int color) {
        if (!this.isAvailable()) {
            return;
        }
        if (this.iconFont != -1) {
            this.drawIcon(ICON_MORE_VERT, cx, cy + 0.5f, 13.0f, color, 18);
            return;
        }
        this.drawCircle(cx, cy - 3.6f, dotRadius, color);
        this.drawCircle(cx, cy, dotRadius, color);
        this.drawCircle(cx, cy + 3.6f, dotRadius, color);
    }

    public void drawSpeakerIcon(float cx, float cy, float size, boolean muted, int color) {
        if (!this.isAvailable()) {
            return;
        }
        if (this.iconFont != -1) {
            this.drawIcon(muted ? ICON_VOLUME_OFF : ICON_VOLUME_UP, cx, cy + 0.5f, size * 1.5f, muted ? -43691 : color, 18);
            return;
        }
        NanoVG.nvgBeginPath(this.vg);
        NanoVG.nvgMoveTo(this.vg, cx - size * 0.45f, cy - size * 0.22f);
        NanoVG.nvgLineTo(this.vg, cx - size * 0.18f, cy - size * 0.22f);
        NanoVG.nvgLineTo(this.vg, cx + size * 0.18f, cy - size * 0.45f);
        NanoVG.nvgLineTo(this.vg, cx + size * 0.18f, cy + size * 0.45f);
        NanoVG.nvgLineTo(this.vg, cx - size * 0.18f, cy + size * 0.22f);
        NanoVG.nvgLineTo(this.vg, cx - size * 0.45f, cy + size * 0.22f);
        NanoVG.nvgClosePath(this.vg);
        NanoVG.nvgFillColor(this.vg, this.rgba(color, this.colorA));
        NanoVG.nvgFill(this.vg);
        if (muted) {
            NanoVG.nvgBeginPath(this.vg);
            NanoVG.nvgMoveTo(this.vg, cx + size * 0.22f, cy - size * 0.4f);
            NanoVG.nvgLineTo(this.vg, cx + size * 0.55f, cy + size * 0.4f);
            NanoVG.nvgStrokeWidth(this.vg, 1.4f);
            NanoVG.nvgStrokeColor(this.vg, this.rgba(-43691, this.colorA));
            NanoVG.nvgStroke(this.vg);
        } else {
            NanoVG.nvgBeginPath(this.vg);
            NanoVG.nvgArc(this.vg, cx + size * 0.12f, cy, size * 0.35f, -0.65f, 0.65f, 2);
            NanoVG.nvgStrokeWidth(this.vg, 1.3f);
            NanoVG.nvgStrokeColor(this.vg, this.rgba(color, this.colorA));
            NanoVG.nvgStroke(this.vg);
        }
    }

    public void drawMiniPlayIcon(float cx, float cy, float size, int color) {
        if (!this.isAvailable()) {
            return;
        }
        if (this.iconFont != -1) {
            this.drawIcon(ICON_PLAY, cx + 0.5f, cy + 0.5f, size * 2.3f, color, 18);
            return;
        }
        NanoVG.nvgBeginPath(this.vg);
        NanoVG.nvgMoveTo(this.vg, cx - size * 0.5f, cy - size * 0.8f);
        NanoVG.nvgLineTo(this.vg, cx + size * 0.7f, cy);
        NanoVG.nvgLineTo(this.vg, cx - size * 0.5f, cy + size * 0.8f);
        NanoVG.nvgClosePath(this.vg);
        NanoVG.nvgFillColor(this.vg, this.rgba(color, this.colorA));
        NanoVG.nvgFill(this.vg);
    }

    public void drawMiniPauseIcon(float cx, float cy, float size, int color) {
        if (!this.isAvailable()) {
            return;
        }
        if (this.iconFont != -1) {
            this.drawIcon(ICON_PAUSE, cx, cy + 0.5f, size * 2.3f, color, 18);
            return;
        }
        float barW = size * 0.45f;
        float barH = size * 1.6f;
        float gap = size * 0.35f;
        this.drawRoundedRect(cx - gap - barW, cy - barH * 0.5f, barW, barH, 0.8f, color);
        this.drawRoundedRect(cx + gap, cy - barH * 0.5f, barW, barH, 0.8f, color);
    }

    public void drawText(String text, float x, float y, float size, int color, boolean bold, int align) {
        if (!this.isAvailable() || text == null || text.isEmpty()) {
            return;
        }
        NanoVG.nvgFontSize(this.vg, size);
        if (bold && this.boldFont != -1) {
            NanoVG.nvgFontFace(this.vg, "CustomFont-Bold");
            NanoVG.nvgFontFaceId(this.vg, this.boldFont);
        } else if (this.regularFont != -1) {
            NanoVG.nvgFontFace(this.vg, "CustomFont");
            NanoVG.nvgFontFaceId(this.vg, this.regularFont);
        }
        NanoVG.nvgTextAlign(this.vg, align);
        NanoVG.nvgFillColor(this.vg, this.rgba(color, this.colorA));
        float advance = NanoVG.nvgText(this.vg, x, y, text);
        if (!this.loggedFirstText) {
            this.loggedFirstText = true;
            int fontId = (bold && this.boldFont != -1) ? this.boldFont : this.regularFont;
            com.mp3player.MP3PlayerClient.LOGGER.info("[NanoVG] First text rendered: '" + text + "', fontId=" + fontId + ", size=" + size + ", advance=" + advance);
        }
    }

    public float getTextWidth(String text, float size, boolean bold) {
        if (!this.isAvailable() || text == null || text.isEmpty()) {
            return 0.0f;
        }
        NanoVG.nvgFontSize(this.vg, size);
        if (bold && this.boldFont != -1) {
            NanoVG.nvgFontFace(this.vg, "CustomFont-Bold");
            NanoVG.nvgFontFaceId(this.vg, this.boldFont);
        } else if (this.regularFont != -1) {
            NanoVG.nvgFontFace(this.vg, "CustomFont");
            NanoVG.nvgFontFaceId(this.vg, this.regularFont);
        }
        float[] bounds = new float[4];
        return NanoVG.nvgTextBounds(this.vg, 0.0f, 0.0f, (CharSequence)text, bounds);
    }

    public void drawPlayPauseButton(float cx, float cy, float radius, boolean isPlaying, boolean isHovered, int accentColor) {
        if (!this.isAvailable()) {
            return;
        }
        this.drawDropShadow(cx - radius, cy - radius, radius * 2.0f, radius * 2.0f, radius, isHovered ? 12.0f : 6.0f, 1.0f, accentColor & 0x66FFFFFF);
        int col = isHovered ? accentColor | 0x222222 : accentColor;
        this.drawCircle(cx, cy, radius, col);
        if (this.iconFont != -1) {
            float iconSize = radius * 1.55f;
            float offX = isPlaying ? 0.0f : radius * 0.08f;
            this.drawIcon(isPlaying ? ICON_PAUSE : ICON_PLAY, cx + offX, cy + 0.5f, iconSize, -1, 18);
            return;
        }
        if (isPlaying) {
            float barW = radius * 0.22f;
            float barH = radius * 0.85f;
            float gap = radius * 0.22f;
            this.drawRoundedRect(cx - gap - barW, cy - barH * 0.5f, barW, barH, 2.0f, -1);
            this.drawRoundedRect(cx + gap, cy - barH * 0.5f, barW, barH, 2.0f, -1);
        } else {
            NanoVG.nvgBeginPath(this.vg);
            float off = radius * 0.1f;
            NanoVG.nvgMoveTo(this.vg, cx - radius * 0.35f + off, cy - radius * 0.48f);
            NanoVG.nvgLineTo(this.vg, cx + radius * 0.52f + off, cy);
            NanoVG.nvgLineTo(this.vg, cx - radius * 0.35f + off, cy + radius * 0.48f);
            NanoVG.nvgClosePath(this.vg);
            NanoVG.nvgFillColor(this.vg, this.rgba(-1, this.colorA));
            NanoVG.nvgFill(this.vg);
        }
    }

    public void drawSkipButton(float cx, float cy, float radius, boolean isForward, boolean isHovered) {
        if (!this.isAvailable()) {
            return;
        }
        int iconCol = isHovered ? (this.darkTheme ? -1 : -15788246) : (this.darkTheme ? -6249035 : -10193781);
        if (this.iconFont != -1) {
            this.drawIcon(isForward ? ICON_SKIP_NEXT : ICON_SKIP_PREV, cx, cy + 0.5f, radius * 1.6f, iconCol, 18);
            return;
        }
        float dir = isForward ? 1.0f : -1.0f;
        float triSize = radius * 0.45f;
        NanoVG.nvgBeginPath(this.vg);
        NanoVG.nvgMoveTo(this.vg, cx - triSize * dir, cy - triSize);
        NanoVG.nvgLineTo(this.vg, cx, cy);
        NanoVG.nvgLineTo(this.vg, cx - triSize * dir, cy + triSize);
        NanoVG.nvgClosePath(this.vg);
        NanoVG.nvgFillColor(this.vg, this.rgba(iconCol, this.colorA));
        NanoVG.nvgFill(this.vg);
        this.drawRoundedRect(cx + (dir > 0.0f ? triSize * 0.2f : -triSize * 0.8f), cy - triSize, radius * 0.18f, triSize * 2.0f, 1.0f, iconCol);
    }

    public void drawProgressBar(float x, float y, float w, float h, float progress, boolean isHovered, int accentColor) {
        if (!this.isAvailable()) {
            return;
        }
        progress = Math.max(0.0f, Math.min(1.0f, progress));
        float barH = isHovered ? h + 2.0f : h;
        float curY = y - (barH - h) * 0.5f;
        int trackBg = this.darkTheme ? 0x44FFFFFF : -3418655;
        this.drawRoundedRect(x, curY, w, barH, barH * 0.5f, trackBg);
        if (progress > 0.001f) {
            float fillW = w * progress;
            this.drawRoundedRect(x, curY, fillW, barH, barH * 0.5f, accentColor);
            if (isHovered) {
                float knobX = x + fillW;
                float knobY = curY + barH * 0.5f;
                this.drawCircle(knobX, knobY, barH * 1.5f, -1);
                this.drawDropShadow(knobX - 4.0f, knobY - 4.0f, 8.0f, 8.0f, 4.0f, 8.0f, 2.0f, accentColor);
            }
        }
    }

    public void drawEqualizerBars(float x, float y, float w, float h, int color, long time) {
        if (!this.isAvailable()) {
            return;
        }
        int bars = 4;
        float barW = (w - (float)((bars - 1) * 2)) / (float)bars;
        for (int i = 0; i < bars; ++i) {
            double wave = Math.sin((double)time * 0.008 + (double)i * 1.3);
            float pct = (float)(0.25 + 0.75 * Math.abs(wave));
            float barH = h * pct;
            this.drawRoundedRect(x + (float)i * (barW + 2.0f), y + h - barH, barW, barH, 1.0f, color);
        }
    }

    public void drawHeartIcon(float cx, float cy, float size, boolean isFav, boolean isHovered) {
        if (!this.isAvailable()) {
            return;
        }
        int color = isFav ? -46226 : (isHovered ? -1 : (this.darkTheme ? -2002081110 : -10193781));
        if (this.iconFont != -1) {
            this.drawIcon(isFav ? ICON_HEART : ICON_HEART_BORDER, cx, cy + 0.5f, size * 1.4f, color, 18);
            return;
        }
        float s = size * 0.5f;
        NanoVG.nvgBeginPath(this.vg);
        NanoVG.nvgMoveTo(this.vg, cx, cy + s * 0.7f);
        NanoVG.nvgBezierTo(this.vg, cx - s * 1.2f, cy - s * 0.3f, cx - s * 1.2f, cy - s * 1.1f, cx, cy - s * 0.5f);
        NanoVG.nvgBezierTo(this.vg, cx + s * 1.2f, cy - s * 1.1f, cx + s * 1.2f, cy - s * 0.3f, cx, cy + s * 0.7f);
        NanoVG.nvgClosePath(this.vg);
        NanoVG.nvgFillColor(this.vg, this.rgba(color, this.colorA));
        NanoVG.nvgFill(this.vg);
    }

    public void drawButton(float x, float y, float w, float h, String text, boolean isHovered, boolean isPressed, int accentColor, int textColor) {
        int outline;
        int bg;
        if (!this.isAvailable()) {
            return;
        }
        float radius = Math.min(h * 0.5f, 7.0f);
        if (this.darkTheme) {
            bg = isPressed ? -15197662 : (isHovered ? -13486522 : -14407885);
            outline = isHovered && !isPressed ? accentColor : 861234807;
        } else {
            bg = isPressed ? -3418655 : (isHovered ? -1906448 : -920071);
            outline = isHovered && !isPressed ? accentColor : 1150591928;
        }
        this.drawRoundedRect(x, y, w, h, radius, bg);
        this.drawRoundedRectOutline(x, y, w, h, radius, 1.0f, outline);
        if (isHovered && !isPressed) {
            this.drawDropShadow(x, y, w, h, radius, 8.0f, 1.0f, accentColor & 0x44FFFFFF);
        }
        int textCol = isHovered ? accentColor | 0xFF000000 : (this.darkTheme ? textColor : -15788246);
        this.drawText(text, x + w * 0.5f, y + h * 0.5f + 1.0f, h * 0.55f, textCol, isHovered, 18);
    }

    public void drawTabBase(float x, float y, float w, float h, boolean isSelected, boolean isHovered, int accentColor) {
        int bg;
        if (!this.isAvailable()) {
            return;
        }
        float radius = 7.0f;
        if (this.darkTheme) {
            bg = isSelected ? -14144452 : (isHovered ? -14671056 : 0);
        } else {
            bg = isSelected ? -1 : (isHovered ? -1906448 : 0);
        }
        if (bg != 0) {
            this.drawRoundedRect(x, y, w, h, radius, bg);
            if (!this.darkTheme && isSelected) {
                this.drawRoundedRectOutline(x, y, w, h, radius, 1.0f, 865379256);
            }
        }
        if (isSelected) {
            this.drawRoundedRect(x + 4.0f, y + h - 2.5f, w - 8.0f, 2.5f, 1.5f, accentColor);
            this.drawDropShadow(x + 4.0f, y + h - 2.5f, w - 8.0f, 2.5f, 1.5f, 6.0f, 1.0f, accentColor & 0x77FFFFFF);
        }
    }

    public void drawTab(float x, float y, float w, float h, String text, boolean isSelected, boolean isHovered, int accentColor) {
        if (!this.isAvailable()) {
            return;
        }
        this.drawTabBase(x, y, w, h, isSelected, isHovered, accentColor);
        int textCol = isSelected ? accentColor : (isHovered ? (this.darkTheme ? -1 : -15788246) : (this.darkTheme ? -6380360 : -10193781));
        this.drawText(text, x + w * 0.5f, y + h * 0.5f, 12.0f, textCol, isSelected, 18);
    }

    public void drawBadge(float x, float y, float h, String text, int badgeColor) {
        if (!this.isAvailable() || text == null || text.isEmpty()) {
            return;
        }
        float textW = this.getTextWidth(text, h * 0.65f, true);
        float pad = 6.0f;
        float w = textW + pad * 2.0f;
        float radius = h * 0.5f;
        this.drawRoundedRect(x, y, w, h, radius, badgeColor & 0xFFFFFF | 0x28000000);
        this.drawRoundedRectOutline(x, y, w, h, radius, 1.0f, badgeColor);
        this.drawDropShadow(x, y, w, h, radius, 6.0f, 0.5f, badgeColor & 0x44FFFFFF);
        this.drawText(text, x + w * 0.5f, y + h * 0.5f + 0.5f, h * 0.65f, badgeColor, true, 18);
    }

    public void scissor(float x, float y, float w, float h) {
        if (!this.isAvailable()) {
            return;
        }
        NanoVG.nvgScissor(this.vg, x, y, w, h);
    }

    public void resetScissor() {
        if (!this.isAvailable()) {
            return;
        }
        NanoVG.nvgResetScissor(this.vg);
    }

    public void drawMiniDisc(float cx, float cy, float radius, int vinylColor, int centerColor) {
        if (!this.isAvailable()) {
            return;
        }
        this.drawCircle(cx, cy, radius, vinylColor);
        this.drawCircleOutline(cx, cy, radius * 0.65f, 0.7f, 0x22FFFFFF);
        this.drawCircle(cx, cy, radius * 0.35f, centerColor);
        this.drawCircle(cx, cy, radius * 0.12f, -15724010);
    }

    public void drawSourceIcon(float cx, float cy, float radius, String sourceApp) {
        String s;
        if (!this.isAvailable()) {
            return;
        }
        String string = s = sourceApp == null ? "" : sourceApp.toLowerCase();
        if (s.contains("spotify") || s.contains("\u0441\u043f\u043e\u0442\u0438\u0444\u0430\u0439")) {
            this.drawCircle(cx, cy, radius, -14829228);
            this.drawDropShadow(cx - radius, cy - radius, radius * 2.0f, radius * 2.0f, radius, 4.0f, 0.5f, 1142798676);
            for (int i = 0; i < 3; ++i) {
                float r = radius * (0.35f + (float)i * 0.22f);
                float yOff = cy - radius * 0.1f + (float)i * radius * 0.22f;
                this.drawCircleOutline(cx, yOff, r, 0.8f, -1);
            }
        } else if (s.contains("yandex") || s.contains("\u044f\u043d\u0434\u0435\u043a\u0441")) {
            this.drawCircle(cx, cy, radius, -13312);
            this.drawDropShadow(cx - radius, cy - radius, radius * 2.0f, radius * 2.0f, radius, 4.0f, 0.5f, 1157614592);
            if (this.iconFont != -1) {
                this.drawIcon(ICON_MUSIC, cx, cy + 0.5f, radius * 1.3f, -16777216, 18);
            } else {
                this.drawMiniPlayIcon(cx + 0.5f, cy, radius * 0.4f, -16777216);
            }
        } else {
            this.drawCircle(cx, cy, radius, -16739073);
            this.drawDropShadow(cx - radius, cy - radius, radius * 2.0f, radius * 2.0f, radius, 4.0f, 0.5f, 1140888831);
            this.drawCircleOutline(cx, cy, radius * 0.6f, 0.8f, -1);
            this.drawRoundedRect(cx - radius * 0.5f, cy - 0.4f, radius, 0.8f, 0.4f, -1);
            this.drawRoundedRect(cx - 0.4f, cy - radius * 0.5f, 0.8f, radius, 0.4f, -1);
        }
    }

    public void drawGearIcon(float cx, float cy, float radius, int color) {
        if (!this.isAvailable()) {
            return;
        }
        if (this.iconFont != -1) {
            this.drawIcon(ICON_SETTINGS, cx, cy + 0.5f, radius * 2.2f, color, 18);
            return;
        }
        int teeth = 8;
        float toothW = radius * 0.35f;
        for (int i = 0; i < teeth; ++i) {
            float angle = (float)((double)i * (Math.PI * 2 / (double)teeth));
            float cos = (float)Math.cos(angle);
            float sin = (float)Math.sin(angle);
            NanoVG.nvgBeginPath(this.vg);
            NanoVG.nvgCircle(this.vg, cx + cos * (radius * 0.92f), cy + sin * (radius * 0.92f), toothW * 0.5f);
            NanoVG.nvgFillColor(this.vg, this.rgba(color, this.colorA));
            NanoVG.nvgFill(this.vg);
        }
        NanoVG.nvgBeginPath(this.vg);
        NanoVG.nvgCircle(this.vg, cx, cy, radius * 0.8f);
        NanoVG.nvgFillColor(this.vg, this.rgba(color, this.colorA));
        NanoVG.nvgFill(this.vg);
        NanoVG.nvgBeginPath(this.vg);
        NanoVG.nvgCircle(this.vg, cx, cy, radius * 0.36f);
        NanoVG.nvgFillColor(this.vg, this.rgba(this.darkTheme ? -15460833 : -1, this.colorA));
        NanoVG.nvgFill(this.vg);
    }

    public void drawToggleSwitch(float x, float y, float w, float h, boolean active, boolean hovered) {
        if (!this.isAvailable()) {
            return;
        }
        float r = h * 0.5f;
        int trackCol = active ? -13058568 : (this.darkTheme ? -14012867 : -3025445);
        if (hovered && !active) {
            trackCol = this.darkTheme ? -12762022 : -6511697;
        }
        this.drawRoundedRect(x, y, w, h, r, trackCol);
        if (active) {
            this.drawDropShadow(x, y, w, h, r, 4.0f, 0.5f, 859356664);
        }
        float knobR = r - 2.0f;
        float knobX = active ? x + w - r : x + r;
        float knobY = y + r;
        this.drawCircle(knobX, knobY, knobR, -1);
        this.drawCircleOutline(knobX, knobY, knobR, 0.8f, 0x22000000);
    }

    public void drawTogglePill(float x, float y, float w, float h, String text, boolean active, boolean hovered, int activeColor) {
        int textCol;
        int outline;
        int bg;
        if (!this.isAvailable()) {
            return;
        }
        float radius = Math.min(h * 0.5f, 7.0f);
        if (active) {
            bg = activeColor & 0xFFFFFF | (this.darkTheme ? 0x28000000 : 0x20000000);
            outline = activeColor;
            textCol = this.darkTheme ? -1 : -15788246;
        } else {
            bg = this.darkTheme ? (hovered ? -14144454 : -14934232) : (hovered ? -1906448 : -1117450);
            outline = hovered ? 1721017272 : (this.darkTheme ? 710239863 : 868996577);
            textCol = this.darkTheme ? -7035976 : -10193781;
        }
        this.drawRoundedRect(x, y, w, h, radius, bg);
        this.drawRoundedRectOutline(x, y, w, h, radius, active ? 1.2f : 0.8f, outline);
        if (active || hovered) {
            int glowCol = active ? activeColor : 1721017272;
            this.drawDropShadow(x, y, w, h, radius, hovered ? 7.0f : 4.0f, 0.5f, glowCol & 0x44FFFFFF);
        }
        this.drawText(text, x + w * 0.5f, y + h * 0.5f + 0.5f, h * 0.52f, textCol, active || hovered, 18);
    }

    public void drawBoundedText(String text, float x, float y, float size, int color, boolean bold, float maxW) {
        if (!this.isAvailable() || text == null || text.isEmpty()) {
            return;
        }
        float tw = this.getTextWidth(text, size, bold);
        if (tw <= maxW) {
            this.drawText(text, x, y, size, color, bold, 17);
            return;
        }
        String truncated = text;
        while (truncated.length() > 3 && this.getTextWidth(truncated + "\u2026", size, bold) > maxW) {
            truncated = truncated.substring(0, truncated.length() - 1);
        }
        this.drawText(truncated + "\u2026", x, y, size, color, bold, 17);
    }

    public void drawSlider(float x, float y, float w, float h, float progress, int activeColor, boolean hovered) {
        if (!this.isAvailable()) {
            return;
        }
        float r = h * 0.5f;
        int trackBg = this.darkTheme ? -14341834 : -1906448;
        this.drawRoundedRect(x, y, w, h, r, trackBg);
        float fillW = Math.max(h, Math.min(w, w * progress));
        if (fillW > 0.0f) {
            this.drawRoundedRect(x, y, fillW, h, r, activeColor);
        }
        float thumbX = x + Math.max(r, Math.min(w - r, w * progress));
        float thumbY = y + r;
        float thumbR = r + 2.0f;
        this.drawDropShadow(thumbX - thumbR, thumbY - thumbR, thumbR * 2.0f, thumbR * 2.0f, thumbR, 4.0f, 0.5f, 0x44000000);
        this.drawCircle(thumbX, thumbY, thumbR, -1);
        this.drawCircleOutline(thumbX, thumbY, thumbR, 1.2f, activeColor);
    }

    public long getContext() {
        return this.vg;
    }
}

