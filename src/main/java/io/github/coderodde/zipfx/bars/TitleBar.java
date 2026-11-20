package io.github.coderodde.zipfx.bars;

import io.github.coderodde.zipfx.ZipFXApp;
import io.github.coderodde.zipfx.buttons.TitleBarButton;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/**
 * This class implements a title bar for the ZipFX.
 */
public final class TitleBar extends HBox {
    
    private static final double HEIGHT = 30.0;
    
    public enum ButtonIdentity {
        MINIMIZE,
        MAXIMIZE,
        CLOSE;
    }
    
    private final TitleBarButton buttonMinimize;
    private final TitleBarButton buttonMaximize;
    private final TitleBarButton buttonClose;
    private final Label labelTitle = 
        new Label("ZipFX " + ZipFXApp.VERSION + " by coderodde 2026");
    
    private final Region regionSpacer = new Region();
    
    private double dragOffsetX;
    private double dragOffsetY;
    
    public TitleBar(ZipFXApp app) {
        setMinWidth (0);
        setPrefWidth(Region.USE_PREF_SIZE);
        setMaxWidth (Double.MAX_VALUE);
        
        setMinHeight (HEIGHT);
        setPrefHeight(HEIGHT);
        setMaxHeight (HEIGHT);
        
        setMinWidth(0);
        
        buttonMinimize =
            new TitleBarButton(app, TitleBarButton.ButtonIdentity.MINIMIZE);
        
        buttonMaximize = 
            new TitleBarButton(app, TitleBarButton.ButtonIdentity.MAXIMIZE);
        
        buttonClose = 
            new TitleBarButton(app, TitleBarButton.ButtonIdentity.CLOSE);
        
        labelTitle.setPadding(new Insets(0, 0, 0, 10));
        labelTitle.setStyle(FXML.Label.CSS);
        
        app.getStage().maximizedProperty().addListener((observable,
                                                        oldValue,
                                                        maximized) ->
            buttonMaximize.setText(maximized ? "❐" : "□")
        );
        
        HBox.setHgrow(regionSpacer, Priority.ALWAYS);
        regionSpacer.setMinWidth(0);
        regionSpacer.setMaxWidth(Double.MAX_VALUE);
        
        getChildren().addAll(labelTitle,
                             regionSpacer,
                             buttonMinimize, 
                             buttonMaximize, 
                             buttonClose);
        
        setAlignment(Pos.CENTER_LEFT);
        setStyle(FXML.CSS);
        
        setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                dragOffsetX = event.getScreenX() - app.getStage().getX();
                dragOffsetY = event.getScreenY() - app.getStage().getY();
            }
        });
        
        setOnMouseDragged(event -> {
            if (event.isPrimaryButtonDown()) {
                app.getStage().setX(event.getScreenX() - dragOffsetX);
                app.getStage().setY(event.getScreenY() - dragOffsetY);
            }
        });
        
        setStyle(FXML.CSS);
    }
    
//    @Override
//    protected double computeMaxWidth(double height) {
//        return Double.MAX_VALUE;
//    }
    
    private static final class FXML {
        
        static final class Label {
            
            static final String CSS = """
                                      -fx-text-fill: white;
                                      """;
        }
        
        static final String CSS = 
            """
            -fx-background-color: black;
            -fx-text-fill: white;
            -fx-padding: 0;
            -fx-border-width: 0;
            -fx-border-insets: 0;
            -fx-background-insets: 0;
            -fx-background-radius: 0;
            """;
    }
}
