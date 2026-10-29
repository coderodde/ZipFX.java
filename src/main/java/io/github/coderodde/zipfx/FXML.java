package io.github.coderodde.zipfx;

/**
 * This class contains all the FXML specifications.
 */
public final class FXML {

    private FXML() {}

    public static final class Buttons {

        public static final class AddFolderButton {

            public static final String CSS_DEFAULT =
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

            public static final String CSS_ON_HOVER = 
                """
                -fx-border-color: #666;
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
        }

        public static final class AddFileButton {

            public static final String CSS_DEFAULT = 
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

            public static final String CSS_ON_HOVER = 
                """
                -fx-border-color: #666;
                -fx-border-width: 1 0 1 1;
                -fx-border-style: solid; 
                -fx-background-color: #666; 
                -fx-text-fill: white;
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
        }

        public static final class ZipButton {

            public static final String CSS_DEFAULT = 
                """
                -fx-border-color: #666; 
                -fx-border-width: 1px;
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

            public static final String CSS_ON_HOVER = 
                """
                -fx-border-color: #666;
                -fx-border-width: 1px;
                -fx-border-style: solid; 
                -fx-background-color: #666; 
                -fx-text-fill: white;
                -fx-font-weight: bold;
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
}
