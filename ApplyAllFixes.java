import java.io.File;
import java.nio.file.Files;

public class ApplyAllFixes {
    public static void main(String[] args) throws Exception {
        File dir = new File("src/main/java/com/mp3player/client/gui");
        for (File f : dir.listFiles()) {
            if (f.getName().endsWith(".java")) {
                String c = new String(Files.readAllBytes(f.toPath()));
                
                c = c.replace("@Override\n    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount)", 
                              "@Override\n    public boolean mouseScrolled(double mouseX, double mouseY, double amount)");
                c = c.replace("@Override\n    public boolean mouseScrolled(double mx, double my, double horizontalAmount, double verticalAmount)", 
                              "@Override\n    public boolean mouseScrolled(double mx, double my, double amount)");
                
                c = c.replace("verticalAmount", "amount");
                
                c = c.replace("@Override\n    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta)", 
                              "public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta)");
                
                c = c.replace("@Override\n    public void mouseMoved(double mx, double my)", 
                              "public void mouseMoved(double mx, double my)");
                              
                Files.write(f.toPath(), c.getBytes());
            }
        }
        
        File tracker = new File("src/main/java/com/mp3player/client/audio/GlobalAudioTracker.java");
        String tc = new String(Files.readAllBytes(tracker.toPath()));
        tc = tc.replace("NetworkPackets.REQUEST_TRACK,", "NetworkPackets.REQUEST_TRACK_C2S,");
        Files.write(tracker.toPath(), tc.getBytes());
        
        System.out.println("Applied fixes.");
    }
}
