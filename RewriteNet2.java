import java.io.File;
import java.nio.file.Files;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class RewriteNet2 {
    public static void main(String[] args) throws Exception {
        File dir = new File("src/main/java/com/mp3player");
        processDir(dir);
        System.out.println("Done rewriting network calls");
    }

    private static void processDir(File dir) throws Exception {
        if (!dir.exists() || dir.isFile()) return;
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                processDir(f);
            } else if (f.getName().endsWith(".java")) {
                if (f.getName().equals("MP3PlayerMod.java") || f.getName().equals("MP3PlayerClient.java") || f.getName().equals("NetworkPackets.java")) {
                    continue; // Skip the ones we just wrote manually
                }
                rewriteFile(f);
            }
        }
    }

    private static void rewriteFile(File f) throws Exception {
        String content = new String(Files.readAllBytes(f.toPath()));
        boolean changed = false;

        if (content.contains("import net.minecraft.component.DataComponentTypes;")) {
            content = content.replace("import net.minecraft.component.DataComponentTypes;", "");
            changed = true;
        }

        // FileSenderManager
        if (content.contains("new FileTransferStartPayload")) {
            content = content.replaceAll("ClientPlayNetworking\\.send\\(new FileTransferStartPayload\\((.*?)\\)\\);",
                "net.minecraft.network.PacketByteBuf buf = net.fabricmc.fabric.api.networking.v1.PacketByteBufs.create();\n" +
                "            buf.writeString($1.split(\",\")[0].trim());\n" +
                "            buf.writeLong(Long.parseLong($1.split(\",\")[1].trim()));\n" +
                "            buf.writeInt(Integer.parseInt($1.split(\",\")[2].trim()));\n" +
                "            buf.writeUuid((java.util.UUID)$1.split(\",\")[3].trim());\n" + // UUID sender
                "            buf.writeUuid((java.util.UUID)$1.split(\",\")[4].trim());\n" + // UUID target
                "            ClientPlayNetworking.send(com.mp3player.network.NetworkPackets.FILE_TRANSFER_START, buf);");
            changed = true;
        }
        
        // Let's just do targeted replacements!
        Files.write(f.toPath(), content.getBytes());
    }
}
