module grupo.unoeste.alo8puzzle {
    requires javafx.controls;
    requires javafx.fxml;


    opens grupo.unoeste.alo8puzzle to javafx.fxml;
    exports grupo.unoeste.alo8puzzle;
}