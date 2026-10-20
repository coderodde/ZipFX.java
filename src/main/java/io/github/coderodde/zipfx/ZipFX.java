package io.github.coderodde.zipfx;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * This JavaFX application is for zipping files and folders.
 */
public class ZipFX extends Application {
    
    private static final PauseTransition statusMessageTimer = 
                     new PauseTransition();
    
    private static final Logger LOGGER = 
                         Logger.getLogger(ZipFX.class.getSimpleName());
    
    private static final double WINDOW_WIDTH  = 500.0;
    private static final double WINDOW_HEIGHT = 350.0;
    
    private static final long STATUS_MESSAGE_EMPTY_LIST_MILLIS = 3000L;

    private final Button buttonAddFile   = new Button("Add file");
    private final Button buttonAddFolder = new Button("Add folder");
    private final Button buttonDoZip     = new Button("Zip");
    private final Label labelStatusText  = new Label();
    private final VBox root              = new VBox();
    private final HBox toolBar           = new HBox();
    private final HBox mainUi            = new HBox();
    private final HBox statusBar         = new HBox();
    
    private final FileList fileList = new FileList();
    
    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("ZipFX");
        
        toolBar.getChildren()
               .addAll(buttonAddFile,
                       buttonAddFolder,
                       buttonDoZip);
        
        mainUi.getChildren().add(fileList);
        
        HBox.setHgrow(fileList, Priority.ALWAYS);
        fileList.setMaxWidth(Double.MAX_VALUE);
        
        statusBar.getChildren().addAll(labelStatusText);
        
        buttonAddFile.setOnAction(eh -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Choose a file");
            
            File file = chooser.showOpenDialog(stage);
            
            if (file != null) {
                fileList.add(new FileListEntry(fileList, file));
            }
        });
        
        buttonAddFolder.setOnAction(eh -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Choose a folder");
            
            File file = chooser.showDialog(stage);
            
            if (file != null) {
                fileList.add(new FileListEntry(fileList, file));
            }
        });
        
//        statusBar.getChildren()
//                 .addAll(labelStatusTitle,
//                         labelStatusText);
        
        root.getChildren()
            .addAll(toolBar,
                    mainUi, 
                    statusBar);
                    
        stage.setScene(new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT));
        stage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
    public void setStatusBarText(String text) {
        labelStatusText.setText(text);
    }
    
    private void clearStatusBarText() {
        labelStatusText.setText("");
    }
    
    private void statusBarShowEmptyListMessage() {
        setStatusMessage("Nothing to zip.", STATUS_MESSAGE_EMPTY_LIST_MILLIS);
    }
    
    private void zipAllSelected() {
        if (fileList.isEmpty()) {
            statusBarShowEmptyListMessage();
            return;
        }
        
        Set<File> fileSet = new HashSet<>();
        
        for (int i = 0; i < fileList.size(); ++i) {
            fileSet.add(fileList.getFileListEntry(i).file());
        }
        
        // Prune duplicates:
        Iterator<FileListEntry> iterator = fileList.iterator();
        
        while (iterator.hasNext()) {
            FileListEntry entry = iterator.next();
            
            if (!fileSet.contains(entry)) {
                iterator.remove();
            }
        }
        
        FileCounter fileCounter = new FileCounter();
        
        CheckAndCountFilesTask countTask = 
            new CheckAndCountFilesTask(fileList, fileCounter);
        
        FileCountUpdateTask fileCounterUpdateTask = 
            new FileCountUpdateTask(this, fileCounter);
        
        Thread zippingThread       = new Thread(countTask);
        Thread counterUpdateThread = new Thread(fileCounterUpdateTask);
        
       
        
        zippingThread.start();
        counterUpdateThread.start();
        
    }
    
    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            
            LOGGER.log(Level.WARNING, "Sleeping failed: {}", ex.getMessage());
        }
    }
    
    private void setStatusMessage(String text, long durationMillis) {
        statusMessageTimer.stop();
        
        labelStatusText.setText(text);
        
        statusMessageTimer.setDuration(Duration.millis(durationMillis));
        statusMessageTimer.setOnFinished(event -> clearStatusBarText());
        statusMessageTimer.playFromStart();
    }
}
