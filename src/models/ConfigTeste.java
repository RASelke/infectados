package models;

public class ConfigTeste {
    public int id;
    public int vacinados;
    public int naoVacinados;
    public int infectados;
    public int rodadas;
    public int fps;
    public int margem;
    public int tamanhoPessoa;
    public double velMax;

    public ConfigTeste(int id, int vacinados, int naoVacinados, int infectados, int rodadas, int fps, int margem, int tamanhoPessoa, double velMax) {
        this.id = id;
        this.vacinados = vacinados;
        this.naoVacinados = naoVacinados;
        this.infectados = infectados;
        this.rodadas = rodadas;
        this.fps = fps;
        this.margem = margem;
        this.tamanhoPessoa = tamanhoPessoa;
        this.velMax = velMax;
    }
}