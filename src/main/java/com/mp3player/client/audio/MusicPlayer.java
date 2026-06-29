package com.mp3player.client.audio;

import com.mp3player.MP3PlayerClient;
import com.mp3player.client.config.ModConfig;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MusicPlayer {
    private static MusicPlayer instance;
    private Clip currentClip;
    private AudioInputStream currentAudioStream;
    private File currentFile;
    private List<File> playlist = new ArrayList<>();
    private List<File> favorites = new ArrayList<>();
    private int currentIndex = 0;
    private float volume = 0.5f;
    private float speed = 1.0f;
    private boolean isPlaying = false;
    private boolean repeat = false;
    private long pausePosition = 0;
    private File musicFolder;

    public static MusicPlayer getInstance() {
        if (instance == null) instance = new MusicPlayer();
        return instance;
    }

    public void loadMusicFolder(File folder) {
        this.musicFolder = folder;
        refreshPlaylist();
    }

    private boolean isConverting = false;
    
    public void refreshPlaylist() {
        if (musicFolder == null) return;
        
        // Сначала конвертируем MP3 если есть
        if (!isConverting) {
            File[] mp3Files = musicFolder.listFiles((dir, name) -> com.mp3player.client.audio.AudioConverter.isConvertible(new java.io.File(dir, name)));
            if (mp3Files != null && mp3Files.length > 0) {
                isConverting = true;
                MP3PlayerClient.LOGGER.info("Found {} audio files, starting conversion...", mp3Files.length);
                AudioConverter.convertAllMp3InFolder(musicFolder, () -> {
                    net.minecraft.client.MinecraftClient.getInstance().execute(() -> {
                        isConverting = false;
                        loadPlaylistFiles();
                    });
                });
                return;
            }
        }
        
        loadPlaylistFiles();
    }
    
    private void loadPlaylistFiles() {
        File playingFile = currentFile;
        int oldSize = playlist.size();
        playlist.clear();
        
        // Загружаем WAV из папки converted (конвертированные MP3)
        File convertedFolder = AudioConverter.getConvertedFolder();
        if (convertedFolder.exists() && convertedFolder.isDirectory()) {
            File[] files = convertedFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".wav"));
            if (files != null) {
                for (File f : files) playlist.add(f);
            }
        }
        
        // Также загружаем WAV из основной папки mp3player
        if (musicFolder.exists() && musicFolder.isDirectory()) {
            File[] files = musicFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".wav"));
            if (files != null) {
                for (File f : files) {
                    if (!playlist.contains(f)) playlist.add(f);
                }
            }
        }
        
        playlist.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        
        // Восстановить индекс текущего трека
        if (playingFile != null) {
            int newIndex = playlist.indexOf(playingFile);
            if (newIndex >= 0) currentIndex = newIndex;
            else if (currentIndex >= playlist.size()) currentIndex = 0;
        }
        
        favorites.clear();
        for (File f : playlist) {
            if (ModConfig.getInstance().favorites.contains(f.getName())) {
                favorites.add(f);
            }
        }
        
        if (playlist.size() != oldSize) {
            MP3PlayerClient.LOGGER.info("Playlist updated: {} tracks", playlist.size());
        }
    }
    
    public boolean isConverting() {
        return isConverting;
    }

    public void play() {
        if (playlist.isEmpty()) {
            MP3PlayerClient.LOGGER.warn("Playlist is empty!");
            return;
        }
        
        // Если есть пауза - продолжить
        if (currentClip != null && !isPlaying && pausePosition > 0) {
            currentClip.setMicrosecondPosition(pausePosition);
            currentClip.start();
            isPlaying = true;
            return;
        }
        
        // Иначе играть текущий трек
        playTrack(currentIndex);
    }
    
    public void broadcastState() {
        if (!ModConfig.getInstance().speakerMode) return;
        // Проверяем что мы подключены к серверу
        net.minecraft.client.MinecraftClient mc = net.minecraft.client.MinecraftClient.getInstance();
        if (mc.getNetworkHandler() == null || mc.player == null) return;
        
        String track = "";
        long pos = 0;
        if (isPlaying && currentClip != null) {
            track = getCurrentTrackName();
            pos = currentClip.getMicrosecondPosition() / 1000;
        }
        try {
            ClientPlayNetworking.send(new com.mp3player.network.GlobalPlayPayload(track, pos, ModConfig.getInstance().speakerRange, this.volume));
        } catch (Exception e) {
            // Игнорируем ошибку если не подключены
        }
    }

    public void playTrack(int index) {
        if (playlist.isEmpty()) {
            MP3PlayerClient.LOGGER.warn("Cannot play: playlist is empty");
            return;
        }
        if (ModConfig.getInstance().muteGlobal) {
            MP3PlayerClient.LOGGER.warn("Cannot play: global mute is enabled");
            return;
        }
        if (index < 0 || index >= playlist.size()) {
            MP3PlayerClient.LOGGER.warn("Invalid track index: {}", index);
            return;
        }
        
        stop();
        currentIndex = index;
        currentFile = playlist.get(index);
        
        MP3PlayerClient.LOGGER.info("Playing: {}", currentFile.getName());
        
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(currentFile);
            AudioFormat baseFormat = audioStream.getFormat();
            
            // Конвертируем в PCM если нужно
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
            currentAudioStream = decodedStream;
            currentClip.open(decodedStream);
            
            // Release the file lock immediately because Clip loads it entirely into RAM
            try { decodedStream.close(); } catch(Exception e) {}
            try { audioStream.close(); } catch(Exception e) {}
            currentAudioStream = null;
            
            currentClip.start();
            setVolume(volume);
            isPlaying = true;
            pausePosition = 0;
            
            currentClip.addLineListener(e -> {
                if (e.getType() == LineEvent.Type.STOP && isPlaying) {
                    if (currentClip != null && currentClip.getMicrosecondPosition() >= currentClip.getMicrosecondLength() - 1000) {
                        if (repeat) {
                            playTrack(currentIndex);
                        } else {
                            next();
                        }
                    }
                }
            });
            
            MP3PlayerClient.LOGGER.info("Started playing: {}", currentFile.getName());
            broadcastState();
            
        } catch (UnsupportedAudioFileException e) {
            MP3PlayerClient.LOGGER.error("Unsupported format: {} - {}", currentFile.getName(), e.getMessage());
        } catch (IOException e) {
            MP3PlayerClient.LOGGER.error("IO error: {}", e.getMessage());
        } catch (LineUnavailableException e) {
            MP3PlayerClient.LOGGER.error("Audio line unavailable: {}", e.getMessage());
        } catch (Exception e) {
            MP3PlayerClient.LOGGER.error("Error playing {}: {}", currentFile.getName(), e.getMessage());
        }
    }

    public void pause() {
        if (currentClip != null && isPlaying) {
            pausePosition = currentClip.getMicrosecondPosition();
            currentClip.stop();
            isPlaying = false;
            broadcastState();
        }
    }

    public void stop() {
        if (currentClip != null) {
            isPlaying = false;
            currentClip.stop();
            currentClip.close();
            currentClip = null;
            pausePosition = 0;
        }
        if (currentAudioStream != null) {
            try { currentAudioStream.close(); } catch(Exception e) {}
            currentAudioStream = null;
        }
    }

    public void next() {
        if (!playlist.isEmpty()) {
            currentIndex = (currentIndex + 1) % playlist.size();
            playTrack(currentIndex);
        }
    }

    public void previous() {
        if (!playlist.isEmpty()) {
            currentIndex = (currentIndex - 1 + playlist.size()) % playlist.size();
            playTrack(currentIndex);
        }
    }

    public void setVolume(float vol) {
        this.volume = Math.max(0, Math.min(1, vol));
        applyVolume();
        broadcastState(); // Force sync when volume changes
    }
    
    public void applyVolume() {
        if (currentClip != null) {
            try {
                float masterVolume = net.minecraft.client.MinecraftClient.getInstance().options.getSoundVolume(net.minecraft.sound.SoundCategory.MASTER);
                float finalVolume = this.volume * masterVolume;
                
                FloatControl gc = (FloatControl) currentClip.getControl(FloatControl.Type.MASTER_GAIN);
                float dB = (float) (Math.log10(Math.max(0.0001, finalVolume)) * 20);
                gc.setValue(Math.max(gc.getMinimum(), Math.min(gc.getMaximum(), dB)));
            } catch (Exception ignored) {}
        }
    }

    public void seekRelative(long deltaMicros) {
        if (currentClip != null) {
            long newPos = Math.max(0, Math.min(currentClip.getMicrosecondLength(), 
                currentClip.getMicrosecondPosition() + deltaMicros));
            currentClip.setMicrosecondPosition(newPos);
            if (!isPlaying) pausePosition = newPos;
            else broadcastState();
        }
    }

    public void seekTo(long micros) {
        if (currentClip != null) {
            long newPos = Math.max(0, Math.min(currentClip.getMicrosecondLength(), micros));
            currentClip.setMicrosecondPosition(newPos);
            if (!isPlaying) pausePosition = newPos;
        }
    }

    public void setSpeed(float s) { this.speed = Math.max(0.5f, Math.min(2.0f, s)); }
    
    /**
     * Воспроизводит отдельный файл (не из плейлиста)
     * Если это MP3 - сначала конвертирует через FFmpeg
     */
    public void playFile(File file) {
        if (file == null || !file.exists()) {
            MP3PlayerClient.LOGGER.warn("File not found: {}", file);
            return;
        }
        if (ModConfig.getInstance().muteGlobal) {
            MP3PlayerClient.LOGGER.warn("Cannot play file: global mute is enabled");
            return;
        }
        
        File fileToPlay = file;
        
        // Если MP3 - конвертируем в OGG
        if (com.mp3player.client.audio.AudioConverter.isConvertible(file)) {
            File converted = AudioConverter.convertToOgg(file);
            if (converted != null && converted.exists()) {
                fileToPlay = converted;
            } else {
                MP3PlayerClient.LOGGER.error("Cannot play audio file without FFmpeg conversion");
                return;
            }
        }
        
        stop();
        
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
            currentAudioStream = decodedStream;
            currentClip.open(decodedStream);
            
            // Release the file lock immediately because Clip loads it entirely into RAM
            try { decodedStream.close(); } catch(Exception e) {}
            try { audioStream.close(); } catch(Exception e) {}
            currentAudioStream = null;
            
            currentClip.start();
            setVolume(volume);
            isPlaying = true;
            currentFile = file;
            pausePosition = 0;
            
            MP3PlayerClient.LOGGER.info("Playing file: {}", file.getName());
            
        } catch (Exception e) {
            MP3PlayerClient.LOGGER.error("Error playing file {}: {}", file.getName(), e.getMessage());
        }
    }
    
    public void toggleFavorite() { 
        if (currentFile != null) { 
            if (favorites.contains(currentFile)) {
                favorites.remove(currentFile);
                ModConfig.getInstance().favorites.remove(currentFile.getName());
            } else {
                favorites.add(currentFile);
                if (!ModConfig.getInstance().favorites.contains(currentFile.getName())) {
                    ModConfig.getInstance().favorites.add(currentFile.getName());
                }
            }
            ModConfig.getInstance().save();
        } 
    }
    
    public void toggleRepeat() {
        this.repeat = !this.repeat;
        MP3PlayerClient.LOGGER.info("Repeat is now " + (this.repeat ? "ON" : "OFF"));
    }
    
    public boolean isRepeat() { return repeat; }
    public boolean isFavorite() { return currentFile != null && favorites.contains(currentFile); }
    public List<File> getPlaylist() { return playlist; }
    public List<File> getFavorites() { return favorites; }
    public int getCurrentIndex() { return currentIndex; }
    public float getVolume() { return volume; }
    public float getSpeed() { return speed; }
    public boolean isPlaying() { return isPlaying; }
    public long getCurrentPosition() { return currentClip != null ? currentClip.getMicrosecondPosition() : 0; }
    public long getDuration() { return currentClip != null ? currentClip.getMicrosecondLength() : 0; }
    public String getCurrentTrackName() {
        if (currentFile != null) { 
            String n = currentFile.getName(); 
            int d = n.lastIndexOf('.'); 
            return d > 0 ? n.substring(0, d) : n; 
        }
        return "No track";
    }
}
