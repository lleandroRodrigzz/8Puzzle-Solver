package grupo.unoeste.alo8puzzle;

import java.util.ArrayList;

public class EightPuzzle {
    private int [][] matriz = new int[3][3];
    private int [][] random = new int[3][3];
    private String finalState;

    public void calculaDistMatriz(){
        ArrayList<Integer> arrayList = new ArrayList();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                arrayList = procuraNumero(matriz, random[i][j]);
                calculaDistManhattan(arrayList.get(0), arrayList.get(1), i, j);

                // aqui vai entrar o algoritimo de buscar (A DECIDIR)
                // BFS/DFS/... e o A*
            }
        }
    }

    public int calculaDistManhattan(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    public void exibeMatriz(int[][] matriz) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                System.out.print(matriz[i][j]);
            }
        }
    }

    public int contaCasasDiferentes(int [][] matriz, int [][] matrizClone) {
        int cont = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if(matriz[i][j] != matrizClone[i][j]) {
                    cont++;
                }
            }
        }
        return cont;
    }

    public int randomOrdenado(int ord){
        if(ord == 0){
            return (int) (Math.random() * 2);
        }
        else if (ord == 1) {
            return (int) (Math.random() * 3);
        }
        else { //ord == 2
            return (int)  (Math.random() * 2) + 1;
        }
    }

    public ArrayList<Integer> procuraNumero(int[][] matriz, int num) {
        ArrayList<Integer> coordenadas = new ArrayList<>();
        boolean achou = false;

        for (int i = 0; i < 3 && !achou; i++)
        {
            for (int j = 0; j < 3 && !achou; j++)
            {
                if(matriz[i][j] == num)
                {
                    achou = true;
                    coordenadas.add(i);
                    coordenadas.add(j);
                }
            }
        }
        return coordenadas;
    }

    public void montaMatriz(){
        int k =0;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                matriz[i][j] = Integer.parseInt(String.valueOf(finalState.charAt(k++)));
            }
        }
    }

    public int[][] copiaMatriz(int [][] matriz){
        int [][] matrizClone = new int[3][3];
        int k =0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                matrizClone[i][j] = matriz[i][j];
            }
        }
        return matrizClone;
    }

    public void randomMatriz(int [][] matriz) {
        int i, j, k, aux, auxNum;
        random = copiaMatriz(matriz);
        boolean igual = true;
        ArrayList<Integer> coordenadas = procuraNumero(matriz,0);
        i = coordenadas.get(0);
        j = coordenadas.get(1);
        while (igual) {
            k = 0;
            while (k < 24) { // ou 24 * 9!
                aux = i;
                i = randomOrdenado(i); //sortiei
                auxNum = random[i][j];
                random[aux][j] = auxNum; //posicao antiga = novo
                random[i][j] = 0; //novo = 0

                aux = j;
                j = randomOrdenado(j);
                auxNum = random[i][j];
                random[i][aux] = auxNum;
                random[i][j] = 0;
                k++;
            }
            if (random != matriz && contaCasasDiferentes(matriz, random) >= 6) {
                igual = false;
            }
        }
    }

    public EightPuzzle() {
    }

    public EightPuzzle(String finalState) {
        this.finalState = finalState;
    }

    public EightPuzzle(int[][] matriz, String finalState) {
        this.matriz = matriz;
        this.finalState = finalState;
    }

    public int[][] getMatriz() {
        return matriz;
    }

    public void setMatriz(int[][] matriz) {
        this.matriz = matriz;
    }

    public String getFinalState() {
        return finalState;
    }

    public void setFinalState(String finalState) {
        this.finalState = finalState;
    }
}
