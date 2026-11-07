/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.MinecraftClient
 */
package com.mp3player.client.audio;

import com.mp3player.MP3PlayerClient;
import com.mp3player.client.audio.AudioConverter;
import com.mp3player.client.audio.MusicPlayer;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;

public class TrackDownloader {
    private static final File DOWNLOAD_FOLDER = new File(Minecraft.getInstance().gameDirectory, "mp3player");

    public static CompletableFuture<Boolean> downloadTrack(String urlString, String fileName) {
        return CompletableFuture.supplyAsync(() -> {
            block9: {
                try {
                    if (!DOWNLOAD_FOLDER.exists()) {
                        DOWNLOAD_FOLDER.mkdirs();
                    }
                    URL url = new URL(urlString);
                    HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(10000);
                    connection.setReadTimeout(30000);
                    if (connection.getResponseCode() != 200) break block9;
                    File outputFile = new File(DOWNLOAD_FOLDER, TrackDownloader.sanitizeFileName(fileName));
                    try (InputStream in = connection.getInputStream();){
                        Files.copy(in, outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                    MP3PlayerClient.LOGGER.info("Downloaded track: {}", (Object)outputFile.getName());
                    MusicPlayer.getInstance().loadMusicFolder(DOWNLOAD_FOLDER);
                    return true;
                }
                catch (Exception e) {
                    MP3PlayerClient.LOGGER.error("Failed to download track: {}", (Object)e.getMessage());
                }
            }
            return false;
        });
    }

    private static String sanitizeFileName(String name) {
        String sanitized = name.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (!(sanitized.toLowerCase().endsWith(".wav") || AudioConverter.isConvertible(new File(sanitized)) || sanitized.toLowerCase().endsWith(".ogg"))) {
            sanitized = sanitized + ".wav";
        }
        return sanitized;
    }

    public static File getDownloadFolder() {
        return DOWNLOAD_FOLDER;
    }
}

