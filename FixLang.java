import java.nio.file.*;
import java.util.*;

public class FixLang {
    public static void main(String[] args) throws Exception {
        Path p = Paths.get("src/main/java/com/mp3player/client/gui/MainScreen.java");
        List<String> lines = Files.readAllLines(p);
        
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains("if (isInside(mx, my, px + 24, py + 4, 16, 14)) {")) {
                lines.set(i, "        if (isInside(mx, my, px + 22, py + 2, 24, 18)) {"); // Увеличил зону клика
            }
            if (lines.get(i).contains("ModConfig.getInstance().save();") && lines.get(i-1).contains("isEnglish")) {
                lines.add(i+1, "            return true;"); // добавил return true
                break;
            }
        }
        
        Files.write(p, lines);
        System.out.println("Lang fixed!");
    }
}
