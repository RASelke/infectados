package models;

import fisica.Vetor2D;

public class Pessoa extends Entidade {
    private boolean vacinado;
    private EstadoSaude estado;
    private int tempoInfectado;
    private boolean pacienteZero;

    public Pessoa(int tamanho, Vetor2D posicao, Vetor2D movimento, boolean vacinado, EstadoSaude estado, boolean pacienteZero) {
        super(tamanho, posicao, movimento);
        this.vacinado = vacinado;
        this.estado = estado;
        this.tempoInfectado = 0;
        this.pacienteZero = pacienteZero;
    }

    @Override
    public void atualizar(int larguraTela, int alturaTela) {
        if (this.estado != EstadoSaude.MORTO) {
            mover(larguraTela, alturaTela);
            atualizarSaude();
        }
    }

    public void atualizarSaude() {
    	int limiteInfeccao = 1000; //Tempo (em ticks) máximo que uma Pessoa pode ficar infectada sem se recuperar, aṕos isso, recupera sozinha.
    	
        if (estado == EstadoSaude.INFECTADO){
            tempoInfectado++;
            
            double chanceMorrer = vacinado ? (200 / limiteInfeccao) : (500 / limiteInfeccao);
            if (pacienteZero) {
                chanceMorrer = 0.0001; 
            }
            
            // Aqui acontece a morte
            if (Math.random() < chanceMorrer){
                estado = EstadoSaude.MORTO;
                movimento.setX(0);
                movimento.setY(0);
            }
            
            if (tempoInfectado > limiteInfeccao && estado != EstadoSaude.MORTO) {
                estado = EstadoSaude.RECUPERADO;
            }
        }
    }

    public void interagir(Pessoa outra) {
        if (this.estado == EstadoSaude.INFECTADO && outra.estado == EstadoSaude.SUSCETIVEL){
            double chanceInfeccao = outra.vacinado ? 0.2 : 0.5;
            if (Math.random() < chanceInfeccao) {
                outra.estado = EstadoSaude.INFECTADO;
            }
        }
    }

    public EstadoSaude getEstado() {
        return estado;
    }

    public boolean isVacinado() {
        return vacinado;
    }
    
    public boolean isPacienteZero() {
    	return pacienteZero;
    }
}