package taboolib.library.configuration;

import java.io.File;
import java.io.IOException;

public class Utils {

    public static void createParentDirs(File file) throws IOException {
        File parent = file.getCanonicalFile().getParentFile();
        if (parent == null) {
            /*
             * The given directory is a filesystem root. All zero of its ancestors exist. This doesn't
             * mean that the root itself exists -- consider x:\ on a Windows machine without such a drive
             * -- or even that the caller can create it, but this method makes no such guarantees even for
             * non-root files.
             */
            return;
        }
        parent.mkdirs();
        if (!parent.isDirectory()) {
            throw new IOException("Unable to create parent directories of " + file);
        }
    }
}
