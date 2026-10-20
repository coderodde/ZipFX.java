package io.github.coderodde.zipfx;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public final class Misc {
    
    private static final Logger LOGGER = Logger.getLogger("ZipFX");
    
    private Misc() {}
    
    public static void sleep(long durationMillis) {
        try {
            Thread.sleep(durationMillis);
        } catch (InterruptedException ex) {
            LOGGER.log(Level.WARNING, "Could not sleep: {0}", ex.getMessage());
        }
    }
}
