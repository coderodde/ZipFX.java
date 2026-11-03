package io.github.coderodde.zipfx;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;
import java.util.zip.ZipOutputStream;

/**
 *
 */
public final class RootZipFileException extends RuntimeException {
 
    private final Path zipFilePath;
    private final ZipOutputStream zos;
    
    public RootZipFileException(Path zipFilePath, ZipOutputStream zos) {
        this.zipFilePath = 
            Objects.requireNonNull(zipFilePath, 
                                   "The input .zip file path is null.");
        
        this.zos = 
            Objects.requireNonNull(zos, 
                                   "The input .zip output stream is null.");
    }
    
    public Path zipFilePath() {
        return zipFilePath;
    }
    
    public ZipOutputStream zipOutputStream() {
        return zos;
    }
}
