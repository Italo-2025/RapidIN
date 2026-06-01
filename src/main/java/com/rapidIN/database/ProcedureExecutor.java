package rapdin.database;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Types;

/**
 * ProcedureExecutor
 *
 * Responsabilidade: abrir conexao com o banco MySQL e chamar
 * a stored procedure proc_solicitar_corrida.
 *
 * O Controller chama o metodo aqui; o FXML chama o Controller.
 * Nao misture logica de tela aqui.
 */
public class ProcedureExecutor {

    // -------------------------------------------------------
    // Configuracoes do banco (ajuste para o seu ambiente)
    // -------------------------------------------------------
    private static final String URL    = "jdbc:mysql://localhost:3306/rapdin?useSSL=false&serverTimezone=America/Sao_Paulo";
    private static final String USUARIO = "root";   // seu usuario MySQL
    private static final String SENHA   = "";       // sua senha MySQL

    /**
     * Abre e retorna uma conexao com o banco rapdin.
     * O chamador e responsavel por fechar a conexao.
     */
    public static Connection conectar() throws Exception {
        // Garante que o driver MySQL esta carregado
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(URL, USUARIO, SENHA);
    }

    /**
     * Chama a procedure proc_solicitar_corrida no banco.
     *
     * @param passageiroId       id da tabela passageiros
     * @param origemEnderecoId   id do endereco de origem
     * @param destinoEnderecoId  id do endereco de destino
     * @return ResultadoCorrida  objeto com corridaId + mensagem retornados pelo banco
     * @throws Exception         qualquer erro de conexao ou SQL
     */
    public static ResultadoCorrida solicitarCorrida(
            int passageiroId,
            int origemEnderecoId,
            int destinoEnderecoId) throws Exception {

        // try-with-resources: fecha conexao e statement automaticamente
        try (Connection conn = conectar();
             CallableStatement stmt = conn.prepareCall(
                     "{CALL proc_solicitar_corrida(?, ?, ?, ?, ?)}")) {

            // --- parametros de ENTRADA (IN) ---
            stmt.setInt(1, passageiroId);
            stmt.setInt(2, origemEnderecoId);
            stmt.setInt(3, destinoEnderecoId);

            // --- parametros de SAIDA (OUT) ---
            stmt.registerOutParameter(4, Types.INTEGER); // p_corrida_id
            stmt.registerOutParameter(5, Types.VARCHAR); // p_mensagem

            // executa a procedure
            stmt.execute();

            // le os valores de saida
            int    corridaId = stmt.getInt(4);
            String mensagem  = stmt.getString(5);

            return new ResultadoCorrida(corridaId, mensagem);
        }
    }

    // -------------------------------------------------------
    // Classe interna simples so pra agrupar os dois retornos
    // -------------------------------------------------------
    public static class ResultadoCorrida {
        public final int    corridaId;
        public final String mensagem;

        public ResultadoCorrida(int corridaId, String mensagem) {
            this.corridaId = corridaId;
            this.mensagem  = mensagem;
        }
    }
}
