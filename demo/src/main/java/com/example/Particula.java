package com.example;

// Importa as classes necessárias
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

/**
 * Representa uma partícula no enxame do PSO.
 * Cada partícula tem uma posição, uma velocidade e uma memória de sua
 * melhor performance individual (pbest).
 */
public class Particula {

    RealVector posicao;
    RealVector velocidade;
    RealVector melhorPosicaoPessoal;
    double melhorValorPessoal;

    /**
     * Construtor da Particula.
     * @param dimensoes O número de dimensões do problema (neste caso, 3).
     */
    public Particula(int dimensoes) {
        this.posicao = new ArrayRealVector(dimensoes);
        this.velocidade = new ArrayRealVector(dimensoes);
        this.melhorPosicaoPessoal = new ArrayRealVector(dimensoes);
        
        // Inicializa o melhor valor pessoal com o menor valor possível.
        this.melhorValorPessoal = Double.NEGATIVE_INFINITY;
    }
}