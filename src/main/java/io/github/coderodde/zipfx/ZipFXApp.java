package io.github.coderodde.zipfx;

import static io.github.coderodde.zipfx.Misc.wordBytes;
import static io.github.coderodde.zipfx.Misc.wordDirectories;
import static io.github.coderodde.zipfx.Misc.wordFiles;
import io.github.coderodde.zipfx.bars.StatusBar;
import io.github.coderodde.zipfx.bars.TitleBar;
import io.github.coderodde.zipfx.bars.ToolBar;
import java.io.File;
import static java.lang.Math.clamp;
import java.util.Objects;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * This JavaFX application is for zipping files and folders.
 */
public class ZipFXApp extends Application {

    public void setCurrentState(State state) {
        this.currentState = state;
    }
    
    public enum State {
        IDLE_EMPTY_LIST,
        IDLE_NON_EMPTY_LIST,
        ZIPPING;
    }
    
    private static final Logger LOGGER = 
                         Logger.getLogger(ZipFXApp.class.getSimpleName());
    
    private static final double WINDOW_WIDTH  = 500.0;
    private static final double WINDOW_HEIGHT = 350.0;
    
    public static final String VERSION = "1.0.0";
    
    private State currentState = State.IDLE_EMPTY_LIST;
    
    private TitleBar      titleBar;
    private final ToolBar toolBar;
    private StatusBar     statusBar;
    
    private final VBox root                 = new VBox();
    private final HBox mainUi               = new HBox();
    
    private final FileList fileList = new FileList(this);
    
    private CheckAndCountFilesTask activeCountTask;
    private FileCountUpdateTask activeCountUpdater;
    
    private ZipTask activeZipTask;
    private FileZipCountUpdateTask activeZipUpdater;
    
    private volatile boolean cancellationRequested;
    
    private Stage stage;
    
    public ZipFXApp() {
        this.toolBar  = new ToolBar(this);
    }
    
    public State getCurrentState() {
        return currentState;
    }
    
    public Stage getStage() {
        return stage;
    }
    
    public FileList getFileList() {
        return fileList;
    }
    
    public StatusBar getStatusBar() {
        return statusBar;
    }
    
    public void cancelAllTasks() {
        if (activeCountTask != null) {
            activeCountTask.cancel();
        }

        if (activeZipTask != null) {
            activeZipTask.cancel();
        }

        if (activeCountUpdater != null) {
            activeCountUpdater.cancel();
        }

        if (activeZipUpdater != null) {
            activeZipUpdater.halt();
            activeZipUpdater.cancel();
        }
    }
    
    public VBox getRoot() {
        return root;
    }
    
    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        this.statusBar = new StatusBar();
        this.titleBar = new TitleBar(this);
        
        setAllStyles();
        
        mainUi.getChildren().add(fileList);
        
        HBox.setHgrow(fileList, Priority.ALWAYS);
        fileList.setMaxWidth(Double.MAX_VALUE);
        
        toolBar.setStyle("-fx-background-color: black;");
        
        root.getChildren()
            .addAll(toolBar,
                    mainUi, 
                    statusBar);
        
        stage.initStyle(StageStyle.UNDECORATED);
        
        BorderPane rootBorderPane = new BorderPane();
        
        rootBorderPane.setTop(titleBar);
        rootBorderPane.setCenter(root);
        
        titleBar.prefWidthProperty().bind(rootBorderPane.widthProperty());
        
        stage.setResizable(true);
        stage.setMinWidth (WINDOW_WIDTH);
        stage.setMinHeight(WINDOW_HEIGHT);
        
        Scene scene = new Scene(rootBorderPane, WINDOW_WIDTH, WINDOW_HEIGHT);
        
        stage.setScene(scene);
        
        new WindowResizeHandler(stage, scene);
        
        VBox.setVgrow(mainUi, Priority.ALWAYS);
        fileList.setMaxHeight(Double.MAX_VALUE);
        
        stage.show();
        System.out.println("TitleBar width: " + titleBar.getWidth());
System.out.println("TitleBar height: " + titleBar.getHeight());
System.out.println("TitleBar children: " + titleBar.getChildren());
System.out.println("Scene width: " + scene.getWidth());
System.out.println("BorderPane width: " + rootBorderPane.getWidth());
System.out.println("TitleBar width: " + titleBar.getWidth());
System.out.println("TitleBar layout bounds: "
                   + titleBar.getLayoutBounds());
System.out.println("Stage width: " + stage.getWidth());
System.out.println("Scene width: " + scene.getWidth());
System.out.println("TitleBar width: " + titleBar.getWidth());
titleBar.getChildren().forEach(node ->
    System.out.printf(
        "%s: x=%.1f, width=",
        node.getClass().getSimpleName(),
        node.getLayoutX()
    )
);
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
    public void setState(State state) {
        currentState = Objects.requireNonNull(state);
        
        switch (currentState) {
            case IDLE_EMPTY_LIST     -> toolBar.setStateForIdleEmptyList();
            case IDLE_NON_EMPTY_LIST -> toolBar.setStateForIdleNonEmptyList();
            case ZIPPING             -> toolBar.setStateForZipping();
            default -> throw new IllegalStateException("Should not get here.");
        }
    }
    
    private void restoreIdleState() {
        setState(fileList.isEmpty() 
                 ? State.IDLE_EMPTY_LIST
                 : State.IDLE_NON_EMPTY_LIST);
    }
    
    
    private void reportAfterZipping(FileDataCounter progressFileDataCounter,
                                    ZipTask zipTask) {
        
        long files             = progressFileDataCounter.getFileCount();
        long folders           = progressFileDataCounter.getDirectoryCount();
        long duration          = zipTask.getDurationMilliseconds();
        long bytesUncompressed = zipTask.getUncompressedByteCount();
        long bytesCompressed   = zipTask.getCompressedByteCount();
        
        String textFiles        = Misc.wordFiles(files);
        String textFolders      = Misc.wordDirectories(folders);
        String textUncompressed = Misc.wordBytes(bytesUncompressed);
        String textCompressed   = Misc.wordBytes(bytesCompressed);
        
        String text = 
            ("%d %s in %d %s. Duration: %.1f seconds. " +
             "Compressed %d %s into %d %s.")
                .formatted(files, 
                           textFiles, 
                           folders, 
                           textFolders,
                           duration / 1e3, 
                           bytesUncompressed, 
                           textUncompressed, 
                           bytesCompressed, 
                           textCompressed);
                
        statusBar.setLowerStatusBarText("Scanned " + text);
    }
    
    private void setAllStyles() {
        
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
    
    public void dispatchZipJob(File outputZipFile) {
        Objects.requireNonNull(outputZipFile);
        
        cancellationRequested = false;
        
        FileDataCounter total    = new FileDataCounter();
        FileDataCounter progress = new FileDataCounter();
        
        CheckAndCountFilesTask countTask = 
            new CheckAndCountFilesTask(fileList, total);
        
        FileCountUpdateTask countUpdater = new FileCountUpdateTask(this, total);
        
        activeCountTask    = countTask;
        activeCountUpdater = countUpdater;
        activeZipTask      = null;
        activeZipUpdater   = null;
        
        setState(State.ZIPPING);
        
        Thread countUpdaterThread = 
            new Thread(countUpdater, "file-count-progress");
        
        countUpdaterThread.setDaemon(true);

        Thread countThread = new Thread(() -> {
            try {
                countTask.run();
            } finally {
                countUpdater.cancel();
                
                Platform.runLater(() -> {
                    activeCountTask    = null;
                    activeCountUpdater = null;
                    
                    if (cancellationRequested || countTask.isCancelled()) {
                        
                        statusBar.setUpperStatusBarText(
                            "Counting files cancelled.");
                        
                        statusBar.setLowerStatusBarText("");
                        restoreIdleState();
                        return;
                    }
                    
                    Throwable countFailure = countTask.getException();
                    
                    if (countFailure != null) {
                        statusBar.setUpperStatusBarText(
                            "Could not count files.");
                        
                        statusBar.setLowerStatusBarText(
                            countFailure.getMessage());
                        
                        restoreIdleState();
                        return;
                    }
                    
                    long bytes       = total.getByteCount();
                    long files       = total.getFileCount();
                    long directories = total.getDirectoryCount();
                    
                    String str = 
                        String.format(
                            "Counted %d %s and %d %s worth %d %s.",
                            files,
                            wordFiles(files),
                            directories,
                            wordDirectories(directories),
                            bytes,
                            wordBytes(bytes));
                    
                    statusBar.setUpperStatusBarText(str);
                    
                    FileZipCountUpdateTask zipUpdater = 
                        new FileZipCountUpdateTask(ZipFXApp.this,
                                                   total,
                                                   progress);
                    
                    ZipTask zipTask = 
                        new ZipTask(
                            total,
                            progress,
                            zipUpdater,
                            fileList,
                            outputZipFile,
                            ZipFXApp.this);
                    
                    activeZipUpdater = zipUpdater;
                    activeZipTask    = zipTask;
                    
                    Thread zipUpdaterThread = 
                        new Thread(zipUpdater, "zip-progress");
                    
                    zipUpdaterThread.setDaemon(true);
                    
                    Thread zipThread = new Thread(() -> {
                        try {
                            zipTask.run();
                        } finally {
                            zipUpdater.halt();
                            
                            Platform.runLater(() -> {
                                activeZipTask    = null;
                                activeZipUpdater = null;
                                
                                if (cancellationRequested ||
                                        zipTask.isCancelled()) {
                                    
                                    statusBar.setUpperStatusBarText(
                                        "Zipping cancelled.");
                                    
                                    statusBar.setLowerStatusBarText("");
                                    
                                } else if (zipTask.getException() != null) {
                                    Throwable cause = zipTask.getException();
                                    
                                    statusBar.setUpperStatusBarText(
                                        "Could not create a .zip file.");
                                    
                                    statusBar.setLowerStatusBarText(
                                        cause.getMessage());
                                } else {
                                    statusBar.setUpperStatusBarText("");
                                    
                                    reportAfterZipping(progress, zipTask);
                                }
                                
                                restoreIdleState();
                            });
                        }
                    }, "zip-writer");
                    
                    zipUpdaterThread.start();
                    zipThread.start();
                });    
            }
        }, "file-counter");
        
        countUpdaterThread.start();
        countThread.start();
    }
    
    public boolean cancellationRequested() {
        return cancellationRequested;
    }
    
    public void requestCancellation() {
        cancellationRequested = true;
    }
}
