/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.font.TextRenderer
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.text.Text
 */
package com.mp3player.client.gui;

import com.mp3player.client.config.ModConfig;
import java.lang.reflect.Method;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class DrawUtils {
    private static Method drawTextMethod;
    private static Method drawTextStringMethod;

    public static String t(String en, String ru) {
        return ModConfig.getInstance().isEnglish ? en : ru;
    }

    public static void drawText(DrawContext ctx, TextRenderer tr, String text, int x, int y, int color, boolean shadow) {
        try {
            if (drawTextMethod != null) {
                drawTextMethod.invoke((Object)ctx, tr, Text.literal((String)text), x, y, color, shadow);
                return;
            }
            if (drawTextStringMethod != null) {
                drawTextStringMethod.invoke((Object)ctx, tr, text, x, y, color, shadow);
                return;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            ctx.drawText(tr, (Text)Text.literal((String)text), x, y, color, shadow);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    static {
        try {
            for (Method m : DrawContext.class.getMethods()) {
                if (!m.getName().equals("drawText") && !m.getName().equals("method_51433")) continue;
                if (m.getParameterCount() == 6 && m.getParameterTypes()[1] == Text.class) {
                    drawTextMethod = m;
                    break;
                }
                if (m.getParameterCount() != 6 || m.getParameterTypes()[1] != String.class) continue;
                drawTextStringMethod = m;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }
}

