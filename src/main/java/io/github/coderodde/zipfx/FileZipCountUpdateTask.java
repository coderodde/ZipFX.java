package io.github.coderodde.zipfx;

import java.util.Objects;
import javafx.concurrent.Task;

/**
 * 
 */
public final class FileZipCountUpdateTask extends Task<Void> {

    private static final long SLEEP_DURATION_MILLIS = 100L;
    
    private final ZipFXApp app;
    private final FileDataCounter totalFileDataCounter;
    private final FileDataCounter zipProgressDataCounter;
    private volatile boolean requestHalt = false;
    
    public FileZipCountUpdateTask(ZipFXApp app,
                                  FileDataCounter totalFileDataCounter,
                                  FileDataCounter zipProgressDataCounter) {
        this.app = 
            Objects.requireNonNull(app, "The input app instance is null.");
        
        this.totalFileDataCounter = 
            Objects.requireNonNull(
                totalFileDataCounter, 
                "The input total file data counter is null.");
        
        this.zipProgressDataCounter = 
            Objects.requireNonNull(
                zipProgressDataCounter, 
                "The input zip progress data counter is null.");
    }
    
    @Override
    protected Void call() throws Exception {
        while (true) {
            if (requestHalt) {
                return null;
            }
            
            printStatistics();
            
            Misc.sleep(SLEEP_DURATION_MILLIS);
        }
    }
    
    public void halt() {
        requestHalt = true;
    }
    
    private void printStatistics() {
        float percentCompressed = 
            (100.0f * zipProgressDataCounter.getByteCount()) 
                    / totalFileDataCounter.getByteCount();
        
        String percentsToPrint = String.format("%.1f %%", percentCompressed);
        
        app.setProgressStatusBarText("Progress: " + percentsToPrint);
    }
}
