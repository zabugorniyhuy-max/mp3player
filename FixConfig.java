import java.nio.file.*;
import java.util.*;

public class FixConfig {
    public static void main(String[] args) throws Exception {
        Path p = Paths.get("src/main/java/com/mp3player/client/config/ModConfig.java");
        List<String> lines = Files.readAllLines(p);
        
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains("public boolean darkTheme = true;")) {
                lines.add(i+1, "    public boolean isEnglish = false;");
                break;
            }
        }
        
        Files.write(p, lines);
        System.out.println("isEnglish added!");
    }
}
