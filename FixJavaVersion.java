import java.nio.file.*;
import java.util.*;

public class FixJavaVersion {
    public static void main(String[] args) throws Exception {
        // Fix mixins.json
        Path mixinPath = Paths.get("src/main/resources/mp3player.mixins.json");
        List<String> mixinLines = Files.readAllLines(mixinPath);
        for (int i = 0; i < mixinLines.size(); i++) {
            if (mixinLines.get(i).contains("\"compatibilityLevel\": \"JAVA_21\"")) {
                mixinLines.set(i, mixinLines.get(i).replace("JAVA_21", "JAVA_17"));
            }
        }
        Files.write(mixinPath, mixinLines);
        
        // Fix build.gradle
        Path buildPath = Paths.get("build.gradle");
        List<String> buildLines = Files.readAllLines(buildPath);
        for (int i = 0; i < buildLines.size(); i++) {
            if (buildLines.get(i).contains("sourceCompatibility = JavaVersion.VERSION_21")) {
                buildLines.set(i, buildLines.get(i).replace("VERSION_21", "VERSION_17"));
            }
            if (buildLines.get(i).contains("targetCompatibility = JavaVersion.VERSION_21")) {
                buildLines.set(i, buildLines.get(i).replace("VERSION_21", "VERSION_17"));
            }
            if (buildLines.get(i).contains("toolchain.languageVersion = JavaLanguageVersion.of(21)")) {
                buildLines.set(i, buildLines.get(i).replace("21", "17"));
            }
        }
        Files.write(buildPath, buildLines);
        
        System.out.println("Java version downgraded to 17!");
    }
}
