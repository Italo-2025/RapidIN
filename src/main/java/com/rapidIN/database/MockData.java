package com.rapidIN.database;

// ============================================================
// ARQUIVO: MockData.java
// RESPONSABILIDADE: Simular o banco de dados durante o
//   desenvolvimento, sem precisar de um banco real.
//
// O QUE É "MOCK" (SIMULAÇÃO)?
//   "Mock" em inglês significa "falso" ou "simulado".
//   Durante o desenvolvimento do sistema, o banco de dados
//   real pode não estar disponível ainda. Para não travar
//   o trabalho, usamos dados falsos que se comportam como
//   se fossem reais. O sistema não percebe a diferença.
//
// COMO FUNCIONA NA PRÁTICA:
//   - procedureExecutor.java verifica MOCK_MODE
//   - Se true, chama os métodos deste arquivo
//   - Se false, chama o banco de dados real
//
// O QUE HÁ AQUI:
//   - 4 usuários de teste pré-cadastrados (2 passageiros, 2 motoristas)
//   - 3 corridas históricas já registradas
//   - Toda a lógica de negócio simulada em memória (sem banco)
//
// USUÁRIOS DISPONÍVEIS PARA TESTE (senha de todos: 123456):
//   joao@email.com   → Passageiro masculino
//   maria@email.com  → Passageira feminina
//   carlos@email.com → Motorista masculino
//   ana@email.com    → Motorista feminina
//
// QUANDO REMOVER ESTE ARQUIVO:
//   Quando o banco de dados real estiver disponível, este
//   arquivo pode ser ignorado. Ele só é relevante quando
//   MOCK_MODE = true em procedureExecutor.java.
// ============================================================
// Importações das classes de dados e das coleções Java
import java.util.ArrayList;   // Modelo de corrida
import java.util.List;   // Modelo de usuário

import com.rapidIN.model.Usuario;        // Lista que cresce dinamicamente
import com.rapidIN.model.corrida;             // Interface de lista

// Visibilidade "class" (sem "public"): esta classe só pode ser
// usada dentro do pacote "database". Ela é interna ao sistema
// de banco de dados — os controladores nunca a acessam diretamente.
class MockData {

    // ── DADOS EM MEMÓRIA ──────────────────────────────────────────────────────
    // Estas listas ficam na memória RAM enquanto o programa estiver rodando.
    // "static final" significa que existem uma única vez e não podem ser substituídas.
    // Porém, o CONTEÚDO das listas pode ser alterado (adicionar/remover itens).
    static final List<Usuario> usuarios = new ArrayList<>();
    static final List<corrida> corridas = new ArrayList<>();

    // Contadores para gerar IDs únicos para novos registros.
    // Começam em 5 e 4 para não conflitar com os dados iniciais.
    private static int proximoIdUsuario = 5;
    private static int proximoIdCorrida = 4;

    // ── BLOCO ESTÁTICO DE INICIALIZAÇÃO ───────────────────────────────────────
    // Este bloco "static { ... }" é executado UMA ÚNICA VEZ, quando a
    // classe é carregada pela primeira vez. Ele preenche as listas
    // com dados de exemplo para os testes.
    static {
        // ── Usuários de teste ─────────────────────────────────────────────────
        // Parâmetros: (id, nome, email, genero, tipo, disponivel)
        // Nota: "disponivel" só é relevante para motoristas.
        //       Para passageiros, sempre false.
        usuarios.add(new Usuario(1, "Joao Silva", "joao@email.com", "M", "PASSAGEIRO", false));
        usuarios.add(new Usuario(2, "Maria Souza", "maria@email.com", "F", "PASSAGEIRO", false));
        usuarios.add(new Usuario(3, "Carlos Mota", "carlos@email.com", "M", "MOTORISTA", true));
        usuarios.add(new Usuario(4, "Ana Ferreira", "ana@email.com", "F", "MOTORISTA", true));

        // ── Corridas históricas ───────────────────────────────────────────────
        // Parâmetros: (id, origem, destino, status, preco, idPassageiro, idMotorista, nomePassageiro, nomeMotorista)
        // Corrida 1: Joao foi atendido por Carlos — corrida concluída
        corrida c1 = new corrida(1, "Rua das Flores, 10", "Av. Paulista, 1000",
                "CONCLUIDA", 18.50, 1, 3, "Joao Silva", "Carlos Mota");
        c1.setGeneroPassageiro("M");

        // Corrida 2: Maria foi atendida por Ana — corrida concluída
        // (Esta é a regra de gênero em funcionamento: passageira F → motorista F)
        corrida c2 = new corrida(2, "Shopping Iguatemi", "Aeroporto Internacional",
                "CONCLUIDA", 45.00, 2, 4, "Maria Souza", "Ana Ferreira");
        c2.setGeneroPassageiro("F");

        // Corrida 3: Corrida de Joao que foi cancelada — sem motorista vinculado
        corrida c3 = new corrida(3, "Centro Historico, 55", "Av. Brasil, 200",
                "CANCELADA", 0.0, 1, 0, "Joao Silva", "");
        c3.setGeneroPassageiro("M");

        corridas.add(c1);
        corridas.add(c2);
        corridas.add(c3);
    }

    // =========================================================================
    // SIMULAÇÃO: LOGIN
    // Verifica se o e-mail existe na lista de usuários E se a senha é "123456".
    // (No mock, todos os usuários têm a mesma senha para facilitar os testes.)
    // Retorna o Usuario encontrado, ou null se o login for inválido.
    // =========================================================================
    static Usuario fazerLogin(String email, String senha) {
        // Todos os usuários mock têm a senha "123456"
        if (!"123456".equals(senha)) {
            return null;
        }

        // "stream()" permite percorrer a lista de forma elegante
        // "filter()" filtra apenas o usuário cujo e-mail bate com o informado
        // "findFirst()" pega o primeiro resultado
        // "orElse(null)" retorna null se nenhum for encontrado
        return usuarios.stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst().orElse(null);
    }

    // =========================================================================
    // SIMULAÇÃO: CADASTRO
    // Verifica se o e-mail já está em uso (evita duplicatas) e,
    // caso contrário, adiciona o novo usuário na lista em memória.
    // Retorna false se o e-mail já estiver cadastrado.
    // =========================================================================
    static boolean cadastrarUsuario(String nome, String email, String senha,
            String genero, String tipo) {
        // Verifica se já existe alguém com esse e-mail
        boolean jaExiste = usuarios.stream()
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(email));
        if (jaExiste) {
            return false; // E-mail duplicado, cadastro recusado
        }
        // Cria o novo usuário com um ID sequencial e adiciona à lista
        // "proximoIdUsuario++" usa o valor atual e depois incrementa
        usuarios.add(new Usuario(proximoIdUsuario++, nome, email, genero, tipo, false));
        return true;
    }

    // =========================================================================
    // SIMULAÇÃO: CALCULAR PREÇO
    // Como não temos um banco real calculando distâncias, usamos uma
    // fórmula simples baseada no tamanho dos endereços digitados.
    // Quanto maiores os endereços, maior o preço estimado.
    //
    // Fórmula: R$5,00 de base + R$0,25 por caractere dos endereços
    // Isso é apenas uma SIMULAÇÃO — o banco real tem cálculos reais.
    // =========================================================================
    static double calcularPreco(String origem, String destino) {
        // Math.round arredonda para 2 casas decimais
        return Math.round((5.0 + (origem.length() + destino.length()) * 0.25) * 100.0) / 100.0;
    }

    // =========================================================================
    // SIMULAÇÃO: SOLICITAR CORRIDA
    // Cria uma nova corrida com status AGUARDANDO e a adiciona à lista.
    // Retorna o ID gerado para a nova corrida, ou -1 se o passageiro
    // não existir.
    // =========================================================================
    static int solicitarCorrida(int idPassageiro, String origem, String destino) {
        // Busca o passageiro para obter nome e gênero
        Usuario passageiro = usuarios.stream()
                .filter(u -> u.getId() == idPassageiro)
                .findFirst().orElse(null);
        if (passageiro == null) {
            return -1; // Passageiro não encontrado
        }
        double preco = calcularPreco(origem, destino);
        int id = proximoIdCorrida++; // Gera um novo ID único para esta corrida

        // Cria a corrida com status AGUARDANDO e sem motorista (idMotorista = 0)
        corrida c = new corrida(id, origem, destino, "AGUARDANDO", preco,
                idPassageiro, 0, passageiro.getNome(), "");
        c.setGeneroPassageiro(passageiro.getGenero()); // Define o gênero para filtrar motoristas
        corridas.add(c); // Adiciona à lista de corridas em memória
        return id;
    }

    // =========================================================================
    // SIMULAÇÃO: CORRIDAS DO PASSAGEIRO
    // Filtra e retorna apenas as corridas onde o idPassageiro bate.
    // =========================================================================
    static List<corrida> corridasPassageiro(int idPassageiro) {
        List<corrida> resultado = new ArrayList<>();
        for (corrida c : corridas) {
            if (c.getIdPassageiro() == idPassageiro) {
                resultado.add(c);
            }
        }
        return resultado;
    }

    // =========================================================================
    // SIMULAÇÃO: CORRIDAS DO MOTORISTA
    // Filtra e retorna apenas as corridas onde o idMotorista bate.
    // =========================================================================
    static List<corrida> corridasMotorista(int idMotorista) {
        List<corrida> resultado = new ArrayList<>();
        for (corrida c : corridas) {
            if (c.getIdMotorista() == idMotorista) {
                resultado.add(c);
            }
        }
        return resultado;
    }

    // =========================================================================
    // SIMULAÇÃO: CORRIDAS DISPONÍVEIS (para motoristas)
    // Retorna corridas com status AGUARDANDO que o motorista pode aceitar.
    //
    // REGRA DE GÊNERO APLICADA AQUI:
    //   - Passageiro masculino (M) → qualquer motorista pode ver a corrida
    //   - Passageira feminina (F) → SOMENTE motoristas femininas veem a corrida
    //
    // Implementação:
    //   A condição "continue" pula a corrida atual e vai para a próxima,
    //   ou seja, o motorista masculino não verá corridas de passageiras femininas.
    // =========================================================================
    static List<corrida> corridasDisponiveis(String generoMotorista) {
        List<corrida> resultado = new ArrayList<>();
        for (corrida c : corridas) {
            // Ignora corridas que não estão aguardando um motorista
            if (!"AGUARDANDO".equals(c.getStatus())) {
                continue;
            }

            // REGRA DE NEGÓCIO: passageira feminina só pode ser atendida
            // por motorista feminina. Se a passageira for F e o motorista
            // não for F, pula esta corrida.
            if ("F".equals(c.getGeneroPassageiro()) && !"F".equals(generoMotorista)) {
                continue;
            }

            resultado.add(c);
        }
        return resultado;
    }

    // =========================================================================
    // SIMULAÇÃO: ACEITAR CORRIDA
    // Vincula o motorista à corrida e muda o status para EM_ANDAMENTO.
    // Só aceita corridas com status AGUARDANDO (evita aceitar duas vezes).
    // Retorna true se conseguiu aceitar, false caso contrário.
    // =========================================================================
    static boolean aceitarCorrida(int idCorrida, int idMotorista) {
        // Busca o motorista para obter o nome
        Usuario motorista = usuarios.stream()
                .filter(u -> u.getId() == idMotorista)
                .findFirst().orElse(null);

        for (corrida c : corridas) {
            // Somente aceita se o ID bater E o status for AGUARDANDO
            if (c.getId() == idCorrida && "AGUARDANDO".equals(c.getStatus())) {
                c.setIdMotorista(idMotorista);
                c.setNomeMotorista(motorista != null ? motorista.getNome() : "");
                c.setStatus("EM_ANDAMENTO"); // Status muda para indicar corrida em curso
                return true;
            }
        }
        return false; // Corrida não encontrada ou já foi aceita por outro motorista
    }

    // =========================================================================
    // SIMULAÇÃO: RECUSAR CORRIDA
    // Muda o status da corrida para CANCELADA.
    // Só pode recusar corridas AGUARDANDO.
    // =========================================================================
    static boolean recusarCorrida(int idCorrida) {
        for (corrida c : corridas) {
            if (c.getId() == idCorrida && "AGUARDANDO".equals(c.getStatus())) {
                c.setStatus("CANCELADA");
                return true;
            }
        }
        return false;
    }

    // =========================================================================
    // SIMULAÇÃO: AVALIAR CORRIDA
    // Registra a avaliação na corrida mock e guarda o comentário.
    // Retorna a mesma mensagem que a interface de avaliação exibirá.
    // =========================================================================
    static String avaliarCorrida(int idCorrida, int avaliadorId, int avaliadoId,
            int nota, String comentario) {
        for (corrida c : corridas) {
            if (c.getId() == idCorrida && "CONCLUIDA".equals(c.getStatus())) {
                c.setComentario(comentario);
                return "Avaliacao registrada (mock).";
            }
        }
        return "Nao foi possivel registrar a avaliacao.";
    }

    // =========================================================================
    // SIMULAÇÃO: ATUALIZAR DISPONIBILIDADE DO MOTORISTA
    // Encontra o motorista pelo ID e altera seu status de disponibilidade.
    // "ifPresent" executa a ação somente se o motorista for encontrado.
    // =========================================================================
    static void atualizarDisponibilidade(int idMotorista, boolean disponivel) {
        usuarios.stream()
                .filter(u -> u.getId() == idMotorista)
                .findFirst()
                .ifPresent(u -> u.setDisponivel(disponivel)); // Altera somente se encontrar
    }
}
