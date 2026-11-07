/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.network.packet.CustomPayload
 */
package com.mp3player.client.audio;

import com.mp3player.MP3PlayerClient;
import com.mp3player.client.audio.AlbumArtExtractor;
import com.mp3player.client.audio.AudioConverter;
import com.mp3player.client.config.ModConfig;
import com.mp3player.client.gsmtc.GsmtcManager;
import com.mp3player.network.GlobalPlayPayload;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class MusicPlayer {
    private static MusicPlayer instance;
    private Clip currentClip;
    private AudioInputStream currentAudioStream;
    private File currentFile;
    private List<File> playlist = new ArrayList<File>();
    private List<File> favorites = new ArrayList<File>();
    private int currentIndex = 0;
    private float volume = 0.5f;
    private float speed = 1.0f;
    private boolean isPlaying = false;
    private boolean repeat = false;
    private long pausePosition = 0L;
    private File musicFolder;
    private boolean isConverting = false;

    public static MusicPlayer getInstance() {
        if (instance == null) {
            instance = new MusicPlayer();
        }
        return instance;
    }

    public void loadMusicFolder(File folder) {
        this.musicFolder = folder;
        this.refreshPlaylist();
    }

    public void refreshPlaylist() {
        File[] mp3Files;
        if (this.musicFolder == null) {
            return;
        }
        if (!this.isConverting && (mp3Files = this.musicFolder.listFiles((dir, name) -> AudioConverter.isConvertible(new File(dir, name)))) != null && mp3Files.length > 0) {
            this.isConverting = true;
            MP3PlayerClient.LOGGER.info("Found {} audio files, starting conversion...", (Object)mp3Files.length);
            AudioConverter.convertAllMp3InFolder(this.musicFolder, () -> Minecraft.getInstance().execute(() -> {
                this.isConverting = false;
                this.loadPlaylistFiles();
            }));
            return;
        }
        this.loadPlaylistFiles();
    }

    private void loadPlaylistFiles() {
        File[] files;
        File playingFile = this.currentFile;
        int oldSize = this.playlist.size();
        this.playlist.clear();
        File convertedFolder = AudioConverter.getConvertedFolder();
        if (convertedFolder.exists() && convertedFolder.isDirectory() && (files = convertedFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".wav"))) != null) {
            for (File f : files) {
                this.playlist.add(f);
            }
        }
        if (this.musicFolder.exists() && this.musicFolder.isDirectory() && (files = this.musicFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".wav"))) != null) {
            for (File f : files) {
                if (this.playlist.contains(f)) continue;
                this.playlist.add(f);
            }
        }
        this.playlist.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        if (playingFile != null) {
            int newIndex = this.playlist.indexOf(playingFile);
            if (newIndex >= 0) {
                this.currentIndex = newIndex;
            } else if (this.currentIndex >= this.playlist.size()) {
                this.currentIndex = 0;
            }
        }
        this.favorites.clear();
        for (File f : this.playlist) {
            if (!ModConfig.getInstance().favorites.contains(f.getName())) continue;
            this.favorites.add(f);
        }
        if (this.playlist.size() != oldSize) {
            MP3PlayerClient.LOGGER.info("Playlist updated: {} tracks", (Object)this.playlist.size());
        }
    }

    public boolean isConverting() {
        return this.isConverting;
    }

    public void play() {
        if (this.playlist.isEmpty()) {
            MP3PlayerClient.LOGGER.warn("Playlist is empty!");
            return;
        }
        try {
            if (GsmtcManager.getInstance().isPlaying()) {
                GsmtcManager.getInstance().pause();
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        if (this.currentClip != null && !this.isPlaying && this.pausePosition > 0L) {
            this.currentClip.setMicrosecondPosition(this.pausePosition);
            this.currentClip.start();
            this.isPlaying = true;
            return;
        }
        this.playTrack(this.currentIndex);
    }

    public void broadcastState() {
        if (!ModConfig.getInstance().speakerMode) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() == null || mc.player == null) {
            return;
        }
        String track = "";
        long pos = 0L;
        float vol = this.volume;
        if (this.isPlaying && this.currentClip != null) {
            track = this.getCurrentTrackName();
            pos = this.currentClip.getMicrosecondPosition() / 1000L;
            vol = this.volume;
        } else if (GsmtcManager.getInstance().isPlaying()) {
            GsmtcManager g = GsmtcManager.getInstance();
            String t = g.getTitle();
            String a = g.getArtist();
            track = a != null && !a.isEmpty() ? a + " - " + t : t;
            pos = (long)(g.getPosition() * 1000.0);
            vol = g.getVolume();
        }
        try {
            ClientPlayNetworking.send((CustomPacketPayload)new GlobalPlayPayload(track, pos, ModConfig.getInstance().speakerRange, vol));
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public void broadcastStop() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() == null || mc.player == null) {
            return;
        }
        try {
            ClientPlayNetworking.send((CustomPacketPayload)new GlobalPlayPayload("", 0L, 0, 0.0f));
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public void playTrack(int index) {
        if (this.playlist.isEmpty()) {
            MP3PlayerClient.LOGGER.warn("Cannot play: playlist is empty");
            return;
        }
        if (ModConfig.getInstance().muteGlobal) {
            MP3PlayerClient.LOGGER.warn("Cannot play: global mute is enabled");
            return;
        }
        if (index < 0 || index >= this.playlist.size()) {
            MP3PlayerClient.LOGGER.warn("Invalid track index: {}", (Object)index);
            return;
        }
        try {
            if (GsmtcManager.getInstance().isPlaying()) {
                GsmtcManager.getInstance().pause();
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        this.stop();
        this.currentIndex = index;
        this.currentFile = this.playlist.get(index);
        MP3PlayerClient.LOGGER.info("Playing: {}", (Object)this.currentFile.getName());
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(this.currentFile);
            AudioFormat baseFormat = audioStream.getFormat();
            AudioFormat targetFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(), 16, baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);
            AudioInputStream decodedStream = AudioSystem.isConversionSupported(targetFormat, baseFormat) ? AudioSystem.getAudioInputStream(targetFormat, audioStream) : audioStream;
            this.currentClip = AudioSystem.getClip();
            this.currentAudioStream = decodedStream;
            this.currentClip.open(decodedStream);
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
            this.currentAudioStream = null;
            this.currentClip.start();
            this.setVolume(this.volume);
            this.isPlaying = true;
            this.pausePosition = 0L;
            this.currentClip.addLineListener(e -> {
                if (e.getType() == LineEvent.Type.STOP && this.isPlaying && this.currentClip != null && this.currentClip.getMicrosecondPosition() >= this.currentClip.getMicrosecondLength() - 1000L) {
                    if (this.repeat) {
                        this.playTrack(this.currentIndex);
                    } else {
                        this.next();
                    }
                }
            });
            MP3PlayerClient.LOGGER.info("Started playing: {}", (Object)this.currentFile.getName());
            AlbumArtExtractor.loadCoverForFile(this.currentFile, this.getCurrentTrackName(), "");
            this.broadcastState();
        }
        catch (UnsupportedAudioFileException e2) {
            MP3PlayerClient.LOGGER.error("Unsupported format: {} - {}", (Object)this.currentFile.getName(), (Object)e2.getMessage());
        }
        catch (IOException e3) {
            MP3PlayerClient.LOGGER.error("IO error: {}", (Object)e3.getMessage());
        }
        catch (LineUnavailableException e4) {
            MP3PlayerClient.LOGGER.error("Audio line unavailable: {}", (Object)e4.getMessage());
        }
        catch (Exception e5) {
            MP3PlayerClient.LOGGER.error("Error playing {}: {}", (Object)this.currentFile.getName(), (Object)e5.getMessage());
        }
    }

    public void pause() {
        if (this.currentClip != null && this.isPlaying) {
            this.pausePosition = this.currentClip.getMicrosecondPosition();
            this.currentClip.stop();
            this.isPlaying = false;
            this.broadcastState();
        }
    }

    public void stop() {
        if (this.currentClip != null) {
            this.isPlaying = false;
            this.currentClip.stop();
            this.currentClip.close();
            this.currentClip = null;
            this.pausePosition = 0L;
        }
        if (this.currentAudioStream != null) {
            try {
                this.currentAudioStream.close();
            }
            catch (Exception exception) {
                // empty catch block
            }
            this.currentAudioStream = null;
        }
    }

    public void next() {
        if (!this.playlist.isEmpty()) {
            this.currentIndex = (this.currentIndex + 1) % this.playlist.size();
            this.playTrack(this.currentIndex);
        }
    }

    public void previous() {
        if (!this.playlist.isEmpty()) {
            this.currentIndex = (this.currentIndex - 1 + this.playlist.size()) % this.playlist.size();
            this.playTrack(this.currentIndex);
        }
    }

    public void setVolume(float vol) {
        this.volume = Math.max(0.0f, Math.min(1.0f, vol));
        this.applyVolume();
        this.broadcastState();
    }

    public void applyVolume() {
        if (this.currentClip != null) {
            try {
                FloatControl gc = (FloatControl)this.currentClip.getControl(FloatControl.Type.MASTER_GAIN);
                if (this.volume <= 1.0E-4f || ModConfig.getInstance().muteGlobal) {
                    gc.setValue(gc.getMinimum());
                    return;
                }
                float minDb = Math.max(gc.getMinimum(), -40.0f);
                float maxDb = Math.min(gc.getMaximum(), 3.0f);
                float dB = this.volume >= 0.99f ? maxDb : minDb + (float)(Math.pow(this.volume, 0.5) * (double)(maxDb - minDb));
                gc.setValue(Math.max(gc.getMinimum(), Math.min(gc.getMaximum(), dB)));
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    public void seekRelative(long deltaMicros) {
        if (this.currentClip != null) {
            long newPos = Math.max(0L, Math.min(this.currentClip.getMicrosecondLength(), this.currentClip.getMicrosecondPosition() + deltaMicros));
            this.currentClip.setMicrosecondPosition(newPos);
            if (!this.isPlaying) {
                this.pausePosition = newPos;
            } else {
                this.broadcastState();
            }
        }
    }

    public void seekTo(long micros) {
        if (this.currentClip != null) {
            long newPos = Math.max(0L, Math.min(this.currentClip.getMicrosecondLength(), micros));
            this.currentClip.setMicrosecondPosition(newPos);
            if (!this.isPlaying) {
                this.pausePosition = newPos;
            }
        }
    }

    public void setSpeed(float s) {
        this.speed = Math.max(0.5f, Math.min(2.0f, s));
    }

    public void playFile(File file) {
        if (file == null || !file.exists()) {
            MP3PlayerClient.LOGGER.warn("File not found: {}", (Object)file);
            return;
        }
        if (ModConfig.getInstance().muteGlobal) {
            MP3PlayerClient.LOGGER.warn("Cannot play file: global mute is enabled");
            return;
        }
        try {
            if (GsmtcManager.getInstance().isPlaying()) {
                GsmtcManager.getInstance().pause();
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        File fileToPlay = file;
        if (AudioConverter.isConvertible(file)) {
            File converted = AudioConverter.convertToOgg(file);
            if (converted != null && converted.exists()) {
                fileToPlay = converted;
            } else {
                MP3PlayerClient.LOGGER.error("Cannot play audio file without FFmpeg conversion");
                return;
            }
        }
        this.stop();
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(fileToPlay);
            AudioFormat baseFormat = audioStream.getFormat();
            AudioFormat targetFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(), 16, baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);
            AudioInputStream decodedStream = AudioSystem.isConversionSupported(targetFormat, baseFormat) ? AudioSystem.getAudioInputStream(targetFormat, audioStream) : audioStream;
            this.currentClip = AudioSystem.getClip();
            this.currentAudioStream = decodedStream;
            this.currentClip.open(decodedStream);
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
            this.currentAudioStream = null;
            this.currentClip.start();
            this.setVolume(this.volume);
            this.isPlaying = true;
            this.currentFile = file;
            this.pausePosition = 0L;
            AlbumArtExtractor.loadCoverForFile(file, this.getCurrentTrackName(), "");
            MP3PlayerClient.LOGGER.info("Playing file: {}", (Object)file.getName());
        }
        catch (Exception e) {
            MP3PlayerClient.LOGGER.error("Error playing file {}: {}", (Object)file.getName(), (Object)e.getMessage());
        }
    }

    public void toggleFavorite() {
        if (this.currentFile != null) {
            if (this.favorites.contains(this.currentFile)) {
                this.favorites.remove(this.currentFile);
                ModConfig.getInstance().favorites.remove(this.currentFile.getName());
            } else {
                this.favorites.add(this.currentFile);
                if (!ModConfig.getInstance().favorites.contains(this.currentFile.getName())) {
                    ModConfig.getInstance().favorites.add(this.currentFile.getName());
                }
            }
            ModConfig.getInstance().save();
        }
    }

    public void toggleRepeat() {
        this.repeat = !this.repeat;
        MP3PlayerClient.LOGGER.info("Repeat is now " + (this.repeat ? "ON" : "OFF"));
    }

    public boolean isRepeat() {
        return this.repeat;
    }

    public boolean isFavorite() {
        return this.currentFile != null && this.favorites.contains(this.currentFile);
    }

    public List<File> getPlaylist() {
        return this.playlist;
    }

    public List<File> getFavorites() {
        return this.favorites;
    }

    public int getCurrentIndex() {
        return this.currentIndex;
    }

    public float getVolume() {
        return this.volume;
    }

    public float getSpeed() {
        return this.speed;
    }

    public boolean isPlaying() {
        return this.isPlaying;
    }

    public long getCurrentPosition() {
        if (!this.isPlaying && this.pausePosition > 0L) {
            return this.pausePosition;
        }
        return this.currentClip != null ? this.currentClip.getMicrosecondPosition() : 0L;
    }

    public long getDuration() {
        return this.currentClip != null ? this.currentClip.getMicrosecondLength() : 0L;
    }

    public String getCurrentTrackName() {
        if (this.currentFile != null) {
            String n = this.currentFile.getName();
            int d = n.lastIndexOf(46);
            return d > 0 ? n.substring(0, d) : n;
        }
        return "No track";
    }
}

