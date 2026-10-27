package io.github.coderodde.zipfx;

import java.io.File;
import java.nio.file.Files;
import java.util.Objects;

/**
 * This class implements the exception type objects of which are thrown when an
 * encountered directory is a symbolic link.
 */
public final class DirectorySymbolicLinkException extends RuntimeException  {
    
    private final File directory;
    private final FileListEntry ownerFileListEntry;
    
    public DirectorySymbolicLinkException(File directory,
                                          FileListEntry ownerFileListEntry) {
        this.directory =
            Objects.requireNonNull(
                directory, 
                "The input directory is null.");
        
        this.ownerFileListEntry = 
            Objects.requireNonNull(
                ownerFileListEntry, 
                "The input owner file list entry is null.");
        
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException(
                "The input file object is not a directory.");
        }
        
        if (!Files.isSymbolicLink(directory.toPath())) {
            throw new IllegalArgumentException(
                "The input directory is not a symbolic link. ");
        }
    }
    
    public File getDirectory() {
        return directory;
    }
    
    public FileListEntry getOwnerFileListEntry() {
        return ownerFileListEntry;
    }
}
