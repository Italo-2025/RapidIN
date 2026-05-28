package com.rapidIN.model;

// ============================================================
// ARQUIVO: Usuario.java
// RESPONSABILIDADE: Define o "molde" de um usuário do sistema.
//
// O QUE É UMA CLASSE MODELO (MODEL)?
//   Uma classe modelo é uma "ficha cadastral" no código.
//   Assim como um formulário de cadastro tem campos como
//   nome, e-mail, telefone etc., esta classe define quais
//   informações um usuário possui no sistema.
//
// TIPOS DE USUÁRIO NESTE SISTEMA:
//   - PASSAGEIRO: pessoa que solicita corridas
//   - MOTORISTA: pessoa que aceita e realiza corridas
//
// REGRA DE NEGÓCIO ESPECIAL:
//   Passageiras do gênero feminino só podem ser atendidas
//   por motoristas do gênero feminino. Esta regra é aplicada
//   em outros arquivos com base no campo "genero" desta classe.
//
// ANALOGIA: Pense nesta classe como o modelo de uma carteirinha
//   de estudante. Ela define quais campos toda carteirinha deve
//   ter (nome, foto, matrícula etc.), mas não é a carteirinha
//   em si — é só o modelo. Cada usuário cadastrado é uma
//   carteirinha diferente criada a partir desse modelo.
// ============================================================

// "public class Usuario" declara o molde.
// Qualquer parte do sistema pode criar um "objeto Usuario"
// a partir deste molde.
public class Usuario {

    // ── CAMPOS (atributos) ────────────────────────────────────────────────────
    // Cada campo abaixo representa uma informação que um usuário possui.
    // "private" significa que esses campos só podem ser lidos ou alterados
    // pelos métodos desta mesma classe (getters e setters abaixo).
    // Isso protege os dados de modificações acidentais.

    private int id;               // Número único que identifica o usuário no banco de dados
    private String nome;          // Nome completo do usuário
    private String email;         // E-mail usado para login
    private String genero;        // Gênero: "M" para masculino, "F" para feminino
    private String tipo;          // Perfil: "PASSAGEIRO" ou "MOTORISTA"
    private boolean disponivel;   // Indica se o motorista está online (aceitando corridas)
                                  // Sempre false para passageiros (não se aplica a eles)

    // ── CONSTRUTORES ─────────────────────────────────────────────────────────
    // Um construtor é chamado quando criamos um novo objeto Usuario.
    // Ter dois construtores dá flexibilidade:

    // Construtor vazio: cria um Usuario sem dados.
    // Útil quando queremos criar o objeto e preencher os campos depois,
    // campo por campo (como fazemos ao ler dados do banco de dados).
    public Usuario() {}

    // Construtor completo: cria um Usuario já com todos os dados.
    // Útil quando já temos todas as informações disponíveis de uma vez.
    public Usuario(int id, String nome, String email, String genero, String tipo, boolean disponivel) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.genero = genero;
        this.tipo = tipo;
        this.disponivel = disponivel;
    }

    // ── GETTERS E SETTERS ─────────────────────────────────────────────────────
    // Como os campos são "private", usamos esses métodos especiais
    // para lê-los (getters) ou alterá-los (setters).
    //
    // Padrão de nomenclatura:
    //   - "get" + nome do campo → retorna o valor atual
    //   - "set" + nome do campo → define um novo valor

    // Identificador único do usuário
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    // Nome completo
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    // E-mail de login
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    // Gênero: "M" ou "F"
    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }

    // Tipo de perfil: "PASSAGEIRO" ou "MOTORISTA"
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    // Status de disponibilidade (somente relevante para motoristas)
    public boolean isDisponivel() { return disponivel; }
    public void setDisponivel(boolean disponivel) { this.disponivel = disponivel; }

    // ── MÉTODOS DE VERIFICAÇÃO ────────────────────────────────────────────────
    // Estes métodos facilitam verificar o tipo do usuário sem
    // precisar escrever a comparação toda hora em outros arquivos.
    // Em vez de: if ("MOTORISTA".equals(usuario.getTipo()))
    // Escrevemos: if (usuario.isMotorista())  — muito mais legível!

    // Retorna true se este usuário é um motorista
    public boolean isMotorista() { return "MOTORISTA".equals(tipo); }

    // Retorna true se este usuário é um passageiro
    public boolean isPassageiro() { return "PASSAGEIRO".equals(tipo); }
}
