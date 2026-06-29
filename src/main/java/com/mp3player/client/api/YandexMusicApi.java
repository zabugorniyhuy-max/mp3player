package com.mp3player.client.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mp3player.MP3PlayerClient;
import com.mp3player.client.api.model.Playlist;
import com.mp3player.client.api.model.Track;
import com.mp3player.client.api.model.UserInfo;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class YandexMusicApi {
    private static final String BASE_URL = "https://api.music.yandex.net";
    private static final ExecutorService executor = Executors.newCachedThreadPool();
    private static final Gson gson = new Gson();
    private static final HttpClient httpClient = HttpClient.newBuilder().build();
    
    private String token;
    private String userId;
    private UserInfo userInfo;
    
    private static YandexMusicApi instance;
    
    public static YandexMusicApi getInstance() {
        if (instance == null) instance = new YandexMusicApi();
        return instance;
    }
    
    public void setToken(String token) { this.token = token; }
    public String getToken() { return token; }
    public boolean isLoggedIn() { return token != null && !token.isEmpty() && userInfo != null; }
    public UserInfo getUserInfo() { return userInfo; }
    
    private HttpRequest.Builder createRequest(String endpoint) {
        return HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + endpoint))
            .header("Authorization", "OAuth " + token)
            .header("Accept", "application/json");
    }
    
    public CompletableFuture<Boolean> login(String oauthToken) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                this.token = oauthToken;
                HttpRequest request = createRequest("/account/status").GET().build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    JsonObject json = gson.fromJson(response.body(), JsonObject.class);
                    JsonObject account = json.getAsJsonObject("result").getAsJsonObject("account");
                    this.userId = account.get("uid").getAsString();
                    String displayName = account.has("displayName") ? 
                        account.get("displayName").getAsString() : account.get("login").getAsString();
                    this.userInfo = new UserInfo(userId, displayName);
                    MP3PlayerClient.LOGGER.info("Yandex Music: Logged in as {}", displayName);
                    return true;
                }
            } catch (Exception e) {
                MP3PlayerClient.LOGGER.error("Yandex Music login failed: {}", e.getMessage());
            }
            this.token = null;
            return false;
        }, executor);
    }
    
    public void logout() {
        this.token = null;
        this.userId = null;
        this.userInfo = null;
    }
    
    public CompletableFuture<List<Track>> getLikedTracks() {
        return CompletableFuture.supplyAsync(() -> {
            List<Track> tracks = new ArrayList<>();
            if (!isLoggedIn()) return tracks;
            try {
                HttpRequest request = createRequest("/users/" + userId + "/likes/tracks").GET().build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    JsonObject json = gson.fromJson(response.body(), JsonObject.class);
                    JsonArray library = json.getAsJsonObject("result").getAsJsonArray("library");
                    for (var item : library) {
                        JsonObject trackObj = item.getAsJsonObject().getAsJsonObject("track");
                        if (trackObj != null) tracks.add(parseTrack(trackObj));
                    }
                }
            } catch (Exception e) {
                MP3PlayerClient.LOGGER.error("Failed to get liked tracks: {}", e.getMessage());
            }
            return tracks;
        }, executor);
    }
    
    public CompletableFuture<List<Playlist>> getPlaylists() {
        return CompletableFuture.supplyAsync(() -> {
            List<Playlist> playlists = new ArrayList<>();
            if (!isLoggedIn()) return playlists;
            try {
                HttpRequest request = createRequest("/users/" + userId + "/playlists/list").GET().build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    JsonObject json = gson.fromJson(response.body(), JsonObject.class);
                    JsonArray result = json.getAsJsonArray("result");
                    for (var item : result) {
                        JsonObject pl = item.getAsJsonObject();
                        playlists.add(new Playlist(
                            pl.get("kind").getAsString(),
                            userId,
                            pl.get("title").getAsString(),
                            pl.has("trackCount") ? pl.get("trackCount").getAsInt() : 0
                        ));
                    }
                }
            } catch (Exception e) {
                MP3PlayerClient.LOGGER.error("Failed to get playlists: {}", e.getMessage());
            }
            return playlists;
        }, executor);
    }
    
    public CompletableFuture<List<Track>> getPlaylistTracks(String kind, String ownerUid) {
        return CompletableFuture.supplyAsync(() -> {
            List<Track> tracks = new ArrayList<>();
            if (!isLoggedIn()) return tracks;
            try {
                HttpRequest request = createRequest("/users/" + ownerUid + "/playlists/" + kind).GET().build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    JsonObject json = gson.fromJson(response.body(), JsonObject.class);
                    JsonArray tracksArray = json.getAsJsonObject("result").getAsJsonArray("tracks");
                    if (tracksArray != null) {
                        for (var item : tracksArray) {
                            JsonObject trackObj = item.getAsJsonObject().getAsJsonObject("track");
                            if (trackObj != null) tracks.add(parseTrack(trackObj));
                        }
                    }
                }
            } catch (Exception e) {
                MP3PlayerClient.LOGGER.error("Failed to get playlist tracks: {}", e.getMessage());
            }
            return tracks;
        }, executor);
    }
    
    public CompletableFuture<String> getTrackDownloadUrl(String trackId) {
        return CompletableFuture.supplyAsync(() -> {
            if (!isLoggedIn()) return null;
            try {
                HttpRequest infoRequest = createRequest("/tracks/" + trackId + "/download-info").GET().build();
                HttpResponse<String> infoResponse = httpClient.send(infoRequest, HttpResponse.BodyHandlers.ofString());
                
                if (infoResponse.statusCode() == 200) {
                    JsonObject json = gson.fromJson(infoResponse.body(), JsonObject.class);
                    JsonArray result = json.getAsJsonArray("result");
                    
                    String downloadInfoUrl = null;
                    for (var item : result) {
                        JsonObject info = item.getAsJsonObject();
                        if ("mp3".equals(info.get("codec").getAsString())) {
                            downloadInfoUrl = info.get("downloadInfoUrl").getAsString();
                            break;
                        }
                    }
                    
                    if (downloadInfoUrl != null) {
                        HttpRequest directRequest = HttpRequest.newBuilder().uri(URI.create(downloadInfoUrl)).GET().build();
                        HttpResponse<String> directResponse = httpClient.send(directRequest, HttpResponse.BodyHandlers.ofString());
                        String xml = directResponse.body();
                        String host = extractXml(xml, "host");
                        String path = extractXml(xml, "path");
                        String s = extractXml(xml, "s");
                        String ts = extractXml(xml, "ts");
                        String sign = md5("XGRlBW9FXlekgbPrRHuSiA" + path.substring(1) + s);
                        return "https://" + host + "/get-mp3/" + sign + "/" + ts + path;
                    }
                }
            } catch (Exception e) {
                MP3PlayerClient.LOGGER.error("Failed to get download URL: {}", e.getMessage());
            }
            return null;
        }, executor);
    }
    
    private Track parseTrack(JsonObject obj) {
        String id = obj.get("id").getAsString();
        String title = obj.get("title").getAsString();
        StringBuilder artists = new StringBuilder();
        JsonArray arr = obj.getAsJsonArray("artists");
        if (arr != null) {
            for (int i = 0; i < arr.size(); i++) {
                if (i > 0) artists.append(", ");
                artists.append(arr.get(i).getAsJsonObject().get("name").getAsString());
            }
        }
        String album = "";
        if (obj.has("albums") && obj.getAsJsonArray("albums").size() > 0) {
            album = obj.getAsJsonArray("albums").get(0).getAsJsonObject().get("title").getAsString();
        }
        long duration = obj.has("durationMs") ? obj.get("durationMs").getAsLong() : 0;
        return new Track(id, title, artists.toString(), album, duration);
    }
    
    private String extractXml(String xml, String tag) {
        int start = xml.indexOf("<" + tag + ">") + tag.length() + 2;
        int end = xml.indexOf("</" + tag + ">");
        return xml.substring(start, end);
    }
    
    private String md5(String input) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) { return ""; }
    }
}
