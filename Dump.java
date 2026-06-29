import java.lang.reflect.Method;
public class Dump {
    public static void main(String[] args) throws Exception {
        Class<?> clazz = Class.forName("net.minecraft.block.jukebox.JukeboxManager");
        for (Method m : clazz.getDeclaredMethods()) {
            System.out.println(m.getName());
            for (Class<?> p : m.getParameterTypes()) {
                System.out.println("  " + p.getName());
            }
        }
    }
}
