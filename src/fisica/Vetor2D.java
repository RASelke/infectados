package fisica;

public class Vetor2D {
    private double x, y;

    public Vetor2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double distanciaPara(Vetor2D outro) {
        double diferencaX = this.x - outro.getX();
        double diferencaY = this.y - outro.getY();
        
        return Math.sqrt(Math.pow(diferencaX, 2) + Math.pow(diferencaY, 2));
    }  
}
