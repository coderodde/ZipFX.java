package io.github.coderodde.zipfx;

import java.io.File;
import java.util.Objects;

/**
 *
 */
public final class RecursiveZipFileException extends RuntimeException {
    
    private final File file;
    
    public RecursiveZipFileException(File file) {
        this.file = 
            Objects.requireNonNull(file, "The input .zip file is null.");
    }
    
    public File file() {
        return file;
    }
}
