import java.nio.file.*;
import java.util.*;

public class AddDebugBoxes {
    public static void main(String[] args) throws Exception {
        Path p = Paths.get("src/main/java/com/mp3player/client/gui/MainScreen.java");
        List<String> lines = Files.readAllLines(p);
        
        // Добавим отрисовку хитбоксов в render()
        int renderIdx = -1;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains("public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {")) {
                renderIdx = i;
                break;
            }
        }
        
        // Вставим в конец метода render
        int renderEnd = -1;
        int brackets = 0;
        for (int i = renderIdx; i < lines.size(); i++) {
            if (lines.get(i).contains("{")) brackets++;
            if (lines.get(i).contains("}")) brackets--;
            if (brackets == 0 && renderIdx != -1) {
                renderEnd = i;
                break;
            }
        }
        
        List<String> debugDraws = Arrays.asList(
            "        // DEBUG HITBOXES",
            "        float eased = easeOutCubic(animationProgress);",
            "        int pw = 320, ph = 220;",
            "        int px = this.width / 2 - pw / 2;",
            "        int py = (int) (this.height + 20 + (this.height / 2 - ph / 2 - this.height - 20) * eased);",
            "        ctx.fill(px + 4, py + 4, px + 4 + 16, py + 4 + 14, 0x88FF0000); // Theme",
            "        ctx.fill(px + 22, py + 2, px + 22 + 24, py + 2 + 18, 0x8800FF00); // Lang",
            "        int btnS = 18; int gap = 8; int btnW_10 = 24;",
            "        int totalW = btnS*3 + gap*4 + btnW_10*2;",
            "        int btnX = px + pw / 2 - totalW / 2;",
            "        int btnY = py + ph - 30;",
            "        if (!sm) btnX += 10;",
            "        ctx.fill(btnX + btnS + gap + btnW_10 + gap, btnY, btnX + btnS + gap + btnW_10 + gap + btnS, btnY + btnS, 0x880000FF); // Play"
        );
        lines.addAll(renderEnd, debugDraws);
        
        // Удалим invisibleBtn, восстановим mouseClicked
        // Это решит проблему с ПКМ
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains("ButtonWidget invisibleBtn")) {
                lines.set(i, "");
                lines.set(i+1, "");
                lines.set(i+2, "");
                lines.set(i+3, "");
                lines.set(i+4, "");
            }
            if (lines.get(i).contains("public boolean processClick(double mx, double my, int btn) {")) {
                lines.set(i, "    @Override\n    public boolean mouseClicked(double mx, double my, int btn) {");
            }
        }
        
        Files.write(p, lines);
        System.out.println("Debug hitboxes added!");
    }
}
