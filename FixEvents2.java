import java.nio.file.*;
import java.util.*;

public class FixEvents2 {
    public static void main(String[] args) throws Exception {
        Path p = Paths.get("src/main/java/com/mp3player/client/gui/MainScreen.java");
        List<String> lines = Files.readAllLines(p);
        
        // Удалим ClickableWidget
        int start = -1, end = -1;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains("this.addDrawableChild(new net.minecraft.client.gui.widget.ClickableWidget(0, 0, this.width, this.height, net.minecraft.text.Text.empty()) {")) {
                start = i;
            }
            if (start != -1 && lines.get(i).contains("});")) {
                end = i;
                break;
            }
        }
        if (start != -1 && end != -1) {
            lines.subList(start, end + 1).clear();
        }
        
        // Вставим ScreenMouseEvents.allowMouseClick
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains("super.init();")) {
                lines.add(i+1, "        net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents.allowMouseClick(this).register((screen, mouseX, mouseY, button) -> {");
                lines.add(i+2, "            boolean handled = processClick(mouseX, mouseY, button);");
                lines.add(i+3, "            return !handled;");
                lines.add(i+4, "        });");
                lines.add(i+5, "        net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents.allowMouseRelease(this).register((screen, mouseX, mouseY, button) -> {");
                lines.add(i+6, "            boolean handled = processRelease(mouseX, mouseY, button);");
                lines.add(i+7, "            return !handled;");
                lines.add(i+8, "        });");
                break;
            }
        }
        
        Files.write(p, lines);
        System.out.println("ScreenMouseEvents registered!");
    }
}
