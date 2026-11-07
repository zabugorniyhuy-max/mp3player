/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.sound.PositionedSoundInstance
 */
package com.mp3player;

import java.lang.reflect.Method;
import java.util.Arrays;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;

public class TestSound {
    public static void main(String[] args) {
        System.out.println("Methods in PositionedSoundInstance:");
        for (Method m : SimpleSoundInstance.class.getMethods()) {
            if (!m.getName().equals("master")) continue;
            System.out.println(m.getName() + " " + Arrays.toString(m.getParameterTypes()));
        }
    }
}

