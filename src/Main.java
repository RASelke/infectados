import javax.swing.JFrame;
import visual.Arena;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

public class Main {
    public static void main(String[] args) {
        JFrame janela = new JFrame("Simulador de Infecção - Modelo SIR");
        
        janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
        GraphicsDevice monitor = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        
        monitor.setFullScreenWindow(janela);
        
        Arena arena = new Arena(140,130,115);
        janela.add(arena);
        
        janela.setVisible(true);
    }
}