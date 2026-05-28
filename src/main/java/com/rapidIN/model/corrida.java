package com.rapidIN.model;

// ============================================================
// ARQUIVO: corrida.java
// RESPONSABILIDADE: Define o "molde" de uma corrida no sistema.
//
// O QUE REPRESENTA UMA CORRIDA?
//   Uma corrida é o registro de um pedido de transporte.
//   Ela possui todas as informações necessárias: de onde sai,
//   para onde vai, quem solicitou, quem vai atender, qual o
//   preço e qual é a situação atual.
//
// CICLO DE VIDA DE UMA CORRIDA (status):
//   1. AGUARDANDO   → Passageiro solicitou, aguarda um motorista aceitar
//   2. EM_ANDAMENTO → Um motorista aceitou e está realizando a corrida
//   3. CONCLUIDA    → Corrida finalizada com sucesso
//   4. CANCELADA    → Corrida recusada por motorista ou cancelada
//
// ANALOGIA: Pense nesta classe como o comprovante de um pedido
//   de delivery. Ele registra o restaurante (origem), o endereço
//   de entrega (destino), quem pediu, quem vai entregar, o valor
//   e o status atual do pedido.
// ============================================================

public class corrida {

    // ── CAMPOS (atributos) ────────────────────────────────────────────────────
    // Cada campo guarda uma informação específica sobre a corrida.

    private int id;                  // Número único que identifica esta corrida no banco de dados
    private String origem;           // Endereço de saída (onde o passageiro está)
    private String destino;          // Endereço de chegada (onde o passageiro quer ir)
    private String status;           // Situação atual: AGUARDANDO, EM_ANDAMENTO, CONCLUIDA ou CANCELADA
    private double preco;            // Valor cobrado pela corrida em reais
    private int idPassageiro;        // Identificador do passageiro que solicitou a corrida
    private int idMotorista;         // Identificador do motorista que aceitou (0 se ainda não aceita)
    private String nomePassageiro;   // Nome do passageiro (para exibição na tela do motorista)
    private String nomeMotorista;    // Nome do motorista (para exibição na tela do passageiro)
    private String generoPassageiro; // Gênero do passageiro: "M" ou "F"
                                     // Usado para aplicar a regra: passageira feminina
                                     // só pode ser atendida por motorista feminina

    // ── CONSTRUTORES ─────────────────────────────────────────────────────────

    // Construtor vazio: cria uma corrida sem dados.
    // Usado quando os campos serão preenchidos um a um
    // (por exemplo, ao ler dados do banco de dados).
    public corrida() {}

    // Construtor completo: cria uma corrida com os dados principais de uma vez.
    // O campo "generoPassageiro" não está aqui pois é definido separadamente via setter.
    public corrida(int id, String origem, String destino, String status, double preco,
                   int idPassageiro, int idMotorista, String nomePassageiro, String nomeMotorista) {
        this.id = id;
        this.origem = origem;
        this.destino = destino;
        this.status = status;
        this.preco = preco;
        this.idPassageiro = idPassageiro;
        this.idMotorista = idMotorista;
        this.nomePassageiro = nomePassageiro;
        this.nomeMotorista = nomeMotorista;
    }

    // ── GETTERS E SETTERS ─────────────────────────────────────────────────────
    // Métodos para ler e alterar cada campo da corrida.
    // "get" retorna o valor atual; "set" define um novo valor.

    // Identificador único da corrida no banco de dados
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    // Endereço de partida
    public String getOrigem() { return origem; }
    public void setOrigem(String origem) { this.origem = origem; }

    // Endereço de destino
    public String getDestino() { return destino; }
    public void setDestino(String destino) { this.destino = destino; }

    // Situação atual da corrida
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // Valor em reais
    public double getPreco() { return preco; }
    public void setPreco(double preco) { this.preco = preco; }

    // ID do passageiro vinculado a esta corrida
    public int getIdPassageiro() { return idPassageiro; }
    public void setIdPassageiro(int idPassageiro) { this.idPassageiro = idPassageiro; }

    // ID do motorista vinculado a esta corrida (0 se ainda não há motorista)
    public int getIdMotorista() { return idMotorista; }
    public void setIdMotorista(int idMotorista) { this.idMotorista = idMotorista; }

    // Nome do passageiro (exibido para o motorista na lista de corridas disponíveis)
    public String getNomePassageiro() { return nomePassageiro; }
    public void setNomePassageiro(String nomePassageiro) { this.nomePassageiro = nomePassageiro; }

    // Nome do motorista (exibido para o passageiro no histórico)
    public String getNomeMotorista() { return nomeMotorista; }
    public void setNomeMotorista(String nomeMotorista) { this.nomeMotorista = nomeMotorista; }

    // Gênero do passageiro desta corrida (usado para filtrar motoristas disponíveis)
    public String getGeneroPassageiro() { return generoPassageiro; }
    public void setGeneroPassageiro(String generoPassageiro) { this.generoPassageiro = generoPassageiro; }

    // ── MÉTODO ESPECIAL DE FORMATAÇÃO ─────────────────────────────────────────
    // Este método formata o preço para exibição na tabela da interface.
    // O JavaFX (PropertyValueFactory) chama automaticamente este método
    // quando a coluna "precoFormatado" é configurada na tabela.
    //
    // Exemplos de resultado:
    //   preco = 18.5  → "R$ 18,50"
    //   preco = 0.0   → "—" (traço, indicando que não há valor definido)
    public String getPrecoFormatado() {
        return preco > 0 ? String.format("R$ %.2f", preco) : "—";
    }
}
