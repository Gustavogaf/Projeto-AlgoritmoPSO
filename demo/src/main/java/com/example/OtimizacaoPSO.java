package com.example;

import org.apache.commons.math3.linear.RealVector;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.XYChart;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Classe principal que implementa o Particle Swarm Optimization (PSO).
 */
public class OtimizacaoPSO {

    // --- PARÂMETROS DO PROBLEMA E DO PSO ---
    private static final int NUM_PARTICULAS = 30;
    private static final int NUM_ITERACOES = 100;
    private static final int DIMENSOES = 3;
    private static final double LIMITE_INFERIOR = -5.0;
    private static final double LIMITE_SUPERIOR = 5.0;
    private static final double W_INICIAL = 0.9;
    private static final double W_FINAL = 0.4;
    private static final double C1 = 2.0;
    private static final double C2 = 2.0;

    private List<Particula> enxame = new ArrayList<>();
    private RealVector gbestPosicao;
    private double gbestValor = Double.NEGATIVE_INFINITY;
    private Random random = new Random();
    
    // --- FUNÇÃO MAIN ---
    public static void main(String[] args) {
        OtimizacaoPSO otimizador = new OtimizacaoPSO();
        try {
            otimizador.executar();
        } catch (IOException e) {
            System.err.println("Ocorreu um erro ao gerar o gráfico: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void executar() throws IOException {
        inicializarEnxame();
        List<Double> melhoresValoresPorIteracao = new ArrayList<>();

        for (int i = 0; i < NUM_ITERACOES; i++) {
            avaliarParticulas();
            atualizarVelocidadeEPosicao(i);
            melhoresValoresPorIteracao.add(this.gbestValor);
        }

        mostrarResultados();
        gerarGraficoConvergencia(melhoresValoresPorIteracao);
    }

    private double funcaoObjetivo(RealVector posicao) {
        double x1 = posicao.getEntry(0);
        double x2 = posicao.getEntry(1);
        double x3 = posicao.getEntry(2);
        return Math.sin(x1) + Math.cos(x2) + (Math.sin(x3) * x3) / 5.0 - 0.1 * posicao.dotProduct(posicao);
    }

    private void inicializarEnxame() {
        for (int i = 0; i < NUM_PARTICULAS; i++) {
            Particula p = new Particula(DIMENSOES);
            for (int j = 0; j < DIMENSOES; j++) {
                p.posicao.setEntry(j, LIMITE_INFERIOR + (LIMITE_SUPERIOR - LIMITE_INFERIOR) * random.nextDouble());
            }
            p.melhorPosicaoPessoal = p.posicao.copy();
            p.melhorValorPessoal = funcaoObjetivo(p.posicao);

            if (p.melhorValorPessoal > gbestValor) {
                gbestValor = p.melhorValorPessoal;
                gbestPosicao = p.posicao.copy();
            }
            enxame.add(p);
        }
    }

    private void avaliarParticulas() {
        for (Particula p : enxame) {
            double valorAtual = funcaoObjetivo(p.posicao);
            if (valorAtual > p.melhorValorPessoal) {
                p.melhorValorPessoal = valorAtual;
                p.melhorPosicaoPessoal = p.posicao.copy();

                if (valorAtual > gbestValor) {
                    gbestValor = valorAtual;
                    gbestPosicao = p.posicao.copy();
                }
            }
        }
    }

    private void atualizarVelocidadeEPosicao(int iteracao) {
        double w = W_INICIAL - (W_INICIAL - W_FINAL) * iteracao / NUM_ITERACOES;

        for (Particula p : enxame) {
            double r1 = random.nextDouble();
            double r2 = random.nextDouble();

            RealVector inercia = p.velocidade.mapMultiply(w);
            RealVector cognitivo = p.melhorPosicaoPessoal.subtract(p.posicao).mapMultiply(C1 * r1);
            RealVector social = gbestPosicao.subtract(p.posicao).mapMultiply(C2 * r2);
            
            p.velocidade = inercia.add(cognitivo).add(social);
            p.posicao = p.posicao.add(p.velocidade);

            for (int i = 0; i < DIMENSOES; i++) {
                double pos = p.posicao.getEntry(i);
                if (pos > LIMITE_SUPERIOR) p.posicao.setEntry(i, LIMITE_SUPERIOR);
                else if (pos < LIMITE_INFERIOR) p.posicao.setEntry(i, LIMITE_INFERIOR);
            }
        }
    }

    private void mostrarResultados() {
        System.out.println("--- Otimização Finalizada ---");
        System.out.printf("Melhor valor da função (f) encontrado: %.6f\n", gbestValor);
        System.out.println("Pesos otimizados (x1, x2, x3):");
        System.out.printf("  x1 (Navegação): %.6f\n", gbestPosicao.getEntry(0));
        System.out.printf("  x2 (Compras):   %.6f\n", gbestPosicao.getEntry(1));
        System.out.printf("  x3 (Pop.):      %.6f\n", gbestPosicao.getEntry(2));
    }
    
    private void gerarGraficoConvergencia(List<Double> melhoresValores) throws IOException {
        double[] iteracoes = new double[NUM_ITERACOES];
        double[] valores = melhoresValores.stream().mapToDouble(d -> d).toArray();
        for (int i = 0; i < NUM_ITERACOES; i++) iteracoes[i] = i + 1;

        XYChart chart = QuickChart.getChart("Curva de Convergência do PSO", "Iteração", "Melhor Valor de f(x)", "f(x)", iteracoes, valores);
        BitmapEncoder.saveBitmap(chart, "./curva_convergencia.png", BitmapEncoder.BitmapFormat.PNG);
        System.out.println("\nGráfico de convergência salvo como 'curva_convergencia.png'");
    }
}