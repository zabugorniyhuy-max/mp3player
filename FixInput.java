import java.nio.file.*;
import java.util.*;

public class FixInput {
    public static void main(String[] args) throws Exception {
        Path p = Paths.get("src/main/java/com/mp3player/client/gui/MainScreen.java");
        List<String> lines = Files.readAllLines(p);
        
        // 1. Изменить @Override mouseClicked на public void handleGlobalClick(double mx, double my, int btn)
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains("public boolean mouseClicked(double mx, double my, int btn)")) {
                lines.set(i-1, ""); // убираем @Override
                lines.set(i, "    public boolean handleGlobalClick(double mx, double my, int btn) {");
            }
            else if (lines.get(i).contains("return super.mouseClicked(mx, my, btn);")) {
                lines.set(i, "        return false;");
            }
            else if (lines.get(i).contains("public boolean mouseReleased(double mx, double my, int btn)")) {
                lines.set(i-1, "");
                lines.set(i, "    public boolean handleGlobalRelease(double mx, double my, int btn) {");
            }
            else if (lines.get(i).contains("return super.mouseReleased(mx, my, btn);")) {
                lines.set(i, "        return false;");
            }
            else if (lines.get(i).contains("public boolean mouseDragged(double mx, double my, int btn, double dx, double dy)")) {
                lines.set(i-1, "");
                lines.set(i, "    public boolean handleGlobalDrag(double mx, double my, int btn, double dx, double dy) {");
            }
            else if (lines.get(i).contains("return super.mouseDragged(mx, my, btn, dx, dy);")) {
                lines.set(i, "        return false;");
            }
        }
        
        // 2. Внедрить init()
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains("protected void init() {")) {
                lines.add(i+1, "        super.init();");
                lines.add(i+2, "        this.addDrawableChild(new net.minecraft.client.gui.widget.ClickableWidget(0, 0, this.width, this.height, net.minecraft.text.Text.empty()) {");
                lines.add(i+3, "            @Override protected void appendClickableNarrations(net.minecraft.client.gui.screen.narration.NarrationMessageBuilder builder) {}");
                lines.add(i+4, "            @Override protected void renderWidget(net.minecraft.client.gui.DrawContext context, int mouseX, int mouseY, float delta) {}");
                lines.add(i+5, "            @Override public void onClick(double mx, double my) { handleGlobalClick(mx, my, 0); }");
                lines.add(i+6, "            @Override protected void onDrag(double mx, double my, double dx, double dy) { handleGlobalDrag(mx, my, 0, dx, dy); }");
                lines.add(i+7, "            @Override public void onRelease(double mx, double my) { handleGlobalRelease(mx, my, 0); }");
                lines.add(i+8, "        });");
                break;
            }
        }
        
        // 3. Добавить кнопку перевода справа от смены темы
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
        
        Files.write(p, lines);
        System.out.println("MainScreen inputs patched!");
    }
}
