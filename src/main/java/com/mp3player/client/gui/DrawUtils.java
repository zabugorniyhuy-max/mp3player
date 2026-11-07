package com.mp3player.client.gui;

import com.mp3player.client.config.ModConfig;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class DrawUtils {
    public static String t(String en, String ru) {
        return ModConfig.getInstance().isEnglish ? en : ru;
    }

    public static void drawText(GuiGraphicsExtractor ctx, Font tr, String text, int x, int y, int color, boolean shadow) {
        if (ctx != null && tr != null && text != null) {
            ctx.text(tr, text, x, y, color, shadow);
        }
    }
}
