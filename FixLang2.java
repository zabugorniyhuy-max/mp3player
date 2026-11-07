import java.nio.file.*;
import java.util.*;

public class FixLang2 {
    public static void main(String[] args) throws Exception {
        Path p = Paths.get("src/main/java/com/mp3player/client/gui/MainScreen.java");
        List<String> lines = Files.readAllLines(p);
        
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains("return true;") && lines.get(i+1).contains("ModConfig.getInstance().save();")) {
                lines.set(i, "            ModConfig.getInstance().save();");
                lines.set(i+1, "            return true;");
                break;
            }
        }
        
        Files.write(p, lines);
        System.out.println("Fixed unreachable statement!");
    }
}
