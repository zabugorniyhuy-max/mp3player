import java.nio.file.*;
import java.util.*;

public class FixDebugBoxes2 {
    public static void main(String[] args) throws Exception {
        Path p = Paths.get("src/main/java/com/mp3player/client/gui/MainScreen.java");
        List<String> lines = Files.readAllLines(p);
        
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains("int pw = 320, ph = 220;") && lines.get(i-2).contains("DEBUG HITBOXES")) {
                lines.set(i, "        // int pw = 320, ph = 220;");
            }
            if (lines.get(i).contains("int px = this.width / 2 - pw / 2;") && lines.get(i-3).contains("DEBUG HITBOXES")) {
                lines.set(i, "        // int px = this.width / 2 - pw / 2;");
            }
            if (lines.get(i).contains("int py = (int) (this.height + 20 + (this.height / 2 - ph / 2 - this.height - 20) * eased);") && lines.get(i-4).contains("DEBUG HITBOXES")) {
                lines.set(i, "        // int py = (int) (this.height + 20 + (this.height / 2 - ph / 2 - this.height - 20) * eased);");
            }
        }
        
        Files.write(p, lines);
        System.out.println("Debug hitboxes fixed 2!");
    }
}
