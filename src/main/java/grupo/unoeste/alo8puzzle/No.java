package grupo.unoeste.alo8puzzle;

import java.util.Arrays;

public class No {

    private int[][] estado;
    private No pai;
    private String acao;


    private int profundidade;
    private int valorHeuristico;       //FA

    public No(int[][] estado, No pai, String acao, int profundidade) {
        this.estado = estado;
        this.pai = pai;
        this.acao = acao;
        this.profundidade = profundidade;
    }

    public int[][] getEstado() { return this.estado; }
    public No getPai() { return this.pai; }
    public String getAcao() { return this.acao; }
    public int getProfundidade() { return this.profundidade; }
    public int getValorHeuristico() { return this.valorHeuristico; }
    public void setValorHeuristico(int valor) { this.valorHeuristico = valor; }


    public int getCustoF() {
        return this.profundidade + this.valorHeuristico;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        No outroNo = (No) obj;
        return Arrays.deepEquals(this.estado, outroNo.estado);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(this.estado);
    }
}