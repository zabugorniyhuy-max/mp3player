import java.nio.file.*;
import java.util.*;

public class ImplementGlfwMouse {
    public static void main(String[] args) throws Exception {
        Path p = Paths.get("src/main/java/com/mp3player/client/gui/MainScreen.java");
        List<String> lines = Files.readAllLines(p);
        
        // Добавляем импорт
        lines.add(1, "import org.lwjgl.glfw.GLFW;");
        
        // Добавляем поля
        int classIdx = -1;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains("public class MainScreen extends Screen {")) {
                classIdx = i;
                break;
            }
        }
        lines.add(classIdx+1, "    private boolean lastLeftClick = false;");
        lines.add(classIdx+2, "    private boolean lastRightClick = false;");
        lines.add(classIdx+3, "    public static String lastError = \"\";");
        
        // В render() добавляем логику
        int renderIdx = -1;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains("public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {")) {
                renderIdx = i;
                break;
            }
        }
        
        List<String> logic = Arrays.asList(
            "        try {",
            "            long window = this.client.getWindow().getHandle();",
            "            boolean leftClick = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;",
            "            boolean rightClick = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;",
            "            if (leftClick && !lastLeftClick) {",
            "                mouseClicked(mouseX, mouseY, 0);",
            "            } else if (rightClick && !lastRightClick) {",
            "                mouseClicked(mouseX, mouseY, 1);",
            "            } else if (leftClick && lastLeftClick) {",
            "                mouseDragged(mouseX, mouseY, 0, 0, 0);",
            "            }",
            "            if (!leftClick && lastLeftClick) {",
            "                mouseReleased(mouseX, mouseY, 0);",
            "            }",
            "            if (!rightClick && lastRightClick) {",
            "                mouseReleased(mouseX, mouseY, 1);",
            "            }",
            "            lastLeftClick = leftClick;",
            "            lastRightClick = rightClick;",
            "        } catch (Throwable t) { lastError = t.toString(); }",
            "        if (!lastError.isEmpty()) DrawUtils.drawText(ctx, this.textRenderer, lastError, 5, 5, 0xFF5555, true);"
        );
        lines.addAll(renderIdx + 1, logic);
        
        // Расширяем хитбокс EN/RU кнопки
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains("if (isInside(mx, my, px + 24, py + 4, 16, 14)) {")) {
                lines.set(i, "        if (isInside(mx, my, px + 22, py + 2, 24, 18)) {");
            }
        }
        
        Files.write(p, lines);
        System.out.println("GLFW logic injected!");
    }
}
