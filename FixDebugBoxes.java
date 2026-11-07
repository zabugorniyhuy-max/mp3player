import java.nio.file.*;
import java.util.*;

public class FixDebugBoxes {
    public static void main(String[] args) throws Exception {
        Path p = Paths.get("src/main/java/com/mp3player/client/gui/MainScreen.java");
        List<String> lines = Files.readAllLines(p);
        
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains("float eased = easeOutCubic(animationProgress);") && lines.get(i-1).contains("DEBUG HITBOXES")) {
                lines.set(i, "        // float eased = easeOutCubic(animationProgress);"); // уже есть в начале render
            }
        }
        
        Files.write(p, lines);
        System.out.println("Debug hitboxes fixed!");
    }
}
