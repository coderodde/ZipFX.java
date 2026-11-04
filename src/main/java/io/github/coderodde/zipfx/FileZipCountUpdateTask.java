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
    private final AtomicLong      elapsedZippingNanos = new AtomicLong();
    
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
        elapsedZippingNanos.addAndGet(nanoseconds);
    }
    
    public void halt() {
        cancel(true);
    }
    
    private void printStatistics() {
        long totalBytes     = totalFileDataCounter.getByteCount();
        long processedBytes = zipProgressDataCounter.getByteCount();
        long elapsedNanos   = elapsedZippingNanos.get();
        
        String progressText;
        String remainingText;
        
        if (totalBytes == 0L) {
            progressText  = "Progress: no file data to compress.";
            remainingText = ""; 
        } else {
            double progress = Math.clamp((double) processedBytes / totalBytes, 
                                         0.0,
                                         1.0);

            progressText = "Progress: %.1f %%".formatted(100.0 * progress);
            
            if (processedBytes >= totalBytes) {
                remainingText = "Finishing archive...";
            } else if (processedBytes == 0L || elapsedNanos == 0L) {
                remainingText = "Remaining time: estimating...";
            } else {
                long remainingBytes     = totalBytes - processedBytes;
                double elapsedSeconds   = elapsedNanos / NANOSECONDS_PER_SECOND;
                double remainingSeconds = elapsedSeconds 
                                        * remainingBytes
                                        / processedBytes;
                
                remainingText = "Remaining time: " 
                              + new RemainingTime(
                                      (int) Math.ceil(remainingSeconds)
                              );
            }
            
            String statusText = remainingText.isEmpty() 
                              ? progressText 
                              : progressText + "    |    " + remainingText;
            
            Platform.runLater(() -> {
                if (!isCancelled()) {
                    app.setProgressStatusBarText(statusText);
//                    app.setProgressStatusBarText(progressText);
//                    app.setFileStatusBarText(remainingText);
                }
            });
        }
    }
}
