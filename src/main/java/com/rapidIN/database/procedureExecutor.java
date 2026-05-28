package com.rapidIN.database;

// ============================================================
// ARQUIVO: procedureExecutor.java
// RESPONSABILIDADE: Executar todas as operações de banco de dados
//   do sistema (login, cadastro, corridas etc.).
//
// O QUE SÃO STORED PROCEDURES?
//   Stored Procedures são como "receitas prontas" guardadas
//   dentro do banco de dados. Em vez de o sistema escrever
//   toda a lógica de busca, o banco já tem funções prontas
//   (como sp_login_usuario) que recebem parâmetros e retornam
//   os dados necessários. É mais seguro e eficiente.
//
// MODO DE DESENVOLVIMENTO (MOCK_MODE):
//   Durante o desenvolvimento, o banco de dados real ainda
//   pode não estar disponível. Por isso existe o MOCK_MODE:
//   quando ativado (true), todos os métodos usam dados
//   falsos do MockData.java em vez de acessar o banco real.
//   Quando o banco estiver pronto, basta mudar para false.
//
// COMO ATIVAR O BANCO REAL:
//   1. Mude MOCK_MODE para false neste arquivo
//   2. Configure as credenciais em conexao.java
//   3. O banco de dados deve ter as stored procedures com
//      os nomes listados em cada método abaixo
//
// ANALOGIA: Este arquivo é como um garçom em um restaurante.
//   O garçom recebe o pedido (parâmetros), vai até a cozinha
//   (banco de dados), busca o prato (dados) e traz de volta.
//   Ele conhece cada prato do cardápio (cada stored procedure).
// ============================================================

// Importa as classes necessárias para trabalhar com banco de dados
import com.rapidIN.model.corrida;   // Modelo de dados de uma corrida
import com.rapidIN.model.Usuario;   // Modelo de dados de um usuário

import java.sql.*;                  // Tudo necessário para SQL: Connection, ResultSet etc.
import java.util.ArrayList;        // Lista dinâmica (cresce conforme adicionamos itens)
import java.util.List;             // Interface de lista (mais genérica)

public class procedureExecutor {

    // ── CHAVE PRINCIPAL: MOCK_MODE ────────────────────────────────────────────
    // true  = usa dados fictícios do MockData.java (modo desenvolvimento)
    // false = acessa o banco MySQL real (modo produção)
    //
    // ALTERE PARA false QUANDO O BANCO DE DADOS REAL ESTIVER DISPONÍVEL.
    public static final boolean MOCK_MODE = true;


    // =========================================================================
    // OPERAÇÃO: LOGIN
    // Verifica se o e-mail e senha informados correspondem a um usuário.
    // Retorna o objeto Usuario se as credenciais estiverem corretas,
    // ou null se o login falhar.
    //
    // Stored procedure chamada: sp_login_usuario(email, senha)
    // =========================================================================
    public static Usuario fazerLogin(String email, String senha) {
        // Se MOCK_MODE estiver ativo, usa dados falsos
        if (MOCK_MODE) return MockData.fazerLogin(email, senha);

        // Modo real: chama a stored procedure no banco de dados
        // O bloco "try-with-resources" garante que o stmt seja fechado
        // automaticamente ao final, mesmo se ocorrer um erro
        try (CallableStatement stmt = conexao.getConexao().prepareCall("{CALL sp_login_usuario(?, ?)}")) {
            stmt.setString(1, email); // Substitui o primeiro "?" pelo e-mail
            stmt.setString(2, senha); // Substitui o segundo "?" pela senha
            ResultSet rs = stmt.executeQuery(); // Executa e obtém o resultado
            if (rs.next()) return mapearUsuario(rs); // Se encontrou, converte para Usuario
        } catch (SQLException e) {
            System.err.println("Erro no login: " + e.getMessage());
        }
        return null; // Login falhou: credenciais inválidas ou erro no banco
    }


    // =========================================================================
    // OPERAÇÃO: CADASTRO
    // Registra um novo usuário no sistema.
    // Retorna true se o cadastro foi bem-sucedido, false se houve erro
    // (ex.: e-mail já cadastrado).
    //
    // Stored procedure chamada: sp_cadastrar_usuario(nome, email, senha, genero, tipo)
    // =========================================================================
    public static boolean cadastrarUsuario(String nome, String email, String senha,
                                           String genero, String tipo) {
        if (MOCK_MODE) return MockData.cadastrarUsuario(nome, email, senha, genero, tipo);

        try (CallableStatement stmt = conexao.getConexao()
                .prepareCall("{CALL sp_cadastrar_usuario(?, ?, ?, ?, ?)}")) {
            stmt.setString(1, nome);
            stmt.setString(2, email);
            stmt.setString(3, senha);
            stmt.setString(4, genero); // "M" ou "F"
            stmt.setString(5, tipo);   // "PASSAGEIRO" ou "MOTORISTA"
            stmt.execute();
            return true; // Cadastro realizado com sucesso
        } catch (SQLException e) {
            System.err.println("Erro ao cadastrar: " + e.getMessage());
            return false; // Falha no cadastro
        }
    }


    // =========================================================================
    // OPERAÇÃO: CALCULAR PREÇO
    // Consulta o banco para obter o preço estimado da corrida entre
    // dois endereços.
    // Retorna o valor em reais (double), ou 0.0 em caso de erro.
    //
    // Stored procedure chamada: sp_calcular_preco(origem, destino, OUT preco)
    // Nota: "OUT" indica que o terceiro parâmetro é uma SAÍDA — ou seja,
    //       o banco de dados escreve o resultado nele (não enviamos valor).
    // =========================================================================
    public static double calcularPreco(String origem, String destino) {
        if (MOCK_MODE) return MockData.calcularPreco(origem, destino);

        try (CallableStatement stmt = conexao.getConexao()
                .prepareCall("{CALL sp_calcular_preco(?, ?, ?)}")) {
            stmt.setString(1, origem);
            stmt.setString(2, destino);
            // Registra o terceiro parâmetro como saída (o banco vai preencher)
            stmt.registerOutParameter(3, Types.DOUBLE);
            stmt.execute();
            return stmt.getDouble(3); // Lê o valor que o banco escreveu no parâmetro 3
        } catch (SQLException e) {
            System.err.println("Erro ao calcular preco: " + e.getMessage());
            return 0.0;
        }
    }


    // =========================================================================
    // OPERAÇÃO: SOLICITAR CORRIDA
    // Registra um novo pedido de corrida no banco de dados.
    // Retorna um número positivo (ID da corrida criada) em caso de sucesso,
    // ou -1 se houver erro.
    //
    // Stored procedure chamada: sp_solicitar_corrida(id_passageiro, origem, destino)
    // =========================================================================
    public static int solicitarCorrida(int idPassageiro, String origem, String destino) {
        if (MOCK_MODE) return MockData.solicitarCorrida(idPassageiro, origem, destino);

        try (CallableStatement stmt = conexao.getConexao()
                .prepareCall("{CALL sp_solicitar_corrida(?, ?, ?)}")) {
            stmt.setInt(1, idPassageiro);
            stmt.setString(2, origem);
            stmt.setString(3, destino);
            stmt.execute();
            return 1; // Sucesso (o banco real poderia retornar o ID gerado)
        } catch (SQLException e) {
            System.err.println("Erro ao solicitar corrida: " + e.getMessage());
            return -1; // Indica falha
        }
    }


    // =========================================================================
    // OPERAÇÃO: CORRIDAS DO PASSAGEIRO
    // Busca todas as corridas (histórico completo) de um passageiro específico.
    // Retorna uma lista de objetos corrida, que pode estar vazia se não houver.
    //
    // Stored procedure chamada: sp_corridas_passageiro(id_passageiro)
    // =========================================================================
    public static List<corrida> corridasPassageiro(int idPassageiro) {
        if (MOCK_MODE) return MockData.corridasPassageiro(idPassageiro);

        List<corrida> lista = new ArrayList<>(); // Lista que será retornada
        try (CallableStatement stmt = conexao.getConexao()
                .prepareCall("{CALL sp_corridas_passageiro(?)}")) {
            stmt.setInt(1, idPassageiro);
            ResultSet rs = stmt.executeQuery();
            // "while (rs.next())" percorre cada linha retornada pelo banco
            while (rs.next()) lista.add(mapearCorrida(rs)); // Converte cada linha em objeto corrida
        } catch (SQLException e) {
            System.err.println("Erro ao buscar corridas: " + e.getMessage());
        }
        return lista;
    }


    // =========================================================================
    // OPERAÇÃO: CORRIDAS DO MOTORISTA
    // Busca o histórico de corridas de um motorista específico.
    //
    // Stored procedure chamada: sp_corridas_motorista(id_motorista)
    // =========================================================================
    public static List<corrida> corridasMotorista(int idMotorista) {
        if (MOCK_MODE) return MockData.corridasMotorista(idMotorista);

        List<corrida> lista = new ArrayList<>();
        try (CallableStatement stmt = conexao.getConexao()
                .prepareCall("{CALL sp_corridas_motorista(?)}")) {
            stmt.setInt(1, idMotorista);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) lista.add(mapearCorrida(rs));
        } catch (SQLException e) {
            System.err.println("Erro ao buscar corridas: " + e.getMessage());
        }
        return lista;
    }


    // =========================================================================
    // OPERAÇÃO: CORRIDAS DISPONÍVEIS PARA MOTORISTA
    // Busca corridas com status "AGUARDANDO" que o motorista pode aceitar.
    // A regra de gênero é aplicada aqui: passageiras femininas só aparecem
    // para motoristas femininas.
    //
    // Stored procedure chamada: sp_corridas_disponiveis(genero_motorista)
    // =========================================================================
    public static List<corrida> corridasDisponiveis(String generoMotorista) {
        if (MOCK_MODE) return MockData.corridasDisponiveis(generoMotorista);

        List<corrida> lista = new ArrayList<>();
        try (CallableStatement stmt = conexao.getConexao()
                .prepareCall("{CALL sp_corridas_disponiveis(?)}")) {
            stmt.setString(1, generoMotorista); // "M" ou "F"
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) lista.add(mapearCorrida(rs));
        } catch (SQLException e) {
            System.err.println("Erro ao buscar corridas disponiveis: " + e.getMessage());
        }
        return lista;
    }


    // =========================================================================
    // OPERAÇÃO: ACEITAR CORRIDA
    // Motorista aceita uma corrida disponível.
    // Vincula o motorista à corrida e muda o status para EM_ANDAMENTO.
    // Retorna true em caso de sucesso.
    //
    // Stored procedure chamada: sp_aceitar_corrida(id_corrida, id_motorista)
    // =========================================================================
    public static boolean aceitarCorrida(int idCorrida, int idMotorista) {
        if (MOCK_MODE) return MockData.aceitarCorrida(idCorrida, idMotorista);

        try (CallableStatement stmt = conexao.getConexao()
                .prepareCall("{CALL sp_aceitar_corrida(?, ?)}")) {
            stmt.setInt(1, idCorrida);
            stmt.setInt(2, idMotorista);
            stmt.execute();
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao aceitar corrida: " + e.getMessage());
            return false;
        }
    }


    // =========================================================================
    // OPERAÇÃO: RECUSAR CORRIDA
    // Motorista recusa uma corrida disponível.
    // Muda o status da corrida para CANCELADA.
    // Retorna true em caso de sucesso.
    //
    // Stored procedure chamada: sp_recusar_corrida(id_corrida)
    // =========================================================================
    public static boolean recusarCorrida(int idCorrida) {
        if (MOCK_MODE) return MockData.recusarCorrida(idCorrida);

        try (CallableStatement stmt = conexao.getConexao()
                .prepareCall("{CALL sp_recusar_corrida(?)}")) {
            stmt.setInt(1, idCorrida);
            stmt.execute();
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao recusar corrida: " + e.getMessage());
            return false;
        }
    }


    // =========================================================================
    // OPERAÇÃO: ATUALIZAR DISPONIBILIDADE DO MOTORISTA
    // Alterna o status do motorista entre ONLINE (disponível para corridas)
    // e OFFLINE (não aceita corridas no momento).
    //
    // Stored procedure chamada: sp_atualizar_disponibilidade(id_motorista, disponivel)
    // =========================================================================
    public static void atualizarDisponibilidade(int idMotorista, boolean disponivel) {
        if (MOCK_MODE) {
            MockData.atualizarDisponibilidade(idMotorista, disponivel);
            return;
        }
        try (CallableStatement stmt = conexao.getConexao()
                .prepareCall("{CALL sp_atualizar_disponibilidade(?, ?)}")) {
            stmt.setInt(1, idMotorista);
            stmt.setBoolean(2, disponivel); // true = online, false = offline
            stmt.execute();
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar disponibilidade: " + e.getMessage());
        }
    }


    // =========================================================================
    // MÉTODOS AUXILIARES DE MAPEAMENTO
    // Estes métodos convertem uma linha de resultado do banco (ResultSet)
    // em um objeto Java (Usuario ou corrida).
    //
    // O QUE É UM ResultSet?
    //   Quando o banco retorna dados, eles chegam como uma "tabela" em memória
    //   chamada ResultSet. Cada coluna tem um nome (ex.: "nome", "email").
    //   Os métodos abaixo leem coluna por coluna e montam o objeto Java
    //   correspondente.
    // =========================================================================

    // Converte uma linha do ResultSet em um objeto Usuario
    // Usado após chamar sp_login_usuario
    private static Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getInt("id_usuario"));        // Lê a coluna "id_usuario"
        u.setNome(rs.getString("nome"));          // Lê a coluna "nome"
        u.setEmail(rs.getString("email"));        // Lê a coluna "email"
        u.setGenero(rs.getString("genero"));      // Lê a coluna "genero" ("M" ou "F")
        u.setTipo(rs.getString("tipo"));          // Lê a coluna "tipo" ("PASSAGEIRO" ou "MOTORISTA")
        u.setDisponivel(rs.getBoolean("disponivel")); // Lê a coluna "disponivel"
        return u;
    }

    // Converte uma linha do ResultSet em um objeto corrida
    // Usado após chamar qualquer stored procedure que retorne corridas
    private static corrida mapearCorrida(ResultSet rs) throws SQLException {
        corrida c = new corrida();
        c.setId(rs.getInt("id_corrida"));                      // Lê o ID da corrida
        c.setOrigem(rs.getString("origem"));                   // Endereço de origem
        c.setDestino(rs.getString("destino"));                 // Endereço de destino
        c.setStatus(rs.getString("status"));                   // Status atual da corrida
        c.setPreco(rs.getDouble("preco"));                     // Valor em reais
        c.setIdPassageiro(rs.getInt("id_passageiro"));         // ID do passageiro
        c.setIdMotorista(rs.getInt("id_motorista"));           // ID do motorista
        c.setNomePassageiro(rs.getString("nome_passageiro"));  // Nome do passageiro
        c.setNomeMotorista(rs.getString("nome_motorista"));    // Nome do motorista
        c.setGeneroPassageiro(rs.getString("genero_passageiro")); // Gênero do passageiro
        return c;
    }
}
