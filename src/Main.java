import javax.swing.JFrame;
import visual.Arena;

public class Main {
    public static void main(String[] args) {
        JFrame janela = new JFrame("INFECTADOS");
        
        janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        janela.setSize(800, 600);
        
        janela.setResizable(false);
        
        janela.setLocationRelativeTo(null); 

        Arena arena = new Arena();
        
        janela.add(arena);

        janela.setVisible(true);
    }
}