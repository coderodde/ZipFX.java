package io.github.coderodde.zipfx;

import java.util.concurrent.atomic.AtomicLong;

/**
 * This class counts the files and directories encountered while scanning the
 * files/folders to compress. Also, it counts the number of bytes so far 
 * encountered.
 */
public final class FileDataCounter {
    
    private final AtomicLong fileCount      = new AtomicLong(0L);
    private final AtomicLong directoryCount = new AtomicLong(0L);
    private final AtomicLong byteCount      = new AtomicLong(0L);
    
    public void increaseFileCounter() {
        fileCount.incrementAndGet();
    }
    
    public void increaseDirectoryCounter() {
        directoryCount.incrementAndGet();
    }
    
    public void addByteCounter(long value) {
        byteCount.addAndGet(value);
    }
    
    public long getFileCount() {
        return fileCount.get();
    }
    
    public long getDirectoryCount() {
        return directoryCount.get();
    }
    
    public long getByteCount() {
        return byteCount.get();
    }
}
