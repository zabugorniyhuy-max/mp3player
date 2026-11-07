/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.sun.jna.Native
 *  net.minecraft.client.MinecraftClient
 */
package com.mp3player.client.gsmtc;

import com.mp3player.MP3PlayerClient;
import com.mp3player.client.gsmtc.GsmtcBridge;
import com.sun.jna.Native;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import net.minecraft.client.MinecraftClient;

public class GsmtcLoader {
    private static GsmtcBridge bridgeInstance = null;
    private static boolean failed = false;

    public static synchronized GsmtcBridge load() {
        if (bridgeInstance != null) {
            return bridgeInstance;
        }
        if (failed) {
            return null;
        }
        try {
            File nativesDir;
            File runDir = MinecraftClient.getInstance().runDirectory;
            if (runDir == null) {
                runDir = new File(".");
            }
            if (!(nativesDir = new File(new File(runDir, "mp3player"), "natives")).exists()) {
                nativesDir.mkdirs();
            }
            File dllFile = new File(nativesDir, "gsmtc_bridge.dll");
            try (InputStream in = GsmtcLoader.class.getResourceAsStream("/assets/mp3player/natives/gsmtc_bridge.dll");){
                if (in != null) {
                    try {
                        Files.copy(in, dllFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        MP3PlayerClient.LOGGER.info("Extracted gsmtc_bridge.dll to {}", (Object)dllFile.getAbsolutePath());
                    }
                    catch (Exception lockedEx) {
                        boolean fallbackSuccess = false;
                        for (int attempt = 1; attempt <= 10; ++attempt) {
                            File fallbackDll = new File(nativesDir, "gsmtc_bridge_v" + attempt + ".dll");
                            try (InputStream inFallback = GsmtcLoader.class.getResourceAsStream("/assets/mp3player/natives/gsmtc_bridge.dll");){
                                if (inFallback == null) continue;
                                Files.copy(inFallback, fallbackDll.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                dllFile = fallbackDll;
                                fallbackSuccess = true;
                                MP3PlayerClient.LOGGER.info("Original DLL locked, extracted to fallback {}", (Object)dllFile.getAbsolutePath());
                                break;
                            }
                            catch (Exception exception) {
                                // empty catch block
                            }
                        }
                        if (!fallbackSuccess) {
                            MP3PlayerClient.LOGGER.warn("All fallback DLL copies failed, will try loading existing: {}", (Object)lockedEx.getMessage());
                        }
                    }
                }
            }
            catch (Exception ex) {
                MP3PlayerClient.LOGGER.warn("Could not extract embedded dll, trying existing: {}", (Object)ex.getMessage());
            }
            bridgeInstance = dllFile.exists() ? (GsmtcBridge)Native.load((String)dllFile.getAbsolutePath(), GsmtcBridge.class) : (GsmtcBridge)Native.load((String)"gsmtc_bridge", GsmtcBridge.class);
            if (bridgeInstance != null) {
                bridgeInstance.gsmtc_init();
                MP3PlayerClient.LOGGER.info("Successfully loaded gsmtc_bridge.dll!");
            }
            return bridgeInstance;
        }
        catch (Throwable t) {
            MP3PlayerClient.LOGGER.error("Failed to load gsmtc_bridge.dll: {}", (Object)t.getMessage());
            failed = true;
            return null;
        }
    }
}

