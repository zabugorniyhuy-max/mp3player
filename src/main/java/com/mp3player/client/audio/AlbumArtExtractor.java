/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.texture.AbstractTexture
 *  net.minecraft.client.texture.NativeImage
 *  net.minecraft.client.texture.NativeImageBackedTexture
 *  net.minecraft.util.Identifier
 */
package com.mp3player.client.audio;

import com.mojang.blaze3d.platform.NativeImage;
import com.mp3player.client.gui.nanovg.NanoVGEngine;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

public class AlbumArtExtractor {
    public static final Identifier LOCAL_COVER_TEXTURE_ID = Identifier.fromNamespaceAndPath((String)"mp3player", (String)"local_album_art");

    public static void loadCoverForFile(File file, String trackTitle, String trackArtist) {
        new Thread(() -> {
            try {
                NanoVGEngine.getInstance().updateLocalCoverImage(null);
                byte[] imgBytes = null;
                if (file != null && file.exists() && (imgBytes = AlbumArtExtractor.extractEmbeddedCover(file)) == null && file.getParentFile() != null) {
                    File[] searchDirs;
                    String rawName = file.getName();
                    int dot = rawName.lastIndexOf(46);
                    String finalBaseName = dot > 0 ? rawName.substring(0, dot) : rawName;
                    File mainDir = new File(Minecraft.getInstance().gameDirectory, "mp3player");
                    File convDir = new File(mainDir, "converted");
                    for (File dir : searchDirs = new File[]{file.getParentFile(), mainDir, convDir, file.getParentFile().getParentFile()}) {
                        if (dir == null || !dir.exists() || !dir.isDirectory()) continue;
                        File trackPng = new File(dir, finalBaseName + ".png");
                        File trackJpg = new File(dir, finalBaseName + ".jpg");
                        if (trackPng.exists()) {
                            imgBytes = Files.readAllBytes(trackPng.toPath());
                            break;
                        }
                        if (trackJpg.exists()) {
                            imgBytes = Files.readAllBytes(trackJpg.toPath());
                            break;
                        }
                        File[] sidecars = dir.listFiles((d, name) -> {
                            String l = name.toLowerCase();
                            return l.equals("cover.jpg") || l.equals("cover.png") || l.equals("folder.jpg") || l.equals("album.jpg") || l.startsWith(finalBaseName.toLowerCase()) && (l.endsWith(".png") || l.endsWith(".jpg"));
                        });
                        if (sidecars == null || sidecars.length <= 0 || !sidecars[0].exists()) continue;
                        imgBytes = Files.readAllBytes(sidecars[0].toPath());
                        break;
                    }
                }
                if (imgBytes != null && imgBytes.length > 0) {
                    byte[] b = imgBytes;
                    NanoVGEngine.getInstance().updateLocalCoverImage(b);
                    Minecraft.getInstance().execute(() -> {
                        try {
                            NativeImage img = NativeImage.read((InputStream)new ByteArrayInputStream(b));
                            if (img != null) {
                                DynamicTexture tex = new DynamicTexture(() -> "local_album_art", img);
                                tex.upload();
                                Minecraft.getInstance().getTextureManager().register(LOCAL_COVER_TEXTURE_ID, (AbstractTexture)tex);
                            }
                        }
                        catch (Throwable throwable) {
                            // empty catch block
                        }
                    });
                    return;
                }
                String q = trackTitle;
                if (trackArtist != null && !trackArtist.isEmpty()) {
                    q = trackArtist + " " + trackTitle;
                }
                AlbumArtExtractor.fetchLocalCoverOnline(q);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }, "AlbumArtExtractor").start();
    }

    private static void fetchLocalCoverOnline(String query) {
        block11: {
            if (query == null || query.trim().isEmpty() || query.equalsIgnoreCase("No track")) {
                return;
            }
            try {
                int start;
                int end;
                String json;
                String enc = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8);
                String apiUrl = "https://itunes.apple.com/search?term=" + enc + "&entity=song&limit=1";
                HttpURLConnection conn = (HttpURLConnection)new URI(apiUrl).toURL().openConnection();
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                conn.setConnectTimeout(4000);
                conn.setReadTimeout(4000);
                if (conn.getResponseCode() != 200) break block11;
                try (InputStream is = conn.getInputStream();){
                    json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                }
                int artIdx = json.indexOf("\"artworkUrl100\":\"");
                if (artIdx != -1 && (end = json.indexOf("\"", start = artIdx + 17)) != -1) {
                    byte[] bytes;
                    String artUrl = json.substring(start, end).replace("\\/", "/").replace("100x100bb", "600x600bb");
                    HttpURLConnection imgConn = (HttpURLConnection)new URI(artUrl).toURL().openConnection();
                    imgConn.setRequestProperty("User-Agent", "Mozilla/5.0");
                    imgConn.setConnectTimeout(4000);
                    imgConn.setReadTimeout(4000);
                    if (imgConn.getResponseCode() == 200 && (bytes = imgConn.getInputStream().readAllBytes()) != null && bytes.length > 0) {
                        NanoVGEngine.getInstance().updateLocalCoverImage(bytes);
                        Minecraft.getInstance().execute(() -> {
                            try {
                                NativeImage img = NativeImage.read((InputStream)new ByteArrayInputStream(bytes));
                                if (img != null) {
                                    DynamicTexture tex = new DynamicTexture(() -> "local_album_art", img);
                                    tex.upload();
                                    Minecraft.getInstance().getTextureManager().register(LOCAL_COVER_TEXTURE_ID, (AbstractTexture)tex);
                                }
                            }
                            catch (Throwable throwable) {
                                // empty catch block
                            }
                        });
                    }
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static byte[] extractEmbeddedCover(File file) {
        if (!file.getName().toLowerCase().endsWith(".mp3")) {
            return null;
        }
        try (FileInputStream fis = new FileInputStream(file);){
            int readLen = Math.min((int)file.length(), 0x300000);
            byte[] buf = new byte[readLen];
            int n = fis.read(buf);
            if (n < 64) {
                byte[] byArray = null;
                return byArray;
            }
            int i = 0;
            while (i < n - 4) {
                int j;
                int end;
                if ((buf[i] & 0xFF) == 255 && (buf[i + 1] & 0xFF) == 216 && (buf[i + 2] & 0xFF) == 255) {
                    end = -1;
                    for (j = i + 3; j < n - 1; ++j) {
                        if ((buf[j] & 0xFF) != 255 || (buf[j + 1] & 0xFF) != 217) continue;
                        end = j + 2;
                        break;
                    }
                    if (end > i && end - i > 1024) {
                        byte[] jpeg = new byte[end - i];
                        System.arraycopy(buf, i, jpeg, 0, jpeg.length);
                        byte[] byArray = jpeg;
                        return byArray;
                    }
                }
                if ((buf[i] & 0xFF) == 137 && buf[i + 1] == 80 && buf[i + 2] == 78 && buf[i + 3] == 71 && buf[i + 4] == 13 && buf[i + 5] == 10 && buf[i + 6] == 26 && buf[i + 7] == 10) {
                    end = -1;
                    for (j = i + 8; j < n - 8; ++j) {
                        if (buf[j] != 73 || buf[j + 1] != 69 || buf[j + 2] != 78 || buf[j + 3] != 68) continue;
                        end = j + 8;
                        break;
                    }
                    if (end > i && end - i > 1024) {
                        byte[] png = new byte[end - i];
                        System.arraycopy(buf, i, png, 0, png.length);
                        byte[] byArray = png;
                        return byArray;
                    }
                }
                ++i;
            }
            return null;
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return null;
    }
}

