import javax.swing.JFrame;
import visual.Arena;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

public class Main {
    public static void main(String[] args) {
    	int vacinados = 20;
        int naoVacinados = 20;
        int infectados = 10;
        int rodadas = 3;        // Quantidade de testes antes de dar a mediana
        int fps = 60;           // Velocidade da simulação (60 é o padrão fluido)
        int margem = 20;        // Distância do muro para as bordas da tela
        int tamanhoPessoa = 10; // Raio das bolinhas
        double velMax = 5.0;    // Velocidade máxima em pixels por frame
    	
        JFrame janela = new JFrame("Simulador de Infecção - Modelo SIR");
        
        janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
        GraphicsDevice monitor = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        
        monitor.setFullScreenWindow(janela);
        
        Arena arena = new Arena(vacinados, naoVacinados, infectados, rodadas, fps, margem, tamanhoPessoa, velMax);
        janela.add(arena);
        
        janela.setVisible(true);
    }
}