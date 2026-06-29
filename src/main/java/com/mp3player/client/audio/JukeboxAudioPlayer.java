package com.mp3player.client.audio;

import com.mp3player.MP3PlayerClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

import javax.sound.sampled.*;
import java.io.File;

public class JukeboxAudioPlayer {
    private final BlockPos pos;
    private final File currentFile;
    private Clip currentClip;
    private boolean isPlaying = false;
    private float maxDistance = 64.0f; // Максимальная дистанция звука
    private long startTime = 0;
    private long durationMs = 0;
    private String trackName = "Unknown";

    public JukeboxAudioPlayer(BlockPos pos, File file) {
        this.pos = pos;
        this.currentFile = file;
        if (file != null) {
            String n = file.getName();
            int dot = n.lastIndexOf('.');
            this.trackName = dot > 0 ? n.substring(0, dot) : n;
        }
    }

    public void play() {
        if (currentFile == null || !currentFile.exists()) {
            return;
        }

        File fileToPlay = currentFile;
        if (com.mp3player.client.audio.AudioConverter.isConvertible(currentFile)) {
            File converted = AudioConverter.convertToOgg(currentFile);
            if (converted != null && converted.exists()) {
                fileToPlay = converted;
            } else {
                return;
            }
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
            
            updateVolume();
            currentClip.start();
            isPlaying = true;
            startTime = System.currentTimeMillis();

            currentClip.addLineListener(e -> {
                if (e.getType() == LineEvent.Type.STOP) {
                    stop();
                }
            });
        } catch (Exception e) {
            MP3PlayerClient.LOGGER.error("Jukebox play error: {}", e.getMessage());
        }
    }

    public void updateVolume() {
        if (currentClip != null && isPlaying) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null) {
                setVolume(0);
                return;
            }

            double distance = Math.sqrt(client.player.squaredDistanceTo(pos.toCenterPos()));
            
            float masterVol = client.options.getSoundVolume(net.minecraft.sound.SoundCategory.MASTER);
            
            if (masterVol == 0 || distance > maxDistance) {
                setVolume(0);
            } else {
                float vol = (float) (1.0 - (distance / maxDistance)) * masterVol;
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
        if (currentClip != null) {
            isPlaying = false;
            currentClip.stop();
            currentClip.close();
            currentClip = null;
        }
    }

    public BlockPos getPos() {
        return pos;
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
