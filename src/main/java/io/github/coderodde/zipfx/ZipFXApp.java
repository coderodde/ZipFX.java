package io.github.coderodde.zipfx;

import java.io.File;
import java.io.IOException;
import static java.lang.Math.clamp;
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
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * This JavaFX application is for zipping files and folders.
 */
public class ZipFXApp extends Application {
    
    private static final PauseTransition statusMessageTimer = 
                     new PauseTransition();
    
    private static final Logger LOGGER = 
                         Logger.getLogger(ZipFXApp.class.getSimpleName());
    
    private static final double TITLE_BAR_BUTTON_WIDTH  = 40.0;
    private static final double TITLE_BAR_BUTTON_HEIGHT = 40.0;
    
    private static final double WINDOW_WIDTH  = 500.0;
    private static final double WINDOW_HEIGHT = 350.0;
    
    private static final long STATUS_MESSAGE_EMPTY_LIST_MILLIS = 3000L;
    private static final long STATUS_MESSAGE_ZIP_IS_SOURCE     = 3000L;
    private static final long STATUS_MESSAGE_IO_EXCEPTION      = 5000L;
    
    
    private static final String FXML_TITLE_BAR_BUTTON_CSS = 
            """
            -fx-background-color: black;
            -fx-background-radius: 0;
            -fx-background-insets: 0;
            -fx-border-width: 0;
            -fx-border-color: transparent;
            -fx-border-radius: 0;
            -fx-text-fill: white;
            """;
    
    private static final String FXML_TITLE_BAR_BUTTON_CSS_ON_HOVER = 
            """
            -fx-background-color: white;
            -fx-background-radius: 0;
            -fx-background-insets: 0;
            -fx-border-width: 0;
            -fx-border-color: transparent;
            -fx-border-radius: 0;
            -fx-text-fill: black;
            """;
    
    private final Button buttonAddFile   = new Button("Add file");
    private final Button buttonAddFolder = new Button("Add folder");
    private final Button buttonDoZip     = new Button("Zip");
    private final Button buttonClose     = new Button("✕");
    private final Button buttonMinimize  = new Button("─");
    private final Button buttonMaximize  = new Button("□");
    private final Label labelStatusText  = new Label();
    private final VBox root              = new VBox();
    private final HBox toolBar           = new HBox();
    private final HBox mainUi            = new HBox();
    private final HBox statusBar         = new HBox();
    
    private final FileList fileList = new FileList();
    
    private double dragOffsetX;
    private double dragOffsetY;
    
    @Override
    public void start(Stage stage) throws Exception {
        setAllStyles();
        
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
                zipAllSelected(file, stage);
            }
        });
        
//        statusBar.getChildren()
//                 .addAll(labelStatusTitle,
//                         labelStatusText);
        
        root.getChildren()
            .addAll(toolBar,
                    mainUi, 
                    statusBar);
        
        
        stage.initStyle(StageStyle.UNDECORATED);
        
        Label title = new Label("ZipFX");
        title.setStyle("-fx-text-fill: white;");
        buttonClose.setOnAction(event -> stage.close());
        
        
        buttonMinimize.setOnAction(event -> stage.setIconified(true));
        buttonMaximize.setOnAction(event -> {
            stage.setMaximized(!stage.isMaximized());
        });
        
        stage.maximizedProperty().addListener((observable, 
                                               oldValue, 
                                               maximized) -> 
            buttonMaximize.setText(maximized ? "❐" : "□")
        );
        
        for (Button button : new Button[] { buttonMinimize, buttonMaximize }) {
            button.setStyle(FXML_TITLE_BAR_BUTTON_CSS);
            
            button.setOnMouseEntered(event -> {
                button.setStyle(FXML_TITLE_BAR_BUTTON_CSS_ON_HOVER);
            });
            
            button.setOnMouseExited(event -> {
                button.setStyle(FXML_TITLE_BAR_BUTTON_CSS);
            });
        }
        
        for (Button button : new Button[] { buttonMinimize,
                                            buttonMaximize,
                                            buttonClose}) {
            
            button.setMinWidth (TITLE_BAR_BUTTON_WIDTH);
            button.setPrefWidth(TITLE_BAR_BUTTON_WIDTH);
            button.setMaxWidth (TITLE_BAR_BUTTON_WIDTH);
            
            button.setMinHeight (TITLE_BAR_BUTTON_HEIGHT);
            button.setPrefHeight(TITLE_BAR_BUTTON_HEIGHT);
            button.setMaxHeight (TITLE_BAR_BUTTON_HEIGHT);
        }
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        HBox titleBar = new HBox(0, 
                                 title, 
                                 spacer,
                                 buttonMinimize,
                                 buttonMaximize,
                                 buttonClose);
        
        titleBar.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                dragOffsetX = event.getScreenX() - stage.getX();
                dragOffsetY = event.getScreenY() - stage.getY();
            }
        });
        
        titleBar.setOnMouseDragged(event -> {
            if (event.isPrimaryButtonDown()) {
                stage.setX(event.getScreenX() - dragOffsetX);
                stage.setY(event.getScreenY() - dragOffsetY);
            }
        });
        
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setStyle("""
                          -fx-background-color: black;
                          -fx-padding: 5px 10px;
                          """);
        
        BorderPane rootBorderPane = new BorderPane();
        rootBorderPane.setTop(titleBar);
        rootBorderPane.setCenter(root);
        
        stage.setResizable(true);
        stage.setMinWidth (WINDOW_WIDTH);
        stage.setMinHeight(WINDOW_HEIGHT);
        
        Scene scene = new Scene(rootBorderPane, WINDOW_WIDTH, WINDOW_HEIGHT);
        
        stage.setScene(scene);
        
        new WindowResizeHandler(stage, scene);
        
        VBox.setVgrow(mainUi, Priority.ALWAYS);
        fileList.setMaxHeight(Double.MAX_VALUE);
        
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
    
    private void zipAllSelected(File file, Stage stage) {
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
        
        FileDataCounter fileCounter = new FileDataCounter();
        
        CheckAndCountFilesTask countTask = 
            new CheckAndCountFilesTask(fileList, fileCounter);
        
        FileCountUpdateTask fileCounterUpdateTask = 
            new FileCountUpdateTask(this, fileCounter);
        
        countTask.setOnSucceeded(event -> {
            countTask.getValue();
            
            zipAll(fileList, file, stage);
        });
        
        Thread zippingThread       = new Thread(countTask);
        Thread counterUpdateThread = new Thread(fileCounterUpdateTask);
        
        zippingThread.start();
        counterUpdateThread.start();
    }
    
    private void zipAll(FileList fileList, File file, Stage stage) {
        try {
            zipAllImpl(fileList, file);
        } catch (IOException ex) {
            setStatusMessage(ex.getMessage(), STATUS_MESSAGE_EMPTY_LIST_MILLIS);
        }
    }
    
    private static void zipAllImpl(FileList fileList, File zipFile) 
    throws IOException {
        
        Path target = zipFile.toPath().toAbsolutePath().normalize();
        
        checkFileListContainsNoZipFile(fileList, zipFile);
        
        try (ZipOutputStream zos = 
            new ZipOutputStream(Files.newOutputStream(zipFile.toPath()))) {
        
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
    
    private void setAllStyles() {
        
        // Stylize the "Close window" button:
        buttonClose.setStyle(FXML.TitleBar.CloseButton.CSS_DEFAULT);
        
        buttonClose.setOnMouseEntered(
            event -> 
                buttonClose.setStyle(FXML.TitleBar.CloseButton.CSS_ON_HOVER)
        );
        
        buttonClose.setOnMouseExited(
            event -> buttonClose.setStyle(FXML.TitleBar.CloseButton.CSS_DEFAULT)
        );
        
        // Stylize the "Add Folder" button:
        buttonAddFolder.setStyle(FXML.Buttons.AddFolderButton.CSS_DEFAULT);
        
        buttonAddFolder.setOnMouseEntered(event ->
            buttonAddFolder.setStyle(FXML.Buttons.AddFolderButton.CSS_ON_HOVER)
        );
        
        buttonAddFolder.setOnMouseExited(event -> 
            buttonAddFolder.setStyle(FXML.Buttons.AddFolderButton.CSS_DEFAULT)
        );
        
        // Stylize the "Add File" button:
        buttonAddFile.setStyle(FXML.Buttons.AddFileButton.CSS_DEFAULT);
        
        buttonAddFile.setOnMouseEntered(event ->
            buttonAddFile.setStyle(FXML.Buttons.AddFileButton.CSS_ON_HOVER)
        );
        
        buttonAddFile.setOnMouseExited(event -> 
            buttonAddFile.setStyle(FXML.Buttons.AddFileButton.CSS_DEFAULT)
        );
        
        // Stylize the "Zip" button:
        buttonDoZip.setStyle(FXML.Buttons.ZipButton.CSS_DEFAULT);
        
        buttonDoZip.setOnMouseEntered(event ->
            buttonDoZip.setStyle(FXML.Buttons.ZipButton.CSS_ON_HOVER)
        );
        
        buttonDoZip.setOnMouseExited(event -> 
            buttonDoZip.setStyle(FXML.Buttons.ZipButton.CSS_DEFAULT)
        );
        
        toolBar.setStyle("-fx-background-color: black;");
    }
    
    private static String wordFiles(int numberOfFiles) {
        return numberOfFiles == 1 ? "file" : "files";
    }
    
    private static String wordDirectories(int numberOfDirectories) {
        return numberOfDirectories == 1 ? "directory" : "directories";
    }
    
    private static final class WindowResizeHandler {
        private static final double BORDER = 6.0;
        
        private static final int LEFT   = 1;
        private static final int RIGHT  = 2;
        private static final int TOP    = 4;
        private static final int BOTTOM = 8;
        
        private final Stage stage;
        private final Scene scene;
        
        private int edges;
        private double startMouseX;
        private double startMouseY;
        private double startX;
        private double startY;
        private double startWidth;
        private double startHeight;
        
        WindowResizeHandler(Stage stage, Scene scene) {
            this.stage = stage;
            this.scene = scene;
            
            scene.addEventFilter(MouseEvent.MOUSE_MOVED, event -> {
                if (edges == 0) {
                    scene.setCursor(cursorFor(edgesAt(event)));
                }
            });
            
            scene.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                if (event.getButton() != MouseButton.PRIMARY) {
                    return;
                }
                
                edges = edgesAt(event);
                
                if (edges == 0) {
                    return;
                }
                
                startMouseX = event.getScreenX();
                startMouseY = event.getScreenY();
                startX      = stage.getX();
                startY      = stage.getY();
                startWidth  = stage.getWidth();
                startHeight = stage.getHeight();
                
                scene.setCursor(cursorFor(edges));
                event.consume();
            });
            
            scene.addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> {
                if (edges == 0 || !event.isPrimaryButtonDown()) {
                    return;
                }
                
                double dx = event.getScreenX() - startMouseX;
                double dy = event.getScreenY() - startMouseY;
                
                if ((edges & LEFT) != 0) {
                    double width = clamp(startWidth - dx,
                                         stage.getMinWidth(),
                                         stage.getMaxWidth());
                    
                    stage.setWidth(width);
                    stage.setX(startX + startWidth - width);
                } else if ((edges & RIGHT) != 0) {
                    stage.setWidth(clamp(startWidth + dx,
                                         stage.getMinWidth(),
                                         stage.getMaxWidth()));
                }
                
                if ((edges & TOP) != 0) {
                    double height = clamp(startHeight - dy,
                                          stage.getMinHeight(),
                                          stage.getMaxHeight());
                    
                    stage.setHeight(height);
                    stage.setY(startY + startHeight - height);
                } else if ((edges & BOTTOM) != 0) {
                    stage.setHeight(clamp(startHeight + dy,
                                          stage.getMinHeight(),
                                          stage.getMaxHeight()));
                }
                
                event.consume();
            });
                
            scene.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
                if (edges != 0 && event.getButton() == MouseButton.PRIMARY) {
                    edges = 0;
                    scene.setCursor(cursorFor(edgesAt(event)));
                    event.consume();
                }
            });
            
            scene.addEventFilter(MouseEvent.MOUSE_EXITED, event -> {
                if (edges == 0) {
                    scene.setCursor(Cursor.DEFAULT);
                }
            });
        }
        
        private int edgesAt(MouseEvent event) {
            if (!stage.isResizable()
                    || stage.isMaximized()
                    || stage.isFullScreen()) {
                return 0;
            }
            
            double x = event.getSceneX();
            double y = event.getSceneY();
            
            if (x < 0 || y < 0 
                      || x > scene.getWidth()
                      || y > scene.getHeight()) {
                return 0;
            }
            
            int result = 0;
            
            if (x <= BORDER) {
                result |= LEFT;
            } else if (x >= scene.getWidth() - BORDER) {
                result |= RIGHT;
            }
            
            if (y <= BORDER) {
                result |= TOP;
            } else if (y >= scene.getHeight() - BORDER) {
                result |= BOTTOM;
            }
            
            return result;
        }
        
        private static Cursor cursorFor(int edges) {
            return switch (edges) {
                case LEFT           -> Cursor.W_RESIZE;
                case RIGHT          -> Cursor.E_RESIZE;
                case TOP            -> Cursor.N_RESIZE;
                case BOTTOM         -> Cursor.S_RESIZE;
                case LEFT  | TOP    -> Cursor.NW_RESIZE;
                case RIGHT | TOP    -> Cursor.NE_RESIZE;
                case LEFT  | BOTTOM -> Cursor.SW_RESIZE;
                case RIGHT | BOTTOM -> Cursor.SE_RESIZE;
                    
                default -> Cursor.DEFAULT;
            };
        }
    }
}
