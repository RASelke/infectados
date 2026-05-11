package visual;

import javax.swing.JPanel;
import javax.swing.Timer;

import fisica.Vetor2D;
import models.EstadoSaude;
import models.Pessoa;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import java.awt.Toolkit;
import java.awt.Dimension;
import javax.swing.JOptionPane;

public class Arena extends JPanel {
    private List<Pessoa> populacao;
    private boolean simulacaoRodando;
    private Timer timer;
    
	private int inicialVacinados;
	private int inicialNaoVacinados;
	private int inicialInfectados;
    
    // Pega o tamanho do monitor
    public static final Dimension TELA = Toolkit.getDefaultToolkit().getScreenSize();
    public static final int LARGURA = (int) TELA.getWidth();
    public static final int ALTURA = (int) TELA.getHeight();
    
    // Velocidade do jogo
    public static final int FPS = 60;

    public Arena(int inicialVacinados, int inicialNaoVacinados, int inicialInfectados) {
    	this.inicialVacinados = inicialInfectados;
    	this.inicialNaoVacinados = inicialNaoVacinados;
    	this.inicialInfectados = inicialVacinados;
    	
        populacao = new ArrayList<>();
        simulacaoRodando = true;
        
        popularArena(inicialVacinados, inicialNaoVacinados, inicialInfectados);
        
        int delay = 1000 / FPS;
        timer = new Timer(delay, e -> {
            if (simulacaoRodando) {
                atualizarFrame();
                repaint();
            }
        });
        timer.start();
    }

    public void popularArena(int qtdVacinados, int qtdNaoVacinados, int qtdInfectados) {
    	// Criando os infectados
        for (int i = 0; i < qtdInfectados; i++) {
            Vetor2D posInicial = new Vetor2D(LARGURA / 2.0, ALTURA / 2.0); // Nasce no meio da tela
            Vetor2D velInicial = new Vetor2D(Math.random() * 4 - 2, Math.random() * 4 - 2); // Velocidade aleatória
            populacao.add(new Pessoa(10, posInicial, velInicial, true, EstadoSaude.INFECTADO));
        }

        // Criando os saudáveis vacinados
        for (int i = 0; i < qtdVacinados; i++) {
            Vetor2D posInicial = new Vetor2D(Math.random() * (LARGURA-40)+20, Math.random() * (LARGURA-40)+20);
            Vetor2D velInicial = new Vetor2D(Math.random() * 4 - 2, Math.random() * 4 - 2);
            populacao.add(new Pessoa(10, posInicial, velInicial, true, EstadoSaude.SUSCETIVEL));
        }
        
        // Criando os saudáveis NÃO vacinados
        for (int i = 0; i < qtdNaoVacinados; i++) {
            Vetor2D posInicial = new Vetor2D(Math.random() * (LARGURA-40)+20, Math.random() * (LARGURA-40)+20);
            Vetor2D velInicial = new Vetor2D(Math.random() * 4 - 2, Math.random() * 4 - 2);
            populacao.add(new Pessoa(10, posInicial, velInicial, false, EstadoSaude.SUSCETIVEL));
        }
    }

    public void atualizarFrame() {
        // 1. Atualizar a vida e mover todo mundo
        for (Pessoa p : populacao) {
            p.atualizar(LARGURA, ALTURA);
        }

        // 2. Verificar colisões de todos contra todos
        for (int i = 0; i < populacao.size(); i++) {
            for (int j = i + 1; j < populacao.size(); j++) {
                Pessoa p1 = populacao.get(i);
                Pessoa p2 = populacao.get(j);
                
                if (p1.getEstado() == EstadoSaude.MORTO || p2.getEstado() == EstadoSaude.MORTO) {
                    continue; 
                }

                if (p1.verificaColisao(p2)) {
                    p1.interagir(p2);
                    p2.interagir(p1);
                    
                    p1.resolverColisaoFisica(p2);
                }
            }
        }

        // 3. Checar se o jogo acabou
        if (verificarFimDeJogo()) {
            simulacaoRodando = false;
            timer.stop();
            exibirEstatisticas();
        }
    }

    public boolean verificarFimDeJogo() {
        for (Pessoa p : populacao) {
            if (p.getEstado() == EstadoSaude.INFECTADO) {
                return false; // Se achar pelo menos 1 infectado, o jogo continua
            }
        }
        return true;
    }
    
    public void exibirEstatisticas() {
        // Contadores para Vacinados
        int mortosVacinados = 0;
        int recuperadosVacinados = 0;
        int ilesosVacinados = 0; // Suscetíveis que nunca pegaram a doença

        // Contadores para NÃO Vacinados
        int mortosNaoVacinados = 0;
        int recuperadosNaoVacinados = 0;
        int ilesosNaoVacinados = 0;

        // Varre a população atual e classifica cada um
        for (Pessoa p : populacao) {
            if (p.isVacinado()) {
                if (p.getEstado() == EstadoSaude.MORTO) mortosVacinados++;
                else if (p.getEstado() == EstadoSaude.RECUPERADO) recuperadosVacinados++;
                else if (p.getEstado() == EstadoSaude.SUSCETIVEL) ilesosVacinados++;
            } else {
                if (p.getEstado() == EstadoSaude.MORTO) mortosNaoVacinados++;
                else if (p.getEstado() == EstadoSaude.RECUPERADO) recuperadosNaoVacinados++;
                else if (p.getEstado() == EstadoSaude.SUSCETIVEL) ilesosNaoVacinados++;
            }
        }

        // Monta o texto do relatório de forma estruturada
        String relatorio ="Fim da Simulação"
                + "População Inicial:\n"
                + "  - Vacinados Saudáveis: " + inicialVacinados + "\n"
                + "  - Não Vacinados Saudáveis: " + inicialNaoVacinados + "\n"
                + "  - Pacientes Zero (Infectados): " + inicialInfectados + "\n"
                + "  - TOTAL: " + (inicialVacinados + inicialNaoVacinados + inicialInfectados) + "\n\n"
                
                + "Resultados - VACINADOS:\n"
                + "  - Sobreviveram sem pegar: " + ilesosVacinados + "\n"
                + "  - Pegaram, mas se Recuperaram: " + recuperadosVacinados + "\n"
                + "  - Óbitos: " + mortosVacinados + "\n\n"
                
                + "Resultados - NÃO VACINADOS (Inclui Paciente Zero):\n"
                + "  - Sobreviveram sem pegar: " + ilesosNaoVacinados + "\n"
                + "  - Pegaram e se Recuperaram: " + recuperadosNaoVacinados + "\n"
                + "  - Óbitos: " + mortosNaoVacinados;

        // Exibe no console do Eclipse para registro
        System.out.println(relatorio);
        
        // Abre o Pop-up na tela do jogo
        JOptionPane.showMessageDialog(this, relatorio, "Fim da Simulação - INFECTADOS", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Fundo da arena
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
        
        // --- COMEÇO DA BORDA VISUAL
        int margem = 20; // A mesma margem que usamos na Entidade
        g.setColor(Color.BLACK); // Cor da nossa borda
        
        // Desenha um retângulo vazio (x, y, largura, altura)
        g.drawRect(margem, margem, LARGURA - (margem * 2), ALTURA - (margem * 2));
        // --- FIM DA BORDA VISUAL

        // Desenhar cada pessoa
        for (Pessoa p : populacao) {
            if (p.getEstado() == EstadoSaude.MORTO) continue;

            if (p.isVacinado() && p.getEstado() == EstadoSaude.SUSCETIVEL){
                g.setColor(Color.CYAN);
            } else {
                g.setColor(Color.BLUE);
            }

            // Escolhe a cor baseada no estado de saúde
            switch (p.getEstado()) {
                case INFECTADO: g.setColor(Color.RED); break;
                case RECUPERADO: g.setColor(Color.GRAY); break;
                default:
                    break;
            }

            int raio = 10; // O tamanho da entidade
            int x = (int) p.getPosicao().getX();
            int y = (int) p.getPosicao().getY();
            
            // Desenha a bolinha centralizada
            g.fillOval(x - raio, y - raio, raio * 2, raio * 2);
        }
    }
}