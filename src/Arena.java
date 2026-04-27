package infectados.src;
import javax.swing.JPanel;
import javax.swing.Timer;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

public class Arena extends JPanel {
    private List<Pessoa> populacao;
    private boolean simulacaoRodando;
    private Timer timer;

    public Arena() {
        populacao = new ArrayList<>();
        simulacaoRodando = true;
        
        popularArena(40, 40, 5);

        timer = new Timer(16, e -> {
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
            Vetor2D posInicial = new Vetor2D(400, 300); // Nasce no meio da tela
            Vetor2D velInicial = new Vetor2D(Math.random() * 4 - 2, Math.random() * 4 - 2); // Velocidade aleatória
            populacao.add(new Pessoa(10, posInicial, velInicial, false, EstadoSaude.INFECTADO));
        }

        // Criando os saudáveis vacinados
        for (int i = 0; i < qtdVacinados; i++) {
            Vetor2D posInicial = new Vetor2D(Math.random() * 700, Math.random() * 500);
            Vetor2D velInicial = new Vetor2D(Math.random() * 4 - 2, Math.random() * 4 - 2);
            populacao.add(new Pessoa(10, posInicial, velInicial, true, EstadoSaude.SUSCETIVEL));
        }
        
        // Criando os saudáveis NÃO vacinados
        for (int i = 0; i < qtdNaoVacinados; i++) {
            Vetor2D posInicial = new Vetor2D(Math.random() * 700, Math.random() * 500);
            Vetor2D velInicial = new Vetor2D(Math.random() * 4 - 2, Math.random() * 4 - 2);
            populacao.add(new Pessoa(10, posInicial, velInicial, false, EstadoSaude.SUSCETIVEL));
        }
    }

    public void atualizarFrame() {
        // 1. Atualizar a vida e mover todo mundo
        for (Pessoa p : populacao) {
            p.atualizar();
        }

        // 2. Verificar colisões de todos contra todos
        for (int i = 0; i < populacao.size(); i++) {
            for (int j = i + 1; j < populacao.size(); j++) {
                Pessoa p1 = populacao.get(i);
                Pessoa p2 = populacao.get(j);

                if (p1.verificaColisao(p2)) {
                    p1.interagir(p2);
                    p2.interagir(p1);
                }
            }
        }

        // 3. Checar se o jogo acabou
        if (verificarFimDeJogo()) {
            simulacaoRodando = false;
            timer.stop();
            System.out.println("Simulação Encerrada! Exibir estatísticas aqui.");
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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Fundo da arena
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());

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
            int x = (int) p.posicao.getX();
            int y = (int) p.posicao.getY();
            
            // Desenha a bolinha centralizada
            g.fillOval(x - raio, y - raio, raio * 2, raio * 2);
        }
    }
}