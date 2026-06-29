package com.mp3player.client;

import com.mp3player.MP3PlayerClient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class ModSounds {
    public static final Identifier KNOCK_ID = Identifier.of(MP3PlayerClient.MODID, "knock");
    public static final SoundEvent KNOCK = SoundEvent.of(KNOCK_ID);
    
    public static void register() {
        Registry.register(Registries.SOUND_EVENT, KNOCK_ID, KNOCK);
        MP3PlayerClient.LOGGER.info("Registered custom sounds");
    }
}
