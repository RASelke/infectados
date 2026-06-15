# 🦠 Simulador de Infecção (Modelo SIR)

![Status](https://img.shields.io/badge/Status-Em%20Desenvolvimento-orange)
![Linguagem](https://img.shields.io/badge/Linguagem-Java-blue)
![Disciplina](https://img.shields.io/badge/Disciplina-POO%20II-brightgreen)

## 📖 Sobre o Projeto
Este projeto é uma simulação computacional baseada no **Modelo Epidemiológico SIR** (Suscetíveis, Infectados, Recuperados e Mortos). Ele foi desenvolvido como trabalho prático para a disciplina de **Programação Orientada a Objetos II**, com o objetivo de aplicar conceitos avançados de arquitetura de software (Herança, Polimorfismo, Encapsulamento) em um cenário de experimentação estatística.

A principal intenção do software não é ser apenas um "jogo visual", mas sim uma **ferramenta de análise de dados**. O sistema executa baterias de testes autônomas para analisar o comportamento da propagação de um vírus sob diferentes variáveis (como a taxa de vacinação prévia da população), extraindo a **mediana estatística** dos resultados para evitar que pontos fora da curva (cenários de muita ou pouca sorte) distorçam a conclusão.

## ⚙️ Funcionalidades Implementadas (Até o Momento)

Apesar de ainda estar em desenvolvimento, o simulador já conta com um ecossistema robusto operando de forma autônoma:

- **Motor de Física 2D:** Entidades dinâmicas com cálculo de vetores para movimentação e sistema de colisão elástica avançada (bolinhas não se sobrepõem e quicam de forma realista).
- **Biologia e Probabilidade:** - População dividida entre **Vacinados** e **Não Vacinados**, afetando diretamente a probabilidade de infecção e a taxa de letalidade.
  - Implementação do **Paciente Zero**, isolado com uma "identidade biológica" própria para garantir que sua existência não afete os cálculos de eficácia da vacina no final.
- **Bateria de Testes Autônoma:** O sistema roda automaticamente 3 rodadas consecutivas de simulação. Ao final, os cenários são ordenados pelo número de óbitos e o **cenário intermediário (mediana)** é selecionado para apresentação.
- **Renderização Gráfica Customizada (Graphics2D):** - Anti-aliasing para suavização visual.
  - Painel de Legenda (HUD) em tempo real.
  - Dashboard de estatísticas translúcido apresentado no encerramento da simulação.

## 🧬 Arquitetura (POO)
O projeto foi desenhado sob os pilares da Orientação a Objetos:
- **Classes Abstratas e Herança:** Toda a física base é resolvida por uma classe abstrata genérica `Entidade`, que repassa suas características matemáticas para a classe `Pessoa` (onde a biologia e a doença acontecem).
- **Polimorfismo:** Entidades diferentes podem ser atualizadas a cada frame da tela (FPS) utilizando o mesmo método abstrato.

## 🚀 Próximos Passos (To-Do)
O projeto ainda passará por refinamentos para enriquecer as variáveis de teste:
- [ ] **Zona de Infecção:** Alterar o contágio de "contato direto" para a criação de zonas de risco ou "poças" de vírus deixadas no mapa.
- [ ] **Ajuste de Balanceamento:** Inverter a lógica atual (focada na chance de morrer) para criar uma probabilidade de chance de recuperação com base no tempo infectado.
- [ ] **Exportação de Dados:** Possibilidade de extrair os dados das rodadas para arquivos estruturados (ex: `.csv`) para análise externa.

## 🛠️ Como Executar

1. Certifique-se de ter o **Java (JDK)** instalado em sua máquina.
2. Clone este repositório ou baixe o código fonte.
3. Importe o projeto em sua IDE de preferência (Eclipse, IntelliJ, etc).
4. Localize e execute o arquivo `Main.java` localizado dentro da pasta `src`.
5. A simulação será iniciada em Tela Cheia (ou maximizada) automaticamente. Para encerrar ao final das baterias, pressione `ALT+F4`.

---
*Projeto acadêmico desenvolvido para estudos em manipulação de objetos, threads e simulações estocásticas em Java.*