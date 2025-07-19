package com.example;

// Importações de bibliotecas externas
import org.apache.commons.math3.linear.ArrayRealVector; // Implementação de vetor
import org.apache.commons.math3.linear.RealVector; // Interface para vetores
import org.knowm.xchart.BitmapEncoder; // Para salvar o gráfico em arquivo
import org.knowm.xchart.QuickChart;   // Para criar o gráfico de forma rápida
import org.knowm.xchart.XYChart;      // Objeto que representa o gráfico

// Importações de classes padrão do Java
import java.io.IOException; // Para tratar exceções de leitura/escrita de arquivos
import java.util.ArrayList; // Para criar a lista de partículas (o enxame)
import java.util.List;      // Interface de Lista
import java.util.Random;    // Para gerar números aleatórios necessários ao PSO

/**
 * Classe principal que implementa e executa o algoritmo Particle Swarm Optimization (PSO)
 * para encontrar os pesos ótimos de uma função de recomendação simulada,
 * conforme especificado nos requisitos do trabalho.
 */
public class OtimizacaoPSO {

    // --- PARÂMETROS GLOBAIS E CONSTANTES DO ALGORITMO ---
    
    // --- Requisitos Técnicos do Trabalho ---
    private static final int NUM_PARTICULAS = 30;    // Pelo menos 30 partículas
    private static final int NUM_ITERACOES = 100;   // Pelo menos 100 iterações
    private static final int DIMENSOES = 3;         // O problema tem 3 dimensões (pesos x1, x2, x3)
    private static final double LIMITE_INFERIOR = -5.0; // Limite inferior para os valores dos pesos
    private static final double LIMITE_SUPERIOR = 5.0;  // Limite superior para os valores dos pesos

    // --- Coeficientes do PSO (Hiperparâmetros) ---
    private static final double W_INICIAL = 0.9; // Peso de inércia inicial (favorece a exploração)
    private static final double W_FINAL = 0.4;   // Peso de inércia final (favorece a explotação)
    private static final double C1 = 2.0;        // Coeficiente cognitivo (confiança na própria partícula)
    private static final double C2 = 2.0;        // Coeficiente social (confiança no enxame)

    // --- Variáveis do Enxame ---
    private List<Particula> enxame;                 // Lista que armazena todas as partículas
    private RealVector gbestPosicao;                // Melhor posição global encontrada por todo o enxame
    private double gbestValor;                      // Valor da função objetivo (fitness) para gbestPosicao
    private final Random random = new Random();     // Objeto para gerar números aleatórios (r1 e r2)

    /**
     * Ponto de entrada do programa.
     */
    public static void main(String[] args) {
        OtimizacaoPSO otimizador = new OtimizacaoPSO();
        try {
            // Chama o método principal que executa toda a lógica
            otimizador.executar();
        } catch (IOException e) {
            // Captura um possível erro ao tentar salvar o arquivo de gráfico
            System.err.println("Ocorreu um erro ao gerar o arquivo de gráfico: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Orquestra a execução completa do algoritmo PSO, passo a passo.
     */
    public void executar() throws IOException {
        // Passo 1: Cria as partículas em posições aleatórias e define o gbest inicial
        inicializarEnxame();
        
        // Lista para armazenar o melhor valor de fitness a cada iteração, para o gráfico
        List<Double> melhoresValoresPorIteracao = new ArrayList<>();

        // Passo 2: Inicia o loop principal do algoritmo
        for (int i = 0; i < NUM_ITERACOES; i++) {
            // Calcula o fitness de cada partícula e atualiza o pbest e gbest
            avaliarParticulas();
            
            // Move as partículas para novas posições
            atualizarVelocidadeEPosicao(i);
            
            // Guarda o melhor valor encontrado NESTA iteração para a curva de convergência
            melhoresValoresPorIteracao.add(this.gbestValor);
        }

        // Passo 3: Ao final de todas as iterações, apresenta os resultados
        mostrarResultadosFinais();
        gerarGraficoConvergencia(melhoresValoresPorIteracao);
    }

    /**
     * A função objetivo (ou função de fitness) que queremos MAXIMIZAR.
     * f(x1,x2,x3) = sin(x1) + cos(x2) + (sin(x3)*x3)/5 - 0.1*(x1^2+x2^2+x3^2)
     * @param posicao Um vetor contendo os valores (x1, x2, x3).
     * @return O valor de fitness (quanto maior, melhor).
     */
    private double funcaoObjetivo(RealVector posicao) {
        double x1 = posicao.getEntry(0);
        double x2 = posicao.getEntry(1);
        double x3 = posicao.getEntry(2);

        // Termos que simulam o impacto positivo dos fatores
        double partePositiva = Math.sin(x1) + Math.cos(x2) + (Math.sin(x3) * x3) / 5.0;
        
        // Termo penalizador que evita pesos excessivamente grandes
        // Nota: posicao.dotProduct(posicao) é uma forma eficiente de calcular x1^2 + x2^2 + x3^2
        double termoPenalizador = 0.1 * posicao.dotProduct(posicao);

        return partePositiva - termoPenalizador;
    }

    /**
     * Cria o enxame, inicializando cada partícula com uma posição e velocidade aleatórias.
     * Também define o gbest inicial.
     */
    private void inicializarEnxame() {
        this.enxame = new ArrayList<>();
        this.gbestValor = Double.NEGATIVE_INFINITY; // gbest inicializado com o pior valor possível

        for (int i = 0; i < NUM_PARTICULAS; i++) {
            Particula p = new Particula(DIMENSOES);
            
            // Define uma posição inicial aleatória para cada dimensão (peso)
            for (int j = 0; j < DIMENSOES; j++) {
                double posAleatoria = LIMITE_INFERIOR + (LIMITE_SUPERIOR - LIMITE_INFERIOR) * random.nextDouble();
                p.posicao.setEntry(j, posAleatoria);
            }
            
            // A melhor posição inicial da partícula é sua própria posição de partida
            p.melhorPosicaoPessoal = p.posicao.copy(); // .copy() é importante para evitar referência
            
            // Calcula o fitness da posição inicial
            p.melhorValorPessoal = funcaoObjetivo(p.posicao);

            // Se esta partícula for a melhor encontrada até agora, ela se torna a gbest
            if (p.melhorValorPessoal > gbestValor) {
                gbestValor = p.melhorValorPessoal;
                gbestPosicao = p.posicao.copy();
            }
            enxame.add(p);
        }
    }

    /**
     * Para cada partícula, calcula o fitness de sua posição atual.
     * Se for melhor que seu pbest, atualiza o pbest.
     * Se for melhor que o gbest, atualiza o gbest.
     */
    private void avaliarParticulas() {
        for (Particula p : enxame) {
            double valorAtual = funcaoObjetivo(p.posicao);
            
            // Verifica se a posição atual é melhor que a memória pessoal da partícula
            if (valorAtual > p.melhorValorPessoal) {
                p.melhorValorPessoal = valorAtual;
                p.melhorPosicaoPessoal = p.posicao.copy();

                // Se também for melhor que a memória global do enxame, atualiza o gbest
                if (valorAtual > gbestValor) {
                    gbestValor = valorAtual;
                    gbestPosicao = p.posicao.copy();
                }
            }
        }
    }

    /**
     * Atualiza a velocidade e a posição de cada partícula, aplicando a fórmula principal do PSO.
     * @param iteracaoAtual O número da iteração atual, usado para calcular a inércia decrescente.
     */
    private void atualizarVelocidadeEPosicao(int iteracaoAtual) {
        // Inércia decrescente: começa alta (0.9) e diminui linearmente até 0.4
        double w = W_INICIAL - (W_INICIAL - W_FINAL) * iteracaoAtual / NUM_ITERACOES;

        for (Particula p : enxame) {
            // Gera dois números aleatórios entre 0.0 e 1.0 para a fórmula
            double r1 = random.nextDouble();
            double r2 = random.nextDouble();

            // Componente de inércia: v_antiga * w
            RealVector inercia = p.velocidade.mapMultiply(w);
            
            // Componente cognitiva: (pbest - x_atual) * c1 * r1
            RealVector cognitivo = p.melhorPosicaoPessoal.subtract(p.posicao).mapMultiply(C1 * r1);

            // Componente social: (gbest - x_atual) * c2 * r2
            RealVector social = gbestPosicao.subtract(p.posicao).mapMultiply(C2 * r2);

            // A nova velocidade é a soma das três componentes
            p.velocidade = inercia.add(cognitivo).add(social);

            // A nova posição é a posição antiga mais a nova velocidade
            p.posicao = p.posicao.add(p.velocidade);

            // Garante que a partícula não "fuja" do espaço de busca definido
            for (int i = 0; i < DIMENSOES; i++) {
                double pos = p.posicao.getEntry(i);
                if (pos > LIMITE_SUPERIOR) p.posicao.setEntry(i, LIMITE_SUPERIOR);
                else if (pos < LIMITE_INFERIOR) p.posicao.setEntry(i, LIMITE_INFERIOR);
            }
        }
    }

    /**
     * Imprime os resultados finais da otimização de forma clara no console.
     */
    private void mostrarResultadosFinais() {
        System.out.println("--- Otimização Finalizada ---");
        System.out.printf("Melhor valor da função (f) encontrado: %.6f\n", gbestValor);
        System.out.println("Pesos otimizados (x1, x2, x3):");
        System.out.printf("  x1 (Navegação): %.6f\n", gbestPosicao.getEntry(0));
        System.out.printf("  x2 (Compras):   %.6f\n", gbestPosicao.getEntry(1));
        System.out.printf("  x3 (Pop.):      %.6f\n", gbestPosicao.getEntry(2));
    }
    
    /**
     * Gera e salva em um arquivo PNG o gráfico da curva de convergência.
     * @param melhoresValores A lista de melhores valores de fitness de cada iteração.
     */
    private void gerarGraficoConvergencia(List<Double> melhoresValores) throws IOException {
        // Prepara os dados para o gráfico (eixo X e eixo Y)
        double[] iteracoes = new double[NUM_ITERACOES];
        double[] valores = melhoresValores.stream().mapToDouble(d -> d).toArray();
        for (int i = 0; i < NUM_ITERACOES; i++) {
            iteracoes[i] = i + 1; // Eixo X de 1 a 100
        }

        // Cria o gráfico usando a biblioteca XChart
        XYChart chart = QuickChart.getChart(
            "Curva de Convergência do PSO", // Título do Gráfico
            "Iteração",                     // Label Eixo X
            "Melhor Valor de f(x)",         // Label Eixo Y
            "f(x)",                         // Nome da série de dados
            iteracoes,                      // Dados do eixo X
            valores                         // Dados do eixo Y
        );
        
        // Salva o gráfico como um arquivo de imagem na pasta raiz do projeto
        BitmapEncoder.saveBitmap(chart, "./curva_convergencia.png", BitmapEncoder.BitmapFormat.PNG);
        System.out.println("\nGráfico de convergência salvo como 'curva_convergencia.png'");
    }
}