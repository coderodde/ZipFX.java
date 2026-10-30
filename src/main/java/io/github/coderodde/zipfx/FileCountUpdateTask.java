package io.github.coderodde.zipfx;

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

    private static final long COUNT_UPDATE_SLEEP = 100L;
    
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
                long files       = fileDataCounter.getFileCount();
                long directories = fileDataCounter.getDirectoryCount();
                long bytes       = fileDataCounter.getByteCount();
                
                String strFile      = wordFiles(files);
                String strDirectory = wordDirectories(directories);
                String strBytes     = wordBytes(bytes);
                
                app.setFileStatusBarText(
                    "Counted %d %s in %d %s worth %d %s."
                        .formatted(files, 
                                   strFile, 
                                   directories, 
                                   strDirectory, 
                                   bytes, 
                                   strBytes));
            });
            
            sleep(COUNT_UPDATE_SLEEP);
        }
    }
}
