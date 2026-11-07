/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
 *  net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
 *  net.minecraft.client.gui.screen.Screen
 *  net.minecraft.client.option.KeyBinding
 *  net.minecraft.client.util.InputUtil$Type
 */
package com.mp3player.client;

import com.mp3player.client.audio.MusicPlayer;
import com.mp3player.client.gsmtc.GsmtcManager;
import com.mp3player.client.gui.MainScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

public class KeyInputHandler {
    public static final KeyBinding.Category KEY_CATEGORY_MP3PLAYER = KeyBinding.Category.create(net.minecraft.util.Identifier.of("mp3player", "main"));
    public static KeyBinding openMenuKeyBinding;
    public static KeyBinding playPauseKeyBinding;
    public static KeyBinding nextTrackKeyBinding;
    public static KeyBinding prevTrackKeyBinding;

    public static void register() {
        openMenuKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.mp3player.open_menu", InputUtil.Type.KEYSYM, 344, KEY_CATEGORY_MP3PLAYER));
        playPauseKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.mp3player.play_pause", InputUtil.Type.KEYSYM, 80, KEY_CATEGORY_MP3PLAYER));
        nextTrackKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.mp3player.next_track", InputUtil.Type.KEYSYM, 93, KEY_CATEGORY_MP3PLAYER));
        prevTrackKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.mp3player.prev_track", InputUtil.Type.KEYSYM, 91, KEY_CATEGORY_MP3PLAYER));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (openMenuKeyBinding != null && client.currentScreen == null) {
                while (openMenuKeyBinding.wasPressed()) {
                    client.setScreen(new MainScreen());
                    break;
                }
            }
            GsmtcManager gsmtc = GsmtcManager.getInstance();
            MusicPlayer local = MusicPlayer.getInstance();
            if (playPauseKeyBinding != null) {
                while (playPauseKeyBinding.wasPressed()) {
                    if (gsmtc.hasTrack()) {
                        gsmtc.togglePlayPause();
                        continue;
                    }
                    if (local.isPlaying()) {
                        local.pause();
                        continue;
                    }
                    local.play();
                }
            }
            if (nextTrackKeyBinding != null) {
                while (nextTrackKeyBinding.wasPressed()) {
                    if (gsmtc.hasTrack()) {
                        gsmtc.next();
                        continue;
                    }
                    local.next();
                }
            }
            if (prevTrackKeyBinding != null) {
                while (prevTrackKeyBinding.wasPressed()) {
                    if (gsmtc.hasTrack()) {
                        gsmtc.previous();
                        continue;
                    }
                    local.previous();
                }
            }
        });
    }
}

