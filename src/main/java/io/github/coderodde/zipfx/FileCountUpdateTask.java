package io.github.coderodde.zipfx;

import java.util.Objects;
import javafx.application.Platform;
import javafx.concurrent.Task;

/**
 *
 * @author rodio
 */
public final class FileCountUpdateTask extends Task<Void> {

    private static final long COUNT_UPDATE_SLEEP = 100L;
    
    private final ZipFX app;
    private final FileCounter fileCounter;
    
    private volatile boolean requestHalt = false;
    
    public FileCountUpdateTask(ZipFX app, FileCounter fileCounter) {
        this.app = Objects.requireNonNull(app, "The input app object is null.");
        
        this.fileCounter = 
            Objects.requireNonNull(fileCounter, "The file counter is null.");
    }
    
    public void halt() {
        requestHalt = true;
    }
    
    @Override
    protected Void call() throws Exception {
        while (true) {
            if (requestHalt) {
                return null;
            }
            
            Platform.runLater(() -> {
                app.setStatusBarText(
                    "Files counted: %d".formatted(fileCounter.getCount()));
            });
            
            Misc.sleep(COUNT_UPDATE_SLEEP);
        }
    }
}
