/*
 * Decompiled with CFR 0.152.
 */
package com.mp3player.client.audio;

import com.mp3player.MP3PlayerClient;
import com.mp3player.client.api.YandexMusicApi;
import com.mp3player.client.api.model.Track;
import java.io.BufferedInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;

public class YandexPlayer {
    private static YandexPlayer instance;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private List<Track> queue = new ArrayList<Track>();
    private int currentIndex = 0;
    private Track currentTrack;
    private Clip currentClip;
    private boolean isPlaying = false;
    private boolean isLoading = false;
    private float volume = 0.5f;

    public static YandexPlayer getInstance() {
        if (instance == null) {
            instance = new YandexPlayer();
        }
        return instance;
    }

    public void setQueue(List<Track> tracks) {
        this.queue = new ArrayList<Track>(tracks);
        this.currentIndex = 0;
    }

    public void playTrack(int index) {
        if (index < 0 || index >= this.queue.size()) {
            return;
        }
        this.currentIndex = index;
        this.playTrack(this.queue.get(index));
    }

    public void playTrack(Track track) {
        this.stop();
        this.currentTrack = track;
        this.isLoading = true;
        MP3PlayerClient.LOGGER.info("Loading Yandex track: {} - {}", (Object)track.artist, (Object)track.title);
        CompletableFuture.runAsync(() -> {
            try {
                String url = YandexMusicApi.getInstance().getTrackDownloadUrl(track.id).join();
                if (url != null) {
                    MP3PlayerClient.LOGGER.info("Got URL, playing...");
                    this.playFromUrl(url);
                } else {
                    MP3PlayerClient.LOGGER.error("Failed to get track URL (need Yandex Plus subscription?)");
                }
            }
            catch (Exception e) {
                MP3PlayerClient.LOGGER.error("Error playing Yandex track: {}", (Object)e.getMessage());
            }
            this.isLoading = false;
        }, this.executor);
    }

    private void playFromUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(new BufferedInputStream(url.openStream()));
            AudioFormat baseFormat = audioStream.getFormat();
            AudioFormat targetFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(), 16, baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);
            AudioInputStream decodedStream = AudioSystem.getAudioInputStream(targetFormat, audioStream);
            this.currentClip = AudioSystem.getClip();
            this.currentClip.open(decodedStream);
            this.setVolume(this.volume);
            this.currentClip.start();
            this.isPlaying = true;
            this.currentClip.addLineListener(e -> {
                if (e.getType() == LineEvent.Type.STOP && this.isPlaying && this.currentClip != null && this.currentClip.getMicrosecondPosition() >= this.currentClip.getMicrosecondLength() - 1000L) {
                    this.next();
                }
            });
            MP3PlayerClient.LOGGER.info("Playing Yandex: {} - {}", (Object)this.currentTrack.artist, (Object)this.currentTrack.title);
        }
        catch (Exception e2) {
            MP3PlayerClient.LOGGER.error("Error streaming from URL: {}", (Object)e2.getMessage());
        }
    }

    public void play() {
        if (this.currentClip != null && !this.isPlaying) {
            this.currentClip.start();
            this.isPlaying = true;
        } else if (this.currentTrack == null && !this.queue.isEmpty()) {
            this.playTrack(this.currentIndex);
        }
    }

    public void pause() {
        if (this.currentClip != null && this.isPlaying) {
            this.currentClip.stop();
            this.isPlaying = false;
        }
    }

    public void stop() {
        if (this.currentClip != null) {
            this.isPlaying = false;
            this.currentClip.stop();
            this.currentClip.close();
            this.currentClip = null;
        }
    }

    public void next() {
        if (this.queue.isEmpty()) {
            return;
        }
        this.currentIndex = (this.currentIndex + 1) % this.queue.size();
        this.playTrack(this.currentIndex);
    }

    public void previous() {
        if (this.queue.isEmpty()) {
            return;
        }
        this.currentIndex = (this.currentIndex - 1 + this.queue.size()) % this.queue.size();
        this.playTrack(this.currentIndex);
    }

    public void setVolume(float vol) {
        this.volume = Math.max(0.0f, Math.min(1.0f, vol));
        if (this.currentClip != null) {
            try {
                FloatControl gc = (FloatControl)this.currentClip.getControl(FloatControl.Type.MASTER_GAIN);
                float dB = (float)(Math.log10(Math.max(1.0E-4, (double)this.volume)) * 20.0);
                gc.setValue(Math.max(gc.getMinimum(), Math.min(gc.getMaximum(), dB)));
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    public void seekTo(long micros) {
        if (this.currentClip != null) {
            this.currentClip.setMicrosecondPosition(Math.max(0L, Math.min(this.currentClip.getMicrosecondLength(), micros)));
        }
    }

    public void seekRelative(long delta) {
        if (this.currentClip != null) {
            this.seekTo(this.currentClip.getMicrosecondPosition() + delta);
        }
    }

    public Track getCurrentTrack() {
        return this.currentTrack;
    }

    public List<Track> getQueue() {
        return this.queue;
    }

    public int getCurrentIndex() {
        return this.currentIndex;
    }

    public boolean isPlaying() {
        return this.isPlaying;
    }

    public boolean isLoading() {
        return this.isLoading;
    }

    public float getVolume() {
        return this.volume;
    }

    public long getCurrentPosition() {
        return this.currentClip != null ? this.currentClip.getMicrosecondPosition() : 0L;
    }

    public long getDuration() {
        return this.currentClip != null ? this.currentClip.getMicrosecondLength() : (this.currentTrack != null ? this.currentTrack.durationMs * 1000L : 0L);
    }

    public String getCurrentTrackName() {
        if (this.currentTrack != null) {
            return this.currentTrack.artist + " - " + this.currentTrack.title;
        }
        return "\u041d\u0438\u0447\u0435\u0433\u043e \u043d\u0435 \u0438\u0433\u0440\u0430\u0435\u0442";
    }

    public void shutdown() {
        this.stop();
        try {
            this.executor.shutdownNow();
        } catch (Throwable ignored) {}
    }
}

