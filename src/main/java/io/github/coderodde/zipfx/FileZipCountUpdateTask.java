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
    private final AtomicLong      accumulateddNanoseconds = new AtomicLong(0L);
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
        while (!isCancelled()) {
            printStatistics();
            Misc.sleep(SLEEP_DURATION_MILLIS);
        }
            
        return null;
    }
    
    public void addFileZippingDuration(long nanoseconds) {
        accumulateddNanoseconds.addAndGet(nanoseconds);
    }
    
    public void halt() {
        cancel(true);
    }
    
    private void printStatistics() {
        long totalBytes     = totalFileDataCounter.getByteCount();
        long processedBytes = zipProgressDataCounter.getByteCount();
        long elapsedNanos   = accumulateddNanoseconds.get();
        
        double progress = totalBytes > 0 
                        ? Math.clamp((double) processedBytes / totalBytes, 
                                     0.0,
                                     1.0)
                        : 0.0;
        
        String progressText = 
            String.format("Progress: %.1f %%", 100.0 * progress);
        
        String remainingText;
        
        if (progress == 1.0) {
            remainingText = "Remaining time: " + new RemainingTime(0);
        } else if (progress == 0.0 || elapsedNanos == 0L) {
            remainingText = "Remaining time: estimating...";
        } else {
            double elapsedSeconds   = elapsedNanos / NANOSECONDS_PER_SECOND;
            double remainingSeconds = elapsedSeconds * (1.0 - progress)
                                                     / progress;
            remainingText = "Remaining time: " 
                          + new RemainingTime(
                                  (int) Math.ceil(remainingSeconds));
        }
        
        Platform.runLater(() -> {
            if (isCancelled()) {
                return;
            }
            
            app.setProgressStatusBarText(progressText);
            app.setFileStatusBarText(remainingText);
        });
        
//        progress = Math.max(progress, MINIMUM_PROGRESS);
//        
//        double percentCompressed = 100.0 * progress;
//        
//        String percentsToPrint = String.format("%.1f %%", percentCompressed);
//        
//        Platform.runLater(() -> {
//            app.setProgressStatusBarText("Progress: " + percentsToPrint);
//        });
//        
//        double totalEstimatedDurationNanoseconds
//                = (double) accumulateddNanoseconds.get() 
//                / progress;
//        
//        double remainingDurationNanoseconds = totalEstimatedDurationNanoseconds
//                                            - accumulateddNanoseconds.get();
//        
//        RemainingTime remainingTime =
//            new RemainingTime((int)(remainingDurationNanoseconds / 
//                                    NANOSECONDS_PER_SECOND));
//        
//        Platform.runLater(() -> {
//            app.setFileStatusBarText(
//                String.format("Remaining time: %s", remainingTime));
//        });
    }
}
