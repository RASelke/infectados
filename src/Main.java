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
        // 1. Prepara o arquivo de SAÍDA (escreve o cabeçalho e limpa testes antigos)
        try (PrintWriter writer = new PrintWriter(new FileWriter("resultados_saida.csv", false))) {
            writer.println("ID_Teste;Vacinados_Iniciais;Nao_Vacinados;Infectados;Vac_Ilesos;Vac_Recup;Vac_Mortos;NaoVac_Ilesos;NaoVac_Recup;NaoVac_Mortos");
        } catch (Exception e) {
            System.out.println("Erro ao criar arquivo de saída.");
        }

        // 2. Lê o arquivo de ENTRADA e monta a Fila de Testes
        Queue<ConfigTeste> filaTestes = new LinkedList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("testes.csv"))) {
            String linha = br.readLine(); // Pula a primeira linha (cabeçalho)
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
        } catch (Exception e) {
            System.out.println("Erro na leitura do arquivo testes.csv.");
        }
        
        if (filaTestes.isEmpty()) {
            System.out.println("Nenhum teste válido encontrado. Adicionando teste padrão de emergência.");
            filaTestes.add(new ConfigTeste(1, 20, 20, 10, 3, 60, 20, 10, 5.0));
        }

        // 3. Inicia a Interface Gráfica
        JFrame janela = new JFrame("Simulador de Infecção - Modelo SIR");
        janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        janela.setUndecorated(true);
        GraphicsDevice monitor = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        monitor.setFullScreenWindow(janela);
        
        // Passamos a FILA inteira para a Arena processar!
        Arena arena = new Arena(filaTestes);
        janela.add(arena);
        janela.setVisible(true);
    }
}