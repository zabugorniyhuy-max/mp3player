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

import com.mojang.blaze3d.platform.InputConstants;
import com.mp3player.client.audio.MusicPlayer;
import com.mp3player.client.gsmtc.GsmtcManager;
import com.mp3player.client.gui.MainScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;

public class KeyInputHandler {
    public static final KeyMapping.Category KEY_CATEGORY_MP3PLAYER = KeyMapping.Category.register(net.minecraft.resources.Identifier.fromNamespaceAndPath("mp3player", "main"));
    public static KeyMapping openMenuKeyBinding;
    public static KeyMapping playPauseKeyBinding;
    public static KeyMapping nextTrackKeyBinding;
    public static KeyMapping prevTrackKeyBinding;

    public static void register() {
        openMenuKeyBinding = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.mp3player.open_menu", InputConstants.Type.KEYBOARD, InputConstants.KEY_RSHIFT, KEY_CATEGORY_MP3PLAYER));
        playPauseKeyBinding = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.mp3player.play_pause", InputConstants.Type.KEYBOARD, InputConstants.KEY_P, KEY_CATEGORY_MP3PLAYER));
        nextTrackKeyBinding = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.mp3player.next_track", InputConstants.Type.KEYBOARD, InputConstants.KEY_RBRACKET, KEY_CATEGORY_MP3PLAYER));
        prevTrackKeyBinding = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.mp3player.prev_track", InputConstants.Type.KEYBOARD, InputConstants.KEY_LBRACKET, KEY_CATEGORY_MP3PLAYER));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.gui.screen() == null) {
                boolean open = false;
                if (openMenuKeyBinding != null) {
                    while (openMenuKeyBinding.consumeClick()) {
                        open = true;
                    }
                }
                if (open) {
                    client.gui.setScreen(new MainScreen());
                    return;
                }
            }
            GsmtcManager gsmtc = GsmtcManager.getInstance();
            MusicPlayer local = MusicPlayer.getInstance();
            if (playPauseKeyBinding != null) {
                while (playPauseKeyBinding.consumeClick()) {
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
                while (nextTrackKeyBinding.consumeClick()) {
                    if (gsmtc.hasTrack()) {
                        gsmtc.next();
                        continue;
                    }
                    local.next();
                }
            }
            if (prevTrackKeyBinding != null) {
                while (prevTrackKeyBinding.consumeClick()) {
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

