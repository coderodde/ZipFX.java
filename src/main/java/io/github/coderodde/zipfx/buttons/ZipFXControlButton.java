package io.github.coderodde.zipfx.buttons;

import javafx.scene.control.Button;

/**
 * 
 */
public final class ZipFXControlButton extends Button {
    
    public enum ButtonIdentity {
        ADD_FILE,
        ADD_FOLDER,
        REMOVE_ALL,
        ZIP
    }
    
    private boolean isEnabled = true;
    private final ButtonIdentity identity;
    
    public ZipFXControlButton(ButtonIdentity identity) {
        this.identity = identity;
        
        styleAsEnabled();
        
        if (isHover()) {
            setStyle(FXML.ENABLED_ON_HOVER_CSS);
        }
    }
    
    public void enable() {
        if (isEnabled) {
            return;
        }
        
        isEnabled = true;
        styleAsEnabled();
    }
    
    public void disable() {
        if (!isEnabled) {
            return;
        }
        
        isEnabled = false;
        styleAsDisabled();
    }
    
    private void styleAsEnabled() {
        setStyle(FXML.ENABLED_CSS);
        
        setOnMouseEntered(event -> {
            setStyle(FXML.ENABLED_ON_HOVER_CSS);
        });
        
        setOnMouseExited(event -> {
            setStyle(FXML.ENABLED_CSS);
        });
    }
    
    private void styleAsDisabled() {
        setStyle(identity == ButtonIdentity.REMOVE_ALL ? 
                FXML.DISABLED_CSS_REMOVE_ALL :
                FXML.DISABLED_CSS_ZIP);
        
        setOnMouseEntered(event -> {});
        setOnMouseExited (event -> {});
    }
    
    private static final class FXML {
        
        static final String ENABLED_CSS = 
            """
            -fx-border-color: #666; 
            -fx-border-width: 1 0 1 1;
            -fx-border-style: solid; 
            -fx-background-color: black; 
            -fx-text-fill: white;
            -fx-font-family: "sans-serif";
            -fx-font-weight: normal;
            -fx-font-size:   12px;
            -fx-min-width:  100px;
            -fx-pref-width: 100px;
            -fx-max-width:  100px;
            -fx-min-height:  30px;
            -fx-pref-height: 30px;
            -fx-max-height:  30px;
            """;
        
        static final String ENABLED_ON_HOVER_CSS =
            """
            -fx-border-color: white;
            -fx-border-width: 1 0 1 1;
            -fx-border-style: solid; 
            -fx-background-color: white; 
            -fx-text-fill: black;
            -fx-font-weight: normal;
            -fx-font-family: "sans-serif";
            -fx-font-size:   12px;
            -fx-min-width:  100px;
            -fx-pref-width: 100px;
            -fx-max-width:  100px;
            -fx-min-height:  30px;
            -fx-pref-height: 30px;
            -fx-max-height:  30px;
            """;
        
        static final String DISABLED_CSS_REMOVE_ALL = 
            """
            -fx-border-color: white; 
            -fx-border-width: 1 0 1 1;
            -fx-border-style: solid; 
            -fx-background-color: #666; 
            -fx-text-fill: white;
            -fx-font-family: "sans-serif";
            -fx-font-weight: normal;
            -fx-font-size:   12px;
            -fx-min-width:  100px;
            -fx-pref-width: 100px;
            -fx-max-width:  100px;
            -fx-min-height:  30px;
            -fx-pref-height: 30px;
            -fx-max-height:  30px;
            """;
        
        static final String DISABLED_CSS_ZIP = 
            """
            -fx-border-color: white; 
            -fx-border-width: 1 1 1 1;
            -fx-border-style: solid; 
            -fx-background-color: #666; 
            -fx-text-fill: white;
            -fx-font-family: "sans-serif";
            -fx-font-weight: normal;
            -fx-font-size:   12px;
            -fx-min-width:  100px;
            -fx-pref-width: 100px;
            -fx-max-width:  100px;
            -fx-min-height:  30px;
            -fx-pref-height: 30px;
            -fx-max-height:  30px;
            """;
    }
}
