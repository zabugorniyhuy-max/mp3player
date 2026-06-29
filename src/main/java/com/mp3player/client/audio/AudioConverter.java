package com.mp3player.client.audio;

import com.mp3player.MP3PlayerClient;
import net.minecraft.client.MinecraftClient;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CompletableFuture;

public class AudioConverter {
    private static File ffmpegExe = null;
    private static boolean ffmpegChecked = false;
    
    public static boolean isConvertible(File file) {
        if (file == null) return false;
        String name = file.getName().toLowerCase();
        return name.endsWith(".mp3") || name.endsWith(".flac") || 
               name.endsWith(".ogg") || name.endsWith(".m4a") || 
               name.endsWith(".wma") || name.endsWith(".aac");
    }
    
    /**
     * Извлекает FFmpeg из ресурсов мода в папку игры
     */
    private static File extractFFmpeg() {
        File gameDir = MinecraftClient.getInstance().runDirectory;
        File mp3playerDir = new File(gameDir, "mp3player");
        File ffmpegFile = new File(mp3playerDir, "ffmpeg.exe");
        
        // Если уже извлечён и размер похож на правду (FFmpeg весит десятки МБ) - возвращаем
        if (ffmpegFile.exists()) {
            if (ffmpegFile.length() > 5_000_000) { // Минимум 5 МБ
                return ffmpegFile;
            } else {
                MP3PlayerClient.LOGGER.warn("FFmpeg file is too small (corrupted?), extracting again...");
                ffmpegFile.delete();
            }
        }
        
        if (!mp3playerDir.exists()) {
            mp3playerDir.mkdirs();
        }
        
        MP3PlayerClient.LOGGER.info("Extracting FFmpeg...");
        
        try (InputStream in = AudioConverter.class.getResourceAsStream("/ffmpeg.exe")) {
            if (in == null) {
                MP3PlayerClient.LOGGER.error("FFmpeg not found in mod resources!");
                return null;
            }
            
            Files.copy(in, ffmpegFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            MP3PlayerClient.LOGGER.info("FFmpeg extracted to: {}", ffmpegFile.getAbsolutePath());
            return ffmpegFile;
            
        } catch (Exception e) {
            MP3PlayerClient.LOGGER.error("Failed to extract FFmpeg: {}", e.getMessage());
            return null;
        }
    }
    
    public static boolean isFFmpegAvailable() {
        if (!ffmpegChecked) {
            ffmpegChecked = true;
            ffmpegExe = extractFFmpeg();
            
            if (ffmpegExe != null && ffmpegExe.exists()) {
                MP3PlayerClient.LOGGER.info("FFmpeg ready! MP3 conversion enabled.");
            } else {
                MP3PlayerClient.LOGGER.warn("FFmpeg not available. MP3 files won't be converted.");
            }
        }
        return ffmpegExe != null && ffmpegExe.exists();
    }
    
    /**
     * Возвращает папку для конвертированных WAV файлов
     */
    public static File getConvertedFolder() {
        File gameDir = MinecraftClient.getInstance().runDirectory;
        File convertedDir = new File(gameDir, "mp3player/converted");
        if (!convertedDir.exists()) {
            convertedDir.mkdirs();
        }
        return convertedDir;
    }
    
    /**
     * Конвертирует MP3 в WAV используя FFmpeg
     * WAV поддерживается Java нативно
     */
    public static File convertToWav(File mp3File) {
        if (!isFFmpegAvailable()) {
            return null;
        }
        
        String name = mp3File.getName();
        String baseName = name.substring(0, name.lastIndexOf('.'));
        
        // WAV файлы создаются в отдельной папке converted
        File convertedDir = getConvertedFolder();
        File wavFile = new File(convertedDir, baseName + ".wav");
        
        // Если WAV уже существует, просто удаляем исходный MP3 чтобы не мешался
        if (wavFile.exists()) {
            MP3PlayerClient.LOGGER.info("WAV already exists, deleting MP3: {}", wavFile.getName());
            mp3File.delete();
            return wavFile;
        }
        
        MP3PlayerClient.LOGGER.info("Converting {} to WAV...", name);
        
        try {
            ProcessBuilder pb = new ProcessBuilder(
                ffmpegExe.getAbsolutePath(),
                "-i", mp3File.getAbsolutePath(),
                "-vn", // Отключаем видео (обложки)
                "-sn", // Отключаем субтитры
                "-acodec", "pcm_s16le",
                "-ar", "44100",
                "-ac", "2",
                "-y",
                wavFile.getAbsolutePath()
            );
            pb.redirectErrorStream(true);
            
            Process process = pb.start();
            
            // Читаем вывод чтобы процесс не завис
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                MP3PlayerClient.LOGGER.debug("FFmpeg: {}", line);
            }
            
            int exitCode = process.waitFor();
            
            if (exitCode == 0 && wavFile.exists()) {
                MP3PlayerClient.LOGGER.info("Converted: {} -> {}", name, wavFile.getName());
                mp3File.delete(); // Удаляем исходный MP3 файл после успешной конвертации
                return wavFile;
            } else {
                MP3PlayerClient.LOGGER.error("FFmpeg conversion failed with code: {}", exitCode);
            }
        } catch (Exception e) {
            MP3PlayerClient.LOGGER.error("Conversion error: {}", e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Для совместимости - вызывает convertToWav
     */
    public static File convertToOgg(File mp3File) {
        return convertToWav(mp3File);
    }
    
    /**
     * Асинхронная конвертация
     */
    public static CompletableFuture<File> convertToOggAsync(File mp3File) {
        return CompletableFuture.supplyAsync(() -> convertToOgg(mp3File));
    }
    
    /**
     * Конвертирует все MP3 файлы в папке
     */
    public static void convertAllMp3InFolder(File folder, Runnable onComplete) {
        if (!isFFmpegAvailable() || folder == null || !folder.exists()) {
            MP3PlayerClient.LOGGER.warn("Cannot convert: FFmpeg={}, folder={}", 
                isFFmpegAvailable(), folder != null ? folder.exists() : "null");
            if (onComplete != null) onComplete.run();
            return;
        }
        
        File[] mp3Files = folder.listFiles((dir, name) -> isConvertible(new java.io.File(dir, name)));
        if (mp3Files == null || mp3Files.length == 0) {
            MP3PlayerClient.LOGGER.info("No audio files to convert");
            if (onComplete != null) onComplete.run();
            return;
        }
        
        MP3PlayerClient.LOGGER.info("Found {} audio files to convert", mp3Files.length);
        
        CompletableFuture.runAsync(() -> {
            try {
                int converted = 0;
                for (File mp3 : mp3Files) {
                    File result = convertToWav(mp3);
                    if (result != null) converted++;
                }
                MP3PlayerClient.LOGGER.info("Conversion complete! {} of {} files converted", converted, mp3Files.length);
            } catch (Exception e) {
                MP3PlayerClient.LOGGER.error("Error during batch conversion: {}", e.getMessage());
            } finally {
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        });
    }
}
