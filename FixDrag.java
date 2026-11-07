import java.nio.file.*;
import java.util.*;

public class FixDrag {
    public static void main(String[] args) throws Exception {
        Path p = Paths.get("src/main/java/com/mp3player/client/gui/MainScreen.java");
        List<String> lines = Files.readAllLines(p);
        
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains("public boolean processDrag(double mx, double my, int btn, double dx, double dy) {")) {
                lines.add(i, "    @Override");
                lines.set(i+1, "    public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {");
                break;
            }
        }
        
        Files.write(p, lines);
        System.out.println("Drag restored!");
    }
}
