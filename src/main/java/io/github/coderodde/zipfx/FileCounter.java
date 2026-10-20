package io.github.coderodde.zipfx;

import java.util.concurrent.atomic.AtomicLong;

/**
 *
 */
public final class FileCounter {
    
    private final AtomicLong count = new AtomicLong(0L);
    
    public void increase() {
        count.incrementAndGet();
    }
    
    public long getCount() {
        return count.get();
    }
}
