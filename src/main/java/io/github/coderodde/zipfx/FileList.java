package io.github.coderodde.zipfx;

import java.util.Iterator;
import javafx.scene.control.ListView;

/**
 * 
 */
public final class FileList extends ListView<FileListEntry> 
                            implements Iterable<FileListEntry> {
    
    public void add(FileListEntry entry) {
        getItems().add(entry);
    }
    
    public void remove(FileListEntry entry) {
        getItems().remove(entry);
    }
    
    public FileListEntry getFileListEntry(int index) {
        return getItems().get(index);
    }
    
    public boolean isEmpty() {
        return getItems().isEmpty();
    }
    
    public int size() {
        return getItems().size();
    }

    @Override
    public Iterator<FileListEntry> iterator() {
        return getItems().iterator();
    }
}
