import java.nio.file.*;
import java.util.*;

public class InjectEvents {
    public static void main(String[] args) throws Exception {
        Path p = Paths.get("src/main/java/com/mp3player/client/gui/MainScreen.java");
        List<String> lines = Files.readAllLines(p);
        
        // 1. Уберем @Override у mouseClicked, mouseReleased, mouseDragged
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains("public boolean mouseClicked(double mx, double my, int btn)")) {
                lines.set(i-1, ""); // remove @Override
                lines.set(i, "    public boolean processClick(double mx, double my, int btn) {");
                lines.add(i+1, "        try {");
            }
            if (lines.get(i).contains("return super.mouseClicked(mx, my, btn);")) {
                lines.set(i, "        return false; } catch (Throwable t) { lastError = t.toString(); return true; }");
            }
            
            if (lines.get(i).contains("public boolean mouseReleased(double mx, double my, int btn)")) {
                lines.set(i-1, ""); // remove @Override
                lines.set(i, "    public boolean processRelease(double mx, double my, int btn) {");
            }
            if (lines.get(i).contains("return super.mouseReleased(mx, my, btn);")) {
                lines.set(i, "        return false;");
            }
            
            if (lines.get(i).contains("public boolean mouseDragged(double mx, double my, int btn, double dx, double dy)")) {
                lines.set(i-1, ""); // remove @Override
                lines.set(i, "    public boolean processDrag(double mx, double my, int btn, double dx, double dy) {");
            }
            if (lines.get(i).contains("return super.mouseDragged(mx, my, btn, dx, dy);")) {
                lines.set(i, "        return false;");
            }
        }
        
        // 2. Добавляем init() с ClickableWidget
        int initIdx = -1;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains("protected void init() {")) {
                initIdx = i;
                break;
            }
        }
        if (initIdx != -1) {
            lines.add(initIdx+1, "        super.init();");
            lines.add(initIdx+2, "        this.addDrawableChild(new net.minecraft.client.gui.widget.ClickableWidget(0, 0, this.width, this.height, net.minecraft.text.Text.empty()) {");
            lines.add(initIdx+3, "            @Override protected void appendClickableNarrations(net.minecraft.client.gui.screen.narration.NarrationMessageBuilder builder) {}");
            lines.add(initIdx+4, "            @Override protected void renderWidget(net.minecraft.client.gui.DrawContext context, int mouseX, int mouseY, float delta) {}");
            lines.add(initIdx+5, "            @Override public void onClick(double mx, double my) { processClick(mx, my, 0); }");
            lines.add(initIdx+6, "            @Override protected void onDrag(double mx, double my, double dx, double dy) { processDrag(mx, my, 0, dx, dy); }");
            lines.add(initIdx+7, "            @Override public void onRelease(double mx, double my) { processRelease(mx, my, 0); }");
            lines.add(initIdx+8, "            @Override public boolean mouseScrolled(double mx, double my, double h, double v) { return MainScreen.this.mouseScrolled(mx, my, h, v); }");
            lines.add(initIdx+9, "        });");
        }
        
        // 3. playClickSound try-catch
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains("private void playClickSound() {")) {
                lines.add(i+1, "        try {");
                lines.add(i+5, "        } catch(Throwable t) {}");
                break;
            }
        }
        
        // 4. Добавляем EN/RU кнопку (ручной дизайн)
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains("drawButton(ctx, mouseX, mouseY, panelX + 4, panelY + 4, 16, 14, darkTheme ? \"☾\" : \"☀\", 200);")) {
                lines.add(i+1, "        // Кнопка перевода (справа от темы)");
                lines.add(i+2, "        drawButton(ctx, mouseX, mouseY, panelX + 24, panelY + 4, 16, 14, ModConfig.getInstance().isEnglish ? \"EN\" : \"RU\", 205);");
                break;
            }
        }
        
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains("darkTheme = !darkTheme;")) {
                lines.add(i+5, "        // Язык (справа от темы)");
                lines.add(i+6, "        if (isInside(mx, my, px + 24, py + 4, 16, 14)) {");
                lines.add(i+7, "            playClickSound();");
                lines.add(i+8, "            ModConfig.getInstance().isEnglish = !ModConfig.getInstance().isEnglish;");
                lines.add(i+9, "            ModConfig.getInstance().save();");
                lines.add(i+10, "            return true;");
                lines.add(i+11, "        }");
                break;
            }
        }
        
        // 5. Поле lastError и отрисовка
        int classIdx = -1;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains("public class MainScreen extends Screen {")) {
                classIdx = i;
                break;
            }
        }
        lines.add(classIdx+1, "    public static String lastError = \"\";");
        
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains("super.render(ctx, mouseX, mouseY, delta);")) {
                lines.add(i, "        if (!lastError.isEmpty()) DrawUtils.drawText(ctx, this.textRenderer, lastError, 5, 5, 0xFF5555, true);");
                break;
            }
        }
        
        // 6. Сдвигаем надпись MP3 Player правее на 45
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains("DrawUtils.drawText(ctx, this.textRenderer, title, panelX + 25, panelY + 10, accent(), true);")) {
                lines.set(i, "        DrawUtils.drawText(ctx, this.textRenderer, title, panelX + 45, panelY + 10, accent(), true);");
            }
        }
        
        Files.write(p, lines);
        System.out.println("Events injected in MainScreen");
    }
}
