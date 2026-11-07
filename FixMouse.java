import java.nio.file.*;
import java.util.*;
public class FixMouse {
    public static void main(String[] args) throws Exception {
        Path p = Paths.get("src/main/java/com/mp3player/client/gui/MainScreen.java");
        List<String> lines = Files.readAllLines(p);
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains("return handleLocalClick(mx, my, px, py, pw, ph, false);")) {
                lines.set(i, "        if (currentTab == 0) {\n            if (handleLocalClick(mx, my, px, py, pw, ph, false)) return true;\n        } else {\n            if (handleLocalClick(mx, my, px, py, pw, ph, true)) return true;\n        }\n        return super.mouseClicked(mx, my, btn);");
                lines.set(i+1, "");
                break;
            }
        }
        Files.write(p, lines);
        System.out.println("Fixed MainScreen.java");
    }
}
