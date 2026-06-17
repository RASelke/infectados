package visual;

import java.util.concurrent.CopyOnWriteArrayList;
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
import java.util.Queue;
import java.io.FileWriter;
import java.io.PrintWriter;
import models.ConfigTeste;

public class Arena extends JPanel {
    private List<Pessoa> populacao;
    private Timer timer;
    private List<ResultadoRodada> historicoResultados;
    private ResultadoRodada resultadoMediano; 
    
    public static final Dimension TELA = Toolkit.getDefaultToolkit().getScreenSize();
    public static final int LARGURA = (int) TELA.getWidth();
    public static final int ALTURA = (int) TELA.getHeight();

    private int inicialVacinados, inicialNaoVacinados, inicialInfectados;
    private int totalRodadas, rodadaAtual = 1;
    private int margem, tamanhoPessoa;
    private double velMax;
    
    private Queue<ConfigTeste> filaTestes;
    private ConfigTeste testeAtual;

    // --- MÁQUINA DE ESTADOS PARA O VÍDEO ---
    private enum Fase { SIMULANDO, PAUSA_MEDIANA, FIM_GLOBAL }
    private Fase faseAtual = Fase.SIMULANDO;
    private int contadorPausa = 0; 
    
    // Lista de Arrays de String: [0] = Linha de Atributos, [1] = Linha de Resultados (CSV)
    private List<String[]> relatorioGlobal; 
    
    private boolean modoVisual;

    private class ResultadoRodada {
        int mortosVac = 0, recVac = 0, ilesosVac = 0;
        int mortosNaoVac = 0, recNaoVac = 0, ilesosNaoVac = 0;	
        int totalMortos = 0; 
    }

    public Arena(Queue<ConfigTeste> filaTestes, boolean modoVisual) {
        this.filaTestes = filaTestes;
        this.modoVisual = modoVisual;
        
        populacao = new java.util.concurrent.CopyOnWriteArrayList<>();
        historicoResultados = new ArrayList<>();
        relatorioGlobal = new ArrayList<>();

        carregarProximoTeste();
        if (this.modoVisual) {
	        timer = new Timer(1000 / 60, e -> {
	            atualizarFrame();
	            repaint();
	        });
	        timer.start();
        }
    }
    
 // --- NOVO MÉTODO PARA RODAR sem VISUAL ---
    public void iniciarModoDesempenhoMaximo() {
        new Thread(() -> {
            while (faseAtual != Fase.FIM_GLOBAL) {
                atualizarFrame(); // Roda a física sem intervalo de tempo (sem limite de FPS)
            }
            repaint(); // Só desenha a tela UMA VEZ no final de tudo!
        }).start();
    }

    private void carregarProximoTeste() {
        if (filaTestes.isEmpty()) {
            faseAtual = Fase.FIM_GLOBAL;
            if(timer != null) timer.stop();
            repaint();
            return;
        }

        testeAtual = filaTestes.poll();
        faseAtual = Fase.SIMULANDO;

        this.inicialVacinados = testeAtual.vacinados;
        this.inicialNaoVacinados = testeAtual.naoVacinados;
        this.inicialInfectados = testeAtual.infectados;
        this.totalRodadas = testeAtual.rodadas;
        this.margem = testeAtual.margem;
        this.tamanhoPessoa = testeAtual.tamanhoPessoa;
        this.velMax = testeAtual.velMax;

        this.rodadaAtual = 1;
        this.historicoResultados.clear();
        this.populacao.clear();

        if (timer != null) {
            timer.setDelay(1000 / testeAtual.fps);
        }

        popularArena(inicialVacinados, inicialNaoVacinados, inicialInfectados);
    }
    
    public void popularArena(int qtdVacinados, int qtdNaoVacinados, int qtdInfectados) {
        for (int i = 0; i < qtdInfectados; i++) {
            Vetor2D posInicial = new Vetor2D(LARGURA / 2.0, ALTURA / 2.0); 
            Vetor2D velInicial = new Vetor2D((Math.random() * velMax * 2) - velMax, (Math.random() * velMax * 2) - velMax); 
            populacao.add(new Pessoa(tamanhoPessoa, posInicial, velInicial, false, EstadoSaude.INFECTADO, true));
        }

        for (int i = 0; i < qtdVacinados; i++) {
            Vetor2D posInicial = new Vetor2D(Math.random() * (LARGURA-40)+20, Math.random() * (ALTURA-40)+20);
            Vetor2D velInicial = new Vetor2D((Math.random() * velMax * 2) - velMax, (Math.random() * velMax * 2) - velMax);
            populacao.add(new Pessoa(tamanhoPessoa, posInicial, velInicial, true, EstadoSaude.SUSCETIVEL, false));
        }
        
        for (int i = 0; i < qtdNaoVacinados; i++) {
            Vetor2D posInicial = new Vetor2D(Math.random() * (LARGURA-40)+20, Math.random() * (ALTURA-40)+20);
            Vetor2D velInicial = new Vetor2D((Math.random() * velMax * 2) - velMax, (Math.random() * velMax * 2) - velMax);
            populacao.add(new Pessoa(tamanhoPessoa, posInicial, velInicial, false, EstadoSaude.SUSCETIVEL, false));
        }
    }

    public void atualizarFrame() {
        if (faseAtual == Fase.PAUSA_MEDIANA) {
            contadorPausa--;
            if (contadorPausa <= 0) {
                carregarProximoTeste(); 
            }
            return; 
        }

        if (faseAtual == Fase.FIM_GLOBAL) return;

        for (Pessoa p : populacao) {
            p.atualizar(LARGURA, ALTURA, this.margem);
        }

        for (int i = 0; i < populacao.size(); i++) {
            for (int j = i + 1; j < populacao.size(); j++) {
                Pessoa p1 = populacao.get(i);
                Pessoa p2 = populacao.get(j);
                
                if (p1.getEstado() == EstadoSaude.MORTO || p2.getEstado() == EstadoSaude.MORTO) continue; 

                if (p1.verificaColisao(p2)) {
                    p1.interagir(p2);
                    p2.interagir(p1);
                    p1.resolverColisaoFisica(p2);
                }
            }
        }

        if (verificarFimDeJogo()) {
            salvarEstatisticasDaRodada();
            
            if (rodadaAtual < totalRodadas) {
                rodadaAtual++;
                populacao.clear(); 
                popularArena(inicialVacinados, inicialNaoVacinados, inicialInfectados);
            } else {
                prepararResultadoIntermediario();
                exportarParaCSV();
                guardarNoRelatorioGlobal();
                
                if (modoVisual) {
                    faseAtual = Fase.PAUSA_MEDIANA;
                    contadorPausa = testeAtual.fps * 5; 
                } else {
                    carregarProximoTeste();
                }
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
            if (p.isPacienteZero()) continue;
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
        imprimirRodadaNoTerminal(r, rodadaAtual);
    }

    private void prepararResultadoIntermediario() {
        historicoResultados.sort((r1, r2) -> Integer.compare(r1.totalMortos, r2.totalMortos));
        resultadoMediano = historicoResultados.get(historicoResultados.size() / 2); 
    }

    private void exportarParaCSV() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("resultados_saida.csv", true))) {
            writer.printf("%d;%d;%d;%d;%d;%d;%d;%d;%d;%d\n", testeAtual.id, inicialVacinados, inicialNaoVacinados, inicialInfectados,
                resultadoMediano.ilesosVac, resultadoMediano.recVac, resultadoMediano.mortosVac,
                resultadoMediano.ilesosNaoVac, resultadoMediano.recNaoVac, resultadoMediano.mortosNaoVac);
        } catch (Exception e) {}
    }

    private void imprimirRodadaNoTerminal(ResultadoRodada r, int numeroRodada) {
        System.out.println("TESTE ID: " + testeAtual.id + " | Rodada: " + numeroRodada + " finalizada. Total óbitos: " + r.totalMortos);
    }
    
    private void guardarNoRelatorioGlobal() {
        // Linha 1: Focada nos atributos e parâmetros (Design mais discreto)
        String linhaAtributos = String.format("[TESTE #%d] Atributos -> População Inicial: %d Vac | %d Ñ-Vac | %d PZ  ||  Parâmetros: %d Rodadas | FPS: %d | Vel: %.1f | Marg: %d | Tam: %d", 
                testeAtual.id, inicialVacinados, inicialNaoVacinados, inicialInfectados, totalRodadas, testeAtual.fps, velMax, margem, tamanhoPessoa);
        
        // Linha 2: Focada no espelho do CSV (Design em negrito e chamativo)
        String linhaResultados = String.format("   ↳ MEDIANA (CSV) -> VACINADOS: %d Ilesos, %d Recup, %d Óbitos  |  NÃO-VACINADOS: %d Ilesos, %d Recup, %d Óbitos", 
                resultadoMediano.ilesosVac, resultadoMediano.recVac, resultadoMediano.mortosVac, 
                resultadoMediano.ilesosNaoVac, resultadoMediano.recNaoVac, resultadoMediano.mortosNaoVac);
        
        relatorioGlobal.add(new String[]{linhaAtributos, linhaResultados});
    }

    // --- AS NOSSAS TELAS VISUAIS ---

    private void desenharHUD(Graphics2D g2d) {
        int suscetiveis = 0, infectados = 0, recuperados = 0, mortos = 0;
        for (Pessoa p : populacao) {
            switch (p.getEstado()) {
                case SUSCETIVEL: suscetiveis++; break;
                case INFECTADO: infectados++; break;
                case RECUPERADO: recuperados++; break;
                case MORTO: mortos++; break;
            }
        }
        
        // Aumentamos a altura do HUD para 460 para caber os parâmetros!
        int x = 30, y = 30, larguraHUD = 260, alturaHUD = 460;
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.85f));
        g2d.setColor(new Color(30, 34, 40)); 
        g2d.fillRoundRect(x, y, larguraHUD, alturaHUD, 20, 20);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)); 
        g2d.setColor(new Color(0, 255, 225)); 
        g2d.drawRoundRect(x, y, larguraHUD, alturaHUD, 20, 20);

        // --- CABEÇALHO ---
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.setColor(Color.LIGHT_GRAY);
        String idTeste = (testeAtual != null) ? String.valueOf(testeAtual.id) : "?";
        g2d.drawString("TESTE ATUAL: #" + idTeste, x + 20, y + 30);
        
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.setColor(Color.YELLOW);
        g2d.drawString("RODADA " + rodadaAtual + " / " + totalRodadas, x + 20, y + 55);
        
        g2d.setColor(Color.GRAY);
        g2d.drawLine(x + 20, y + 70, x + larguraHUD - 20, y + 70);

        // --- ESTATÍSTICAS AO VIVO ---
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.setColor(Color.WHITE);
        g2d.drawString("ESTATÍSTICAS AO VIVO", x + 20, y + 95);

        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        int espacamento = 28, linhaAtual = y + 130;

        g2d.setColor(new Color(0, 150, 255));
        g2d.fillOval(x + 20, linhaAtual - 11, 12, 12);
        g2d.setColor(Color.WHITE);
        g2d.drawString("Suscetíveis: " + suscetiveis, x + 40, linhaAtual);
        linhaAtual += espacamento;

        g2d.setColor(new Color(255, 50, 50));
        g2d.fillOval(x + 20, linhaAtual - 11, 12, 12);
        g2d.setColor(Color.WHITE);
        g2d.drawString("Infectados: " + infectados, x + 40, linhaAtual);
        linhaAtual += espacamento;

        g2d.setColor(new Color(150, 150, 150));
        g2d.fillOval(x + 20, linhaAtual - 11, 12, 12);
        g2d.setColor(Color.WHITE);
        g2d.drawString("Recuperados: " + recuperados, x + 40, linhaAtual);
        linhaAtual += espacamento;

        g2d.setColor(new Color(100, 0, 0));
        g2d.fillOval(x + 20, linhaAtual - 11, 12, 12);
        g2d.setColor(Color.WHITE);
        g2d.drawString("Óbitos: " + mortos, x + 40, linhaAtual);
        
        linhaAtual += 35;
        g2d.setFont(new Font("Arial", Font.ITALIC, 12));
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawString("População Total: " + (suscetiveis + infectados + recuperados + mortos), x + 20, linhaAtual);

        // --- NOVA SEÇÃO: PARÂMETROS DO TESTE ---
        linhaAtual += 20;
        g2d.setColor(Color.GRAY);
        g2d.drawLine(x + 20, linhaAtual, x + larguraHUD - 20, linhaAtual);
        
        linhaAtual += 25;
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.setColor(Color.WHITE);
        g2d.drawString("PARÂMETROS APLICADOS", x + 20, linhaAtual);
        
        linhaAtual += 25;
        g2d.setFont(new Font("Consolas", Font.PLAIN, 12)); // Fonte estilo "código" para os dados
        g2d.setColor(new Color(200, 200, 200));
        g2d.drawString("Vac. Iniciais: " + inicialVacinados, x + 20, linhaAtual);
        linhaAtual += 20;
        g2d.drawString("Não-Vac. Inic: " + inicialNaoVacinados, x + 20, linhaAtual);
        linhaAtual += 20;
        g2d.drawString("Paciente Zero: " + inicialInfectados, x + 20, linhaAtual);
        linhaAtual += 20;
        
        String fpsStr = (testeAtual != null) ? String.valueOf(testeAtual.fps) : "?";
        g2d.drawString("Velocidade: " + velMax + " | FPS: " + fpsStr, x + 20, linhaAtual);
        linhaAtual += 20;
        g2d.drawString("Tam(Raio): " + tamanhoPessoa + "  | Marg: " + margem, x + 20, linhaAtual);
    }

    private void desenharTelaMediana(Graphics2D g2d) {
    	if (resultadoMediano == null) return;
    	
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, LARGURA, ALTURA);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

        int painelLargura = 550, painelAltura = 450; 
        int x = (LARGURA - painelLargura) / 2;
        int y = (ALTURA - painelAltura) / 2;

        g2d.setColor(new Color(40, 44, 52));
        g2d.fillRoundRect(x, y, painelLargura, painelAltura, 30, 30);
        g2d.setColor(new Color(0, 255, 225));
        g2d.drawRoundRect(x, y, painelLargura, painelAltura, 30, 30);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString("RESULTADOS DO TESTE #" + testeAtual.id, x + 130, y + 50);

        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        
        g2d.setColor(Color.CYAN);
        g2d.drawString("PÚBLICO VACINADO (" + inicialVacinados + ")", x + 40, y + 110);
        g2d.setColor(Color.WHITE);
        g2d.drawString("- Ilesos: " + resultadoMediano.ilesosVac, x + 40, y + 140);
        g2d.drawString("- Recuperados: " + resultadoMediano.recVac, x + 40, y + 170);
        g2d.setColor(new Color(255, 100, 100)); 
        g2d.drawString("- Óbitos: " + resultadoMediano.mortosVac, x + 40, y + 200);

        g2d.setColor(Color.BLUE);
        g2d.drawString("NÃO VACINADOS (" + inicialNaoVacinados + ")", x + 300, y + 110);
        g2d.setColor(Color.WHITE);
        g2d.drawString("- Ilesos: " + resultadoMediano.ilesosNaoVac, x + 300, y + 140);
        g2d.drawString("- Recuperados: " + resultadoMediano.recNaoVac, x + 300, y + 170);
        g2d.setColor(new Color(255, 100, 100));
        g2d.drawString("- Óbitos: " + resultadoMediano.mortosNaoVac, x + 300, y + 200);

        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawLine(x + 40, y + 240, x + 510, y + 240);
        g2d.setColor(Color.WHITE);
        g2d.drawString("Origem: " + inicialInfectados + " Paciente(s) Zero excluído(s) da conta.", x + 80, y + 280);
        
        int segundosRestantes = (contadorPausa / testeAtual.fps) + 1;
        g2d.setColor(Color.YELLOW);
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.drawString("Iniciando próxima bateria em " + segundosRestantes + "s...", x + 110, y + 360);
    }

    private void desenharTelaGlobal(Graphics2D g2d) {
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.85f));
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, LARGURA, ALTURA);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

        int painelLargura = 980; // Painel bem mais largo para caber todas as informações
        int painelAltura = 150 + (relatorioGlobal.size() * 60); 
        int x = (LARGURA - painelLargura) / 2;
        int y = (ALTURA - painelAltura) / 2;

        g2d.setColor(new Color(20, 20, 30));
        g2d.fillRoundRect(x, y, painelLargura, painelAltura, 30, 30);
        g2d.setColor(new Color(0, 255, 150)); // Borda verde fluorescente
        g2d.drawRoundRect(x, y, painelLargura, painelAltura, 30, 30);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        g2d.drawString("BATERIA DE TESTES CONCLUÍDA - RELATÓRIO DE EXPORTAÇÃO (CSV)", x + 60, y + 45);

        g2d.setColor(Color.DARK_GRAY);
        g2d.drawLine(x + 30, y + 65, x + painelLargura - 30, y + 65);

        int linhaY = y + 100;
        
        for (String[] linhas : relatorioGlobal) {
            // Linha de Atributos: Cinza, fonte de código e mais discreta
            g2d.setFont(new Font("Consolas", Font.PLAIN, 14));
            g2d.setColor(new Color(180, 180, 180));
            g2d.drawString(linhas[0], x + 40, linhaY);
            linhaY += 20;

            // Linha de Resultados: Negrito, azulada e espelho exato do CSV
            g2d.setFont(new Font("Arial", Font.BOLD, 15));
            g2d.setColor(new Color(220, 240, 255));
            g2d.drawString(linhas[1], x + 40, linhaY);
            linhaY += 40; 
        }

        g2d.setColor(Color.DARK_GRAY);
        g2d.drawLine(x + 30, linhaY - 15, x + painelLargura - 30, linhaY - 15);

        g2d.setColor(Color.YELLOW);
        g2d.setFont(new Font("Arial", Font.ITALIC, 16));
        g2d.drawString("Vídeo e simulação finalizados com sucesso. Pressione ALT+F4 para sair.", x + 220, linhaY + 20);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        if (!modoVisual && faseAtual != Fase.FIM_GLOBAL) {
            g2d.setColor(new Color(20, 20, 30));
            g2d.fillRect(0, 0, LARGURA, ALTURA);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 24));
            g2d.drawString("PROCESSANDO BATERIA DE TESTES EM MODO DE DESEMPENHO MÁXIMO...", LARGURA / 2 - 400, ALTURA / 2);
            return; // Abandona o método de pintura para poupar processamento
        }
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        g2d.setColor(new Color(245, 245, 245));
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        g2d.setColor(Color.LIGHT_GRAY); 
        g2d.drawRect(this.margem , this.margem, LARGURA - (this.margem * 2), ALTURA - (this.margem * 2));

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
                case INFECTADO: { corPreenchimento = new Color(255, 0, 0); corBorda = new Color(128, 0, 0); break; }
                case RECUPERADO: { corPreenchimento = new Color(128, 128, 128); corBorda = new Color(169, 169, 169); break; }
                default: break;
            }
            int raio = this.tamanhoPessoa;
            int x = (int) p.getPosicao().getX() - raio;
            int y = (int) p.getPosicao().getY() - raio;
            int diametro = raio * 2;
            g2d.setColor(corPreenchimento);
            g2d.fillOval(x, y, diametro, diametro);
            g2d.setColor(corBorda);
            g2d.drawOval(x, y, diametro, diametro);
        }

        if (faseAtual == Fase.SIMULANDO) {
            desenharHUD(g2d);
        } else if (faseAtual == Fase.PAUSA_MEDIANA) {
            desenharTelaMediana(g2d);
        } else if (faseAtual == Fase.FIM_GLOBAL) {
            desenharTelaGlobal(g2d);
        }

        Toolkit.getDefaultToolkit().sync();
    }
}