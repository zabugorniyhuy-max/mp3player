/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.sound.SoundCategory
 *  net.minecraft.util.math.BlockPos
 */
package com.mp3player.client.audio;

import com.mp3player.MP3PlayerClient;
import com.mp3player.client.audio.AudioConverter;
import java.io.File;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;

public class JukeboxAudioPlayer {
    private final BlockPos pos;
    private final File currentFile;
    private Clip currentClip;
    private boolean isPlaying = false;
    private float maxDistance = 64.0f;
    private long startTime = 0L;
    private long durationMs = 0L;
    private String trackName = "Unknown";
    private boolean isPaused = false;
    private long pausedPositionMs = 0L;

    public JukeboxAudioPlayer(BlockPos pos, File file) {
        this.pos = pos;
        this.currentFile = file;
        if (file != null) {
            String n = file.getName();
            int dot = n.lastIndexOf(46);
            this.trackName = dot > 0 ? n.substring(0, dot) : n;
        }
    }

    public void play() {
        if (this.currentFile == null || !this.currentFile.exists()) {
            return;
        }
        File fileToPlay = this.currentFile;
        if (AudioConverter.isConvertible(this.currentFile)) {
            File converted = AudioConverter.convertToOgg(this.currentFile);
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
            this.updateVolume();
            this.currentClip.start();
            this.isPlaying = true;
            this.startTime = System.currentTimeMillis();
            this.currentClip.addLineListener(e -> {
                if (e.getType() == LineEvent.Type.STOP) {
                    this.stop();
                }
            });
        }
        catch (Exception e2) {
            MP3PlayerClient.LOGGER.error("Jukebox play error: {}", (Object)e2.getMessage());
        }
    }

    public void updateVolume() {
        if (this.currentClip != null && (this.isPlaying || this.isPaused)) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null) {
                this.setVolume(0.0f);
                return;
            }
            double distance = Math.sqrt(client.player.squaredDistanceTo(this.pos.toCenterPos()));
            float masterVol = client.options.getSoundVolume(SoundCategory.MASTER);
            if (masterVol == 0.0f || distance > (double)this.maxDistance) {
                this.setVolume(0.0f);
            } else {
                float vol = (float)(1.0 - distance / (double)this.maxDistance) * masterVol;
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

    public void pause() {
        if (this.currentClip != null && this.isPlaying) {
            this.pausedPositionMs = this.currentClip.getMicrosecondPosition() / 1000L;
            this.currentClip.stop();
            this.isPlaying = false;
            this.isPaused = true;
        }
    }

    public void resume() {
        if (this.currentClip != null && this.isPaused) {
            this.currentClip.setMicrosecondPosition(this.pausedPositionMs * 1000L);
            this.updateVolume();
            this.currentClip.start();
            this.startTime = System.currentTimeMillis() - this.pausedPositionMs;
            this.isPlaying = true;
            this.isPaused = false;
        }
    }

    public void togglePlayPause() {
        if (this.isPlaying) {
            this.pause();
        } else if (this.isPaused) {
            this.resume();
        } else {
            this.play();
        }
    }

    public void stop() {
        if (this.currentClip != null) {
            this.isPlaying = false;
            this.isPaused = false;
            try {
                this.currentClip.stop();
                this.currentClip.close();
            }
            catch (Exception exception) {
                // empty catch block
            }
            this.currentClip = null;
        }
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public boolean isPlaying() {
        return this.isPlaying;
    }

    public boolean isPaused() {
        return this.isPaused;
    }

    public boolean isPlayingOrPaused() {
        return this.isPlaying || this.isPaused;
    }

    public String getTrackName() {
        return this.trackName;
    }

    public long getDurationMs() {
        return this.durationMs;
    }

    public long getCurrentPositionMs() {
        if (this.isPaused) {
            return this.pausedPositionMs;
        }
        if (this.isPlaying && this.startTime > 0L) {
            return Math.max(0L, System.currentTimeMillis() - this.startTime);
        }
        return 0L;
    }
}

