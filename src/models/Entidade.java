package infectados.src;

public abstract class Entidade {
    private int tamanho;
    protected Vetor2D posicao, movimento;

    public Entidade(int tamanho, Vetor2D posicao, Vetor2D movimento) {
        this.tamanho = tamanho;
        this.posicao = posicao;
        this.movimento = movimento;
    }

    public void mover(int larguraTela, int alturaTela) {
        double novaX = posicao.getX() + movimento.getX();
        double novaY = posicao.getY() + movimento.getY();

        posicao.setX(novaX);
        posicao.setY(novaY);

        if (posicao.getX() >= larguraTela || posicao.getX() <= 0){
            movimento.setX(movimento.getX() * -1);
        }
        if (posicao.getY() >= alturaTela || posicao.getY() <= 0){
            movimento.setY(movimento.getY() * -1);
        }
    }

    public boolean verificaColisao(Entidade outra) {
        return this.posicao.distanciaPara(outra.posicao) <= (this.tamanho + outra.tamanho);
    }

    public abstract void atualizar();
}
