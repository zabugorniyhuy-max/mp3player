/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.MinecraftClient
 */
package com.mp3player.client.audio;

import com.mp3player.MP3PlayerClient;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;

public class AudioConverter {
    private static File ffmpegExe = null;
    private static boolean ffmpegChecked = false;

    public static boolean isConvertible(File file) {
        if (file == null) {
            return false;
        }
        String name = file.getName().toLowerCase();
        return name.endsWith(".mp3") || name.endsWith(".flac") || name.endsWith(".ogg") || name.endsWith(".m4a") || name.endsWith(".wma") || name.endsWith(".aac");
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static File extractFFmpeg() {
        File gameDir = Minecraft.getInstance().gameDirectory;
        File mp3playerDir = new File(gameDir, "mp3player");
        File ffmpegFile = new File(mp3playerDir, "ffmpeg.exe");
        if (ffmpegFile.exists()) {
            if (ffmpegFile.length() > 5000000L) {
                return ffmpegFile;
            }
            MP3PlayerClient.LOGGER.warn("FFmpeg file is too small (corrupted?), extracting again...");
            ffmpegFile.delete();
        }
        if (!mp3playerDir.exists()) {
            mp3playerDir.mkdirs();
        }
        MP3PlayerClient.LOGGER.info("Extracting FFmpeg...");
        try (InputStream in = AudioConverter.class.getResourceAsStream("/ffmpeg.exe");){
            if (in == null) {
                MP3PlayerClient.LOGGER.error("FFmpeg not found in mod resources!");
                File file2 = null;
                return file2;
            }
            Files.copy(in, ffmpegFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            MP3PlayerClient.LOGGER.info("FFmpeg extracted to: {}", (Object)ffmpegFile.getAbsolutePath());
            File file = ffmpegFile;
            return file;
        }
        catch (Exception e) {
            MP3PlayerClient.LOGGER.error("Failed to extract FFmpeg: {}", (Object)e.getMessage());
            return null;
        }
    }

    public static boolean isFFmpegAvailable() {
        if (!ffmpegChecked) {
            ffmpegChecked = true;
            ffmpegExe = AudioConverter.extractFFmpeg();
            if (ffmpegExe != null && ffmpegExe.exists()) {
                MP3PlayerClient.LOGGER.info("FFmpeg ready! MP3 conversion enabled.");
            } else {
                MP3PlayerClient.LOGGER.warn("FFmpeg not available. MP3 files won't be converted.");
            }
        }
        return ffmpegExe != null && ffmpegExe.exists();
    }

    public static File getConvertedFolder() {
        File gameDir = Minecraft.getInstance().gameDirectory;
        File convertedDir = new File(gameDir, "mp3player/converted");
        if (!convertedDir.exists()) {
            convertedDir.mkdirs();
        }
        return convertedDir;
    }

    public static File convertToWav(File mp3File) {
        if (!AudioConverter.isFFmpegAvailable()) {
            return null;
        }
        String name = mp3File.getName();
        String baseName = name.substring(0, name.lastIndexOf(46));
        File convertedDir = AudioConverter.getConvertedFolder();
        File wavFile = new File(convertedDir, baseName + ".wav");
        if (wavFile.exists()) {
            MP3PlayerClient.LOGGER.info("WAV already exists, deleting MP3: {}", (Object)wavFile.getName());
            mp3File.delete();
            return wavFile;
        }
        MP3PlayerClient.LOGGER.info("Converting {} to WAV...", (Object)name);
        try {
            String line;
            ProcessBuilder pb = new ProcessBuilder(ffmpegExe.getAbsolutePath(), "-i", mp3File.getAbsolutePath(), "-vn", "-sn", "-acodec", "pcm_s16le", "-ar", "44100", "-ac", "2", "-y", wavFile.getAbsolutePath());
            pb.redirectErrorStream(true);
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((line = reader.readLine()) != null) {
                MP3PlayerClient.LOGGER.debug("FFmpeg: {}", (Object)line);
            }
            int exitCode = process.waitFor();
            if (exitCode == 0 && wavFile.exists()) {
                MP3PlayerClient.LOGGER.info("Converted: {} -> {}", (Object)name, (Object)wavFile.getName());
                mp3File.delete();
                return wavFile;
            }
            MP3PlayerClient.LOGGER.error("FFmpeg conversion failed with code: {}", (Object)exitCode);
        }
        catch (Exception e) {
            MP3PlayerClient.LOGGER.error("Conversion error: {}", (Object)e.getMessage());
        }
        return null;
    }

    public static File convertToOgg(File mp3File) {
        return AudioConverter.convertToWav(mp3File);
    }

    public static CompletableFuture<File> convertToOggAsync(File mp3File) {
        return CompletableFuture.supplyAsync(() -> AudioConverter.convertToOgg(mp3File));
    }

    public static void convertAllMp3InFolder(File folder, Runnable onComplete) {
        if (!AudioConverter.isFFmpegAvailable() || folder == null || !folder.exists()) {
            MP3PlayerClient.LOGGER.warn("Cannot convert: FFmpeg={}, folder={}", (Object)AudioConverter.isFFmpegAvailable(), folder != null ? Boolean.valueOf(folder.exists()) : "null");
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }
        File[] mp3Files = folder.listFiles((dir, name) -> AudioConverter.isConvertible(new File(dir, name)));
        if (mp3Files == null || mp3Files.length == 0) {
            MP3PlayerClient.LOGGER.info("No audio files to convert");
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }
        MP3PlayerClient.LOGGER.info("Found {} audio files to convert", (Object)mp3Files.length);
        CompletableFuture.runAsync(() -> {
            try {
                int converted = 0;
                for (File mp3 : mp3Files) {
                    File result = AudioConverter.convertToWav(mp3);
                    if (result == null) continue;
                    ++converted;
                }
                MP3PlayerClient.LOGGER.info("Conversion complete! {} of {} files converted", (Object)converted, (Object)mp3Files.length);
            }
            catch (Exception e) {
                MP3PlayerClient.LOGGER.error("Error during batch conversion: {}", (Object)e.getMessage());
            }
            finally {
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        });
    }
}

