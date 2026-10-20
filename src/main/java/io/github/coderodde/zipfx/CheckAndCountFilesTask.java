package io.github.coderodde.zipfx;

import java.io.File;
import java.nio.file.Files;
import java.util.Objects;
import javafx.concurrent.Task;

/**
 * This JavaFX task is responsible for counting the total number of files 
 * involved <b>and</b> validating that each reachable file/folder is <b>not</b>
 * a symbolic link.
 */
public final class CheckAndCountFilesTask extends Task<Long> {

    private final FileList fileList;
    private final FileCounter counter;
    
    public CheckAndCountFilesTask(FileList fileList, FileCounter counter) {
        this.fileList =
            Objects.requireNonNull(fileList, "The input entry list is null.");
    
        this.counter = Objects.requireNonNull(counter, 
                                              "The file counter is null.");
    }
    
    @Override
    protected Long call() throws Exception {
        for (FileListEntry entry : fileList) {
            processEntry(entry);
        }
        
        return counter.getCount();
    }
    
    private void processEntry(FileListEntry entry) {
        processEntryImpl(entry.file(), entry);
    }
    
    private void processEntryImpl(File file, FileListEntry entry) {
        if (Files.isSymbolicLink(file.toPath())) {
            if (file.isFile()) {
                throw new FileIsSymbolicLinkException(
                    file,
                    entry,
                    "File \"%s\" is a symbolic link."
                            .formatted(file.getAbsoluteFile().getName()));
                
            } else if (file.isDirectory()) {
                throw new FileIsSymbolicLinkException(
                    file,
                    entry,
                    "Directory \"%s\" is a symbolic link."
                            .formatted(file.getAbsoluteFile().getName()));
            } else {
                throw new IllegalStateException("Unknown state.");
            }
        }
        
        if (file.isFile()) {
            counter.increase();
            return;
        }
        
        File[] children = file.listFiles();
        
        if (children == null) {
            return;
        }
        
        for (File child : children) {
            if (child.isFile()) {
                counter.increase();
            } else {
                processEntryImpl(child, entry);
            }
        }
    }
}
