package io.github.coderodde.zipfx;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import javafx.application.Platform;
import javafx.concurrent.Task;

/**
 * 
 */
public final class FileZipCountUpdateTask extends Task<Void> {

    private static final long SLEEP_DURATION_MILLIS    = 1_000L;
    private static final double MINIMUM_PROGRESS       = 1E-4; // 1/10_000
    private static final double NANOSECONDS_PER_SECOND = 1E9;
    
    private final ZipFXApp        app;
    private final FileDataCounter totalFileDataCounter;
    private final FileDataCounter zipProgressDataCounter;
    private final AtomicLong      accumaltedNanoseconds = new AtomicLong(0L);
    private volatile boolean      requestHalt = false;
    
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
    
    public void addFileZippingDuration(long nanoseconds) {
        accumaltedNanoseconds.addAndGet(nanoseconds);
    }
    
    public void halt() {
        requestHalt = true;
    }
    
    private void printStatistics() {
        double progress = ((double) zipProgressDataCounter.getByteCount()) 
                        / totalFileDataCounter.getByteCount();
        
        progress = Math.max(progress, MINIMUM_PROGRESS);
        
        double percentCompressed = 100.0 * progress;
        
        String percentsToPrint = String.format("%.1f %%", percentCompressed);
        
        Platform.runLater(() -> {
            app.setProgressStatusBarText("Progress: " + percentsToPrint);
        });
        
        double totalEstimatedDurationNanoseconds
                = (double) accumaltedNanoseconds.get() 
                / progress;
        
        double remainingDurationNanoseconds = totalEstimatedDurationNanoseconds
                                            - accumaltedNanoseconds.get();
        
        RemainingTime remainingTime =
            new RemainingTime((int)(remainingDurationNanoseconds / 
                                    NANOSECONDS_PER_SECOND));
        
        Platform.runLater(() -> {
            app.setFileStatusBarText(
                String.format("Remaining time: %s", remainingTime));
        });
    }
}
