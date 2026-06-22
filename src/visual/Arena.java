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
import java.util.Queue;
import java.io.FileWriter;
import java.io.PrintWriter;
import models.ConfigTeste;

public class Arena extends JPanel {
    private List<Pessoa> populacao;
    private Timer timer;
    private List<ResultadoRodada> historicoResultados;
    private ResultadoConsolidado resultadoAtual; 
    
    public static final Dimension TELA = Toolkit.getDefaultToolkit().getScreenSize();
    public static final int LARGURA = (int) TELA.getWidth();
    public static final int ALTURA = (int) TELA.getHeight();

    private int inicialVacinados, inicialNaoVacinados, inicialInfectados;
    private int totalRodadas, rodadaAtual = 1;
    private int margem, tamanhoPessoa;
    private double velMax;
    
    private Queue<ConfigTeste> filaTestes;
    private ConfigTeste testeAtual;

    // --- MÁQUINA DE ESTADOS ATUALIZADA (COM TRANSIÇÃO) ---
    private enum Fase { SIMULANDO, TRANSIÇÃO_RODADA, PAUSA_MEDIANA, FIM_GLOBAL }
    private Fase faseAtual = Fase.SIMULANDO;
    
    private int contadorPausa = 0; 
    private int contadorTransicao = 0;
    private int maxTransicao = 0;
    
    private boolean modoVisual;
    private List<ResumoPlanilha> relatorioGlobal; 

    private class ResultadoRodada {
        int mortosVac = 0, recVac = 0, ilesosVac = 0;
        int mortosNaoVac = 0, recNaoVac = 0, ilesosNaoVac = 0;	
        int totalMortos = 0; 
    }

    private class ResultadoConsolidado {
        int ilesosVac, recVac, mortosVac;
        int ilesosNaoVac, recNaoVac, mortosNaoVac;
        int minMortos = Integer.MAX_VALUE;
        int maxMortos = Integer.MIN_VALUE;
        int mediaMortos = 0;
    }

    private class ResumoPlanilha {
        int id, popTotal, vacinados, naoVacinados, min, max, media;
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

    public void iniciarModoDesempenhoMaximo() {
        new Thread(() -> {
            while (faseAtual != Fase.FIM_GLOBAL) {
                atualizarFrame(); 
            }
            repaint(); 
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
        if (testeAtual == null) {
            faseAtual = Fase.FIM_GLOBAL;
            return;
        }

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
        
        // NOVA FASE: A Animação de Transição
        if (faseAtual == Fase.TRANSIÇÃO_RODADA) {
            contadorTransicao--;
            int meioTempo = maxTransicao / 2;
            
            // Exatamente na metade da animação (quando a tela está 100% escura), trocamos as bolinhas de lugar
            if (contadorTransicao == meioTempo) {
                rodadaAtual++;
                populacao.clear(); 
                popularArena(inicialVacinados, inicialNaoVacinados, inicialInfectados);
            } else if (contadorTransicao <= 0) {
                faseAtual = Fase.SIMULANDO; // A tela clareou, volta a rodar a simulação
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
                if (modoVisual) {
                    // Prepara a transição de Fade de 2 segundos (1s escurecendo, 1s clareando)
                    faseAtual = Fase.TRANSIÇÃO_RODADA;
                    maxTransicao = testeAtual.fps * 2; 
                    contadorTransicao = maxTransicao;
                } else {
                    // Modo rápido ignora a animação
                    rodadaAtual++;
                    populacao.clear(); 
                    popularArena(inicialVacinados, inicialNaoVacinados, inicialInfectados);
                }
            } else {
                consolidarResultadosDoTeste();
                exportarParaCSV();
                guardarNoRelatorioGlobal();
                
                if (modoVisual) {
                    faseAtual = Fase.PAUSA_MEDIANA ;
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
        System.out.println("TESTE #" + testeAtual.id + " | Rodada " + rodadaAtual + " terminada. Óbitos: " + r.totalMortos);
    }

    private void consolidarResultadosDoTeste() {
        resultadoAtual = new ResultadoConsolidado();
        int somaMortos = 0;

        for (ResultadoRodada r : historicoResultados) {
            resultadoAtual.ilesosVac += r.ilesosVac;
            resultadoAtual.recVac += r.recVac;
            resultadoAtual.mortosVac += r.mortosVac;
            
            resultadoAtual.ilesosNaoVac += r.ilesosNaoVac;
            resultadoAtual.recNaoVac += r.recNaoVac;
            resultadoAtual.mortosNaoVac += r.mortosNaoVac;
            
            somaMortos += r.totalMortos;
            if (r.totalMortos < resultadoAtual.minMortos) resultadoAtual.minMortos = r.totalMortos;
            if (r.totalMortos > resultadoAtual.maxMortos) resultadoAtual.maxMortos = r.totalMortos;
        }

        int qtd = historicoResultados.size();
        resultadoAtual.ilesosVac /= qtd;
        resultadoAtual.recVac /= qtd;
        resultadoAtual.mortosVac /= qtd;
        resultadoAtual.ilesosNaoVac /= qtd;
        resultadoAtual.recNaoVac /= qtd;
        resultadoAtual.mortosNaoVac /= qtd;
        resultadoAtual.mediaMortos = somaMortos / qtd;
    }

    private void exportarParaCSV() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("resultados_saida.csv", true))) {
            writer.printf("%d;%d;%d;%d;%d;%d;%d;%d;%d;%d;%d;%d;%d\n", testeAtual.id, inicialVacinados, inicialNaoVacinados, inicialInfectados,
                resultadoAtual.ilesosVac, resultadoAtual.recVac, resultadoAtual.mortosVac,
                resultadoAtual.ilesosNaoVac, resultadoAtual.recNaoVac, resultadoAtual.mortosNaoVac,
                resultadoAtual.minMortos, resultadoAtual.mediaMortos, resultadoAtual.maxMortos);
        } catch (Exception e) {}
    }
    
    private void guardarNoRelatorioGlobal() {
        ResumoPlanilha res = new ResumoPlanilha();
        res.id = testeAtual.id;
        res.popTotal = inicialVacinados + inicialNaoVacinados;
        res.vacinados = inicialVacinados;
        res.naoVacinados = inicialNaoVacinados;
        res.min = resultadoAtual.minMortos;
        res.media = resultadoAtual.mediaMortos;
        res.max = resultadoAtual.maxMortos;
        relatorioGlobal.add(res);
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
        
        int x = 30, y = 30, larguraHUD = 260, alturaHUD = 460;
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.85f));
        g2d.setColor(new Color(30, 34, 40)); 
        g2d.fillRoundRect(x, y, larguraHUD, alturaHUD, 20, 20);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)); 
        g2d.setColor(new Color(0, 255, 225)); 
        g2d.drawRoundRect(x, y, larguraHUD, alturaHUD, 20, 20);

        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.setColor(Color.LIGHT_GRAY);
        String idTeste = (testeAtual != null) ? String.valueOf(testeAtual.id) : "?";
        g2d.drawString("TESTE ATUAL: #" + idTeste, x + 20, y + 30);
        
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.setColor(Color.YELLOW);
        g2d.drawString("RODADA " + rodadaAtual + " / " + totalRodadas, x + 20, y + 55);
        
        g2d.setColor(Color.GRAY);
        g2d.drawLine(x + 20, y + 70, x + larguraHUD - 20, y + 70);

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

        linhaAtual += 20;
        g2d.setColor(Color.GRAY);
        g2d.drawLine(x + 20, linhaAtual, x + larguraHUD - 20, linhaAtual);
        
        linhaAtual += 25;
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.setColor(Color.WHITE);
        g2d.drawString("PARÂMETROS APLICADOS", x + 20, linhaAtual);
        
        linhaAtual += 25;
        g2d.setFont(new Font("Consolas", Font.PLAIN, 12)); 
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
    	if (resultadoAtual == null) return;
    	
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, LARGURA, ALTURA);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

        int painelLargura = 600, painelAltura = 480; 
        int x = (LARGURA - painelLargura) / 2;
        int y = (ALTURA - painelAltura) / 2;

        g2d.setColor(new Color(40, 44, 52));
        g2d.fillRoundRect(x, y, painelLargura, painelAltura, 30, 30);
        g2d.setColor(new Color(0, 255, 225));
        g2d.drawRoundRect(x, y, painelLargura, painelAltura, 30, 30);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString("RESULTADOS MÉDIOS DO TESTE #" + testeAtual.id, x + 100, y + 50);

        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        
        g2d.setColor(Color.CYAN);
        g2d.drawString("PÚBLICO VACINADO (" + inicialVacinados + ")", x + 40, y + 110);
        g2d.setColor(Color.WHITE);
        g2d.drawString("- Ilesos: " + resultadoAtual.ilesosVac, x + 40, y + 140);
        g2d.drawString("- Recuperados: " + resultadoAtual.recVac, x + 40, y + 170);
        g2d.setColor(new Color(255, 100, 100)); 
        g2d.drawString("- Óbitos: " + resultadoAtual.mortosVac, x + 40, y + 200);

        g2d.setColor(Color.BLUE);
        g2d.drawString("NÃO VACINADOS (" + inicialNaoVacinados + ")", x + 300, y + 110);
        g2d.setColor(Color.WHITE);
        g2d.drawString("- Ilesos: " + resultadoAtual.ilesosNaoVac, x + 300, y + 140);
        g2d.drawString("- Recuperados: " + resultadoAtual.recNaoVac, x + 300, y + 170);
        g2d.setColor(new Color(255, 100, 100));
        g2d.drawString("- Óbitos: " + resultadoAtual.mortosNaoVac, x + 300, y + 200);

        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawLine(x + 40, y + 240, x + 560, y + 240);
        
        g2d.setColor(Color.ORANGE);
        g2d.setFont(new Font("Arial", Font.BOLD, 15));
        g2d.drawString(String.format("VARIAÇÃO DE ÓBITOS: Mínimo (%d) | Média (%d) | Máximo (%d)", 
                resultadoAtual.minMortos, resultadoAtual.mediaMortos, resultadoAtual.maxMortos), x + 45, y + 280);
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        g2d.drawString("Origem: " + inicialInfectados + " Paciente(s) Zero excluído(s) da conta.", x + 100, y + 320);
        
        int segundosRestantes = (contadorPausa / testeAtual.fps) + 1;
        g2d.setColor(Color.YELLOW);
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        
        // A MUDANÇA DE MENSAGEM: Se a fila estiver vazia, avisa que são os resultados finais
        String mensagemExibicao = filaTestes.isEmpty() 
            ? "Iniciando resultados finais em " + segundosRestantes + "s..." 
            : "Iniciando próxima bateria em " + segundosRestantes + "s...";
            
        g2d.drawString(mensagemExibicao, x + 110, y + 400);
    }

    private void desenharTelaGlobal(Graphics2D g2d) {
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.85f));
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, LARGURA, ALTURA);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

        int limiteVisuais = Math.min(relatorioGlobal.size(), 15);
        int startIndex = relatorioGlobal.size() - limiteVisuais;

        int painelLargura = 920; 
        int painelAltura = 220 + (limiteVisuais * 32); 
        int x = (LARGURA - painelLargura) / 2;
        int y = (ALTURA - painelAltura) / 2;

        g2d.setColor(new Color(25, 28, 36)); 
        g2d.fillRoundRect(x, y, painelLargura, painelAltura, 30, 30);
        g2d.setColor(new Color(0, 255, 150)); 
        g2d.drawRoundRect(x, y, painelLargura, painelAltura, 30, 30);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 26));
        g2d.drawString("CONSOLIDAÇÃO DA BATERIA DE TESTES", x + 190, y + 45);

        if (relatorioGlobal.size() > 15) {
            g2d.setFont(new Font("Arial", Font.ITALIC, 14));
            g2d.setColor(new Color(255, 165, 0)); 
            g2d.drawString("(Mostrando últimos 15 testes. Consulte o arquivo CSV para acessar os " + relatorioGlobal.size() + " totais)", x + 190, y + 70);
        }

        int[] colX = { x + 40, x + 140, x + 270, x + 400, x + 530, x + 660, x + 790 };
        String[] cabecalhos = { "TESTE", "POP. TOTAL", "VACINADOS", "NÃO-VAC", "ÓBITO (MÍN)", "ÓBITO (MÉD)", "ÓBITO (MÁX)" };

        int linhaY = y + 110;

        g2d.setColor(new Color(40, 44, 52));
        g2d.fillRect(x + 20, linhaY - 22, painelLargura - 40, 30);

        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.setColor(new Color(0, 255, 225));
        for (int c = 0; c < cabecalhos.length; c++) {
            g2d.drawString(cabecalhos[c], colX[c], linhaY);
        }

        linhaY += 32;

        g2d.setFont(new Font("Consolas", Font.BOLD, 15)); 
        
        for (int i = startIndex; i < relatorioGlobal.size(); i++) {
            ResumoPlanilha res = relatorioGlobal.get(i);
            
            if (i % 2 == 0) g2d.setColor(new Color(35, 39, 47));
            else g2d.setColor(new Color(25, 28, 36));
            g2d.fillRect(x + 20, linhaY - 22, painelLargura - 40, 32);
            
            g2d.setColor(Color.WHITE);
            g2d.drawString("#" + res.id, colX[0], linhaY);
            g2d.drawString(String.valueOf(res.popTotal), colX[1], linhaY);
            g2d.drawString(String.valueOf(res.vacinados), colX[2], linhaY);
            g2d.drawString(String.valueOf(res.naoVacinados), colX[3], linhaY);

            g2d.setColor(new Color(100, 255, 100)); 
            g2d.drawString(String.valueOf(res.min), colX[4], linhaY);
            
            g2d.setColor(new Color(255, 200, 100)); 
            g2d.drawString(String.valueOf(res.media), colX[5], linhaY);
            
            g2d.setColor(new Color(255, 100, 100)); 
            g2d.drawString(String.valueOf(res.max), colX[6], linhaY);
            
            linhaY += 32; 
        }

        g2d.setColor(Color.DARK_GRAY);
        g2d.drawLine(x + 30, linhaY - 10, x + painelLargura - 30, linhaY - 10);

        g2d.setColor(Color.YELLOW);
        g2d.setFont(new Font("Arial", Font.ITALIC, 16));
        g2d.drawString("Baterias de teste concluídas. Pressione ALT+F4 para sair.", x + 250, linhaY + 20);
        
        // A ASSINATURA FINAL DO GITHUB
        g2d.setColor(new Color(0, 200, 255));
        g2d.setFont(new Font("Consolas", Font.BOLD, 15));
        g2d.drawString("Repositório: github.com/RASelke/infectados", x + 270, linhaY + 50);
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
            return; 
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

        // Desenha os Painéis normais
        if (faseAtual == Fase.SIMULANDO || faseAtual == Fase.TRANSIÇÃO_RODADA) {
            desenharHUD(g2d);
        } else if (faseAtual == Fase.PAUSA_MEDIANA) {
            desenharTelaMediana(g2d);
        } else if (faseAtual == Fase.FIM_GLOBAL) {
            desenharTelaGlobal(g2d);
        }
        
        // A MÁGICA VISUAL: Película do Fade In / Fade Out aplicável por cima do jogo (mas sob o HUD)
        if (faseAtual == Fase.TRANSIÇÃO_RODADA) {
            float ratio = 0f;
            int meioTempo = maxTransicao / 2;
            
            if (contadorTransicao > meioTempo) {
                // Fade Out: A tela vai escurecendo de 0% a 100%
                ratio = 1.0f - ((float)(contadorTransicao - meioTempo) / meioTempo);
            } else {
                // Fade In: A tela clareia do 100% de volta a 0%
                ratio = (float)contadorTransicao / meioTempo;
            }
            
            // Garante que o Alpha nunca saia do limite permitido (0.0 até 1.0)
            ratio = Math.max(0f, Math.min(1f, ratio));
            
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, ratio));
            g2d.setColor(new Color(25, 28, 36)); // Usa a cor elegante do Dashboard para o fade
            g2d.fillRect(0, 0, LARGURA, ALTURA);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }

        Toolkit.getDefaultToolkit().sync();
    }
}