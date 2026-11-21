package io.github.coderodde.zipfx;

import io.github.coderodde.zipfx.ZipFXApp.State;
import java.util.Iterator;
import java.util.Objects;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

/**
 * 
 */
public final class FileListView extends ListView<FileListViewEntry> 
                             implements Iterable<FileListViewEntry> {
    
    private final ZipFXApp app;
    
    public FileListView(ZipFXApp app) {
        this.app = Objects.requireNonNull(app);
        
        setStyles();
    }
    
    public void add(FileListViewEntry entry) {
        getItems().add(entry);
        
        app.setState(State.IDLE_NON_EMPTY_LIST);
    }
    
    public void remove(FileListViewEntry entry) {
        getItems().remove(entry);
        
        if (getItems().isEmpty()) {
            app.setState(State.IDLE_EMPTY_LIST);
        }
    }
    
    public FileListViewEntry getFileListEntry(int index) {
        return getItems().get(index);
    }
    
    public boolean isEmpty() {
        return getItems().isEmpty();
    }
    
    public int size() {
        return getItems().size();
    }

    @Override
    public Iterator<FileListViewEntry> iterator() {
        return getItems().iterator();
    }
    
    public FileListViewEntry[] toArray() {
        return getItems().toArray(FileListViewEntry[]::new);
    }
    
    private void setStyles() {
        setStyle(FXML);
        
        setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(FileListViewEntry item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setGraphic(item);
                }
                
                setStyle(getIndex() % 2 == 0 
                    ? "-fx-background-color: #ddd" 
                    : "-fx-background-color: #ccc");
            }
        });
    }
    
    private static final String FXML = 
        """
        -fx-background-color: #ddd;
        """;
}
