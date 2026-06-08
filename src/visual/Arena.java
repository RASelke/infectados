package visual;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Font;
import java.awt.AlphaComposite;
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

public class Arena extends JPanel {
    private List<Pessoa> populacao;
    private boolean simulacaoRodando;
    private Timer timer;
    
    private int inicialVacinados;
    private int inicialNaoVacinados;
    private int inicialInfectados;
    
    // CONTROLE DE RODADAS
    private int rodadaAtual = 1;
    private final int TOTAL_RODADAS = 3;
    private List<ResultadoRodada> historicoResultados;
    private ResultadoRodada resultadoMediano; // O cenário intermediário que irá para o painel final
    private boolean mostrarTelaFinal = false; // Controla o Dashboard
    
    // Constantes de Ecrã
    public static final Dimension TELA = Toolkit.getDefaultToolkit().getScreenSize();
    public static final int LARGURA = (int) TELA.getWidth();
    public static final int ALTURA = (int) TELA.getHeight();
    public static final int FPS = 60;

    // CLASSE INTERNA PARA GUARDAR OS DADOS DE CADA RODADA
    private class ResultadoRodada {
        int mortosVac = 0, recVac = 0, ilesosVac = 0;
        int mortosNaoVac = 0, recNaoVac = 0, ilesosNaoVac = 0;
        int totalMortos = 0; // Usado para definir qual é a mediana
    }

    public Arena(int inicialVacinados, int inicialNaoVacinados, int inicialInfectados) {
        this.inicialVacinados = inicialVacinados;
        this.inicialNaoVacinados = inicialNaoVacinados;
        this.inicialInfectados = inicialInfectados;
        
        populacao = new ArrayList<>();
        historicoResultados = new ArrayList<>();
        simulacaoRodando = true;
        
        popularArena(this.inicialVacinados, this.inicialNaoVacinados, this.inicialInfectados);
        
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
        // 1. Paciente Zero (Infectado = true, Vacinado = false, Identidade = true)
        for (int i = 0; i < qtdInfectados; i++) {
            Vetor2D posInicial = new Vetor2D(LARGURA / 2.0, ALTURA / 2.0); 
            Vetor2D velInicial = new Vetor2D(Math.random() * 4 - 2, Math.random() * 4 - 2); 
            populacao.add(new Pessoa(10, posInicial, velInicial, false, EstadoSaude.INFECTADO, true));
        }

        // 2. Vacinados Saudáveis (Identidade Paciente Zero = false)
        for (int i = 0; i < qtdVacinados; i++) {
            Vetor2D posInicial = new Vetor2D(Math.random() * (LARGURA-40)+20, Math.random() * (ALTURA-40)+20);
            Vetor2D velInicial = new Vetor2D(Math.random() * 4 - 2, Math.random() * 4 - 2);
            populacao.add(new Pessoa(10, posInicial, velInicial, true, EstadoSaude.SUSCETIVEL, false));
        }
        
        // 3. Não Vacinados Saudáveis (Identidade Paciente Zero = false)
        for (int i = 0; i < qtdNaoVacinados; i++) {
            Vetor2D posInicial = new Vetor2D(Math.random() * (LARGURA-40)+20, Math.random() * (ALTURA-40)+20);
            Vetor2D velInicial = new Vetor2D(Math.random() * 4 - 2, Math.random() * 4 - 2);
            populacao.add(new Pessoa(10, posInicial, velInicial, false, EstadoSaude.SUSCETIVEL, false));
        }
    }

    public void atualizarFrame() {
        for (Pessoa p : populacao) {
            p.atualizar(LARGURA, ALTURA);
        }

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

        // LÓGICA DO CICLO DE SIMULAÇÃO 
        if (verificarFimDeJogo()) {
            salvarEstatisticasDaRodada();
            
            if (rodadaAtual < TOTAL_RODADAS) {
                // Reinicia a arena para a próxima rodada
                rodadaAtual++;
                populacao.clear(); // Limpa as entidades antigas
                popularArena(inicialVacinados, inicialNaoVacinados, inicialInfectados);
            } else {
                // Se acabou, calcula a mediana e mostra o painel final
                prepararResultadoIntermediario();
                simulacaoRodando = false;
                timer.stop();
                mostrarTelaFinal = true;
                repaint();
            }
        }
    }

    public boolean verificarFimDeJogo() {
        for (Pessoa p : populacao) {
            if (p.getEstado() == EstadoSaude.INFECTADO) return false; 
        }
        return true;
    }
    
    private void salvarEstatisticasDaRodada() {
        ResultadoRodada r = new ResultadoRodada();

        for (Pessoa p : populacao) {
            if (p.isPacienteZero()) continue; // Descartamos o Paciente Zero aqui!

            if (p.isVacinado()) {
                if (p.getEstado() == EstadoSaude.MORTO) r.mortosVac++;
                else if (p.getEstado() == EstadoSaude.RECUPERADO) r.recVac++;
                else if (p.getEstado() == EstadoSaude.SUSCETIVEL) r.ilesosVac++;
            } else {
                if (p.getEstado() == EstadoSaude.MORTO) r.mortosNaoVac++;
                else if (p.getEstado() == EstadoSaude.RECUPERADO) r.recNaoVac++;
                else if (p.getEstado() == EstadoSaude.SUSCETIVEL) r.ilesosNaoVac++;
            }
        }
        
        r.totalMortos = r.mortosVac + r.mortosNaoVac;
        historicoResultados.add(r);
    }
    
    private void prepararResultadoIntermediario() {
        // Ordena do cenário com menos mortos para o cenário com mais mortos
        historicoResultados.sort((r1, r2) -> Integer.compare(r1.totalMortos, r2.totalMortos));
        // Apanha o caso intermediário (índice 1 de uma lista de tamanho 3)
        resultadoMediano = historicoResultados.get(1); 
    }

    private void desenharTelaFinal(Graphics2D g2d) {
        // Fundo Translúcido
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, LARGURA, ALTURA);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

        // Caixa Principal do Dashboard
        int painelLargura = 550;
        int painelAltura = 420;
        int x = (LARGURA - painelLargura) / 2;
        int y = (ALTURA - painelAltura) / 2;

        g2d.setColor(new Color(40, 44, 52));
        g2d.fillRoundRect(x, y, painelLargura, painelAltura, 30, 30);
        g2d.setColor(new Color(0, 255, 225));
        g2d.drawRoundRect(x, y, painelLargura, painelAltura, 30, 30);

        // Textos
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString("RESULTADOS (MEDIANA DE 3 RODADAS)", x + 70, y + 50);

        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        
        // Coluna Vacinados
        g2d.setColor(Color.CYAN);
        g2d.drawString("PÚBLICO VACINADO (" + inicialVacinados + ")", x + 40, y + 110);
        g2d.setColor(Color.WHITE);
        g2d.drawString("- Ilesos: " + resultadoMediano.ilesosVac, x + 40, y + 140);
        g2d.drawString("- Recuperados: " + resultadoMediano.recVac, x + 40, y + 170);
        g2d.setColor(new Color(255, 100, 100)); // Cor vermelha suave para os óbitos
        g2d.drawString("- Óbitos: " + resultadoMediano.mortosVac, x + 40, y + 200);

        // Coluna Não Vacinados
        g2d.setColor(Color.BLUE);
        g2d.drawString("NÃO VACINADOS (" + inicialNaoVacinados + ")", x + 300, y + 110);
        g2d.setColor(Color.WHITE);
        g2d.drawString("- Ilesos: " + resultadoMediano.ilesosNaoVac, x + 300, y + 140);
        g2d.drawString("- Recuperados: " + resultadoMediano.recNaoVac, x + 300, y + 170);
        g2d.setColor(new Color(255, 100, 100));
        g2d.drawString("- Óbitos: " + resultadoMediano.mortosNaoVac, x + 300, y + 200);

        // Rodapé do painel
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawLine(x + 40, y + 240, x + 510, y + 240);
        g2d.setColor(Color.WHITE);
        g2d.drawString("Origem: " + inicialInfectados + " Paciente(s) Zero excluído(s) da conta.", x + 80, y + 280);
        
        g2d.setFont(new Font("Arial", Font.ITALIC, 14));
        g2d.drawString("Pressiona ALT+F4 para sair", x + 185, y + 360);
    }
    
    private void desenharLegenda(Graphics2D g2d) {
        int x = 40; 
        int y = 40; 
        int larguraHUD = 250;
        int alturaHUD = 170;

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRoundRect(x, y, larguraHUD, alturaHUD, 15, 15);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)); 
        g2d.setColor(Color.WHITE);
        g2d.drawRoundRect(x, y, larguraHUD, alturaHUD, 15, 15);

        // INFORMAÇÃO DA RODADA ATUAL
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.setColor(Color.YELLOW);
        g2d.drawString("RODADA " + rodadaAtual + " DE " + TOTAL_RODADAS, x + 55, y + 25);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString("LEGENDA", x + 85, y + 45);

        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        
        g2d.setColor(Color.BLUE);
        g2d.fillOval(x + 15, y + 60, 12, 12);
        g2d.setColor(Color.WHITE);
        g2d.drawString("Suscetível (Não vacinado)", x + 35, y + 71);
        
        g2d.setColor(Color.CYAN);
        g2d.fillOval(x + 15, y + 85, 12, 12);
        g2d.setColor(Color.WHITE);
        g2d.drawString("Suscetível (Vacinado)", x + 35, y + 96);
        
        g2d.setColor(Color.RED);
        g2d.fillOval(x + 15, y + 110, 12, 12);
        g2d.setColor(Color.WHITE);
        g2d.drawString("Infectado (Transmissor)", x + 35, y + 121);

        g2d.setColor(Color.GRAY);
        g2d.fillOval(x + 15, y + 135, 12, 12);
        g2d.setColor(Color.WHITE);
        g2d.drawString("Recuperado (Imune)", x + 35, y + 146);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        g2d.setColor(new Color(245, 245, 245));
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        int margem = 120; 
        g2d.setColor(Color.LIGHT_GRAY); 
        g2d.drawRect(margem, margem, LARGURA - (margem * 2), ALTURA - (margem * 2));

        for (Pessoa p : populacao) {
            if (p.getEstado() == EstadoSaude.MORTO) continue;
            
            Color corPreenchimento = Color.BLACK;
            Color corBorda = Color.BLACK;

            if (p.isVacinado() && p.getEstado() == EstadoSaude.SUSCETIVEL){
                corPreenchimento = new Color(0, 255, 225); 
                corBorda = new Color(0, 139, 139); 
            } else {
                corPreenchimento = new Color(0, 0, 225); 
                corBorda = new Color(17, 17, 132); 
            }

            switch (p.getEstado()) {
                case INFECTADO: {
                    corPreenchimento = new Color(255, 0, 0); 
                    corBorda = new Color(128, 0, 0); 
                    break;
                }
                case RECUPERADO: {
                    corPreenchimento = new Color(128, 128, 128); 
                    corBorda = new Color(169, 169, 169); 
                    break;
                }
                default: break;
            }

            int raio = 10;
            int x = (int) p.getPosicao().getX() - raio;
            int y = (int) p.getPosicao().getY() - raio;
            int diametro = raio * 2;
            
            g2d.setColor(corPreenchimento);
            g2d.fillOval(x, y, diametro, diametro);
            g2d.setColor(corBorda);
            g2d.drawOval(x, y, diametro, diametro);
        }

        desenharLegenda(g2d);
        
        // Desenha o Dashboard final apenas se as 3 rodadas tiverem acabado
        if (mostrarTelaFinal) {
            desenharTelaFinal(g2d);
        }

        Toolkit.getDefaultToolkit().sync();
    }
}