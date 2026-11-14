package io.github.coderodde.zipfx;

import io.github.coderodde.zipfx.ZipFXApp.State;
import java.util.Iterator;
import java.util.Objects;
import javafx.scene.control.ListView;

/**
 * 
 */
public final class FileList extends ListView<FileListEntry> 
                            implements Iterable<FileListEntry> {
    
    private final ZipFXApp app;
    
    public FileList(ZipFXApp app) {
        this.app = Objects.requireNonNull(app);
        
        setStyles();
    }
    
    public void add(FileListEntry entry) {
        getItems().add(entry);
        
        app.setState(State.IDLE_NON_EMPTY_LIST);
    }
    
    public void remove(FileListEntry entry) {
        getItems().remove(entry);
        
        if (getItems().isEmpty()) {
            app.setState(State.IDLE_EMPTY_LIST);
        }
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
    
    public FileListEntry[] toArray() {
        return getItems().toArray(FileListEntry[]::new);
    }
    
    private void setStyles() {
        setStyle(FXML);
    }
    
    private static final String FXML = 
        """
        -fx-background-color: #ddd;
        """;
}
