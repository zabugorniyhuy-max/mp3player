/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.network.packet.CustomPayload
 */
package com.mp3player.client.network;

import com.mp3player.network.FileTransferChunkPayload;
import com.mp3player.network.FileTransferEndPayload;
import com.mp3player.network.FileTransferStartPayload;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.UUID;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.CustomPayload;

public class FileSenderManager {
    private static final FileSenderManager INSTANCE = new FileSenderManager();
    private static final int CHUNK_SIZE = 16000;
    private static final int CHUNKS_PER_TICK = 4;
    private final Queue<UploadTask> uploadQueue = new ArrayDeque<UploadTask>();
    private UploadTask currentTask = null;

    public static FileSenderManager getInstance() {
        return INSTANCE;
    }

    public void startUpload(File file, UUID target) {
        if (!file.exists()) {
            return;
        }
        UUID sender = MinecraftClient.getInstance().player.getUuid();
        this.uploadQueue.add(new UploadTask(file, sender, target));
    }

    public void tick() {
        if (this.currentTask == null) {
            this.currentTask = this.uploadQueue.poll();
            if (this.currentTask != null) {
                this.currentTask.start();
            }
        }
        if (this.currentTask != null) {
            boolean finished = false;
            for (int i = 0; i < 4; ++i) {
                if (!this.currentTask.tick()) continue;
                finished = true;
                break;
            }
            if (finished) {
                this.currentTask.finish();
                this.currentTask = null;
            }
        }
    }

    public int getUploadsCount() {
        return this.uploadQueue.size() + (this.currentTask != null ? 1 : 0);
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
            this.totalChunks = (int)Math.ceil((double)this.fileSize / 16000.0);
        }

        public void start() {
            ClientPlayNetworking.send((CustomPayload)new FileTransferStartPayload(this.file.getName(), this.fileSize, this.totalChunks, this.sender, this.target));
            try {
                this.stream = new FileInputStream(this.file);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        public boolean tick() {
            if (this.stream == null || this.currentChunk >= this.totalChunks) {
                return true;
            }
            try {
                byte[] buffer = new byte[16000];
                int bytesRead = this.stream.read(buffer);
                if (bytesRead > 0) {
                    byte[] actualData;
                    if (bytesRead < 16000) {
                        actualData = new byte[bytesRead];
                        System.arraycopy(buffer, 0, actualData, 0, bytesRead);
                    } else {
                        actualData = buffer;
                    }
                    ClientPlayNetworking.send((CustomPayload)new FileTransferChunkPayload(this.file.getName(), this.currentChunk, actualData, this.sender, this.target));
                    ++this.currentChunk;
                }
                return bytesRead < 16000;
            }
            catch (Exception e) {
                e.printStackTrace();
                return true;
            }
        }

        public void finish() {
            try {
                if (this.stream != null) {
                    this.stream.close();
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
            ClientPlayNetworking.send((CustomPayload)new FileTransferEndPayload(this.file.getName(), this.sender, this.target));
        }
    }
}

