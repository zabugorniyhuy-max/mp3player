/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.texture.AbstractTexture
 *  net.minecraft.client.texture.NativeImage
 *  net.minecraft.client.texture.NativeImageBackedTexture
 *  net.minecraft.util.Identifier
 *  org.lwjgl.glfw.GLFWNativeWin32
 */
package com.mp3player.client.gsmtc;

import com.mojang.blaze3d.platform.NativeImage;
import com.mp3player.client.audio.MusicPlayer;
import com.mp3player.client.gsmtc.GsmtcBridge;
import com.mp3player.client.gsmtc.GsmtcLoader;
import com.mp3player.client.gui.nanovg.NanoVGEngine;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

public class GsmtcManager {
    private static GsmtcManager instance;
    private final GsmtcBridge bridge;
    private final byte[] buffer = new byte[1024];
    private volatile boolean hasTrack = false;
    private volatile String title = "";
    private volatile String artist = "";
    private volatile String album = "";
    private volatile String sourceApp = "";
    private volatile int status = 0;
    private volatile double position = 0.0;
    private volatile double duration = 0.0;
    private volatile double basePosition = 0.0;
    private volatile long lastPositionUpdateTimestamp = 0L;
    private volatile double lastReportedNativePos = -1.0;
    private volatile long lastSeekTime = 0L;
    public static final Identifier COVER_TEXTURE_ID;
    private DynamicTexture coverTexture = null;
    private volatile int currentThumbVersion = -1;
    private volatile byte[] currentCoverBytes = null;
    private volatile String lastNotifiedTitle = "";
    private volatile long toastStartTime = 0L;
    private static final long TOAST_DURATION_MS = 6000L;
    private volatile boolean hudEnabled = true;
    private volatile boolean toastEnabled = false;
    private final ScheduledExecutorService pollExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "Gsmtc-PollThread");
        t.setDaemon(true);
        return t;
    });
    private String lastCoverSearchQuery = "";
    private volatile float externalVolume = 1.0f;

    public byte[] getCoverBytes() {
        return this.currentCoverBytes;
    }

    private GsmtcManager() {
        this.bridge = GsmtcLoader.load();
        if (this.bridge != null) {
            this.pollExecutor.scheduleWithFixedDelay(this::pollNative, 100L, 80L, TimeUnit.MILLISECONDS);
        }
    }

    public static synchronized GsmtcManager getInstance() {
        if (instance == null) {
            instance = new GsmtcManager();
        }
        return instance;
    }

    public boolean isAvailable() {
        return this.bridge != null;
    }

    private void pollNative() {
        if (this.bridge == null) {
            return;
        }
        try {
            boolean statusChanged;
            boolean ht = this.bridge.gsmtc_has_track() == 1;
            int newStatus = this.bridge.gsmtc_get_status();
            boolean bl = statusChanged = newStatus != this.status;
            if (statusChanged && newStatus == 1) {
                try {
                    if (MusicPlayer.getInstance().isPlaying()) {
                        Minecraft.getInstance().execute(() -> MusicPlayer.getInstance().pause());
                    }
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
            }
            this.status = newStatus;
            this.hasTrack = ht;
            if (ht) {
                int tLen = this.bridge.gsmtc_get_title(this.buffer, this.buffer.length);
                String newTitle = tLen > 0 ? new String(this.buffer, 0, tLen, StandardCharsets.UTF_8) : "";
                int aLen = this.bridge.gsmtc_get_artist(this.buffer, this.buffer.length);
                this.artist = aLen > 0 ? new String(this.buffer, 0, aLen, StandardCharsets.UTF_8) : "";
                int alLen = this.bridge.gsmtc_get_album(this.buffer, this.buffer.length);
                this.album = alLen > 0 ? new String(this.buffer, 0, alLen, StandardCharsets.UTF_8) : "";
                int sLen = this.bridge.gsmtc_get_source(this.buffer, this.buffer.length);
                this.sourceApp = sLen > 0 ? new String(this.buffer, 0, sLen, StandardCharsets.UTF_8) : "";
                double newPos = this.bridge.gsmtc_get_position();
                this.duration = this.bridge.gsmtc_get_duration();
                if (!newTitle.isEmpty() && !newTitle.equals(this.lastNotifiedTitle)) {
                    this.lastNotifiedTitle = newTitle;
                    this.toastStartTime = System.currentTimeMillis();
                    this.basePosition = newPos;
                    this.lastReportedNativePos = newPos;
                    this.lastPositionUpdateTimestamp = System.currentTimeMillis();
                } else if (statusChanged || this.lastPositionUpdateTimestamp == 0L) {
                    this.basePosition = newPos;
                    this.lastReportedNativePos = newPos;
                    this.lastPositionUpdateTimestamp = System.currentTimeMillis();
                } else if (System.currentTimeMillis() - this.lastSeekTime >= 1500L && Math.abs(newPos - this.lastReportedNativePos) > 0.05) {
                    this.lastReportedNativePos = newPos;
                    this.basePosition = newPos;
                    this.lastPositionUpdateTimestamp = System.currentTimeMillis();
                }
                this.position = this.getPosition();
                this.title = newTitle;
                int thumbVer = this.bridge.gsmtc_get_thumbnail_version();
                if (thumbVer != this.currentThumbVersion) {
                    this.currentThumbVersion = thumbVer;
                    this.updateThumbnail();
                }
            } else {
                this.title = "";
                this.artist = "";
                this.album = "";
                this.position = 0.0;
                this.basePosition = 0.0;
                this.duration = 0.0;
                this.lastPositionUpdateTimestamp = 0L;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    public void tick() {
        if (!this.hasTrack) {
            return;
        }
        this.position = this.getPosition();
    }

    public void fetchCoverOnline(String trackArtist, String trackTitle) {
        String query = ((String)(trackArtist.isEmpty() ? trackTitle : trackArtist + " " + trackTitle)).trim();
        if (query.isEmpty() || query.equalsIgnoreCase(this.lastCoverSearchQuery)) {
            return;
        }
        this.lastCoverSearchQuery = query;
        Thread t = new Thread(() -> {
            block10: {
                try {
                    int start;
                    int end;
                    String json;
                    String enc = URLEncoder.encode(query, StandardCharsets.UTF_8);
                    String apiUrl = "https://itunes.apple.com/search?term=" + enc + "&entity=song&limit=1";
                    HttpURLConnection conn = (HttpURLConnection)new URI(apiUrl).toURL().openConnection();
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
                    conn.setConnectTimeout(4000);
                    conn.setReadTimeout(4000);
                    if (conn.getResponseCode() != 200) break block10;
                    try (InputStream is = conn.getInputStream();){
                        json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    }
                    int artIdx = json.indexOf("\"artworkUrl100\":\"");
                    if (artIdx != -1 && (end = json.indexOf("\"", start = artIdx + 17)) != -1) {
                        byte[] bytes;
                        String artUrl = json.substring(start, end).replace("\\/", "/").replace("100x100bb", "600x600bb");
                        HttpURLConnection imgConn = (HttpURLConnection)new URI(artUrl).toURL().openConnection();
                        imgConn.setRequestProperty("User-Agent", "Mozilla/5.0");
                        imgConn.setConnectTimeout(5000);
                        imgConn.setReadTimeout(5000);
                        if (imgConn.getResponseCode() == 200 && (bytes = imgConn.getInputStream().readAllBytes()).length > 0) {
                            Minecraft.getInstance().execute(() -> this.applyCoverBytes(bytes));
                        }
                    }
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
            }
        }, "CoverArtFetcher");
        t.setDaemon(true);
        t.start();
    }

    private void applyCoverBytes(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return;
        }
        this.currentCoverBytes = bytes;
        NanoVGEngine.getInstance().updateGsmtcCoverImage(bytes);
        Minecraft.getInstance().execute(() -> {
            try {
                NativeImage image = NativeImage.read((InputStream)new ByteArrayInputStream(bytes));
                if (image != null) {
                    if (this.coverTexture != null) {
                        this.coverTexture.close();
                    }
                    this.coverTexture = new DynamicTexture(() -> "gsmtc_album_art", image);
                    this.coverTexture.upload();
                    Minecraft.getInstance().getTextureManager().register(COVER_TEXTURE_ID, (AbstractTexture)this.coverTexture);
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        });
    }

    private void updateThumbnail() {
        if (this.bridge == null) {
            return;
        }
        int size = this.bridge.gsmtc_get_thumbnail_size();
        if (size <= 0) {
            if (!this.title.isEmpty()) {
                this.fetchCoverOnline(this.artist, this.title);
            } else {
                this.clearTexture();
            }
            return;
        }
        byte[] imgBytes = new byte[size];
        int read = this.bridge.gsmtc_get_thumbnail_data(imgBytes, size);
        if (read <= 0) {
            if (!this.title.isEmpty()) {
                this.fetchCoverOnline(this.artist, this.title);
            } else {
                this.clearTexture();
            }
            return;
        }
        Minecraft.getInstance().execute(() -> this.applyCoverBytes(imgBytes));
    }

    private void clearTexture() {
        this.currentCoverBytes = null;
        NanoVGEngine.getInstance().updateGsmtcCoverImage(null);
        Minecraft.getInstance().execute(() -> {
            if (this.coverTexture != null) {
                this.coverTexture.close();
                this.coverTexture = null;
            }
        });
    }

    public void play() {
        if (this.bridge != null) {
            try {
                if (MusicPlayer.getInstance().isPlaying()) {
                    Minecraft.getInstance().execute(() -> MusicPlayer.getInstance().pause());
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            this.bridge.gsmtc_play();
            this.lastPositionUpdateTimestamp = System.currentTimeMillis();
        }
    }

    public void pause() {
        if (this.bridge != null) {
            this.bridge.gsmtc_pause();
            this.basePosition = this.getPosition();
            this.lastPositionUpdateTimestamp = System.currentTimeMillis();
        }
    }

    public void togglePlayPause() {
        if (this.bridge != null) {
            try {
                if (!this.isPlaying() && MusicPlayer.getInstance().isPlaying()) {
                    Minecraft.getInstance().execute(() -> MusicPlayer.getInstance().pause());
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            this.bridge.gsmtc_toggle_play_pause();
            if (this.isPlaying()) {
                this.basePosition = this.getPosition();
            }
            this.lastPositionUpdateTimestamp = System.currentTimeMillis();
        }
    }

    public void next() {
        if (this.bridge != null) {
            this.bridge.gsmtc_next();
        }
    }

    public void previous() {
        if (this.bridge != null) {
            this.bridge.gsmtc_previous();
        }
    }

    public void seek(double seconds) {
        this.basePosition = Math.max(0.0, Math.min(seconds, this.duration > 0.0 ? this.duration : seconds));
        this.lastPositionUpdateTimestamp = System.currentTimeMillis();
        this.lastReportedNativePos = this.basePosition;
        this.lastSeekTime = System.currentTimeMillis();
        this.position = this.basePosition;
        if (this.bridge != null) {
            this.bridge.gsmtc_seek(seconds);
        }
    }

    public void seekRelative(double deltaSeconds) {
        this.seek(this.getPosition() + deltaSeconds);
    }

    public float getVolume() {
        if (this.bridge != null) {
            try {
                float v = this.bridge.gsmtc_get_volume();
                if (v >= 0.0f && v <= 1.0f) {
                    this.externalVolume = v;
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        return this.externalVolume;
    }

    public void setVolume(float vol) {
        this.externalVolume = Math.max(0.0f, Math.min(1.0f, vol));
        if (this.bridge != null) {
            try {
                this.bridge.gsmtc_set_volume(this.externalVolume);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
    }

    public boolean hasTrack() {
        return this.hasTrack;
    }

    public boolean isPlaying() {
        return this.status == 1;
    }

    public boolean isPaused() {
        return this.status == 2;
    }

    public int getStatus() {
        return this.status;
    }

    public String getTitle() {
        return this.title;
    }

    public String getArtist() {
        return this.artist;
    }

    public String getAlbum() {
        return this.album;
    }

    public String getSourceApp() {
        return this.sourceApp;
    }

    public double getPosition() {
        if (this.isPlaying() && this.lastPositionUpdateTimestamp > 0L) {
            double elapsed = (double)(System.currentTimeMillis() - this.lastPositionUpdateTimestamp) / 1000.0;
            double cur = this.basePosition + elapsed;
            if (this.duration > 0.0) {
                cur = Math.min(cur, this.duration);
            }
            return Math.max(0.0, cur);
        }
        return Math.max(0.0, this.basePosition);
    }

    public double getDuration() {
        return Math.max(0.0, this.duration);
    }

    public double getProgressFraction() {
        if (this.duration <= 0.0) {
            return 0.0;
        }
        return Math.max(0.0, Math.min(1.0, this.getPosition() / this.duration));
    }

    public boolean hasCover() {
        return this.coverTexture != null;
    }

    public boolean isToastActive() {
        return this.toastEnabled && this.hasTrack && System.currentTimeMillis() - this.toastStartTime < 6000L;
    }

    public float getToastAlpha() {
        long elapsed = System.currentTimeMillis() - this.toastStartTime;
        if (elapsed < 300L) {
            return (float)elapsed / 300.0f;
        }
        if (elapsed > 5500L) {
            return Math.max(0.0f, (float)(6000L - elapsed) / 500.0f);
        }
        return 1.0f;
    }

    public boolean isHudEnabled() {
        return this.hudEnabled;
    }

    public void setHudEnabled(boolean enabled) {
        this.hudEnabled = enabled;
    }

    public void toggleHud() {
        this.hudEnabled = !this.hudEnabled;
    }

    public boolean isToastEnabled() {
        return this.toastEnabled;
    }

    public void setToastEnabled(boolean enabled) {
        this.toastEnabled = enabled;
    }

    public void toggleToast() {
        this.toastEnabled = !this.toastEnabled;
    }

    public String getFormattedPosition() {
        long s = (long)this.position;
        return String.format("%d:%02d", s / 60L, s % 60L);
    }

    public String getFormattedDuration() {
        long s = (long)this.duration;
        return String.format("%d:%02d", s / 60L, s % 60L);
    }

    public void preheatBrowser() {
        if (this.bridge == null) {
            return;
        }
        try {
            File runDir;
            File cacheDir;
            long hwnd = 0L;
            if (hwnd != 0L) {
                this.bridge.browser_set_parent_hwnd(hwnd);
            }
            if (!(cacheDir = new File(new File(runDir = Minecraft.getInstance().gameDirectory, "mp3player"), "browser_cache")).exists()) {
                cacheDir.mkdirs();
            }
            this.bridge.browser_init(cacheDir.getAbsolutePath());
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    public boolean openBrowser(int x, int y, int width, int height, String url) {
        if (this.bridge == null) {
            return false;
        }
        try {
            File runDir;
            File cacheDir;
            long hwnd = 0L;
            if (hwnd != 0L) {
                this.bridge.browser_set_parent_hwnd(hwnd);
            }
            if (!(cacheDir = new File(new File(runDir = Minecraft.getInstance().gameDirectory, "mp3player"), "browser_cache")).exists()) {
                cacheDir.mkdirs();
            }
            this.bridge.browser_init(cacheDir.getAbsolutePath());
            return this.bridge.browser_open(x, y, width, height, url) == 1;
        }
        catch (Throwable t) {
            return false;
        }
    }

    public boolean isBrowserReady() {
        if (this.bridge == null) {
            return false;
        }
        try {
            return this.bridge.browser_is_ready() == 1;
        }
        catch (Throwable ignored) {
            return false;
        }
    }

    public void setBrowserBounds(int x, int y, int width, int height) {
        if (this.bridge != null) {
            try {
                this.bridge.browser_set_bounds(x, y, width, height);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
    }

    public void setBrowserVisible(boolean visible) {
        if (this.bridge != null) {
            try {
                this.bridge.browser_set_visible(visible ? 1 : 0);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
    }

    public void navigateBrowser(String url) {
        if (this.bridge != null) {
            try {
                this.bridge.browser_navigate(url);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
    }

    public void browserGoBack() {
        if (this.bridge != null) {
            try {
                this.bridge.browser_go_back();
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
    }

    public void browserGoForward() {
        if (this.bridge != null) {
            try {
                this.bridge.browser_go_forward();
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
    }

    public void browserReload() {
        if (this.bridge != null) {
            try {
                this.bridge.browser_reload();
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
    }

    public boolean isBrowserOpen() {
        if (this.bridge == null) {
            return false;
        }
        try {
            return this.bridge.browser_is_open() == 1;
        }
        catch (Throwable t) {
            return false;
        }
    }

    public boolean browserWantsClose() {
        if (this.bridge == null) {
            return false;
        }
        try {
            if (this.bridge.browser_wants_close() == 1) {
                this.bridge.browser_clear_wants_close();
                return true;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return false;
    }

    public void closeBrowser() {
        if (this.bridge != null) {
            try {
                this.bridge.browser_close();
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
    }

    static {
        COVER_TEXTURE_ID = Identifier.fromNamespaceAndPath((String)"mp3player", (String)"gsmtc_album_art");
    }
}

