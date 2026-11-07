/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.text.Text
 */
package com.mp3player.client.network;

import com.mp3player.client.audio.MusicPlayer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class FileReceiverManager {
    private static final FileReceiverManager INSTANCE = new FileReceiverManager();
    private final Map<String, ReceiveTask> activeDownloads = new HashMap<String, ReceiveTask>();

    public static FileReceiverManager getInstance() {
        return INSTANCE;
    }

    public void startDownload(String fileName, long fileSize, int totalChunks, UUID sender) {
        File targetFile;
        String key = sender.toString() + "_" + fileName;
        File musicFolder = new File(MinecraftClient.getInstance().runDirectory, "mp3player");
        if (!musicFolder.exists()) {
            musicFolder.mkdirs();
        }
        if ((targetFile = new File(musicFolder, fileName)).exists()) {
            MusicPlayer.getInstance().loadMusicFolder(new File(MinecraftClient.getInstance().runDirectory, "mp3player"));
            return;
        }
        File tempFile = new File(musicFolder, fileName + ".downloading");
        try {
            if (tempFile.exists()) {
                tempFile.delete();
            }
            tempFile.createNewFile();
            ReceiveTask task = new ReceiveTask(fileName, tempFile, targetFile, totalChunks);
            this.activeDownloads.put(key, task);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void receiveChunk(String fileName, int chunkIndex, byte[] data, UUID sender) {
        String key = sender.toString() + "_" + fileName;
        ReceiveTask task = this.activeDownloads.get(key);
        if (task != null) {
            task.writeChunk(data);
        }
    }

    public void endDownload(String fileName, UUID sender) {
        String key = sender.toString() + "_" + fileName;
        ReceiveTask task = this.activeDownloads.remove(key);
        if (task != null) {
            task.finish();
            MusicPlayer.getInstance().loadMusicFolder(new File(MinecraftClient.getInstance().runDirectory, "mp3player"));
            if (MinecraftClient.getInstance().player != null) {
                MinecraftClient.getInstance().player.sendMessage((Text)Text.literal((String)("\u00a7a\u041f\u043e\u043b\u0443\u0447\u0435\u043d\u0430 \u043d\u043e\u0432\u0430\u044f \u043f\u0435\u0441\u043d\u044f: \u00a7e" + fileName)), false);
            }
        }
    }

    public int getDownloadsCount() {
        return this.activeDownloads.size();
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
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void writeChunk(byte[] data) {
            if (this.stream == null) {
                return;
            }
            try {
                this.stream.write(data);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void finish() {
            try {
                if (this.stream != null) {
                    this.stream.close();
                }
                if (this.tempFile.exists()) {
                    this.tempFile.renameTo(this.targetFile);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

