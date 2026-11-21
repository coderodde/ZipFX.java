package io.github.coderodde.zipfx;

import java.io.File;
import java.util.Objects;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/**
 * This 
 */
public final class FileListViewEntry extends HBox {
    
    private final File file;
    private final Label labelFileName = new Label();
    private final Button buttonRemove = new Button("Remove");
    
    public FileListViewEntry(FileListView fileList, File file) {
        Objects.requireNonNull(fileList, "The file list is null.");
        
        this.file = Objects.requireNonNull(file, "The file is null.");
        
        labelFileName.setText(file.getAbsolutePath());
        
        buttonRemove.setOnAction(event -> fileList.remove(this));
        
        Region regionSpacer = new Region();
        
        HBox.setHgrow(regionSpacer, Priority.ALWAYS);
        
        regionSpacer.setMinWidth(0);
        
        setSpacing(8);
        getChildren().addAll(labelFileName, regionSpacer, buttonRemove);
        
        setMaxWidth(Double.MAX_VALUE);
    }
    
    public File file() {
        return file;
    }
    
    @Override
    public int hashCode() {
        return file.hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        return o instanceof FileListViewEntry other 
            && Objects.equals(file, other.file);
    }
}
