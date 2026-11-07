import java.nio.file.*;
import java.util.*;

public class FixDebugBoxes3 {
    public static void main(String[] args) throws Exception {
        Path p = Paths.get("src/main/java/com/mp3player/client/gui/MainScreen.java");
        List<String> lines = Files.readAllLines(p);
        
        List<String> out = new ArrayList<>();
        boolean inDebug = false;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains("DEBUG HITBOXES")) {
                inDebug = true;
            }
            if (inDebug && lines.get(i).contains("0x880000FF")) {
                inDebug = false;
                continue;
            }
            if (!inDebug) {
                out.add(lines.get(i));
            }
        }
        
        Files.write(p, out);
        System.out.println("Debug hitboxes removed!");
    }
}
