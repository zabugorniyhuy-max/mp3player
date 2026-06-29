package com.mp3player.client.gui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import java.lang.reflect.Method;

public class DrawUtils {
    private static Method drawTextMethod;
    private static Method drawTextStringMethod;

    static {
        try {
            // Ищем метод для Text
            for (Method m : DrawContext.class.getMethods()) {
                if (m.getName().equals("drawText") || m.getName().equals("method_51433")) { // drawText
                    if (m.getParameterCount() == 6 && m.getParameterTypes()[1] == Text.class) {
                        drawTextMethod = m;
                        break;
                    }
                    if (m.getParameterCount() == 6 && m.getParameterTypes()[1] == String.class) {
                        drawTextStringMethod = m;
                    }
                }
            }
        } catch (Throwable t) {
        }
    }

    public static void drawText(DrawContext ctx, TextRenderer tr, String text, int x, int y, int color, boolean shadow) {
        try {
            if (drawTextMethod != null) {
                drawTextMethod.invoke(ctx, tr, Text.literal(text), x, y, color, shadow);
                return;
            }
            if (drawTextStringMethod != null) {
                drawTextStringMethod.invoke(ctx, tr, text, x, y, color, shadow);
                return;
            }
        } catch (Throwable t) {
        }
        
        // Fallback for 1.21.1 compile-time mapping
        try {
            ctx.drawText(tr, Text.literal(text), x, y, color, shadow);
        } catch (Throwable t) {
            // Ignore if method not found at runtime
        }
    }
}
