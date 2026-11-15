package io.github.coderodde.zipfx.buttons;

import io.github.coderodde.zipfx.ZipFXApp;
import java.util.Objects;
import javafx.scene.control.Button;

/**
 * The stylized title bar button.
 */
public final class TitleBarButton extends Button {
    
    private static final double WIDTH_HEIGHT = 30.0;
    
    public enum ButtonIdentity {
        MINIMIZE,
        MAXIMIZE,
        CLOSE;
    }
    
    private final ZipFXApp app;
    
    public TitleBarButton(ZipFXApp app, ButtonIdentity buttonIdentity) {
        this.app = Objects.requireNonNull(app);
        
        Objects.requireNonNull(buttonIdentity);
        
        setMinWidth(WIDTH_HEIGHT);
        setMaxWidth(WIDTH_HEIGHT);
        setPrefWidth(WIDTH_HEIGHT);
        
        setMinHeight(WIDTH_HEIGHT);
        setMaxHeight(WIDTH_HEIGHT);
        setPrefHeight(WIDTH_HEIGHT);
        
        switch (buttonIdentity) {
            case MINIMIZE -> setAsMinimize();
            case MAXIMIZE -> setAsMaximize();
            case CLOSE    -> setAsClose();
            default       -> 
                throw new EnumConstantNotPresentException(
                    ButtonIdentity.class, 
                    buttonIdentity.name());
        }
    }
    
    private void setAsMinimize() {
        setOnAction(
            event -> app.getStage().setIconified(true)
        );
        
        setStyle(FXML.BUTTON_CSS);
        
        setOnMouseEntered(
            event -> setStyle(FXML.BUTTON_CSS_ON_HOVER)
        );
        
        setOnMouseExited(
            event -> setStyle(FXML.BUTTON_CSS)
        );
        
        setText("─");
        
        if (isHover()) {
            setStyle(FXML.BUTTON_CSS_ON_HOVER);
        }
    }
    
    private void setAsMaximize() {
        setOnAction(event ->
            app.getStage().setMaximized(!app.getStage().isMaximized())
        );
        
        setStyle(FXML.BUTTON_CSS);
        
        setOnMouseEntered(
            event -> setStyle(FXML.BUTTON_CSS_ON_HOVER)
        );
        
        setOnMouseExited(
            event -> setStyle(FXML.BUTTON_CSS)
        );
        
        if (isHover()) {
            setStyle(FXML.BUTTON_CSS_ON_HOVER);
        }
        
        setText("□");
        
        app.getStage().maximizedProperty().addListener((observable,
                                                        oldValue,
                                                        maximized) -> 
            setText(maximized ? "❐" : "□")
        );
    }
    
    private void setAsClose() {
        setOnAction(event -> app.getStage().close());
        setText("✕");
    }
    
    private static final class FXML {
        
        static final String BUTTON_CSS = 
            """
            -fx-background-color: black;
            -fx-padding: 0;
            -fx-background-insets: 0;
            -fx-background-radius: 0;
            -fx-border-width: 0;
            -fx-border-color: transparent;
            -fx-border-radius: 0;
            -fx-border-insets: 0;
            -fx-text-fill: white;
            """;

        static final String BUTTON_CSS_ON_HOVER = 
            """
            -fx-background-color: white;
            -fx-padding: 0;
            -fx-border-width: 0;
            -fx-background-insets: 0;
            -fx-background-radius: 0;
            -fx-border-color: transparent;
            -fx-border-radius: 0;
            -fx-border-insets: 0;
            -fx-text-fill: black;
            """;
    }
}
