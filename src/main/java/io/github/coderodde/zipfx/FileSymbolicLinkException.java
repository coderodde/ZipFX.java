package io.github.coderodde.zipfx;

import java.io.File;
import java.nio.file.Files;
import java.util.Objects;

/**
 * This class implements the exception type objects of which are thrown when an
 * encountered directory is a symbolic link.
 */
public final class FileSymbolicLinkException extends RuntimeException  {
    
    private final File file;
    private final FileListEntry ownerFileListEntry;
    
    public FileSymbolicLinkException(File file,
                                     FileListEntry ownerFileListEntry) {
        this.file =
            Objects.requireNonNull(
                file, 
                "The input file is null.");
        
        this.ownerFileListEntry = 
            Objects.requireNonNull(
                ownerFileListEntry, 
                "The input owner file list entry is null.");
        
        if (!file.isFile()) {
            throw new IllegalArgumentException(
                "The input file object is not a file.");
        }
        
        if (!Files.isSymbolicLink(file.toPath())) {
            throw new IllegalArgumentException(
                "The input file is not a symbolic link. ");
        }
    }
    
    public File getFile() {
        return file;
    }
    
    public FileListEntry getOwnerFileListEntry() {
        return ownerFileListEntry;
    }
}
