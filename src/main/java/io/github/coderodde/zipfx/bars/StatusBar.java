package io.github.coderodde.zipfx.bars;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * 
 */
public final class StatusBar extends VBox {
    
    private final Label labelUpperStatusBar = new Label();
    private final Label labelLowerStatusBar = new Label();
    
    public StatusBar() {
        getChildren().addAll(labelUpperStatusBar,
                             labelLowerStatusBar);
        
        labelUpperStatusBar.setStyle(FXML.STATUS_BAR_LABEL);
        labelLowerStatusBar.setStyle(FXML.STATUS_BAR_LABEL);
        
        setStyle(FXML.STATUS_BAR);
        
        labelUpperStatusBar.setMinWidth  (Double.MAX_VALUE);
        labelLowerStatusBar.setMaxWidth  (Double.MAX_VALUE);
        labelUpperStatusBar.setPrefWidth (Double.MAX_VALUE);
        labelLowerStatusBar.setMinHeight (Double.MAX_VALUE);
        labelUpperStatusBar.setPrefHeight(Double.MAX_VALUE);
        labelLowerStatusBar.setMaxHeight (Double.MAX_VALUE);
    }
    
    public void setUpperStatusBarText(String text) {
        labelUpperStatusBar.setText(text);
    }
    
    public void setLowerStatusBarText(String text) {
        labelLowerStatusBar.setText(text);
    }
    
    private static final class FXML {
        static final String STATUS_BAR_LABEL = 
            """
            -fx-background: black;
            -fx-text-fill: white;
            -fx-font-family: "Consolas";
            -fx-font-size: 11px;
            -fx-padding: 2px 5px;
            """;
        
        static final String STATUS_BAR =
            """
            -fx-background-color: black;
            -fx-padding: 0;
            -fx-spacing: 0;
            """;
    }
}
