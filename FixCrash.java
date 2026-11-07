import java.nio.file.*;
import java.util.*;

public class FixCrash {
    public static void main(String[] args) throws Exception {
        String[] files = {
            "src/main/java/com/mp3player/client/gui/MusicDiscScreen.java",
            "src/main/java/com/mp3player/client/gui/ShareRequestScreen.java",
            "src/main/java/com/mp3player/client/gui/ShareScreen.java"
        };
        
        for (String file : files) {
            Path p = Paths.get(file);
            List<String> lines = Files.readAllLines(p);
            boolean changed = false;
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).contains("this.renderBackground(ctx, mouseX, mouseY, delta);") ||
                    lines.get(i).contains("this.renderBackground(context, mouseX, mouseY, delta);")) {
                    lines.set(i, "        // " + lines.get(i).trim());
                    changed = true;
                }
            }
            if (changed) {
                Files.write(p, lines);
                System.out.println("Fixed crash in " + file);
            }
        }
    }
}
