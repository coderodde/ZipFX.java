package io.github.coderodde.zipfx;

import java.io.File;
import java.util.Objects;

/**
 *
 */
public final class FileIsSymbolicLinkException extends RuntimeException {
    
    private final File file;
    private final FileListViewEntry fileListEntry;
    
    public FileIsSymbolicLinkException(File symbolicLink, 
                                       FileListViewEntry entry,
                                       String message) {
        super(message);
        
        this.file = Objects.requireNonNull(symbolicLink, 
                                           "The input file is null.");
        this.fileListEntry = 
            Objects.requireNonNull(entry, "The input file list entry is null.");
    }
    
    public File getFile() {
        return file;
    }
    
    public FileListViewEntry getFileListEntry() {
        return fileListEntry;
    }
}
