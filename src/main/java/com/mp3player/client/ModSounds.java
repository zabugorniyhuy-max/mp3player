/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.registry.Registries
 *  net.minecraft.registry.Registry
 *  net.minecraft.sound.SoundEvent
 *  net.minecraft.util.Identifier
 */
package com.mp3player.client;

import com.mp3player.MP3PlayerClient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class ModSounds {
    public static final Identifier KNOCK_ID = Identifier.of((String)"mp3player", (String)"knock");
    public static final SoundEvent KNOCK = SoundEvent.of((Identifier)KNOCK_ID);

    public static void register() {
        Registry.register((Registry)Registries.SOUND_EVENT, (Identifier)KNOCK_ID, (Object)KNOCK);
        MP3PlayerClient.LOGGER.info("Registered custom sounds");
    }
}

