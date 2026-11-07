import java.nio.file.*;
import java.util.*;

public class FixShareScreens {
    public static void main(String[] args) throws Exception {
        String[] files = {
            "src/main/java/com/mp3player/client/gui/MusicDiscScreen.java",
            "src/main/java/com/mp3player/client/gui/ShareRequestScreen.java",
            "src/main/java/com/mp3player/client/gui/ShareScreen.java"
        };
        for (String file : files) {
            Path p = Paths.get(file);
            List<String> lines = Files.readAllLines(p);
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).contains("this.renderBackground(ctx, mouseX, mouseY, delta);")) {
                    lines.set(i, "        // " + lines.get(i).trim());
                }
            }
            Files.write(p, lines);
        }
        System.out.println("Fixed renderBackground!");
    }
}
