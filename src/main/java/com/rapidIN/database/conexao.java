package com.rapidIN.database;

// ============================================================
// ARQUIVO: conexao.java
// RESPONSABILIDADE: Abrir, manter e fechar a conexão com
//   o banco de dados MySQL.
//
// O QUE É UM BANCO DE DADOS?
//   Um banco de dados é onde o sistema armazena as informações
//   permanentemente. Pense nele como um arquivo Excel gigante
//   que fica em um servidor. Para o programa ler ou salvar
//   dados (usuários, corridas etc.), ele precisa "conversar"
//   com esse banco de dados. Para isso, é necessário
//   estabelecer uma CONEXÃO — como discar para um número
//   de telefone antes de falar.
//
// PADRÃO SINGLETON APLICADO AQUI:
//   O sistema mantém apenas UMA conexão aberta com o banco
//   durante toda a execução. Abrir múltiplas conexões é
//   custoso e desnecessário. Quando precisamos de uma conexão,
//   reutilizamos a que já está aberta.
//
// COMO CONFIGURAR PARA O BANCO REAL:
//   Altere as três constantes abaixo (URL, USUARIO, SENHA)
//   com os dados fornecidos pelo time de banco de dados.
//   Depois, em procedureExecutor.java, mude MOCK_MODE para false.
//
// ANALOGIA: É como uma linha telefônica dedicada entre o
//   sistema e o banco de dados. Uma vez estabelecida, fica
//   aberta até o programa ser fechado.
// ============================================================

// Ferramentas da biblioteca padrão do Java para conexão com bancos de dados
import java.sql.Connection;       // Representa a conexão ativa com o banco
import java.sql.DriverManager;   // Responsável por criar a conexão
import java.sql.SQLException;    // Tipo de erro específico para problemas com banco de dados

public class conexao {

    // ── CONFIGURAÇÕES DE CONEXÃO ──────────────────────────────────────────────
    // ATENÇÃO: Altere estes três valores quando receber as credenciais do banco real.
    //
    // URL: endereço do servidor de banco de dados no formato:
    //   jdbc:mysql://[endereço do servidor]:[porta]/[nome do banco]
    //   - "localhost" significa que o banco está na mesma máquina que o sistema
    //   - "3306" é a porta padrão do MySQL
    //   - "rapidin_db" é o nome do banco de dados
    private static final String URL     = "jdbc:mysql://localhost:3306/rapdin";

    // USUARIO: nome de usuário para autenticação no banco de dados
    private static final String USUARIO = "root";

    // SENHA: senha do usuário do banco de dados
    private static final String SENHA   = "ItaloIsis2009";

    // ── ESTADO INTERNO ────────────────────────────────────────────────────────
    // Guarda a conexão ativa. Começa nula (sem conexão).
    // Quando precisamos conectar, criamos e armazenamos aqui.
    private static Connection conexaoAtiva = null;

    // Construtor privado: impede a criação de objetos desta classe.
    // Todos os métodos são estáticos (da classe, não de um objeto),
    // então nunca precisamos criar uma instância de conexao.
    private conexao() {}

    // ── getConexao ────────────────────────────────────────────────────────────
    // Retorna a conexão com o banco de dados.
    // Se ainda não há conexão (ou ela foi fechada/perdida),
    // cria uma nova automaticamente.
    //
    // Outros arquivos chamam este método sempre que precisam
    // enviar ou receber dados do banco.
    public static Connection getConexao() {
        try {
            // Verifica se não existe conexão ou se ela foi fechada.
            // Se a conexão está inativa, cria uma nova.
            if (conexaoAtiva == null || conexaoAtiva.isClosed()) {
                // DriverManager.getConnection tenta abrir a conexão com
                // o banco usando as configurações definidas acima.
                // Se as credenciais estiverem erradas, lança um SQLException.
                conexaoAtiva = DriverManager.getConnection(URL, USUARIO, SENHA);
                System.out.println("Conexao com o banco estabelecida!");
            }
        } catch (SQLException e) {
            // Se não conseguir conectar (banco offline, senha errada etc.),
            // exibe o erro no console sem travar o programa.
            System.err.println("Erro ao conectar ao banco: " + e.getMessage());
        }
        return conexaoAtiva;
    }

    // ── fecharConexao ─────────────────────────────────────────────────────────
    // Encerra a conexão com o banco de dados de forma correta.
    // Chamado pelo App.java quando o usuário fecha o programa (método stop).
    // Fechar a conexão adequadamente é importante para liberar recursos
    // no servidor de banco de dados.
    public static void fecharConexao() {
        try {
            // Só tenta fechar se a conexão existir e estiver aberta
            if (conexaoAtiva != null && !conexaoAtiva.isClosed()) {
                conexaoAtiva.close();
                System.out.println("Conexao encerrada.");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao fechar conexao: " + e.getMessage());
        }
    }
}
