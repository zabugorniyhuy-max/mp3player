package com.mp3player;

import java.lang.reflect.Method;

public class Test {
    public static void main(String[] args) throws Exception {
        Class<?> clazz = Class.forName("net.minecraft.block.entity.JukeboxBlockEntity");
        for (Method m : clazz.getDeclaredMethods()) {
            System.out.println(m.getName() + " " + m.getReturnType().getName());
        }
    }
}
