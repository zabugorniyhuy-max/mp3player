package com.mp3player.client.audio;

import com.mp3player.MP3PlayerClient;
import net.minecraft.client.MinecraftClient;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CompletableFuture;

public class TrackDownloader {
    private static final File DOWNLOAD_FOLDER = new File(MinecraftClient.getInstance().runDirectory, "mp3player");
    
    public static CompletableFuture<Boolean> downloadTrack(String urlString, String fileName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (!DOWNLOAD_FOLDER.exists()) {
                    DOWNLOAD_FOLDER.mkdirs();
                }
                
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(30000);
                
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    File outputFile = new File(DOWNLOAD_FOLDER, sanitizeFileName(fileName));
                    
                    try (InputStream in = connection.getInputStream()) {
                        Files.copy(in, outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                    
                    MP3PlayerClient.LOGGER.info("Downloaded track: {}", outputFile.getName());
                    MusicPlayer.getInstance().loadMusicFolder(DOWNLOAD_FOLDER);
                    return true;
                }
            } catch (Exception e) {
                MP3PlayerClient.LOGGER.error("Failed to download track: {}", e.getMessage());
            }
            return false;
        });
    }
    
    private static String sanitizeFileName(String name) {
        String sanitized = name.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (!sanitized.toLowerCase().endsWith(".wav") && 
            !com.mp3player.client.audio.AudioConverter.isConvertible(new java.io.File(sanitized)) &&
            !sanitized.toLowerCase().endsWith(".ogg")) {
            sanitized += ".wav";
        }
        return sanitized;
    }
    
    public static File getDownloadFolder() {
        return DOWNLOAD_FOLDER;
    }
}
