import java.nio.file.*;
import java.util.*;

public class FixButtons {
    public static void main(String[] args) throws Exception {
        Path p = Paths.get("src/main/java/com/mp3player/client/gui/MainScreen.java");
        List<String> lines = Files.readAllLines(p);
        
        // Переместим надпись MP3 Player (panelX + 25 -> panelX + 45)
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains("DrawUtils.drawText(ctx, this.textRenderer, title, panelX + 25, panelY + 10, accent(), true);")) {
                lines.set(i, "        DrawUtils.drawText(ctx, this.textRenderer, title, panelX + 45, panelY + 10, accent(), true);");
            }
        }
        
        // Добавим ванильные кнопки
        int initIdx = -1;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains("protected void init() {")) {
                initIdx = i;
                break;
            }
        }
        
        if (initIdx != -1) {
            lines.add(initIdx+1, "        themeBtn = net.minecraft.client.gui.widget.ButtonWidget.builder(net.minecraft.text.Text.literal(darkTheme ? \"C\" : \"S\"), btn -> { darkTheme = !darkTheme; ModConfig.getInstance().darkTheme = darkTheme; ModConfig.getInstance().save(); btn.setMessage(net.minecraft.text.Text.literal(darkTheme ? \"C\" : \"S\")); }).dimensions(0, 0, 16, 14).build();");
            lines.add(initIdx+2, "        langBtn = net.minecraft.client.gui.widget.ButtonWidget.builder(net.minecraft.text.Text.literal(ModConfig.getInstance().isEnglish ? \"EN\" : \"RU\"), btn -> { ModConfig.getInstance().isEnglish = !ModConfig.getInstance().isEnglish; ModConfig.getInstance().save(); btn.setMessage(net.minecraft.text.Text.literal(ModConfig.getInstance().isEnglish ? \"EN\" : \"RU\")); }).dimensions(0, 0, 16, 14).build();");
            lines.add(initIdx+3, "        this.addDrawableChild(themeBtn);");
            lines.add(initIdx+4, "        this.addDrawableChild(langBtn);");
        }
        
        // Добавим поля
        lines.add(21, "    private net.minecraft.client.gui.widget.ButtonWidget themeBtn;");
        lines.add(22, "    private net.minecraft.client.gui.widget.ButtonWidget langBtn;");
        
        // Обновим координаты кнопок в render
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains("super.render(ctx, mouseX, mouseY, delta);")) {
                lines.add(i, "        themeBtn.setX(panelX + 4); themeBtn.setY(panelY + 4);");
                lines.add(i+1, "        langBtn.setX(panelX + 24); langBtn.setY(panelY + 4);");
                break;
            }
        }
        
        // Уберем кастомную отрисовку этих двух кнопок
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains("drawButton(ctx, mouseX, mouseY, panelX + 4, panelY + 4, 16, 14, darkTheme ? \"☾\" : \"☀\", 200);")) {
                lines.set(i, "        // Ванильная кнопка темы");
            }
            if (lines.get(i).contains("drawButton(ctx, mouseX, mouseY, panelX + 24, panelY + 4, 16, 14, ModConfig.getInstance().isEnglish ? \"EN\" : \"RU\", 205);")) {
                lines.set(i, "        // Ванильная кнопка языка");
            }
        }
        
        // Добавим отрисовку координат мыши, чтобы проверить, совпадает ли она с кнопками
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains("super.render(ctx, mouseX, mouseY, delta);")) {
                lines.add(i, "        DrawUtils.drawText(ctx, this.textRenderer, \"Mouse: \" + mouseX + \", \" + mouseY, 10, 10, 0xFFFFFF, true);");
                break;
            }
        }
        
        Files.write(p, lines);
        System.out.println("FixButtons executed!");
    }
}
