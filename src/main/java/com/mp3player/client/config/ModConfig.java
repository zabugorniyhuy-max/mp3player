/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  net.minecraft.client.MinecraftClient
 */
package com.mp3player.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mp3player.MP3PlayerClient;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import net.minecraft.client.MinecraftClient;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static ModConfig instance;
    private static File configFile;
    public String encryptedToken = "";
    public float musicVolume = 0.5f;
    public boolean darkTheme = true;
    public boolean isEnglish = false;
    public boolean speakerMode = false;
    public int speakerRange = 32;
    public int currentTab = 0;
    public boolean muteGlobal = false;
    public boolean useNanoVG = true;
    public List<String> favorites = new ArrayList<String>();

    public static ModConfig getInstance() {
        if (instance == null) {
            ModConfig.load();
        }
        return instance;
    }

    public static void load() {
        configFile = new File(MinecraftClient.getInstance().runDirectory, "config/mp3player.json");
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile);){
                instance = (ModConfig)GSON.fromJson((Reader)reader, ModConfig.class);
            }
            catch (Exception e) {
                instance = new ModConfig();
            }
        } else {
            instance = new ModConfig();
        }
    }

    public void save() {
        try {
            configFile.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(configFile);){
                GSON.toJson((Object)this, (Appendable)writer);
            }
        }
        catch (Exception e) {
            MP3PlayerClient.LOGGER.error("Failed to save config: {}", (Object)e.getMessage());
        }
    }

    public void setToken(String token) {
        if (token == null || token.isEmpty()) {
            this.encryptedToken = "";
        } else {
            byte[] bytes = token.getBytes();
            byte[] key = "MP3PlayerMod".getBytes();
            for (int i = 0; i < bytes.length; ++i) {
                int n = i;
                bytes[n] = (byte)(bytes[n] ^ key[i % key.length]);
            }
            this.encryptedToken = Base64.getEncoder().encodeToString(bytes);
        }
        this.save();
    }

    public String getToken() {
        if (this.encryptedToken == null || this.encryptedToken.isEmpty()) {
            return "";
        }
        try {
            byte[] bytes = Base64.getDecoder().decode(this.encryptedToken);
            byte[] key = "MP3PlayerMod".getBytes();
            for (int i = 0; i < bytes.length; ++i) {
                int n = i;
                bytes[n] = (byte)(bytes[n] ^ key[i % key.length]);
            }
            return new String(bytes);
        }
        catch (Exception e) {
            return "";
        }
    }
}

