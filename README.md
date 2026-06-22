# 🦠 Simulador de Infecção (Modelo SIR)

![Status](https://img.shields.io/badge/Status-Concluído-success)
![Linguagem](https://img.shields.io/badge/Linguagem-Java-blue)
![Disciplina](https://img.shields.io/badge/Disciplina-POO%20II-brightgreen)
![Repositório](https://img.shields.io/badge/GitHub-RASelke%2Finfectados-lightgrey)

## 📖 Sobre o Projeto
Este projeto é uma simulação computacional baseada no **Modelo Epidemiológico SIR** (Suscetíveis, Infectados, Recuperados e Mortos). Ele foi desenvolvido como trabalho prático para a disciplina de **Programação Orientada a Objetos II**, com o objetivo de aplicar conceitos avançados de engenharia e arquitetura de software em um cenário de experimentação estatística.

A principal intenção do software não é ser apenas um simulador visual, mas sim uma **ferramenta de análise de dados estocástica (Método de Monte Carlo)**. O sistema executa baterias de testes em lote (*Batch Processing*) a partir de um arquivo importado para analisar a propagação de um vírus sob diferentes variáveis, extraindo os cenários de **Mínimo, Média e Máximo** de óbitos.

---

## ⚙️ Funcionalidades e Destaques Técnicos

O simulador conta com um ecossistema robusto e blindado contra falhas (*Race Conditions*), operando de forma autônoma:

* **Motor de Física 2D:** Entidades dinâmicas com cálculo de vetores para movimentação e sistema de colisão elástica avançada (conservação de momento e correção de sobreposição).
* **Biologia e Probabilidade:** * População dividida entre **Vacinados** e **Não Vacinados**, afetando a probabilidade de infecção e letalidade.
  * Implementação de um **Paciente Zero** blindado para não adulterar os cálculos de eficácia final da vacina.
* **Multithreading e Modos de Execução:** O simulador possui uma *State Machine* (Máquina de Estados) com dois modos de operação:
  * **Modo Visual (Apresentação):** Processamento com limite de FPS, *Splash Screen* de introdução, HUD de métricas ao vivo, transições cinematográficas (*Fade In/Fade Out*) e renderização em tabela no fechamento da bateria.
  * **Modo de Desempenho Máximo (Headless):** Renderização gráfica desligada e FPS destravado. Permite executar milhares de testes em milissegundos utilizando 100% da CPU. O tráfego de dados é protegido por coleções nativas à prova de concorrência (`CopyOnWriteArrayList`).
* **Persistência de Dados (I/O):** Leitura de configurações flexíveis no `testes.csv` e consolidação de resultados formatados automaticamente no arquivo `resultados_saida.csv`.

---

## 🛠️ Como Executar o Projeto

1. Certifique-se de ter o **Java (JDK)** instalado na sua máquina.
2. Clone este repositório para sua máquina local.
3. Edite o arquivo `testes.csv` na raiz do projeto para criar ou alterar os cenários que deseja simular.
4. Execute a classe `Main.java` localizada dentro da pasta `src`.
5. **Dica:** Para alternar entre a experiência visual e a simulação super-rápida de dados, altere a variável `modoVisual` dentro do arquivo `Main.java` para `true` ou `false`.
6. Ao final da execução em lote, os dados estatísticos detalhados estarão salvos e prontos para uso no arquivo `resultados_saida.csv`. Pressione `ALT+F4` caso esteja na interface gráfica para encerrar.