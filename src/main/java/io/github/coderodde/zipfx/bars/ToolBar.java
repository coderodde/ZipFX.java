package io.github.coderodde.zipfx.bars;

import io.github.coderodde.zipfx.FileList;
import io.github.coderodde.zipfx.FileListEntry;
import io.github.coderodde.zipfx.ZipFXApp;
import io.github.coderodde.zipfx.buttons.ToolBarButton.ButtonIdentity;
import io.github.coderodde.zipfx.buttons.ToolBarButton;
import java.io.File;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

/**
 * 
 */
public final class ToolBar extends HBox {
    
    private final ZipFXApp app;
    private final ToolBarButton buttonAddFile;  
    private final ToolBarButton buttonAddFolder;
    private final ToolBarButton buttonRemoveAll;
    private final ToolBarButton buttonZip;
    private final ToolBarButton buttonCancel;
    
    
    public ToolBar(ZipFXApp app) {
        this.app        = app;
        buttonAddFile   = new ToolBarButton(ButtonIdentity.ADD_FILE);
        buttonAddFolder = new ToolBarButton(ButtonIdentity.ADD_FOLDER);
        buttonRemoveAll = new ToolBarButton(ButtonIdentity.REMOVE_ALL);
        buttonZip       = new ToolBarButton(ButtonIdentity.ZIP);
        buttonCancel    = new ToolBarButton(ButtonIdentity.CANCEL);
        
        Region regionSpacer = new Region();
        
        regionSpacer.setStyle("-fx-background-color: black;");
        regionSpacer.setMinWidth(0);
        regionSpacer.setMaxWidth(Double.MAX_VALUE);
        
        HBox.setHgrow(regionSpacer, Priority.ALWAYS);
        
        getChildren().addAll(buttonAddFile,
                             buttonAddFolder,
                             buttonRemoveAll,
                             buttonZip,
                             buttonCancel,
                             regionSpacer);
        
        setStyle("-fx-background-color: black;");
        setMaxWidth(Double.MAX_VALUE);
        
        setButtonEnableStates();
        giveNamesToControlButtons();
        setEventListeners();
    }
    
    
    public void setStateForIdleEmptyList() {
        buttonAddFile   .enable();
        buttonAddFolder .enable();
        
        buttonCancel    .disable();
        buttonRemoveAll .disable();
        buttonZip       .disable();
    }
    
    public void setStateForIdleNonEmptyList() {
        buttonAddFile   .enable();
        buttonAddFolder .enable();
        
        buttonCancel    .disable();
        buttonRemoveAll .enable();
        buttonZip       .enable();
    }
    
    public void setStateForZipping() {
        buttonAddFile   .disable();
        buttonAddFolder .disable();
        
        buttonCancel    .enable();
        buttonRemoveAll .disable();
        buttonZip       .disable();
    }
    
    public void setButtonEnableStates() {
        buttonAddFile  .enable();
        buttonAddFolder.enable();
        buttonRemoveAll.disable();
        buttonZip      .disable();
        buttonCancel   .disable();
    }
    
    private void giveNamesToControlButtons() {
        buttonAddFile  .setText("Add file");
        buttonAddFolder.setText("Add folder");
        buttonRemoveAll.setText("Remove all");
        buttonZip      .setText("Zip");
        buttonCancel   .setText("Cancel");
    }

    private void setEventListeners() {
        buttonAddFile.setOnAction(eh -> {
            if (app.getCurrentState() == ZipFXApp.State.ZIPPING) {
                return;
            }
            
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Choose a file");
            
            File file = chooser.showOpenDialog(app.getStage());
            
            if (file != null) {
                FileList fileList = app.getFileList();
                fileList.add(new FileListEntry(fileList, file));
            }
        });
        
        buttonAddFolder.setOnAction(eh -> {
            if (app.getCurrentState() == ZipFXApp.State.ZIPPING) {
                return;
            }
            
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Choose a folder");
            
            File file = chooser.showDialog(app.getStage());
            
            if (file != null) {
                FileList fileList = app.getFileList();
                fileList.add(new FileListEntry(fileList, file));
            }
        });
        
        buttonRemoveAll.setOnAction(event -> {
            if (app.getCurrentState() == ZipFXApp.State.ZIPPING) {
                return;
            }
            
            app.getFileList().getItems().clear();
        });
        
        buttonZip.setOnAction(event -> {
            if (app.getCurrentState() == ZipFXApp.State.ZIPPING) {
                // One zipping operation is already going on.
                return;
            }
            
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Choose the .zip file");
            
            chooser.getExtensionFilters()
                   .add(new FileChooser.ExtensionFilter(
                           "ZIP archives (*.zip)", 
                           "*.zip"));
            
            File file = chooser.showSaveDialog(app.getStage());
            
            if (file != null) {
                app.setCurrentState(ZipFXApp.State.ZIPPING);
                app.dispatchZipJob(file);
            }
        });
        
        buttonCancel.setOnAction(event -> {
            if (app.getCurrentState() != ZipFXApp.State.ZIPPING || 
                app.cancellationRequested()) {
                return;
            }
            
            app.requestCancellation();
            buttonCancel.disable();
            
            app.getStatusBar().setUpperStatusBarText("Cancelling...");
            app.getStatusBar().setLowerStatusBarText("");
            
            app.cancelAllTasks();
        });
    }
}
