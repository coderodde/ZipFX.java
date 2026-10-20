package io.github.coderodde.zipfx;

import java.io.File;
import java.util.Objects;

/**
 *
 */
public final class FileIsSymbolicLinkException extends RuntimeException {
    
    private final File file;
    private final FileListEntry fileListEntry;
    
    public FileIsSymbolicLinkException(File symbolicLink, 
                                       FileListEntry entry,
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
    
    public FileListEntry getFileListEntry() {
        return fileListEntry;
    }
}
