package app.eelaa.core.util;

/**
 * Operating system detector utility class.
 *
 * @author Alireza Pourtaghi
 */
public final class OSDetector {
    public enum OS {LINUX, MACOS, OTHER}

    public static OS os() {
        final var name = System.getProperty("os.name").toLowerCase();
        if (name.contains("mac")) {
            return OS.MACOS;
        } else if (name.contains("linux")) {
            return OS.LINUX;
        } else {
            return OS.OTHER;
        }
    }
}
