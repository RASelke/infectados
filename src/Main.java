import javax.swing.JFrame;
import visual.Arena;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

public class Main {
    public static void main(String[] args) {
        JFrame janela = new JFrame("Simulador de Infecção - Modelo SIR");
        janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // 1. Remove a barra de título da janela
        
        // 2. Captura o monitor principal do seu sistema
        GraphicsDevice monitor = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        
        monitor.setFullScreenWindow(janela);
        
        Arena arena = new Arena(50,50,30);
        janela.add(arena);
        
        janela.setVisible(true);
    }
}