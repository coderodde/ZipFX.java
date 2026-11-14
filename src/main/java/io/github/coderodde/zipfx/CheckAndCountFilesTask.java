package io.github.coderodde.zipfx;

import java.io.File;
import java.nio.file.Files;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import javafx.concurrent.Task;

/**
 * This JavaFX task is responsible for counting the total number of files 
 * involved <b>and</b> validating that each reachable file/folder is <b>not</b>
 * a symbolic link.
 */
public final class CheckAndCountFilesTask extends Task<Void> {

    private final FileList fileList;
    private final FileDataCounter counter;
    
    public CheckAndCountFilesTask(FileList fileList, FileDataCounter counter) {
        this.fileList =
            Objects.requireNonNull(fileList, "The input entry list is null.");
    
        this.counter = Objects.requireNonNull(counter, 
                                              "The file counter is null.");
    }
    
    @Override
    protected Void call() throws Exception {
        checkCancellation();
        
        for (FileListEntry entry : fileList) {
            checkCancellation();
            processEntry(entry);
        }
        
        checkCancellation();
        return null;
    }
    
    private void processEntry(FileListEntry entry) {
        checkCancellation();
        processEntryImpl(entry.file(), entry);
    }
    
    private void processEntryImpl(File file, FileListEntry entry) {
        checkCancellation();
        
        if (Files.isSymbolicLink(file.toPath())) {
            if (file.isFile()) {
                throw new FileSymbolicLinkException(file, entry);
            } else if (file.isDirectory()) {
                throw new DirectorySymbolicLinkException(file, entry);
            } else {
                throw new IllegalStateException("Unknown state.");
            }
        }
        
        checkCancellation();
        
        if (file.isFile()) {
            counter.increaseFileCounter();
            counter.addByteCounter(file.length());
            return;
        }
        
        counter.increaseDirectoryCounter();
        
        File[] children = file.listFiles();
        
        checkCancellation();
        
        if (children == null) {
            return;
        }
        
        for (File child : children) {
            checkCancellation();
            processEntryImpl(child, entry);
        }
    }
    
    private void checkCancellation() {
        if (isCancelled()) {
            throw new CancellationException("File counting was cancelled.");
        }
    }
}
