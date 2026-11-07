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
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public class ModSounds {
    public static final Identifier KNOCK_ID = Identifier.fromNamespaceAndPath((String)"mp3player", (String)"knock");
    public static final SoundEvent KNOCK = SoundEvent.createVariableRangeEvent((Identifier)KNOCK_ID);

    public static void register() {
        Registry.register((Registry)BuiltInRegistries.SOUND_EVENT, (Identifier)KNOCK_ID, (Object)KNOCK);
        MP3PlayerClient.LOGGER.info("Registered custom sounds");
    }
}

