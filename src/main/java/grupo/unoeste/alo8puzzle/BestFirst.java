package grupo.unoeste.alo8puzzle;

import java.util.*;

public class BestFirst {

    private int nosExpandidos;

    public List<No> solve(int[][] estadoInicial, int[][] estadoFinal, Heuristica tipoHeuristica, NivelBusca nivel) {
        this.nosExpandidos = 0;

        //sintaxe da fila de prioridade usando COMPARATOR
        PriorityQueue<No> filaDePrioridade = new PriorityQueue<>(
                Comparator.comparingInt(No::getValorHeuristico)
        );
        Set<String> visitados = new HashSet<>();

        boolean solucaoEncontrada = false;
        No noFinal = null;
        List<No> caminhoDaSolucao;

        No noInicial = new No(estadoInicial, null, "", 0);
        int heuristicaInicial = calcularValorHeuristico(noInicial, estadoFinal, tipoHeuristica, nivel);
        noInicial.setValorHeuristico(heuristicaInicial);
        filaDePrioridade.add(noInicial);

        while (!filaDePrioridade.isEmpty() && !solucaoEncontrada) {
            No noAtual = filaDePrioridade.poll();
            this.nosExpandidos++;
            String estadoAtualStr = estadoParaString(noAtual.getEstado());

            if (!visitados.contains(estadoAtualStr)) {
                visitados.add(estadoAtualStr);

                if (Arrays.deepEquals(noAtual.getEstado(), estadoFinal)) {
                    solucaoEncontrada = true;
                    noFinal = noAtual;
                }
                else {
                    List<No> listaDeVizinhos = gerarVizinhos(noAtual);
                    for (int i = 0; i < listaDeVizinhos.size(); i++) {
                        No vizinho = listaDeVizinhos.get(i);

                        if (!visitados.contains(estadoParaString(vizinho.getEstado()))) {
                            int valorHeuristico = calcularValorHeuristico(vizinho, estadoFinal, tipoHeuristica, nivel);
                            vizinho.setValorHeuristico(valorHeuristico);
                            filaDePrioridade.add(vizinho);
                        }
                    }
                }
            }
        }

        if (solucaoEncontrada)
            caminhoDaSolucao = reconstruirCaminho(noFinal);
        else
            caminhoDaSolucao = Collections.emptyList();


        return caminhoDaSolucao;
    }


    private int calcularValorHeuristico(No no, int[][] estadoFinal, Heuristica tipoHeuristica, NivelBusca nivel) {

        if (nivel == NivelBusca.NIVEL_1) {
            if (tipoHeuristica == Heuristica.PECAS_FORA_DO_LUGAR) {
                return calcularPecasForaDoLugar(no.getEstado(), estadoFinal);
            }
            else {
                return calcularDistanciaManhattan(no.getEstado(), estadoFinal);
            }
        }
        else {
            int minHeuristicaFutura = Integer.MAX_VALUE;
            List<No> filhos = gerarVizinhos(no);

            if (filhos.isEmpty()) {
                return Integer.MAX_VALUE; //ARRUMAR
            }
            for (No filho : filhos) {
                List<No> netos = gerarVizinhos(filho);
                if (netos.isEmpty() && filho.getProfundidade() > no.getProfundidade()) {
                    int heuristicaDoFilho;
                    if (tipoHeuristica == Heuristica.PECAS_FORA_DO_LUGAR) {
                        heuristicaDoFilho = calcularPecasForaDoLugar(filho.getEstado(), estadoFinal);
                    }
                    else {
                        heuristicaDoFilho = calcularDistanciaManhattan(filho.getEstado(), estadoFinal);
                    }
                    if(heuristicaDoFilho < minHeuristicaFutura){
                        minHeuristicaFutura = heuristicaDoFilho;
                    }

                }
                else {
                    for (No neto : netos) {
                        if(!Arrays.deepEquals(neto.getEstado(), no.getEstado())){
                            int heuristicaDoNeto;
                            if (tipoHeuristica == Heuristica.PECAS_FORA_DO_LUGAR) {
                                heuristicaDoNeto = calcularPecasForaDoLugar(neto.getEstado(), estadoFinal);
                            }
                            else {
                                heuristicaDoNeto = calcularDistanciaManhattan(neto.getEstado(), estadoFinal);
                            }
                            if (heuristicaDoNeto < minHeuristicaFutura) {
                                minHeuristicaFutura = heuristicaDoNeto;
                            }
                        }
                    }
                }
            }

            if (minHeuristicaFutura == Integer.MAX_VALUE) { //perguntar MAX VALUE
                return calcularValorHeuristico(no, estadoFinal, tipoHeuristica, NivelBusca.NIVEL_1);
            }

            return minHeuristicaFutura;
        }
    }

    private int calcularPecasForaDoLugar(int[][] estadoAtual, int[][] estadoFinal) {
        int contador = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (estadoAtual[i][j] != 0 && estadoAtual[i][j] != estadoFinal[i][j]) {
                    contador++;
                }
            }
        }
        return contador;
    }

    private int calcularDistanciaManhattan(int[][] estadoAtual, int[][] estadoFinal) {
        int distanciaTotal = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int valor = estadoAtual[i][j];
                if (valor != 0) {
                    int[] posFinal = encontrarPosicao(estadoFinal, valor);
                    distanciaTotal += Math.abs(i - posFinal[0]) + Math.abs(j - posFinal[1]);
                }
            }
        }
        return distanciaTotal;
    }

    private List<No> gerarVizinhos(No no) {
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
        int[] posicaoEncontrada = null;
        for (int i = 0; i < 3 && posicaoEncontrada == null; i++) {
            for (int j = 0; j < 3 && posicaoEncontrada == null; j++) {
                if (matriz[i][j] == valor) {
                    posicaoEncontrada = new int[]{i, j};
                }
            }
        }
        return posicaoEncontrada;
    }

    private List<No> reconstruirCaminho(No noFinal) {
        LinkedList<No> caminho = new LinkedList<>();
        for (No no = noFinal; no != null; no = no.getPai()) {
            caminho.addFirst(no);
        }
        return caminho;
    }

    private int[][] copiarEstado(int[][] estado) {
        int[][] novaMatriz = new int[3][3];
        for(int i = 0; i < 3; i++) novaMatriz[i] = Arrays.copyOf(estado[i], 3);
        return novaMatriz;
    }

    private String estadoParaString(int[][] estado) {
        return Arrays.deepToString(estado);
    }

    public int getNosExpandidos() {
        return this.nosExpandidos;
    }
}