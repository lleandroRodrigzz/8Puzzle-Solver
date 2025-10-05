package grupo.unoeste.alo8puzzle;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class EightPuzzleApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(EightPuzzleApplication.class.getResource("8puzzle-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 700);

        //aqui estamos adicionando as classes do css, para que o java entenda em que momento podemos aplicar as classes css;
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

        stage.setTitle("Trabalho Bimestral - Inteligencia Artificial I");
        stage.setScene(scene);
        stage.show();
    }
}
