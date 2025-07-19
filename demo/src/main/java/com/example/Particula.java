package com.example;

// Importa as classes necessárias da biblioteca Apache Commons Math para lidar com vetores.
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

/**
 * Representa uma única partícula no enxame do PSO.
 * Esta classe encapsula todos os dados essenciais de uma partícula,
 * tornando o código principal mais limpo e organizado.
 */
public class Particula {

    // --- ATRIBUTOS DA PARTÍCULA ---

    /**
     * Posição atual da partícula no espaço de busca.
     * É um vetor que contém os valores dos pesos (x1, x2, x3) a serem otimizados.
     */
    RealVector posicao;

    /**
     * Velocidade atual da partícula.
     * É um vetor que determina a direção e a intensidade da mudança da posição
     * na próxima iteração.
     */
    RealVector velocidade;

    /**
     * A melhor posição já encontrada por ESTA partícula (pbest - personal best).
     * Funciona como a "memória" individual da partícula.
     */
    RealVector melhorPosicaoPessoal;

    /**
     * O valor da função objetivo para a 'melhorPosicaoPessoal'.
     * Armazena o "fitness" da melhor solução que esta partícula encontrou até agora.
     */
    double melhorValorPessoal;

    /**
     * Construtor da classe Particula.
     * É chamado para criar uma nova partícula no início do algoritmo.
     * @param dimensoes O número de dimensões do problema (neste caso, 3, para x1, x2, x3).
     */
    public Particula(int dimensoes) {
        // Inicializa o vetor de posição usando a classe ArrayRealVector da biblioteca.
        this.posicao = new ArrayRealVector(dimensoes);
        
        // Inicializa o vetor de velocidade.
        this.velocidade = new ArrayRealVector(dimensoes);
        
        // Inicializa o vetor que guardará a melhor posição pessoal.
        this.melhorPosicaoPessoal = new ArrayRealVector(dimensoes);
        
        // Inicializa o melhor valor pessoal com o menor valor possível em Java.
        // Isso garante que a primeira avaliação da função objetivo sempre será maior
        // e se tornará o primeiro "melhor valor".
        this.melhorValorPessoal = Double.NEGATIVE_INFINITY;
    }
}