/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.network.packet.CustomPayload
 *  net.minecraft.sound.SoundCategory
 */
package com.mp3player.client.audio;

import com.mp3player.MP3PlayerClient;
import com.mp3player.client.audio.AudioConverter;
import com.mp3player.client.audio.MusicPlayer;
import com.mp3player.network.RequestTrackPayload;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class GlobalAudioTracker {
    private static final GlobalAudioTracker INSTANCE = new GlobalAudioTracker();
    private final Map<UUID, GlobalPlayer> activePlayers = new HashMap<UUID, GlobalPlayer>();

    private GlobalAudioTracker() {
    }

    public static GlobalAudioTracker getInstance() {
        return INSTANCE;
    }

    public void playGlobal(UUID sender, String trackName, long position, int range, float senderVolume) {
        if (trackName == null || trackName.isEmpty()) {
            this.stopGlobal(sender);
            return;
        }
        if (this.activePlayers.containsKey(sender)) {
            GlobalPlayer existing = this.activePlayers.get(sender);
            if (existing.getTrackName().equals(trackName) && existing.isPlaying() && !existing.stubMode) {
                existing.updateLastSeen();
                existing.senderVolume = senderVolume;
                existing.updateVolume();
                existing.syncPosition(position);
                return;
            }
            existing.stop();
        }
        File trackFile = null;
        for (File f : MusicPlayer.getInstance().getPlaylist()) {
            String n = f.getName();
            int dot = n.lastIndexOf('.');
            String fName = dot > 0 ? n.substring(0, dot) : n;
            if (!fName.equalsIgnoreCase(trackName) && !n.equalsIgnoreCase(trackName)) continue;
            trackFile = f;
            break;
        }
        if (trackFile == null) {
            File[] files;
            File mainFolder;
            File[] files2;
            File convertedFolder = new File(new File(Minecraft.getInstance().gameDirectory, "mp3player"), "converted");
            if (convertedFolder.exists() && (files2 = convertedFolder.listFiles()) != null) {
                for (File f : files2) {
                    String n;
                    int d;
                    if (!f.getName().toLowerCase().endsWith(".wav") || !((d = (n = f.getName()).lastIndexOf(46)) > 0 ? n.substring(0, d) : n).equalsIgnoreCase(trackName)) continue;
                    trackFile = f;
                    break;
                }
            }
            if (trackFile == null && (mainFolder = new File(Minecraft.getInstance().gameDirectory, "mp3player")).exists() && (files = mainFolder.listFiles()) != null) {
                for (File f : files) {
                    String n;
                    int d;
                    if (!f.getName().toLowerCase().endsWith(".wav") && !AudioConverter.isConvertible(f) || !((d = (n = f.getName()).lastIndexOf(46)) > 0 ? n.substring(0, d) : n).equalsIgnoreCase(trackName)) continue;
                    trackFile = f;
                    break;
                }
            }
        }
        GlobalPlayer player = new GlobalPlayer(this, sender, trackName, trackFile, range, senderVolume);
        this.activePlayers.put(sender, player);
        if (trackFile != null) {
            player.play(position);
        } else {
            player.setStubPlaying(position);
            player.requestFile();
        }
    }

    public void stopGlobal(UUID sender) {
        if (this.activePlayers.containsKey(sender)) {
            this.activePlayers.get(sender).stop();
            this.activePlayers.remove(sender);
        }
    }

    public void stopAll() {
        for (GlobalPlayer player : this.activePlayers.values()) {
            player.stop();
        }
        this.activePlayers.clear();
    }

    public void updateAll() {
        this.activePlayers.values().removeIf(p -> {
            if (p.isStale()) {
                p.stop();
                return true;
            }
            p.updateVolume();
            return false;
        });
    }

    public GlobalPlayer getPlayer(UUID uuid) {
        return this.activePlayers.get(uuid);
    }

    public Map<UUID, GlobalPlayer> getActivePlayers() {
        return this.activePlayers;
    }

    public class GlobalPlayer {
        private final UUID uuid;
        private final String trackName;
        private final File file;
        private Clip currentClip;
        private boolean isPlaying = false;
        private boolean stubMode = false;
        private long startTime = 0L;
        private long durationMs = 0L;
        private long stubPositionMs = 0L;
        private final float maxDistance;
        private float senderVolume;
        private long lastSeenTime = System.currentTimeMillis();
        private long lastRequestTime = 0L;

        public GlobalPlayer(GlobalAudioTracker this$0, UUID uuid, String trackName, File file, int range, float senderVolume) {
            this.uuid = uuid;
            this.trackName = trackName;
            this.file = file;
            this.maxDistance = range;
            this.senderVolume = senderVolume;
        }

        public void updateLastSeen() {
            this.lastSeenTime = System.currentTimeMillis();
        }

        public boolean isStale() {
            return System.currentTimeMillis() - this.lastSeenTime > 5000L;
        }

        public void setStubPlaying(long positionMs) {
            this.stubMode = true;
            this.isPlaying = true;
            this.stubPositionMs = positionMs;
            this.startTime = System.currentTimeMillis() - positionMs;
            this.lastSeenTime = System.currentTimeMillis();
        }

        public void requestFile() {
            if (System.currentTimeMillis() - this.lastRequestTime > 10000L) {
                this.lastRequestTime = System.currentTimeMillis();
                try {
                    ClientPlayNetworking.send((CustomPacketPayload)new RequestTrackPayload(this.trackName, this.uuid));
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        }

        public void syncPosition(long positionMs) {
            long currentMs;
            if (this.stubMode) {
                this.stubPositionMs = positionMs;
                this.startTime = System.currentTimeMillis() - positionMs;
                return;
            }
            if (this.currentClip != null && this.isPlaying && Math.abs((currentMs = this.currentClip.getMicrosecondPosition() / 1000L) - positionMs) > 500L) {
                this.currentClip.setMicrosecondPosition(positionMs * 1000L);
            }
        }

        public void play(long startPositionMs) {
            if (this.file == null || !this.file.exists()) {
                return;
            }
            File fileToPlay = this.file;
            if (AudioConverter.isConvertible(this.file)) {
                File converted = AudioConverter.convertToOgg(this.file);
                if (converted != null && converted.exists()) {
                    fileToPlay = converted;
                } else {
                    return;
                }
            }
            try {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(fileToPlay);
                AudioFormat baseFormat = audioStream.getFormat();
                AudioFormat targetFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(), 16, baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);
                AudioInputStream decodedStream = AudioSystem.isConversionSupported(targetFormat, baseFormat) ? AudioSystem.getAudioInputStream(targetFormat, audioStream) : audioStream;
                this.currentClip = AudioSystem.getClip();
                this.currentClip.open(decodedStream);
                this.durationMs = this.currentClip.getMicrosecondLength() / 1000L;
                try {
                    decodedStream.close();
                }
                catch (Exception exception) {
                    // empty catch block
                }
                try {
                    audioStream.close();
                }
                catch (Exception exception) {
                    // empty catch block
                }
                if (startPositionMs > 0L) {
                    this.currentClip.setMicrosecondPosition(startPositionMs * 1000L);
                }
                this.currentClip.start();
                this.updateVolume();
                this.isPlaying = true;
                this.stubMode = false;
                this.startTime = System.currentTimeMillis();
                this.lastSeenTime = System.currentTimeMillis();
                this.currentClip.addLineListener(e -> {
                    if (e.getType() == LineEvent.Type.STOP) {
                        this.stop();
                    }
                });
            }
            catch (Exception e2) {
                MP3PlayerClient.LOGGER.error("Global play error: {}", (Object)e2.getMessage());
            }
        }

        public void updateVolume() {
            if (this.currentClip != null && this.isPlaying && !this.stubMode) {
                Minecraft client = Minecraft.getInstance();
                if (client.player == null || client.level == null) {
                    this.setVolume(0.0f);
                    return;
                }
                float masterVol = client.options.getFinalSoundSourceVolume(SoundSource.MASTER);
                if (masterVol == 0.0f) {
                    this.setVolume(0.0f);
                    return;
                }
                if (client.player.getUUID().equals(this.uuid)) {
                    this.setVolume(masterVol);
                    return;
                }
                Player target = client.level.getPlayerByUUID(this.uuid);
                if (target == null) {
                    this.setVolume(0.0f);
                    return;
                }
                double distance = Math.sqrt(client.player.distanceToSqr((Entity)target));
                if (distance > (double)this.maxDistance) {
                    this.setVolume(0.0f);
                } else {
                    float vol = (float)(1.0 - distance / (double)this.maxDistance) * masterVol * this.senderVolume;
                    this.setVolume(vol);
                }
            }
        }

        private void setVolume(float vol) {
            if (this.currentClip != null) {
                try {
                    FloatControl gc = (FloatControl)this.currentClip.getControl(FloatControl.Type.MASTER_GAIN);
                    float dB = (float)(Math.log10(Math.max(1.0E-4, (double)vol)) * 20.0);
                    gc.setValue(Math.max(gc.getMinimum(), Math.min(gc.getMaximum(), dB)));
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        }

        public void stop() {
            this.isPlaying = false;
            this.stubMode = false;
            if (this.currentClip != null) {
                this.currentClip.stop();
                this.currentClip.close();
                this.currentClip = null;
            }
        }

        public boolean isPlaying() {
            return this.isPlaying;
        }

        public String getTrackName() {
            return this.trackName;
        }

        public long getDurationMs() {
            return this.durationMs;
        }

        public long getCurrentPositionMs() {
            return System.currentTimeMillis() - this.startTime;
        }
    }
}

