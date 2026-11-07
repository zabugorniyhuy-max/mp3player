import java.nio.file.*;
import java.util.*;

public class FixSyntax2 {
    public static void main(String[] args) throws Exception {
        Path p = Paths.get("src/main/java/com/mp3player/client/gui/MainScreen.java");
        List<String> lines = Files.readAllLines(p);
        
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains("if (currentTab == 0) return handleLocalClick(mx, my, px, py, pw, ph, false);")) {
                lines.set(i, "        boolean res; if (currentTab == 0) res = handleLocalClick(mx, my, px, py, pw, ph, false); else res = handleLocalClick(mx, my, px, py, pw, ph, true);");
                lines.set(i+1, "        return res;");
                lines.add(i+2, "        } catch (Throwable t) { lastError = t.toString(); return true; }");
                break;
            }
        }
        
        Files.write(p, lines);
        System.out.println("Syntax fixed 2!");
    }
}
