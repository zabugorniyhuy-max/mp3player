package com.mp3player.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mp3player.MP3PlayerClient;
import net.minecraft.client.MinecraftClient;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Base64;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static ModConfig instance;
    private static File configFile;
    
    public String encryptedToken = "";
    public float musicVolume = 0.5f;
    public boolean darkTheme = true;
    public boolean speakerMode = false;
    public int speakerRange = 32;
    public int currentTab = 0;
    public boolean muteGlobal = false;
    public java.util.List<String> favorites = new java.util.ArrayList<>();
    
    public static ModConfig getInstance() {
        if (instance == null) load();
        return instance;
    }
    
    public static void load() {
        configFile = new File(MinecraftClient.getInstance().runDirectory, "config/mp3player.json");
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                instance = GSON.fromJson(reader, ModConfig.class);
            } catch (Exception e) {
                instance = new ModConfig();
            }
        } else {
            instance = new ModConfig();
        }
    }
    
    public void save() {
        try {
            configFile.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(configFile)) {
                GSON.toJson(this, writer);
            }
        } catch (Exception e) {
            MP3PlayerClient.LOGGER.error("Failed to save config: {}", e.getMessage());
        }
    }
    
    public void setToken(String token) {
        if (token == null || token.isEmpty()) {
            this.encryptedToken = "";
        } else {
            byte[] bytes = token.getBytes();
            byte[] key = "MP3PlayerMod".getBytes();
            for (int i = 0; i < bytes.length; i++) bytes[i] ^= key[i % key.length];
            this.encryptedToken = Base64.getEncoder().encodeToString(bytes);
        }
        save();
    }
    
    public String getToken() {
        if (encryptedToken == null || encryptedToken.isEmpty()) return "";
        try {
            byte[] bytes = Base64.getDecoder().decode(encryptedToken);
            byte[] key = "MP3PlayerMod".getBytes();
            for (int i = 0; i < bytes.length; i++) bytes[i] ^= key[i % key.length];
            return new String(bytes);
        } catch (Exception e) { return ""; }
    }
}
