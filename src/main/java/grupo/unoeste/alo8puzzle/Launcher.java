package grupo.unoeste.alo8puzzle;

import javafx.application.Application;

public class Launcher {
    public static void main(String[] args) {

        EightPuzzleController puzzle = new EightPuzzleController("410785623");

        /*puzzle.montaMatriz();
        puzzle.randomMatriz(puzzle.getMatriz());
        System.out.println("Estado Final:");
        puzzle.exibeMatriz(puzzle.getMatriz());
        System.out.println("\n");
        System.out.println("Estado Inicial Embaralhado:");
        puzzle.exibeMatriz(puzzle.getRandom());*/
        Application.launch(EightPuzzleApplication.class, args);
    }
}
