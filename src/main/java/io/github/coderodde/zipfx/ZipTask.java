package io.github.coderodde.zipfx;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javafx.concurrent.Task;

/**
 * 
 */
public class ZipTask extends Task<Void> {
    
    private static final int BUFFER_SIZE = 64 * 1024;
    private static final long NANOS_PER_SECOND = 1_000_000_000L;

    private final FileDataCounter progressFileDataCounter;
    private final FileZipCountUpdateTask zipCountUpdateTask;
    private final Set<Path> sourcePaths = new LinkedHashSet<>();
    private final Path outputPath;
    private final byte[] buffer = new byte[BUFFER_SIZE];
    
    private long durationMilliseconds;
    private long compressedBytes;
    private long uncompressedBytes;
    
    public ZipTask(FileDataCounter totalFileDataCounter,
                   FileDataCounter progressFileDataCounter,
                   FileZipCountUpdateTask zipCountUpdateTask,
                   FileList fileList,
                   File zipOutputFile,
                   ZipFXApp app) {
        
        Objects.requireNonNull(totalFileDataCounter,
                               "The input total file data counter is null.");
        
        Objects.requireNonNull(app, "The input app is null.");
        Objects.requireNonNull(fileList, "The file list is null.");
        
        this.progressFileDataCounter = 
            Objects.requireNonNull(
                progressFileDataCounter, 
                "The input progress file data counter is null.");
        
        this.zipCountUpdateTask =
            Objects.requireNonNull(
                zipCountUpdateTask, 
                "The input zip count update task is null.");
        
        
        this.outputPath = 
            Objects.requireNonNull(
                zipOutputFile, 
                "The input .zip file is null.")
                    .toPath()
                    .toAbsolutePath()
                    .normalize();
        
        for (FileListEntry entry : fileList) {
            sourcePaths.add(entry.file()
                                 .toPath()
                                 .toAbsolutePath()
                                 .normalize());
        }
    }
    
    public long getDurationMilliseconds() {
        return durationMilliseconds;
    }
    
    public long getUncompressedByteCount() {
        return uncompressedBytes;
    }
    
    public long getCompressedByteCount() {
        return compressedBytes;
    }
    
    @Override
    protected Void call() throws Exception {
        long startNanos = System.nanoTime();
        
        Path temporaryArchive = null;
        
        try {
            checkCancellation();
            
            if (sourcePaths.isEmpty()) {
                throw new IOException("Nothing to zip.");
            }
            
            Path destination = validateDestination();
            
            checkCancellation();
            
            temporaryArchive = 
                Files.createTempFile(destination.getParent(), ".zipfx", ".tmp");
        
            try (ZipOutputStream output = 
                new ZipOutputStream(
                    new BufferedOutputStream(
                        Files.newOutputStream(temporaryArchive)))) {
                
                for (Path source : sourcePaths) {
                    checkCancellation();
                    
                    addEntry(output, source, source.getFileName().toString());
                }
            }
            
            checkCancellation();
            
            long archiveSize = Files.size(temporaryArchive);
            
            checkCancellation();
            
            publishArchive(temporaryArchive, destination);
            
            compressedBytes = archiveSize;
            
            durationMilliseconds = 
                TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
        
            return null;
        } finally {
            if (temporaryArchive != null) {
                Files.deleteIfExists(temporaryArchive);
            }
        }
    }
    
    private Path validateDestination() throws IOException {
        if (outputPath.getFileName() == null) {
            throw new IOException();
        }
        
        Path destination = 
            outputPath
                .getParent()
                .toRealPath()
                .resolve(outputPath.getFileName());
        
        if (Files.isSymbolicLink(destination)) {
            throw new FileIsSymbolicLinkException(destination.toFile(), null, "");
        }
        
        boolean destinationExists = 
            Files.exists(destination, LinkOption.NOFOLLOW_LINKS);
        
        if (destinationExists 
            && !Files.isRegularFile(destination, LinkOption.NOFOLLOW_LINKS)) {
            
            throw new IOException(
                "The output is not a regular file: " + destination
            );
        }
        
        for (Path source : sourcePaths) {
            checkCancellation();
            
            if (source.getFileName() == null) {
                throw new IOException(
                    "Cannot zip a filesystem root: " + source
                );
            }
            
            rejectSymbolicLink(source);
            
            Path realSource = source.toRealPath();
            
            if (destinationExists 
                && Files.isSameFile(realSource, destination)) {
                
                throw new IOException("The output ZIP is also a source file: " + 
                                      destination);
            }
            
            if (Files.isDirectory(realSource)
                && destination.startsWith(realSource)) {
                
                throw new IOException(
                    "The output ZIP file is inside a selected directory: " 
                    + destination
                );
            }
        }
        
        return destination;
    }
    
    private void addEntry(ZipOutputStream output,
                          Path source,
                          String entryName) throws IOException {
        
        checkCancellation();
        rejectSymbolicLink(source);
        
        if (Files.isDirectory((source), LinkOption.NOFOLLOW_LINKS)) {
            output.putNextEntry(new ZipEntry(entryName + "/"));
            output.closeEntry();
            progressFileDataCounter.increaseDirectoryCounter();
            
            try (DirectoryStream<Path> children =
                    Files.newDirectoryStream(source)) {
                
                for (Path child : children) {
                    addEntry(output,
                             child,
                             entryName + "/" + child.getFileName());
                }
                
                return;
            }
        }
        
        if (!Files.isRegularFile(source, LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException("Not a regular file or directory: " + source);
        }
        
        writeFile(output, source, entryName);
    }
        
    private void writeFile(ZipOutputStream output,
                           Path source,
                           String entryName) throws IOException {
        
        long lastUpdateNanos = System.nanoTime();
        
        output.putNextEntry(new ZipEntry(entryName));
        
        try (InputStream input = Files.newInputStream(source)) {
            int bytesRead;
            
            while ((bytesRead = input.read(buffer)) != -1) {
                checkCancellation();
                output.write(buffer, 0, bytesRead);
                
                long now = System.nanoTime();
                
                zipCountUpdateTask.addFileZippingDuration(
                    now - lastUpdateNanos
                );
                
                lastUpdateNanos = now;
                
                uncompressedBytes += bytesRead;
                progressFileDataCounter.addByteCounter(bytesRead);
            }
        }
        
        output.closeEntry();
        
        zipCountUpdateTask.addFileZippingDuration(
            System.nanoTime() - lastUpdateNanos
        );
        
        progressFileDataCounter.increaseFileCounter();
    }
    
    private void checkCancellation() {
        if (isCancelled()) {
            throw new CancellationException("Zipping was cancelled.");
        }
    }
    
    private static void rejectSymbolicLink(Path path) throws IOException {
        if (Files.isSymbolicLink(path)) {
            throw new IOException("Cannot zip a symbolic link: " + path);
        }
    }
    
    private static void publishArchive(Path temporaryArchive,
                                       Path destination) throws IOException {
        try {
            Files.move(temporaryArchive, 
                       destination,
                       StandardCopyOption.ATOMIC_MOVE, 
                       StandardCopyOption.REPLACE_EXISTING);
            
        } catch (AtomicMoveNotSupportedException ex) {
            
            Files.move(temporaryArchive, 
                       destination, 
                       StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
