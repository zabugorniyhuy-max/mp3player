import java.nio.file.*;
import java.util.*;

public class ForceCoords {
    public static void main(String[] args) throws Exception {
        Path p = Paths.get("src/main/java/com/mp3player/client/gui/MainScreen.java");
        List<String> lines = Files.readAllLines(p);
        
        // Добавляем поля lastMx, lastMy
        int classStart = -1;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains("public class MainScreen extends Screen {")) {
                classStart = i;
                break;
            }
        }
        lines.add(classStart + 1, "    private double lastMx = 0;");
        lines.add(classStart + 2, "    private double lastMy = 0;");
        lines.add(classStart + 3, "    private String lastDebug = \"\";");
        
        // Записываем их в render
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains("public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {")) {
                lines.add(i + 1, "        lastMx = mouseX;");
                lines.add(i + 2, "        lastMy = mouseY;");
                break;
            }
        }
        
        // Подменяем mx и my в handleGlobalClick
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains("public boolean handleGlobalClick(double mx, double my, int btn) {")) {
                lines.add(i + 1, "        mx = lastMx;");
                lines.add(i + 2, "        my = lastMy;");
                lines.add(i + 3, "        lastDebug = \"Clicked at \" + mx + \", \" + my;");
                break;
            }
        }
        
        // Подменяем в handleGlobalRelease
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains("public boolean handleGlobalRelease(double mx, double my, int btn) {")) {
                lines.add(i + 1, "        mx = lastMx;");
                lines.add(i + 2, "        my = lastMy;");
                break;
            }
        }
        
        // Подменяем в handleGlobalDrag
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains("public boolean handleGlobalDrag(double mx, double my, int btn, double dx, double dy) {")) {
                lines.add(i + 1, "        mx = lastMx;");
                lines.add(i + 2, "        my = lastMy;");
                break;
            }
        }
        
        // Отрисовка дебага
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains("DrawUtils.drawText(ctx, this.textRenderer, \"Mouse: \" + mouseX + \", \" + mouseY, 10, 10, 0xFFFFFF, true);")) {
                lines.add(i + 1, "        DrawUtils.drawText(ctx, this.textRenderer, lastDebug, 10, 25, 0xFFFFFF, true);");
                break;
            }
        }
        
        Files.write(p, lines);
        System.out.println("Forced coordinates in MainScreen");
    }
}
