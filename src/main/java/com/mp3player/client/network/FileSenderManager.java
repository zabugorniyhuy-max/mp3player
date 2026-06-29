package com.mp3player.client.network;

import com.mp3player.network.FileTransferChunkPayload;
import com.mp3player.network.FileTransferEndPayload;
import com.mp3player.network.FileTransferStartPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.UUID;

public class FileSenderManager {
    private static final FileSenderManager INSTANCE = new FileSenderManager();
    public static FileSenderManager getInstance() { return INSTANCE; }

    private static final int CHUNK_SIZE = 16000; // 30 KB
    private static final int CHUNKS_PER_TICK = 4; // 120 KB/tick = ~2.4 MB/sec

    private final Queue<UploadTask> uploadQueue = new ArrayDeque<>();
    private UploadTask currentTask = null;

    public void startUpload(File file, UUID target) {
        if (!file.exists()) return;
        UUID sender = MinecraftClient.getInstance().player.getUuid();
        uploadQueue.add(new UploadTask(file, sender, target));
    }

    public void tick() {
        if (currentTask == null) {
            currentTask = uploadQueue.poll();
            if (currentTask != null) {
                currentTask.start();
            }
        }

        if (currentTask != null) {
            boolean finished = false;
            for (int i = 0; i < CHUNKS_PER_TICK; i++) {
                if (currentTask.tick()) {
                    finished = true;
                    break;
                }
            }
            if (finished) {
                currentTask.finish();
                currentTask = null;
            }
        }
    }
    
    public int getUploadsCount() {
        return uploadQueue.size() + (currentTask != null ? 1 : 0);
    }

    private static class UploadTask {
        private final File file;
        private final UUID sender;
        private final UUID target;
        private final long fileSize;
        private final int totalChunks;
        
        private int currentChunk = 0;
        private FileInputStream stream;

        public UploadTask(File file, UUID sender, UUID target) {
            this.file = file;
            this.sender = sender;
            this.target = target;
            this.fileSize = file.length();
            this.totalChunks = (int) Math.ceil((double) fileSize / CHUNK_SIZE);
        }

        public void start() {
            ClientPlayNetworking.send(new FileTransferStartPayload(file.getName(), fileSize, totalChunks, sender, target));
            try {
                stream = new FileInputStream(file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Returns true if finished
        public boolean tick() {
            if (stream == null || currentChunk >= totalChunks) return true;

            try {
                byte[] buffer = new byte[CHUNK_SIZE];
                int bytesRead = stream.read(buffer);
                if (bytesRead > 0) {
                    byte[] actualData;
                    if (bytesRead < CHUNK_SIZE) {
                        actualData = new byte[bytesRead];
                        System.arraycopy(buffer, 0, actualData, 0, bytesRead);
                    } else {
                        actualData = buffer;
                    }

                    ClientPlayNetworking.send(new FileTransferChunkPayload(file.getName(), currentChunk, actualData, sender, target));
                    currentChunk++;
                }
                
                return bytesRead < CHUNK_SIZE;
            } catch (Exception e) {
                e.printStackTrace();
                return true;
            }
        }

        public void finish() {
            try {
                if (stream != null) stream.close();
            } catch (Exception ignored) {}
            ClientPlayNetworking.send(new FileTransferEndPayload(file.getName(), sender, target));
        }
    }
}
