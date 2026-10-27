package io.github.coderodde.zipfx;

import java.io.File;
import java.util.Objects;
import javafx.concurrent.Task;

/**
 * 
 */
public class ZipTask extends Task<Void> {

    private final FileDataCounter totalFileDataCounter;
    private final FileDataCounter progressFileDataCounter;
    private final FileList fileList;
    private final File zipOutputFile;
    private final ZipFXApp app;
    
    public ZipTask(FileDataCounter totalFileDataCounter,
                   FileDataCounter progressFileDataCounter,
                   FileList fileList,
                   File zipOutputFile,
                   ZipFXApp app) {
        
        this.totalFileDataCounter = 
            Objects.requireNonNull(
                totalFileDataCounter,
                "The input total file data counter is null.");
        
        this.progressFileDataCounter = 
            Objects.requireNonNull(
                progressFileDataCounter, 
                "The input progress file data counter is null.");
        
        this.fileList =
            Objects.requireNonNull(fileList, "The file list is null.");
        
        this.zipOutputFile = 
            Objects.requireNonNull(
                zipOutputFile, 
                "The input .zip file is null.");
        
        this.app = Objects.requireNonNull(app, "The input app is null.");
    }
    
    @Override
    protected Void call() throws Exception {
//        try {
//            
//        } catch (IOException ex) {
//            set
//        }
        
        return null;
    }
}
