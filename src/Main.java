import javax.swing.JFrame;
import visual.Arena;

public class Main {
    public static void main(String[] args) {
        JFrame janela = new JFrame("Simulador de Infecção - Modelo SIR");
        janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // 1. Remove a barra de título (o "X" de fechar, minimizar, etc)
        janela.setUndecorated(true);
        
        // 2. Avisa o Windows para maximizar o programa cobrindo toda a tela
        janela.setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        Arena arena = new Arena(130,130,130);
        janela.add(arena);
        
        janela.setVisible(true);
    }
}