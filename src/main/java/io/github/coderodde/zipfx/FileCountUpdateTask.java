package io.github.coderodde.zipfx;

import static io.github.coderodde.zipfx.Misc.integerToString;
import static io.github.coderodde.zipfx.Misc.sleep;
import static io.github.coderodde.zipfx.Misc.wordBytes;
import static io.github.coderodde.zipfx.Misc.wordDirectories;
import static io.github.coderodde.zipfx.Misc.wordFiles;
import java.util.Objects;
import javafx.application.Platform;
import javafx.concurrent.Task;

/**
 *
 */
public final class FileCountUpdateTask extends Task<Void> {

    // Milliseconds
    private static final long COUNT_UPDATE_SLEEP = 500L;
    
    private final ZipFXApp app;
    private final FileDataCounter fileDataCounter;
    
    private volatile boolean requestHalt = false;
    
    public FileCountUpdateTask(ZipFXApp app, FileDataCounter fileCounter) {
        this.app = Objects.requireNonNull(app, "The input app object is null.");
        
        this.fileDataCounter = 
            Objects.requireNonNull(fileCounter, 
                                   "The file data counter is null.");
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
                long numFiles       = fileDataCounter.getFileCount();
                long numDirectories = fileDataCounter.getDirectoryCount();
                long numBytes       = fileDataCounter.getByteCount();
                
                String strFiles       = integerToString(numFiles);
                String strDirectories = integerToString(numDirectories);
                String strBytes       = integerToString(numBytes);
                
                String wordFile      = wordFiles(numFiles);
                String wordDirectory = wordDirectories(numDirectories);
                String wordBytes     = wordBytes(numBytes);
                
                app.setFileStatusBarText(
                    "Counted %s %s in %s %s worth %s %s."
                        .formatted(strFiles, 
                                   wordFile, 
                                   strDirectories,
                                   wordDirectory, 
                                   strBytes,
                                   wordBytes));
            });
            
            sleep(COUNT_UPDATE_SLEEP);
        }
    }
}
