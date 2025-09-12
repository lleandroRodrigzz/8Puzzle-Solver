package grupo.unoeste.alo8puzzle;

import javafx.application.Application;

public class Launcher {
    public static void main(String[] args) {

        EightPuzzle puzzle = new EightPuzzle("410785623");

        puzzle.montaMatriz();
        puzzle.randomMatriz(puzzle.getMatriz());
        Application.launch(HelloApplication.class, args);
    }
}
