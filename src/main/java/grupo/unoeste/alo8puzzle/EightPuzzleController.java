package grupo.unoeste.alo8puzzle;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;

public class EightPuzzleController implements Initializable {

    //<editor-fold desc="FXML Fields">
    @FXML private TextField initialStateField;
    @FXML private TextField goalStateField;
    @FXML private ComboBox<String> algorithmComboBox;
    @FXML private Button shuffleButton;
    @FXML private Button solveButton;
    @FXML private GridPane puzzleGrid;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Label statusLabel;
    @FXML private Label durationLabel;
    @FXML private Label expandedNodesLabel;
    @FXML private Label searchDepthLabel;
    @FXML private TextArea pathTextArea;
    @FXML private CheckBox resolverDiretoCheckBox;

    private String finalState;

    // Variáveis para controlar a exibição passo a passo da solução
    private List<No> solutionPath;
    private int currentStepIndex;
    private Timeline solutionTimeline; // ADICIONE ESTA LINHA

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.finalState = goalStateField.getText();

        // --- MUDANÇA 1: Adicionar todas as 8 opções de busca ao ComboBox ---
        algorithmComboBox.setItems(FXCollections.observableArrayList(
                "Best-First: Peças Fora do Lugar (Nível 1)",
                "Best-First: Peças Fora do Lugar (Nível 2)",
                "Best-First: Distância Manhattan (Nível 1)",
                "Best-First: Distância Manhattan (Nível 2)",
                "A*: Peças Fora do Lugar (Nível 1)",
                "A*: Peças Fora do Lugar (Nível 2)",
                "A*: Distância Manhattan (Nível 1)",
                "A*: Distância Manhattan (Nível 2)"
        ));
        // Seleciona uma opção A* com Manhattan por padrão, que geralmente é a mais eficiente
        algorithmComboBox.getSelectionModel().select("A*: Distância Manhattan (Nível 1)");

        prevButton.setDisable(true);
        nextButton.setDisable(true);
        updatePuzzleGridFromTextField();

        initialStateField.textProperty().addListener((obs, oldV, newV) -> updatePuzzleGridFromTextField());
        goalStateField.textProperty().addListener((obs, oldV, newV) -> {
            if (newV != null && newV.matches("\\d{9}")) this.finalState = newV;
        });

        shuffleButton.setOnAction(event -> handleShuffleButton());
        solveButton.setOnAction(event -> handleSolveButton());
        prevButton.setOnAction(event -> handlePrevButton());
        nextButton.setOnAction(event -> handleNextButton());
    }

    // --- MÉTODOS DE MANIPULAÇÃO DA UI ---

    private void updatePuzzleGridFromTextField() {
        String state = initialStateField.getText();
        if (state != null && state.matches("\\d{9}")) {
            updatePuzzleGrid(stringToMatrix(state));
        }
    }

    public void updatePuzzleGrid(int[][] currentMatrix) {
        puzzleGrid.getChildren().clear();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int tileValue = currentMatrix[i][j];
                Label tile;

                if (tileValue == 0) {
                    tile = new Label("");
                    tile.getStyleClass().add("empty-tile");
                } else {
                    tile = new Label(String.valueOf(tileValue));
                    tile.getStyleClass().add("puzzle-tile");
                }
                tile.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                puzzleGrid.add(tile, j, i);
            }
        }
    }

    private void handleShuffleButton() {
        if (this.finalState == null || !this.finalState.matches("\\d{9}")) {
            statusLabel.setText("Estado Final inválido para embaralhar!");
            statusLabel.getStyleClass().setAll("status-error");
            return;
        }

        int[][] goalMatrix = stringToMatrix(this.finalState);
        int[][] shuffledMatrix = shuffleMatrix(goalMatrix); // Usando um método de embaralhamento melhorado
        initialStateField.setText(matrixToString(shuffledMatrix));
        statusLabel.setText("Tabuleiro embaralhado. Pronto para resolver.");
        statusLabel.getStyleClass().setAll("status-info");
    }

    // --- INTEGRAÇÃO 2: Implementar a lógica do botão Resolver ---
    private void handleSolveButton() {
        solveButton.setDisable(true);
        shuffleButton.setDisable(true);
        statusLabel.setText("Resolvendo... Por favor, aguarde.");
        statusLabel.getStyleClass().setAll("status-info");
        clearResults();

        new Thread(() -> {
            int[][] initialMatrix = stringToMatrix(initialStateField.getText());
            int[][] goalMatrix = stringToMatrix(goalStateField.getText());

            if (initialMatrix == null || goalMatrix == null) {
                Platform.runLater(() -> {
                    statusLabel.setText("Erro: Estados inicial ou final inválidos!");
                    statusLabel.getStyleClass().setAll("status-error");
                    solveButton.setDisable(false);
                    shuffleButton.setDisable(false);
                });
                return;
            }

            String selectedAlgorithm = algorithmComboBox.getValue();
            Heuristica heuristicaEscolhida;
            NivelBusca nivelEscolhido;

            if (selectedAlgorithm.contains("Peças Fora do Lugar")) {
                heuristicaEscolhida = Heuristica.PECAS_FORA_DO_LUGAR;
            } else {
                heuristicaEscolhida = Heuristica.DISTANCIA_MANHATTAN;
            }

            if (selectedAlgorithm.contains("Nível 2")) {
                nivelEscolhido = NivelBusca.NIVEL_2;
            } else {
                nivelEscolhido = NivelBusca.NIVEL_1;
            }

            long startTime = System.currentTimeMillis();
            List<No> path;
            int expandedNodes;

            // --- MUDANÇA 2: Decidir qual algoritmo instanciar ---
            if (selectedAlgorithm.startsWith("A*")) {
                AStar aStar = new AStar();
                path = aStar.solve(initialMatrix, goalMatrix, heuristicaEscolhida, nivelEscolhido);
                expandedNodes = aStar.getNosExpandidos();
            } else { // Se não for A*, é Best-First
                BestFirst bestFirst = new BestFirst();
                path = bestFirst.solve(initialMatrix, goalMatrix, heuristicaEscolhida, nivelEscolhido);
                expandedNodes = bestFirst.getNosExpandidos();
            }
            // --- FIM DA MUDANÇA 2 ---

            long endTime = System.currentTimeMillis();

            Platform.runLater(() -> {
                this.solutionPath = path;
                this.currentStepIndex = 0;
                displayResults(path, endTime - startTime, expandedNodes);
                solveButton.setDisable(false);
                shuffleButton.setDisable(false);
            });
        }).start();
    }

    private void animateSolution(List<No> path) {
        // Se uma animação anterior estiver rodando, pare-a.
        if (solutionTimeline != null) {
            solutionTimeline.stop();
        }

        // Cria uma nova Timeline
        solutionTimeline = new Timeline();
        solutionTimeline.getKeyFrames().clear();

        long delayEntrePassos = 500; // Intervalo de 500ms (meio segundo) entre cada movimento

        // Para cada passo na solução, criamos um "KeyFrame"
        // Um KeyFrame é um evento que acontece em um ponto específico no tempo.
        for (int i = 0; i < path.size(); i++) {
            final No passoAtual = path.get(i);
            final int passoIndex = i;

            KeyFrame keyFrame = new KeyFrame(
                    Duration.millis(i * delayEntrePassos), // Define QUANDO o evento vai acontecer
                    event -> {
                        // Define O QUE vai acontecer: atualizar o tabuleiro e o índice
                        updatePuzzleGrid(passoAtual.getEstado());
                        this.currentStepIndex = passoIndex;
                    }
            );
            solutionTimeline.getKeyFrames().add(keyFrame);
        }

        // Ação a ser executada QUANDO a animação inteira terminar
        solutionTimeline.setOnFinished(event -> {
            // Reabilita os botões para que o usuário possa revisar os passos
            updateNavigationButtons();
            solveButton.setDisable(false);
            shuffleButton.setDisable(false);
        });

        // Desabilita os botões durante a animação
        prevButton.setDisable(true);
        nextButton.setDisable(true);
        solveButton.setDisable(true);
        shuffleButton.setDisable(true);

        // Inicia a animação!
        solutionTimeline.play();
    }

    // --- INTEGRAÇÃO 3: Lógica dos botões de navegação ---
    private void handlePrevButton() {
        if (solutionPath != null && currentStepIndex > 0) {
            currentStepIndex--;
            updatePuzzleGrid(solutionPath.get(currentStepIndex).getEstado());
            updateNavigationButtons();
        }
    }

    private void handleNextButton() {
        if (solutionPath != null && currentStepIndex < solutionPath.size() - 1) {
            currentStepIndex++;
            updatePuzzleGrid(solutionPath.get(currentStepIndex).getEstado());
            updateNavigationButtons();
        }
    }

    private void updateNavigationButtons() {
        prevButton.setDisable(currentStepIndex <= 0);
        nextButton.setDisable(currentStepIndex >= solutionPath.size() - 1);
    }

    // --- MÉTODOS AUXILIARES ---

    private void displayResults(List<No> path, long duration, int expandedNodes) {
        if (path.isEmpty()) {
            statusLabel.setText("Solução não encontrada.");
            statusLabel.getStyleClass().setAll("status-error");
            pathTextArea.setText("Não foi possível encontrar um caminho da origem ao destino.");
            prevButton.setDisable(true);
            nextButton.setDisable(true);
        }
        else {
            statusLabel.setText("Solução encontrada com sucesso!");
            statusLabel.getStyleClass().setAll("status-success");
            durationLabel.setText(duration + " ms");
            expandedNodesLabel.setText(String.valueOf(expandedNodes));
            searchDepthLabel.setText(String.valueOf(path.size() - 1));

            StringBuilder pathText = new StringBuilder("0. Estado Inicial\n");
            for (int i = 1; i < path.size(); i++) {
                pathText.append(i).append(". Mover para ").append(path.get(i).getAcao()).append("\n");
            }
            pathTextArea.setText(pathText.toString());

            // --- LÓGICA DA CHECKBOX ATUALIZADA ---
            if (resolverDiretoCheckBox.isSelected()) {
                // Se a caixa estiver MARCADA, inicia a animação passo a passo
                animateSolution(path);
            } else {
                // Se a caixa estiver DESMARCADA, mostra o início para navegação manual
                updatePuzzleGrid(path.get(0).getEstado());
                updateNavigationButtons();
            }
        }
    }

    private void clearResults() {
        durationLabel.setText("N/A");
        expandedNodesLabel.setText("N/A");
        searchDepthLabel.setText("N/A");
        pathTextArea.clear();
        solutionPath = null;
        currentStepIndex = 0;
        prevButton.setDisable(true);
        nextButton.setDisable(true);
    }

    private int[][] shuffleMatrix(int[][] matrix) {
        int[][] shuffled = new int[3][3];
        for (int i = 0; i < 3; i++) {
            shuffled[i] = matrix[i].clone();
        }

        Random random = new Random();
        for (int i = 0; i < 100; i++) { // Realiza 100 movimentos aleatórios
            List<No> neighbors = gerarVizinhos(new No(shuffled, null, "", 0));
            if (!neighbors.isEmpty()) {
                shuffled = neighbors.get(random.nextInt(neighbors.size())).getEstado();
            }
        }
        return shuffled;
    }

    private int[][] stringToMatrix(String stateStr) {
        if (stateStr == null || !stateStr.matches("\\d{9}")) return null;
        int[][] matrix = new int[3][3];
        for (int i = 0, k = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                matrix[i][j] = Character.getNumericValue(stateStr.charAt(k++));
            }
        }
        return matrix;
    }

    private String matrixToString(int[][] matrix) {
        StringBuilder sb = new StringBuilder(9);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                sb.append(matrix[i][j]);
            }
        }
        return sb.toString();
    }

    // Método auxiliar que faltava no seu Controller original
    private List<No> gerarVizinhos(No no) {
        BestFirst tempSearch = new BestFirst(); // Apenas para acessar o método privado
        // Para uma solução mais elegante, o método gerarVizinhos poderia ser movido para uma classe de utilidades
        // Mas para simplificar, vamos criar uma instância temporária.
        // O ideal seria tornar 'gerarVizinhos' público ou estático em BestFirst se ele não depender do estado da instância.

        // Simulação de como acessar. O ideal é refatorar 'gerarVizinhos'
        // para ser público e estático em uma classe de utilidades ou na própria classe 'No'.
        // Por agora, vamos recriar a lógica aqui para não modificar a outra classe.
        List<No> vizinhos = new ArrayList<>();
        int[] posZero = encontrarPosicao(no.getEstado(), 0);
        int linha = posZero[0];
        int coluna = posZero[1];

        int[] dLinha = {-1, 1, 0, 0};
        int[] dColuna = {0, 0, -1, 1};
        String[] acoes = {"Cima", "Baixo", "Esquerda", "Direita"};

        for (int i = 0; i < 4; i++) {
            int novaLinha = linha + dLinha[i];
            int novaColuna = coluna + dColuna[i];

            if (novaLinha >= 0 && novaLinha < 3 && novaColuna >= 0 && novaColuna < 3) {
                int[][] novoEstado = copiarEstado(no.getEstado());
                novoEstado[linha][coluna] = novoEstado[novaLinha][novaColuna];
                novoEstado[novaLinha][novaColuna] = 0;
                vizinhos.add(new No(novoEstado, no, acoes[i], no.getProfundidade() + 1));
            }
        }
        return vizinhos;
    }

    private int[] encontrarPosicao(int[][] matriz, int valor) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (matriz[i][j] == valor) return new int[]{i, j};
            }
        }
        return null;
    }

    private int[][] copiarEstado(int[][] estado) {
        int[][] novaMatriz = new int[3][3];
        for(int i = 0; i < 3; i++) novaMatriz[i] = estado[i].clone();
        return novaMatriz;
    }
}