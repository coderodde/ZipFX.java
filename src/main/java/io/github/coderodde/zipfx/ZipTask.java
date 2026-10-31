package io.github.coderodde.zipfx;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javafx.concurrent.Task;

/**
 * 
 */
public class ZipTask extends Task<Void> {
    
    private static final long NANOS_PER_SECOND = 1_000_000_000L;

    private final FileDataCounter totalFileDataCounter;
    private final FileDataCounter progressFileDataCounter;
    private final FileZipCountUpdateTask zipCountUpdateTask;
    private final FileList fileList;
    private final File zipOutputFile;
    private final ZipFXApp app;
    private long zippingDuration;
    private long compressedBytes;
    private long uncompressedBytes;
    
    public ZipTask(FileDataCounter totalFileDataCounter,
                   FileDataCounter progressFileDataCounter,
                   FileZipCountUpdateTask zipCountUpdateTask,
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
        
        this.zipCountUpdateTask =
            Objects.requireNonNull(
                zipCountUpdateTask, 
                "The input zip count update task is null.");
        
        this.fileList =
            Objects.requireNonNull(fileList, "The file list is null.");
        
        this.zipOutputFile = 
            Objects.requireNonNull(
                zipOutputFile, 
                "The input .zip file is null.");
        
        this.app = Objects.requireNonNull(app, "The input app is null.");
    }
    
    public long getDurationMilliseconds() {
        return zippingDuration;
    }
    
    public long getUncompressedByteCount() {
        return uncompressedBytes;
    }
    
    public long getCompressedByteCount() {
        return compressedBytes;
    }
    
    @Override
    protected Void call() throws Exception {
        if (fileList.isEmpty()) {
            app.statusBarShowEmptyListMessage();
            return null;
        }
        
        long ta = System.currentTimeMillis();
        
        Set<FileListEntry> uniqueFiles = 
            new HashSet<>(Arrays.asList(fileList.toArray()));
        
        
        try {
            Path targetZipPath = 
                zipOutputFile
                    .toPath()
                    .toAbsolutePath()
                    .normalize();
            
//            checkFileListContainsNoZipFile(targetZipPath, fileList);

            try (ZipOutputStream zos = 
                new ZipOutputStream(Files.newOutputStream(targetZipPath))) {
                
                for (FileListEntry entry : uniqueFiles) {
                    Path path = entry.file()
                                     .toPath()
                                     .toAbsolutePath()
                                     .normalize();
                    
                    if (path.getFileName() == null) {
                        throw new RootZipFileException();
                    }
                    
                    addEntry(zos, 
                             path, 
                             path.getFileName().toString(), 
                             targetZipPath,
                             entry);
                }
                
            } catch (RecursiveZipFileException ex) {
                
            } catch (RootZipFileException ex) {
                
            }
        } catch (IOException ex) {
            
        }
        
        zippingDuration = System.currentTimeMillis() - ta;
        compressedBytes = zipOutputFile.length();
        return null;
    }
    
    private void addEntry(ZipOutputStream zos,
                          Path path,
                          String entryName,
                          Path output,
                          FileListEntry ownerFileListEntry) throws IOException {
        
        if (Files.isSymbolicLink(path)) {
            
            if (Files.isDirectory(path)) {
                throw new DirectorySymbolicLinkException(path.toFile(),
                                                         ownerFileListEntry);
            } else if (Files.isRegularFile(path)) {
                throw new FileSymbolicLinkException(path.toFile(),
                                                    ownerFileListEntry);
            } else {
                throw new IllegalStateException("Unknown state.");
            }
        }
        
        if (Files.isRegularFile(path) && path.equals(output)) {
            throw new RecursiveZipFileException(zipOutputFile);
        }
        
        if (Files.isDirectory(path)) {
            zos.putNextEntry(new ZipEntry(entryName + "/"));
            zos.closeEntry();
            
            try (var children = Files.newDirectoryStream(path)) {
                for (Path child : children) {
                    addEntry(zos,
                             child,
                             entryName + "/" + child.getFileName(),
                             output,
                             ownerFileListEntry);
                }
            }
        } else if (Files.isRegularFile(path)) {
            long ta = System.nanoTime();
            
            zos.putNextEntry(new ZipEntry(entryName));
            Files.copy(path, zos);
            zos.closeEntry();
            
            long fileDuration = System.nanoTime() - ta;
            
            zipCountUpdateTask.addFileZippingDuration(fileDuration);
            uncompressedBytes += path.toFile().length();
        } else {
            throw new IOException("Not a regular file or directory: " + path);
        }
    }
    
}
