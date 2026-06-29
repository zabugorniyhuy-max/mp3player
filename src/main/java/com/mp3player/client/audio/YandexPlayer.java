package com.mp3player.client.audio;

import com.mp3player.MP3PlayerClient;
import com.mp3player.client.api.YandexMusicApi;
import com.mp3player.client.api.model.Track;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class YandexPlayer {
    private static YandexPlayer instance;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    
    private List<Track> queue = new ArrayList<>();
    private int currentIndex = 0;
    private Track currentTrack;
    
    private Clip currentClip;
    private boolean isPlaying = false;
    private boolean isLoading = false;
    private float volume = 0.5f;
    
    public static YandexPlayer getInstance() {
        if (instance == null) instance = new YandexPlayer();
        return instance;
    }
    
    public void setQueue(List<Track> tracks) {
        this.queue = new ArrayList<>(tracks);
        this.currentIndex = 0;
    }
    
    public void playTrack(int index) {
        if (index < 0 || index >= queue.size()) return;
        currentIndex = index;
        playTrack(queue.get(index));
    }
    
    public void playTrack(Track track) {
        stop();
        currentTrack = track;
        isLoading = true;
        
        MP3PlayerClient.LOGGER.info("Loading Yandex track: {} - {}", track.artist, track.title);
        
        CompletableFuture.runAsync(() -> {
            try {
                String url = YandexMusicApi.getInstance().getTrackDownloadUrl(track.id).join();
                if (url != null) {
                    MP3PlayerClient.LOGGER.info("Got URL, playing...");
                    playFromUrl(url);
                } else {
                    MP3PlayerClient.LOGGER.error("Failed to get track URL (need Yandex Plus subscription?)");
                }
            } catch (Exception e) {
                MP3PlayerClient.LOGGER.error("Error playing Yandex track: {}", e.getMessage());
            }
            isLoading = false;
        }, executor);
    }
    
    private void playFromUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(new BufferedInputStream(url.openStream()));
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
            
            AudioInputStream decodedStream = AudioSystem.getAudioInputStream(targetFormat, audioStream);
            
            currentClip = AudioSystem.getClip();
            currentClip.open(decodedStream);
            setVolume(volume);
            currentClip.start();
            isPlaying = true;
            
            currentClip.addLineListener(e -> {
                if (e.getType() == LineEvent.Type.STOP && isPlaying) {
                    if (currentClip != null && currentClip.getMicrosecondPosition() >= currentClip.getMicrosecondLength() - 1000) {
                        next();
                    }
                }
            });
            
            MP3PlayerClient.LOGGER.info("Playing Yandex: {} - {}", currentTrack.artist, currentTrack.title);
            
        } catch (Exception e) {
            MP3PlayerClient.LOGGER.error("Error streaming from URL: {}", e.getMessage());
        }
    }
    
    public void play() {
        if (currentClip != null && !isPlaying) {
            currentClip.start();
            isPlaying = true;
        } else if (currentTrack == null && !queue.isEmpty()) {
            playTrack(currentIndex);
        }
    }
    
    public void pause() {
        if (currentClip != null && isPlaying) {
            currentClip.stop();
            isPlaying = false;
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
    
    public void next() {
        if (queue.isEmpty()) return;
        currentIndex = (currentIndex + 1) % queue.size();
        playTrack(currentIndex);
    }
    
    public void previous() {
        if (queue.isEmpty()) return;
        currentIndex = (currentIndex - 1 + queue.size()) % queue.size();
        playTrack(currentIndex);
    }
    
    public void setVolume(float vol) {
        this.volume = Math.max(0, Math.min(1, vol));
        if (currentClip != null) {
            try {
                FloatControl gc = (FloatControl) currentClip.getControl(FloatControl.Type.MASTER_GAIN);
                float dB = (float) (Math.log10(Math.max(0.0001, volume)) * 20);
                gc.setValue(Math.max(gc.getMinimum(), Math.min(gc.getMaximum(), dB)));
            } catch (Exception ignored) {}
        }
    }
    
    public void seekTo(long micros) {
        if (currentClip != null) {
            currentClip.setMicrosecondPosition(Math.max(0, Math.min(currentClip.getMicrosecondLength(), micros)));
        }
    }
    
    public void seekRelative(long delta) {
        if (currentClip != null) {
            seekTo(currentClip.getMicrosecondPosition() + delta);
        }
    }
    
    // Getters
    public Track getCurrentTrack() { return currentTrack; }
    public List<Track> getQueue() { return queue; }
    public int getCurrentIndex() { return currentIndex; }
    public boolean isPlaying() { return isPlaying; }
    public boolean isLoading() { return isLoading; }
    public float getVolume() { return volume; }
    public long getCurrentPosition() { return currentClip != null ? currentClip.getMicrosecondPosition() : 0; }
    public long getDuration() { return currentClip != null ? currentClip.getMicrosecondLength() : (currentTrack != null ? currentTrack.durationMs * 1000 : 0); }
    
    public String getCurrentTrackName() {
        if (currentTrack != null) {
            return currentTrack.artist + " - " + currentTrack.title;
        }
        return "Ничего не играет";
    }
}
