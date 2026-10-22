package io.github.coderodde.zipfx;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
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
    private static final long STATUS_MESSAGE_ZIP_IS_SOURCE     = 3000L;

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
        
        buttonDoZip.setOnAction(event -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Choose the .zip file");
            
            chooser.getExtensionFilters()
                   .add(new FileChooser.ExtensionFilter(
                           "ZIP archives (*.zip)", 
                           "*.zip"));
            
            File file = chooser.showSaveDialog(stage);
            
            if (file != null) {
                zipAllSelected(stage);
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
    
    private void zipAllSelected(Stage stage) {
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
            
            if (!fileSet.contains(entry.file())) {
                iterator.remove();
            }
        }
        
        FileCounter fileCounter = new FileCounter();
        
        CheckAndCountFilesTask countTask = 
            new CheckAndCountFilesTask(fileList, fileCounter);
        
        FileCountUpdateTask fileCounterUpdateTask = 
            new FileCountUpdateTask(this, fileCounter);
        
        countTask.setOnSucceeded(event -> {
            long count = countTask.getValue();
            
            setStatusBarText("Files to zip: " + count);
            
//            zipAll(fileList, stage);
        });
        
        Thread zippingThread       = new Thread(countTask);
        Thread counterUpdateThread = new Thread(fileCounterUpdateTask);
        
        zippingThread.start();
        counterUpdateThread.start();
    }
    
    private void zipAll(FileList fileList, Stage stage) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select the target .zip file");
        
        File file = chooser.showSaveDialog(stage);
        
        if (file != null) {
            try {
                zipAllImpl(fileList, file);
            } catch (IOException ex) {
                setStatusMessage(ex.getMessage(), STATUS_MESSAGE_EMPTY_LIST_MILLIS);
            }
        }
    }
    
    private static void zipAllImpl(FileList fileList, File zipFile) 
    throws IOException {
        
        Path target = zipFile.toPath().toAbsolutePath().normalize();
        
        checkFileListContainsNoZipFile(fileList, zipFile);
        
        ZipOutputStream zos = 
            new ZipOutputStream(Files.newOutputStream(zipFile.toPath()));
        
        for (FileListEntry entry : fileList) {
            Path path = entry.file().toPath().toAbsolutePath().normalize();
            
            if (path.getFileName() == null) {
                LOGGER.log(Level.WARNING, 
                           "Filesystem root is requested for zipping.");
            }
            
            addEntry(zos, 
                     path, 
                     path.getFileName().toString(),
                     target);
        }
    }
    
    private static void addEntry(ZipOutputStream zos,
                                 Path path,
                                 String entryName,
                                 Path output) throws IOException {
        
        if (Files.isSymbolicLink(path) || path.equals(output)) {
            return;
        }
        
        if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
            zos.putNextEntry(new ZipEntry(entryName + "/"));
            zos.closeEntry();
            
            try (var children = Files.newDirectoryStream(path)) {
                for (Path child : children) {
                    addEntry(zos,
                             child,
                             entryName + "/" + child.getFileName(),
                             output);
                }
            }
        } else if (Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) {
            zos.putNextEntry(new ZipEntry(entryName));
            Files.copy(path, zos);
            zos.closeEntry();
        } else {
            throw new IOException("Not a regular file or directory: " + path);
        }
    }
    
    private static void checkFileListContainsNoZipFile(
        FileList fileList, 
        File zipFile) throws IOException {
        
        for (FileListEntry entry : fileList) {
            if (entry.file()
                     .toPath()
                     .toAbsolutePath()
                     .normalize()
                     .equals(zipFile)) {
                
                throw new IOException("Output ZIP is a also a source file.");
            }
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
