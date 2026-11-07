import java.nio.file.*;
import java.util.*;

public class FixInvisibleButton {
    public static void main(String[] args) throws Exception {
        Path p = Paths.get("src/main/java/com/mp3player/client/gui/MainScreen.java");
        List<String> lines = Files.readAllLines(p);
        
        // Удалить ScreenMouseEvents
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains("net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents.allowMouseClick(this).register(")) {
                lines.set(i, "");
                lines.set(i+1, "");
                lines.set(i+2, "");
                lines.set(i+3, "");
            }
            if (lines.get(i).contains("net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents.allowMouseRelease(this).register(")) {
                lines.set(i, "");
                lines.set(i+1, "");
                lines.set(i+2, "");
                lines.set(i+3, "");
            }
        }
        
        // Добавить lastMx, lastMy
        int classIdx = -1;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains("public class MainScreen extends Screen {")) {
                classIdx = i;
                break;
            }
        }
        lines.add(classIdx+1, "    private double lastMx = 0;");
        lines.add(classIdx+2, "    private double lastMy = 0;");
        
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains("public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {")) {
                lines.add(i+1, "        lastMx = mouseX;");
                lines.add(i+2, "        lastMy = mouseY;");
                break;
            }
        }
        
        // Добавить ButtonWidget в init
        int initIdx = -1;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains("super.init();")) {
                initIdx = i;
                break;
            }
        }
        lines.add(initIdx+1, "        net.minecraft.client.gui.widget.ButtonWidget invisibleBtn = net.minecraft.client.gui.widget.ButtonWidget.builder(");
        lines.add(initIdx+2, "            net.minecraft.text.Text.empty(),");
        lines.add(initIdx+3, "            btn -> { processClick(lastMx, lastMy, 0); }");
        lines.add(initIdx+4, "        ).dimensions(0, 0, 9999, 9999).build();");
        lines.add(initIdx+5, "        this.addDrawableChild(invisibleBtn);"); // добавляем как drawable, чтобы children работал
        
        // Удалить super.render, чтобы кнопка не рисовалась
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains("super.render(ctx, mouseX, mouseY, delta);")) {
                lines.set(i, "        // super.render(ctx, mouseX, mouseY, delta);");
            }
        }
        
        Files.write(p, lines);
        System.out.println("Invisible button applied!");
    }
}
