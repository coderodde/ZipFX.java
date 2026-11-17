package io.github.coderodde.zipfx.bars;

import io.github.coderodde.zipfx.ZipFXApp;
import io.github.coderodde.zipfx.buttons.TitleBarButton;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/**
 * This class implements a title bar for the ZipFX.
 */
public final class TitleBar extends HBox {
    
    private static final double WIDTH_HEIGHT = 30.0;
    
    public enum ButtonIdentity {
        MINIMIZE,
        MAXIMIZE,
        CLOSE;
    }
    
    private ZipFXApp app;
    private final TitleBarButton buttonMinimize;
    private final TitleBarButton buttonMaximize;
    private final TitleBarButton buttonClose;
    private final Label labelTitle = 
        new Label("ZipFX " + ZipFXApp.VERSION + " by coderodde 2026");
    
    private final Region regionSpacer = new Region();
    private HBox hBoxTitleBar;
    private final BorderPane borderPaneRoot = new BorderPane();
    
    private double dragOffsetX;
    private double dragOffsetY;
    
    public TitleBar(ZipFXApp app) {
        buttonMinimize =
            new TitleBarButton(app, TitleBarButton.ButtonIdentity.MINIMIZE);
        
        buttonMaximize = 
            new TitleBarButton(app, TitleBarButton.ButtonIdentity.MAXIMIZE);
        
        buttonClose = 
            new TitleBarButton(app, TitleBarButton.ButtonIdentity.CLOSE);
        
        buttonMinimize.setOnAction(event -> app.getStage().setIconified(true));
        buttonMaximize.setOnAction(event -> 
            app.getStage().setMaximized(!app.getStage().isMaximized())
        );
        
        // TODO: Cancel possible compressions:
        buttonClose.setOnAction(event -> app.getStage().close());
        
        labelTitle.setPadding(new Insets(0, 0, 0, 10));
        labelTitle.setStyle("-fx-text-fill: white;");
        
        app.getStage().maximizedProperty().addListener((observable,
                                                        oldValue,
                                                        maximized) ->
            buttonMaximize.setText(maximized ? "❐" : "□")
        );
        
        HBox.setHgrow(regionSpacer, Priority.ALWAYS);
        
        hBoxTitleBar = 
            new HBox(0,
                     labelTitle, 
                     regionSpacer, 
                     buttonMinimize, 
                     buttonMaximize, 
                     buttonClose);
        
        hBoxTitleBar.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                dragOffsetX = event.getScreenX() - app.getStage().getX();
                dragOffsetY = event.getScreenY() - app.getStage().getY();
            }
        });
        
        hBoxTitleBar.setOnMouseDragged(event -> {
            if (event.isPrimaryButtonDown()) {
                app.getStage().setX(event.getScreenX() - dragOffsetX);
                app.getStage().setY(event.getScreenY() - dragOffsetY);
            }
        });
        
        // Set the mouse listeners for the Minimize button:
        if (buttonMinimize.isHover()) {
            buttonMinimize.setStyle(FXML.MinimizeButton.CSS_ON_HOVER);
        } else {
            buttonMinimize.setStyle(FXML.MinimizeButton.CSS_DEFAULT);
        }
        
        buttonMinimize.setOnMouseEntered(event -> 
            buttonMinimize.setStyle(FXML.MinimizeButton.CSS_ON_HOVER)
        );
        
        buttonMinimize.setOnMouseExited(event -> 
            buttonMinimize.setStyle(FXML.MinimizeButton.CSS_DEFAULT)
        );
        
        // Set the mouse listeners for the Maximize button:
        if (buttonMaximize.isHover()) {
            buttonMaximize.setStyle(FXML.MaximizeButton.CSS_ON_HOVER);
        } else {
            buttonMaximize.setStyle(FXML.MaximizeButton.CSS_DEFAULT);
        }
        
        buttonMaximize.setOnMouseEntered(event -> 
            buttonMaximize.setStyle(FXML.MaximizeButton.CSS_ON_HOVER)
        );
        
        buttonMaximize.setOnMouseExited(event -> 
            buttonMaximize.setStyle(FXML.MaximizeButton.CSS_DEFAULT)
        );
        
        // Set the mouse listeners for the Close button:
        if (buttonClose.isHover()) {
            buttonClose.setStyle(FXML.CloseButton.CSS_ON_HOVER);
        } else {
            buttonClose.setStyle(FXML.CloseButton.CSS_DEFAULT);
        }
        
        buttonClose.setOnMouseEntered(event -> 
            buttonClose.setStyle(FXML.CloseButton.CSS_ON_HOVER)
        );
        
        buttonClose.setOnMouseExited(event -> 
            buttonClose.setStyle(FXML.CloseButton.CSS_DEFAULT)
        );
        
        hBoxTitleBar.setAlignment(Pos.CENTER_LEFT);
        hBoxTitleBar.setStyle(FXML.CSS);
        
        getChildren().add(hBoxTitleBar);
        HBox.setHgrow(hBoxTitleBar, Priority.ALWAYS);
    }
    
    private static final class FXML {
        
        static final class CloseButton {
            
            static final String CSS_DEFAULT = 
                """
                -fx-background-color: transparent;
                -fx-text-fill: white;
                """;
            
            static final String CSS_ON_HOVER = 
                """                
                -fx-background-color: #c71e2f;
                -fx-background-radius: 0;
                -fx-border-insets: 0;
                -fx-border-width: 0;
                -fx-border-color: transparent;
                -fx-border-radius: 0;
                -fx-text-fill: white;
                """;
        }
        
        static final class MinimizeButton {
            
            static final String CSS_DEFAULT = 
                """
                -fx-background-color: black;
                -fx-background-radius: 0;
                -fx-border-insets: 0;
                -fx-border-width: 0;
                -fx-border-color: transparent;
                -fx-border-radius: 0;
                -fx-text-fill: white;
                """;
            
            static final String CSS_ON_HOVER = 
                """
                -fx-background-color: white;
                -fx-background-radius: 0;
                -fx-border-insets: 0;
                -fx-border-width: 0;
                -fx-border-color: transparent;
                -fx-border-radius: 0;
                -fx-text-fill: black;
                """;
        }
        
        static final class MaximizeButton {
            
            // Identical CSS as for the Minimize button:
            static final String CSS_DEFAULT  = FXML.MinimizeButton.CSS_DEFAULT;
            static final String CSS_ON_HOVER = FXML.MinimizeButton.CSS_ON_HOVER;
        }
        
        static final String CSS = 
            """
            -fx-background-color: black;
            -fx-padding: 0;
            -fx-border-width: 0;
            -fx-border-insets: 0;
            -fx-background-insets: 0;
            -fx-background-radius: 0;
            """;
    }
}
