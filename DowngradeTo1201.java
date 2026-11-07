import java.nio.file.*;
import java.util.*;

public class DowngradeTo1201 {
    public static void main(String[] args) throws Exception {
        Path p = Paths.get("build.gradle");
        List<String> lines = Files.readAllLines(p);
        
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains("minecraft \"com.mojang:minecraft:")) {
                lines.set(i, "    minecraft \"com.mojang:minecraft:1.20.1\"");
            }
            if (lines.get(i).contains("mappings \"net.fabricmc:yarn:")) {
                lines.set(i, "    mappings \"net.fabricmc:yarn:1.20.1+build.10:v2\"");
            }
            if (lines.get(i).contains("modImplementation \"net.fabricmc:fabric-loader:")) {
                lines.set(i, "    modImplementation \"net.fabricmc:fabric-loader:0.14.22\"");
            }
            if (lines.get(i).contains("modImplementation \"net.fabricmc.fabric-api:fabric-api:")) {
                lines.set(i, "    modImplementation \"net.fabricmc.fabric-api:fabric-api:0.83.0+1.20.1\"");
            }
        }
        
        Files.write(p, lines);
        System.out.println("Downgraded to 1.20.1");
    }
}
