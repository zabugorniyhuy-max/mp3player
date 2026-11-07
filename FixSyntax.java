import java.nio.file.*;
import java.util.*;

public class FixSyntax {
    public static void main(String[] args) throws Exception {
        Path p = Paths.get("src/main/java/com/mp3player/client/gui/MainScreen.java");
        List<String> lines = Files.readAllLines(p);
        
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains("return super.mouseClicked(mx, my, btn);")) {
                lines.set(i, "        return false; } catch (Throwable t) { lastError = t.toString(); return true; }");
            }
        }
        
        Files.write(p, lines);
        System.out.println("Syntax fixed!");
    }
}
