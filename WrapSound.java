import java.nio.file.*;
import java.util.*;
public class WrapSound {
    public static void main(String[] args) throws Exception {
        Path p = Paths.get("src/main/java/com/mp3player/client/gui/MainScreen.java");
        List<String> lines = Files.readAllLines(p);
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains("private void playClickSound() {")) {
                lines.set(i+1, "        try { " + lines.get(i+1));
                lines.set(i+3, lines.get(i+3) + " } catch(Throwable t) {}");
                break;
            }
        }
        Files.write(p, lines);
        System.out.println("Wrapped playClickSound in MainScreen");
    }
}
