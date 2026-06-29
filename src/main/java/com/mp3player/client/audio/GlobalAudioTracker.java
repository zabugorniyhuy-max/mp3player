package com.mp3player.client.audio;

import com.mp3player.MP3PlayerClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

import javax.sound.sampled.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import com.mp3player.network.RequestTrackPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class GlobalAudioTracker {
    private static final GlobalAudioTracker INSTANCE = new GlobalAudioTracker();
    
    // Map of Player UUID -> GlobalPlayer
    private final Map<UUID, GlobalPlayer> activePlayers = new HashMap<>();
    
    private GlobalAudioTracker() {}
    
    public static GlobalAudioTracker getInstance() {
        return INSTANCE;
    }
    
    public void playGlobal(UUID sender, String trackName, long position, int range, float senderVolume) {
        // Если пустое имя трека — игрок остановил колонку
        if (trackName == null || trackName.isEmpty()) {
            stopGlobal(sender);
            return;
        }
        
        // Если уже играет тот же трек от того же игрока — просто обновляем позицию, не перезапускаем
        if (activePlayers.containsKey(sender)) {
            GlobalPlayer existing = activePlayers.get(sender);
            if (existing.getTrackName().equals(trackName) && existing.isPlaying()) {
                if (!existing.stubMode) {
                    // Трек тот же и это реальный файл — ничего не трогаем, просто обновляем таймстемп и громкость
                    existing.updateLastSeen();
                    existing.senderVolume = senderVolume;
                    existing.updateVolume();
                    existing.syncPosition(position);
                    return;
                }
                // Если мы были в stub-режиме, мы хотим перепроверить, вдруг файл уже скачался!
            }
            // Другой трек или нужно обновить stub — остановим старый
            existing.stop();
        }
        
        File trackFile = null;
        // 1. Поиск в плейлисте
        for (File f : MusicPlayer.getInstance().getPlaylist()) {
            String n = f.getName();
            int dot = n.lastIndexOf('.');
            String fName = dot > 0 ? n.substring(0, dot) : n;
            if (fName.equals(trackName) || n.equals(trackName)) {
                trackFile = f;
                break;
            }
        }
        // 2. Если не найдено, форсированный скан папок (т.к. файл могли только что передать)
        if (trackFile == null) {
            File convertedFolder = new File(new File(MinecraftClient.getInstance().runDirectory, "mp3player"), "converted");
            if (convertedFolder.exists()) {
                File[] files = convertedFolder.listFiles();
                if (files != null) {
                    for (File f : files) {
                        if (f.getName().toLowerCase().endsWith(".wav")) {
                            String n = f.getName();
                            int d = n.lastIndexOf('.');
                            if ((d > 0 ? n.substring(0, d) : n).equalsIgnoreCase(trackName)) { trackFile = f; break; }
                        }
                    }
                }
            }
            if (trackFile == null) {
                File mainFolder = new File(MinecraftClient.getInstance().runDirectory, "mp3player");
                if (mainFolder.exists()) {
                    File[] files = mainFolder.listFiles();
                    if (files != null) {
                        for (File f : files) {
                            if (f.getName().toLowerCase().endsWith(".wav") || com.mp3player.client.audio.AudioConverter.isConvertible(f)) {
                                String n = f.getName();
                                int d = n.lastIndexOf('.');
                                if ((d > 0 ? n.substring(0, d) : n).equalsIgnoreCase(trackName)) { trackFile = f; break; }
                            }
                        }
                    }
                }
            }
        }
        
        // Даже если файла нет, создаём stub-плеер чтобы голограмма над игроком отображалась
        GlobalPlayer player = new GlobalPlayer(sender, trackName, trackFile, range, senderVolume);
        activePlayers.put(sender, player);
        if (trackFile != null) {
            player.play(position);
        } else {
            // Нет файла, но голограмма всё равно будет показывать название трека
            player.setStubPlaying(position);
            player.requestFile();
        }
    }
    
    public void stopGlobal(UUID sender) {
        if (activePlayers.containsKey(sender)) {
            activePlayers.get(sender).stop();
            activePlayers.remove(sender);
        }
    }
    
    public void stopAll() {
        for (GlobalPlayer player : activePlayers.values()) {
            player.stop();
        }
        activePlayers.clear();
    }
    
    public void updateAll() {
        activePlayers.values().removeIf(p -> {
            if (p.isStale()) {
                p.stop();
                return true;
            }
            p.updateVolume();
            return false;
        });
    }
    
    public GlobalPlayer getPlayer(UUID uuid) {
        return activePlayers.get(uuid);
    }
    
    public Map<UUID, GlobalPlayer> getActivePlayers() {
        return activePlayers;
    }
    
    public class GlobalPlayer {
        private final UUID uuid;
        private final String trackName;
        private final File file;
        private Clip currentClip;
        private boolean isPlaying = false;
        private boolean stubMode = false; // Нет файла, но голограмма отображается
        private long startTime = 0;
        private long durationMs = 0;
        private long stubPositionMs = 0;
        private final float maxDistance;
        private float senderVolume;
        private long lastSeenTime = System.currentTimeMillis();
        
        public GlobalPlayer(UUID uuid, String trackName, File file, int range, float senderVolume) {
            this.uuid = uuid;
            this.trackName = trackName;
            this.file = file;
            this.maxDistance = (float) range;
            this.senderVolume = senderVolume;
        }
        
        /** Обновляем отметку о последнем полученном пакете */
        public void updateLastSeen() {
            this.lastSeenTime = System.currentTimeMillis();
        }
        
        /** Проверяем, не устарел ли плеер (нет пакетов > 5 секунд) */
        public boolean isStale() {
            return System.currentTimeMillis() - lastSeenTime > 5000;
        }
        
        /** Stub-режим: файла нет, но голограмма есть */
        public void setStubPlaying(long positionMs) {
            this.stubMode = true;
            this.isPlaying = true;
            this.stubPositionMs = positionMs;
            this.startTime = System.currentTimeMillis() - positionMs;
            this.lastSeenTime = System.currentTimeMillis();
        }
        
        private long lastRequestTime = 0;
        
        public void requestFile() {
            if (System.currentTimeMillis() - lastRequestTime > 10000) { // Запрашиваем раз в 10 секунд
                lastRequestTime = System.currentTimeMillis();
                try {
                    ClientPlayNetworking.send(new RequestTrackPayload(trackName, uuid));
                } catch (Exception e) {}
            }
        }
        
        public void syncPosition(long positionMs) {
            if (stubMode) {
                this.stubPositionMs = positionMs;
                this.startTime = System.currentTimeMillis() - positionMs;
                return;
            }
            if (currentClip != null && isPlaying) {
                long currentMs = currentClip.getMicrosecondPosition() / 1000;
                // Only seek if the difference is more than 500ms to avoid stuttering
                if (Math.abs(currentMs - positionMs) > 500) {
                    currentClip.setMicrosecondPosition(positionMs * 1000);
                }
            }
        }
        
        public void play(long startPositionMs) {
            if (file == null || !file.exists()) return;
            
            File fileToPlay = file;
            if (com.mp3player.client.audio.AudioConverter.isConvertible(file)) {
                File converted = AudioConverter.convertToOgg(file);
                if (converted != null && converted.exists()) {
                    fileToPlay = converted;
                } else return;
            }
            
            try {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(fileToPlay);
                AudioFormat baseFormat = audioStream.getFormat();
                
                AudioFormat targetFormat = new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        baseFormat.getSampleRate(),
                        16,
                        baseFormat.getChannels(),
                        baseFormat.getChannels() * 2,
                        baseFormat.getSampleRate(),
                        false
                );
                
                AudioInputStream decodedStream;
                if (AudioSystem.isConversionSupported(targetFormat, baseFormat)) {
                    decodedStream = AudioSystem.getAudioInputStream(targetFormat, audioStream);
                } else {
                    decodedStream = audioStream;
                }
                
                currentClip = AudioSystem.getClip();
                currentClip.open(decodedStream);
                this.durationMs = currentClip.getMicrosecondLength() / 1000;
                
                try { decodedStream.close(); } catch(Exception e) {}
                try { audioStream.close(); } catch(Exception e) {}
                
                if (startPositionMs > 0) {
                    currentClip.setMicrosecondPosition(startPositionMs * 1000);
                }
                currentClip.start();
                updateVolume();
                isPlaying = true;
                stubMode = false;
                startTime = System.currentTimeMillis();
                lastSeenTime = System.currentTimeMillis();
                
                currentClip.addLineListener(e -> {
                    if (e.getType() == LineEvent.Type.STOP) {
                        stop();
                    }
                });
            } catch (Exception e) {
                MP3PlayerClient.LOGGER.error("Global play error: {}", e.getMessage());
            }
        }
        
        public void updateVolume() {
            if (currentClip != null && isPlaying && !stubMode) {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client.player == null || client.world == null) {
                    setVolume(0);
                    return;
                }
                
                float masterVol = client.options.getSoundVolume(net.minecraft.sound.SoundCategory.MASTER);
                
                if (masterVol == 0) {
                    setVolume(0);
                    return;
                }

                if (client.player.getUuid().equals(uuid)) {
                    setVolume(masterVol);
                    return;
                }
                
                PlayerEntity target = client.world.getPlayerByUuid(uuid);
                if (target == null) {
                    setVolume(0);
                    return;
                }
                
                double distance = Math.sqrt(client.player.squaredDistanceTo(target.getPos()));
                if (distance > maxDistance) {
                    setVolume(0);
                } else {
                    float vol = (float) (1.0 - (distance / maxDistance)) * masterVol * senderVolume;
                    setVolume(vol);
                }
            }
        }
        
        private void setVolume(float vol) {
            if (currentClip != null) {
                try {
                    FloatControl gc = (FloatControl) currentClip.getControl(FloatControl.Type.MASTER_GAIN);
                    float dB = (float) (Math.log10(Math.max(0.0001, vol)) * 20);
                    gc.setValue(Math.max(gc.getMinimum(), Math.min(gc.getMaximum(), dB)));
                } catch (Exception ignored) {}
            }
        }
        
        public void stop() {
            isPlaying = false;
            stubMode = false;
            if (currentClip != null) {
                currentClip.stop();
                currentClip.close();
                currentClip = null;
            }
        }
        
        public boolean isPlaying() {
            return isPlaying;
        }
        
        public String getTrackName() {
            return trackName;
        }
        
        public long getDurationMs() {
            return durationMs;
        }
        
        public long getCurrentPositionMs() {
            return System.currentTimeMillis() - startTime;
        }
    }
}
