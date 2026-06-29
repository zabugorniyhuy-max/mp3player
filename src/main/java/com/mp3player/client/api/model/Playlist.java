package com.mp3player.client.api.model;

public class Playlist {
    public final String kind;
    public final String ownerUid;
    public final String title;
    public final int trackCount;
    
    public Playlist(String kind, String ownerUid, String title, int trackCount) {
        this.kind = kind;
        this.ownerUid = ownerUid;
        this.title = title;
        this.trackCount = trackCount;
    }
}
