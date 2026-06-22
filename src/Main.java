import javax.swing.JFrame;
import visual.Arena;
import models.ConfigTeste;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Queue;

public class Main {
    public static void main(String[] args) {
        
        // =========================================================
        // CHAVE SELETORA:
        // true  -> Desenha tudo.
        // false -> Tela preta, ignora FPS, roda tudo na velocidade da luz.
        boolean modoVisual = false; 
        // =========================================================

        try (PrintWriter writer = new PrintWriter(new FileWriter("resultados_saida.csv", false))) {
            writer.println("ID_Teste;Vacinados_Iniciais;Nao_Vacinados;Infectados;Vac_Ilesos;Vac_Recup;Vac_Mortos;NaoVac_Ilesos;NaoVac_Recup;NaoVac_Mortos;Minimo_Mortos;Media_Mortos;Maximo_Mortos");
        } catch (Exception e) {
            System.out.println("Erro ao criar arquivo de saída.");
        }

        Queue<ConfigTeste> filaTestes = new LinkedList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("testes.csv"))) {
            String linha = br.readLine(); 
            while ((linha = br.readLine()) != null) {
                String[] dados = linha.split(";");
                if (dados.length == 9) {
                    filaTestes.add(new ConfigTeste(
                        Integer.parseInt(dados[0]), Integer.parseInt(dados[1]), 
                        Integer.parseInt(dados[2]), Integer.parseInt(dados[3]), 
                        Integer.parseInt(dados[4]), Integer.parseInt(dados[5]), 
                        Integer.parseInt(dados[6]), Integer.parseInt(dados[7]), 
                        Double.parseDouble(dados[8])
                    ));
                }
            }
        } catch (Exception e) {}
        
        if (filaTestes.isEmpty()) {
            filaTestes.add(new ConfigTeste(1, 20, 20, 10, 3, 60, 20, 10, 5.0));
        }

        JFrame janela = new JFrame("Simulador de Infecção - Modelo SIR");
        janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        janela.setUndecorated(true);
        GraphicsDevice monitor = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        monitor.setFullScreenWindow(janela);
        
        // Passamos a chave seletora para a Arena!
        Arena arena = new Arena(filaTestes, modoVisual);
        janela.add(arena);
        janela.setVisible(true);
        
        // Se NÃO estivermos no modo visual, acionamos a turbina!
        if (!modoVisual) {
            arena.iniciarModoDesempenhoMaximo();
        }
    }
}