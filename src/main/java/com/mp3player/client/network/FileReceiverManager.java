package com.mp3player.client.network;

import com.mp3player.client.audio.MusicPlayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FileReceiverManager {
    private static final FileReceiverManager INSTANCE = new FileReceiverManager();
    public static FileReceiverManager getInstance() { return INSTANCE; }

    private final Map<String, ReceiveTask> activeDownloads = new HashMap<>();

    public void startDownload(String fileName, long fileSize, int totalChunks, UUID sender) {
        String key = sender.toString() + "_" + fileName;
        File musicFolder = new File(MinecraftClient.getInstance().runDirectory, "mp3player");
        if (!musicFolder.exists()) musicFolder.mkdirs();
        
        File targetFile = new File(musicFolder, fileName);
        if (targetFile.exists()) {
            // Файл уже есть, но нужно обновить плейлист, так как другой игрок мог только что включить этот трек
            MusicPlayer.getInstance().loadMusicFolder(new File(MinecraftClient.getInstance().runDirectory, "mp3player"));
            return;
        }

        File tempFile = new File(musicFolder, fileName + ".downloading");
        try {
            if (tempFile.exists()) tempFile.delete();
            tempFile.createNewFile();
            ReceiveTask task = new ReceiveTask(fileName, tempFile, targetFile, totalChunks);
            activeDownloads.put(key, task);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void receiveChunk(String fileName, int chunkIndex, byte[] data, UUID sender) {
        String key = sender.toString() + "_" + fileName;
        ReceiveTask task = activeDownloads.get(key);
        if (task != null) {
            task.writeChunk(data);
        }
    }

    public void endDownload(String fileName, UUID sender) {
        String key = sender.toString() + "_" + fileName;
        ReceiveTask task = activeDownloads.remove(key);
        if (task != null) {
            task.finish();
            MusicPlayer.getInstance().loadMusicFolder(new File(MinecraftClient.getInstance().runDirectory, "mp3player"));
            if (MinecraftClient.getInstance().player != null) {
                MinecraftClient.getInstance().player.sendMessage(Text.literal("§aПолучена новая песня: §e" + fileName), false);
            }
        }
    }
    
    public int getDownloadsCount() {
        return activeDownloads.size();
    }

    private static class ReceiveTask {
        private final File tempFile;
        private final File targetFile;
        private FileOutputStream stream;

        public ReceiveTask(String fileName, File tempFile, File targetFile, int totalChunks) {
            this.tempFile = tempFile;
            this.targetFile = targetFile;
            try {
                this.stream = new FileOutputStream(tempFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void writeChunk(byte[] data) {
            if (stream == null) return;
            try {
                stream.write(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void finish() {
            try {
                if (stream != null) stream.close();
                if (tempFile.exists()) {
                    tempFile.renameTo(targetFile);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
