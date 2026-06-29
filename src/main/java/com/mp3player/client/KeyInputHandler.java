package com.mp3player.client;

import com.mp3player.client.gui.MainScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class KeyInputHandler {
    public static final String KEY_CATEGORY_MP3PLAYER = "MP3 Player";
    public static final String KEY_OPEN_MENU = "Open Menu";

    public static KeyBinding openMenuKeyBinding;
    private static boolean wasFallbackKeyPressed = false;

    public static void register() {
        KeyBinding binding = null;
        try {
            Object keySym = InputUtil.Type.KEYSYM;
            int keyCode = GLFW.GLFW_KEY_RIGHT_SHIFT;
            Object inputKey = InputUtil.Type.KEYSYM.createFromCode(keyCode);
            
            Object[][] allPossibleArgs = new Object[][] {
                new Object[] { KEY_OPEN_MENU, keySym, keyCode, KEY_CATEGORY_MP3PLAYER },
                new Object[] { KEY_OPEN_MENU, keyCode, KEY_CATEGORY_MP3PLAYER },
                new Object[] { KEY_OPEN_MENU, inputKey, KEY_CATEGORY_MP3PLAYER }
            };

            for (java.lang.reflect.Constructor<?> ctor : KeyBinding.class.getDeclaredConstructors()) {
                ctor.setAccessible(true);
                for (Object[] args : allPossibleArgs) {
                    if (ctor.getParameterCount() == args.length) {
                        try {
                            binding = (KeyBinding) ctor.newInstance(args);
                            if (binding != null) break;
                        } catch (Throwable t) {
                            // Игнорируем несовпадение типов аргументов
                        }
                    }
                }
                if (binding != null) break;
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }

        if (binding != null) {
            openMenuKeyBinding = KeyBindingHelper.registerKeyBinding(binding);
        } else {
            System.err.println("[MP3Player] CRITICAL: Failed to create KeyBinding via reflection! Falling back to raw GLFW key polling (Right Shift).");
        }

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            boolean isPressed = false;

            if (openMenuKeyBinding != null) {
                while (openMenuKeyBinding.wasPressed()) {
                    isPressed = true;
                }
            } else {
                if (client.currentScreen == null && client.getWindow() != null) {
                    long window = client.getWindow().getHandle();
                    boolean mPressed = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
                    
                    if (mPressed && !wasFallbackKeyPressed) {
                        isPressed = true;
                        wasFallbackKeyPressed = true;
                    } else if (!mPressed) {
                        wasFallbackKeyPressed = false;
                    }
                }
            }

            if (isPressed) {
                if (client.currentScreen instanceof MainScreen screen) {
                    screen.startClosing();
                } else if (client.currentScreen == null) {
                    client.setScreen(new MainScreen());
                }
            }
        });
    }
}
