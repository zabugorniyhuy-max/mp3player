package com.mp3player.client.api.model;

public class Track {
    public final String id;
    public final String title;
    public final String artist;
    public final String album;
    public final long durationMs;
    
    public Track(String id, String title, String artist, String album, long durationMs) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.durationMs = durationMs;
    }
    
    public String getFormattedDuration() {
        long seconds = durationMs / 1000;
        return String.format("%d:%02d", seconds / 60, seconds % 60);
    }
}
