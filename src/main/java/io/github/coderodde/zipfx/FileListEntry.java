package io.github.coderodde.zipfx;

import java.io.File;
import java.util.Objects;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/**
 * This 
 */
public final class FileListEntry extends HBox {
    
    private final File file;
    private final Label labelFileName = new Label();
    private final Button buttonRemove = new Button("Remove");
    
    public FileListEntry(FileList fileList, File file) {
        Objects.requireNonNull(fileList, "The file list is null.");
        
        this.file = Objects.requireNonNull(file, "The file is null.");
        
        labelFileName.setText(file.getAbsolutePath());
        
        buttonRemove.setOnAction(event -> fileList.remove(this));
        
        setSpacing(8);
        getChildren().addAll(labelFileName, buttonRemove);
    }
    
    public File file() {
        return file;
    }
}
