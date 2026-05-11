package models;

import fisica.Vetor2D;

public abstract class Entidade {
    private int tamanho;
    protected Vetor2D posicao;
	protected Vetor2D movimento;

    public Entidade(int tamanho, Vetor2D posicao, Vetor2D movimento) {
        this.tamanho = tamanho;
        this.setPosicao(posicao);
        this.movimento = movimento;
    }

    public void mover(int larguraTela, int alturaTela) {
        double novaX = posicao.getX() + movimento.getX();
        double novaY = posicao.getY() + movimento.getY();
        
        posicao.setX(novaX);
        posicao.setY(novaY);
        
        int margem = 20;
        
        // 2. Calculamos os limites reais. 
        // O limite mínimo é a margem + o raio da bolinha.
        // O limite máximo é a tela - a margem - o raio da bolinha.
        double limiteMinX = margem + tamanho;
        double limiteMaxX = larguraTela - margem - tamanho;
        double limiteMinY = margem + tamanho;
        double limiteMaxY = alturaTela - margem - tamanho - 40;
        
        // 3. Lógica de quicar no eixo X
        if (posicao.getX() >= limiteMaxX){
            posicao.setX(limiteMaxX); // Força a bolinha para dentro caso ela tente vazar
            movimento.setX(movimento.getX() * -1); // Inverte a direção
        } else if (posicao.getX() <= limiteMinX){
            posicao.setX(limiteMinX);
            movimento.setX(movimento.getX() * -1);
        }
        
        // 4. Lógica de quicar no eixo Y
        if (posicao.getY() >= limiteMaxY){
            posicao.setY(limiteMaxY);
            movimento.setY(movimento.getY() * -1);
        } else if (posicao.getY() <= limiteMinY){
            posicao.setY(limiteMinY);
            movimento.setY(movimento.getY() * -1);
        }
    }

 // Método para calcular o "quique" entre duas entidades
    public void resolverColisaoFisica(Entidade outra) {
        // 1. Distância matemática entre os centros
        double dx = outra.posicao.getX() - this.posicao.getX();
        double dy = outra.posicao.getY() - this.posicao.getY();
        double distancia = Math.sqrt(dx * dx + dy * dy);

        // Evita divisão por zero
        if (distancia == 0) return;

        // 2. Descobrir a direção exata da batida (Vetor Normal)
        double normalX = dx / distancia;
        double normalY = dy / distancia;

        // 3. Resolver a sobreposição (Separar para não grudarem)
        double sobreposicao = (this.tamanho + outra.tamanho) - distancia;
        if (sobreposicao > 0) {
            // Empurra metade da sobreposição para cada lado
            this.posicao.setX(this.posicao.getX() - normalX * (sobreposicao / 2));
            this.posicao.setY(this.posicao.getY() - normalY * (sobreposicao / 2));
            outra.posicao.setX(outra.posicao.getX() + normalX * (sobreposicao / 2));
            outra.posicao.setY(outra.posicao.getY() + normalY * (sobreposicao / 2));
        }

        // 4. Calcular a velocidade de aproximação
        double velRelativaX = this.movimento.getX() - outra.movimento.getX();
        double velRelativaY = this.movimento.getY() - outra.movimento.getY();
        
        // Produto escalar para saber a força na direção da batida
        double velocidadeNaNormal = velRelativaX * normalX + velRelativaY * normalY;

        // Se elas já estão se afastando uma da outra, cancela a física
        if (velocidadeNaNormal < 0) return;

        // 5. Aplicar o "quique" (inverte as velocidades na direção do impacto)
        this.movimento.setX(this.movimento.getX() - velocidadeNaNormal * normalX);
        this.movimento.setY(this.movimento.getY() - velocidadeNaNormal * normalY);
        outra.movimento.setX(outra.movimento.getX() + velocidadeNaNormal * normalX);
        outra.movimento.setY(outra.movimento.getY() + velocidadeNaNormal * normalY);
    }
    
    public boolean verificaColisao(Entidade outra) {
        return this.getPosicao().distanciaPara(outra.getPosicao()) <= (this.tamanho + outra.tamanho);
    }

    public abstract void atualizar();

	public Vetor2D getPosicao() {
		return posicao;
	}

	public void setPosicao(Vetor2D posicao) {
		this.posicao = posicao;
	}
}
