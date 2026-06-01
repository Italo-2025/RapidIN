-- ============================================================
--  SCRIPT COMPLETO — BANCO DE DADOS RapidIN
--  Plataforma de Mobilidade Urbana (Projeto Educacional)
-- ============================================================
--
--  COMO USAR:
--    1. Abra o MySQL Workbench
--    2. Conecte-se ao servidor local (localhost, porta 3306)
--    3. Abra este arquivo: File > Open SQL Script
--    4. Clique em Execute (raio ⚡) ou pressione Ctrl+Shift+Enter
--    5. Aguarde a mensagem "Script executado com sucesso"
--
--  APÓS EXECUTAR:
--    Em procedureExecutor.java, mude:
--      public static final boolean MOCK_MODE = true;
--    para:
--      public static final boolean MOCK_MODE = false;
--
--  USUÁRIOS DE TESTE (senha de todos: 123456):
--    joao@email.com   → Passageiro masculino
--    maria@email.com  → Passageira feminina
--    carlos@email.com → Motorista masculino (online)
--    ana@email.com    → Motorista feminina  (online)
--
--  COMPATIBILIDADE:
--    MySQL 8.0+ (recomendado) ou MySQL 5.7+
-- ============================================================


-- ============================================================
-- PASSO 1: CRIAÇÃO DO BANCO DE DADOS
-- ============================================================
-- DROP DATABASE IF EXISTS `RapidIN`;   ← descomente para recriar do zero

CREATE DATABASE IF NOT EXISTS `RapidIN`
    CHARACTER SET utf8mb4          -- Suporte completo a Unicode (acentos, emojis etc.)
    COLLATE utf8mb4_unicode_ci;    -- Ordenação insensível a maiúsculas/minúsculas

USE `RapidIN`;


-- ============================================================
-- PASSO 2: TABELAS
-- ============================================================

-- ------------------------------------------------------------
-- TABELA: usuarios
--   Armazena passageiros e motoristas.
--   As colunas devem ser EXATAMENTE estas, pois o Java
--   as lê pelo nome em mapearUsuario():
--     id_usuario, nome, email, genero, tipo, disponivel
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuarios (
    id_usuario  INT           NOT NULL AUTO_INCREMENT,
    nome        VARCHAR(100)  NOT NULL,
    email       VARCHAR(100)  NOT NULL,
    senha       VARCHAR(255)  NOT NULL,         -- Armazenada em texto puro (projeto educacional)
                                                -- Em produção real: usar bcrypt ou SHA2
    genero      CHAR(1)       NOT NULL,         -- 'M' = Masculino | 'F' = Feminino
    tipo        VARCHAR(10)   NOT NULL,         -- 'PASSAGEIRO' ou 'MOTORISTA'
    disponivel  TINYINT(1)    NOT NULL DEFAULT 0, -- 0 = offline/não se aplica | 1 = online

    PRIMARY KEY (id_usuario),
    UNIQUE KEY uq_email (email),
    CONSTRAINT chk_genero CHECK (genero IN ('M', 'F')),
    CONSTRAINT chk_tipo   CHECK (tipo   IN ('PASSAGEIRO', 'MOTORISTA'))
);


-- ------------------------------------------------------------
-- TABELA: corridas
--   Registra cada pedido de corrida do início ao fim.
--   As colunas devem ser EXATAMENTE estas, pois o Java
--   as lê pelo nome em mapearCorrida().
--   nome_passageiro e nome_motorista são obtidos via JOIN
--   nas stored procedures — não ficam nesta tabela.
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS corridas (
    id_corrida        INT            NOT NULL AUTO_INCREMENT,
    origem            VARCHAR(200)   NOT NULL,
    destino           VARCHAR(200)   NOT NULL,
    status            VARCHAR(15)    NOT NULL DEFAULT 'AGUARDANDO',
                      -- Valores possíveis: AGUARDANDO | EM_ANDAMENTO | CONCLUIDA | CANCELADA
    preco             DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    id_passageiro     INT            NOT NULL,
    id_motorista      INT                NULL DEFAULT NULL, -- NULL enquanto aguarda um motorista
    genero_passageiro CHAR(1)        NOT NULL,
                      -- Cópia do gênero do passageiro no momento da corrida.
                      -- Necessário para aplicar a regra de encaminhamento por gênero
                      -- mesmo que o usuário atualize seu cadastro depois.

    PRIMARY KEY (id_corrida),
    CONSTRAINT fk_corrida_passageiro FOREIGN KEY (id_passageiro)
        REFERENCES usuarios (id_usuario),
    CONSTRAINT fk_corrida_motorista  FOREIGN KEY (id_motorista)
        REFERENCES usuarios (id_usuario),
    CONSTRAINT chk_status CHECK (status IN ('AGUARDANDO', 'EM_ANDAMENTO', 'CONCLUIDA', 'CANCELADA')),
    CONSTRAINT chk_genero_passageiro CHECK (genero_passageiro IN ('M', 'F'))
);

-- ------------------------------------------------------------
-- TABELA: avaliacoes
--   Armazena avaliações feitas após corrida concluída.
--   Única avaliação por corrida/avaliador.
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS avaliacoes (
    id_avaliacao INT          NOT NULL AUTO_INCREMENT,
    id_corrida   INT          NOT NULL,
    avaliador_id INT          NOT NULL,
    avaliado_id  INT          NOT NULL,
    nota         INT          NOT NULL,
    comentario   TEXT         NULL,
    data_hora    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id_avaliacao),
    UNIQUE KEY uq_avaliacao_corrida_avaliador (id_corrida, avaliador_id),
    CONSTRAINT fk_avaliacao_corrida   FOREIGN KEY (id_corrida)   REFERENCES corridas(id_corrida),
    CONSTRAINT fk_avaliacao_avaliador FOREIGN KEY (avaliador_id) REFERENCES usuarios(id_usuario),
    CONSTRAINT fk_avaliacao_avaliado  FOREIGN KEY (avaliado_id)  REFERENCES usuarios(id_usuario),
    CONSTRAINT chk_nota CHECK (nota BETWEEN 1 AND 5)
);


-- ============================================================
-- PASSO 3: STORED PROCEDURES
-- ============================================================
-- DELIMITER muda o separador de comandos de ';' para '$$'
-- para que o MySQL não confunda os ';' internos das procedures
-- com o fim do comando CREATE PROCEDURE.
-- Ao final voltamos o separador para ';'.

DELIMITER $$


-- ------------------------------------------------------------
-- sp_login_usuario
--   Verifica e-mail + senha e retorna os dados do usuário.
--   Chamada em: procedureExecutor.fazerLogin()
--
--   Parâmetros de entrada:
--     p_email → e-mail digitado na tela de login
--     p_senha → senha digitada na tela de login
--
--   Retorna (colunas lidas pelo Java):
--     id_usuario, nome, email, genero, tipo, disponivel
-- ------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_login_usuario$$
CREATE PROCEDURE sp_login_usuario(
    IN p_email VARCHAR(100),
    IN p_senha VARCHAR(255)
)
BEGIN
    SELECT
        id_usuario,
        nome,
        email,
        genero,
        tipo,
        disponivel
    FROM usuarios
    WHERE email = p_email
      AND senha = p_senha
    LIMIT 1;
    -- Se não encontrar nenhuma linha, o Java recebe ResultSet vazio
    -- e retorna null (login inválido).
END$$


-- ------------------------------------------------------------
-- sp_cadastrar_usuario
--   Insere um novo usuário no sistema.
--   Chamada em: procedureExecutor.cadastrarUsuario()
--
--   Se o e-mail já existir, lança um erro SQL (SIGNAL).
--   O Java captura o SQLException e retorna false para a tela.
-- ------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_cadastrar_usuario$$
CREATE PROCEDURE sp_cadastrar_usuario(
    IN p_nome   VARCHAR(100),
    IN p_email  VARCHAR(100),
    IN p_senha  VARCHAR(255),
    IN p_genero CHAR(1),
    IN p_tipo   VARCHAR(10)
)
BEGIN
    -- Verifica se o e-mail já está em uso
    IF EXISTS (SELECT 1 FROM usuarios WHERE email = p_email) THEN
        -- SIGNAL dispara um erro que o Java captura como SQLException
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'E-mail ja cadastrado.';
    ELSE
        INSERT INTO usuarios (nome, email, senha, genero, tipo, disponivel)
        VALUES (p_nome, p_email, p_senha, p_genero, p_tipo, 0);
        -- disponivel começa como 0 (offline) para todos os perfis
    END IF;
END$$


-- ------------------------------------------------------------
-- sp_calcular_preco
--   Calcula o preço estimado de uma corrida.
--   Chamada em: procedureExecutor.calcularPreco()
--
--   O terceiro parâmetro é de SAÍDA (OUT): o banco escreve
--   o resultado nele e o Java o lê com stmt.getDouble(3).
--
--   Fórmula: R$5,00 de base + R$0,25 por caractere dos endereços
--   (Idêntica ao MockData.calcularPreco para testes consistentes)
-- ------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_calcular_preco$$
CREATE PROCEDURE sp_calcular_preco(
    IN  p_origem  VARCHAR(200),
    IN  p_destino VARCHAR(200),
    OUT p_preco   DOUBLE
)
BEGIN
    SET p_preco = ROUND(
        5.0 + (CHAR_LENGTH(p_origem) + CHAR_LENGTH(p_destino)) * 0.25,
        2
    );
END$$


-- ------------------------------------------------------------
-- sp_solicitar_corrida
--   Cria uma nova corrida com status AGUARDANDO.
--   Chamada em: procedureExecutor.solicitarCorrida()
--
--   O gênero do passageiro é copiado para a corrida neste
--   momento para garantir que a regra de encaminhamento
--   funcione mesmo que o usuário atualize o perfil depois.
-- ------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_solicitar_corrida$$
CREATE PROCEDURE sp_solicitar_corrida(
    IN p_id_passageiro INT,
    IN p_origem        VARCHAR(200),
    IN p_destino       VARCHAR(200)
)
BEGIN
    DECLARE v_preco             DOUBLE;
    DECLARE v_genero_passageiro CHAR(1);

    -- Calcula o preço usando a mesma fórmula de sp_calcular_preco
    SET v_preco = ROUND(
        5.0 + (CHAR_LENGTH(p_origem) + CHAR_LENGTH(p_destino)) * 0.25,
        2
    );

    -- Busca o gênero do passageiro para armazenar na corrida
    SELECT genero
    INTO   v_genero_passageiro
    FROM   usuarios
    WHERE  id_usuario = p_id_passageiro;

    -- Cria a corrida. id_motorista fica NULL até um motorista aceitar.
    INSERT INTO corridas
        (origem, destino, status, preco, id_passageiro, id_motorista, genero_passageiro)
    VALUES
        (p_origem, p_destino, 'AGUARDANDO', v_preco, p_id_passageiro, NULL, v_genero_passageiro);
END$$


-- ------------------------------------------------------------
-- sp_corridas_passageiro
--   Retorna todas as corridas de um passageiro (histórico).
--   Chamada em: procedureExecutor.corridasPassageiro()
--
--   O JOIN traz os nomes de passageiro e motorista.
--   LEFT JOIN para motorista porque o campo pode ser NULL
--   (corrida ainda sem motorista designado).
--
--   Colunas lidas pelo Java em mapearCorrida():
--     id_corrida, origem, destino, status, preco,
--     id_passageiro, id_motorista, nome_passageiro,
--     nome_motorista, genero_passageiro
-- ------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_corridas_passageiro$$
CREATE PROCEDURE sp_corridas_passageiro(
    IN p_id_passageiro INT
)
BEGIN
    SELECT
        c.id_corrida,
        c.origem,
        c.destino,
        c.status,
        c.preco,
        c.id_passageiro,
        IFNULL(c.id_motorista, 0)   AS id_motorista,   -- 0 quando ainda não há motorista
        u_p.nome                     AS nome_passageiro,
        IFNULL(u_m.nome, '')         AS nome_motorista, -- string vazia quando não há motorista
        (SELECT a.comentario
         FROM avaliacoes a
         WHERE a.id_corrida = c.id_corrida
           AND a.avaliado_id = p_id_passageiro
         LIMIT 1)                   AS comentario,
        c.genero_passageiro
    FROM  corridas  c
    JOIN  usuarios  u_p ON c.id_passageiro = u_p.id_usuario
    LEFT JOIN usuarios u_m ON c.id_motorista  = u_m.id_usuario
    WHERE c.id_passageiro = p_id_passageiro
    ORDER BY c.id_corrida DESC;  -- Mais recente primeiro
END$$


-- ------------------------------------------------------------
-- sp_corridas_motorista
--   Retorna todas as corridas de um motorista (histórico).
--   Chamada em: procedureExecutor.corridasMotorista()
-- ------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_corridas_motorista$$
CREATE PROCEDURE sp_corridas_motorista(
    IN p_id_motorista INT
)
BEGIN
    SELECT
        c.id_corrida,
        c.origem,
        c.destino,
        c.status,
        c.preco,
        c.id_passageiro,
        IFNULL(c.id_motorista, 0)   AS id_motorista,
        u_p.nome                     AS nome_passageiro,
        IFNULL(u_m.nome, '')         AS nome_motorista,
        (SELECT a.comentario
         FROM avaliacoes a
         WHERE a.id_corrida = c.id_corrida
           AND a.avaliado_id = p_id_motorista
         LIMIT 1)                   AS comentario,
        c.genero_passageiro
    FROM  corridas  c
    JOIN  usuarios  u_p ON c.id_passageiro = u_p.id_usuario
    LEFT JOIN usuarios u_m ON c.id_motorista  = u_m.id_usuario
    WHERE c.id_motorista = p_id_motorista
    ORDER BY c.id_corrida DESC;
END$$


-- ------------------------------------------------------------
-- sp_corridas_disponiveis
--   Retorna corridas AGUARDANDO que o motorista pode aceitar.
--   Chamada em: procedureExecutor.corridasDisponiveis()
--
--   REGRA DE GÊNERO IMPLEMENTADA AQUI:
--     A condição WHERE garante que:
--     - Corridas de passageiros MASCULINOS → qualquer motorista vê
--     - Corridas de passageiras FEMININAS  → só motorista feminina vê
--
--   Lógica da condição:
--     (genero_passageiro <> 'F')  → passageiro não é feminino: mostra para todos
--     OR (p_genero_motorista = 'F') → passageiro é feminino mas motorista é feminina: mostra
--     Caso contrário (passageiro F + motorista M): não mostra
-- ------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_corridas_disponiveis$$
CREATE PROCEDURE sp_corridas_disponiveis(
    IN p_genero_motorista CHAR(1)
)
BEGIN
    SELECT
        c.id_corrida,
        c.origem,
        c.destino,
        c.status,
        c.preco,
        c.id_passageiro,
        IFNULL(c.id_motorista, 0)   AS id_motorista,
        u_p.nome                     AS nome_passageiro,
        IFNULL(u_m.nome, '')         AS nome_motorista,
        NULL                         AS comentario,
        c.genero_passageiro
    FROM  corridas  c
    JOIN  usuarios  u_p ON c.id_passageiro = u_p.id_usuario
    LEFT JOIN usuarios u_m ON c.id_motorista  = u_m.id_usuario
    WHERE c.status = 'AGUARDANDO'
      AND (
              c.genero_passageiro <> 'F'      -- Passageiro masculino: qualquer motorista
           OR p_genero_motorista  =  'F'      -- Passageira feminina: só motorista feminina
          )
    ORDER BY c.id_corrida ASC;  -- Mais antigas primeiro (fila por ordem de chegada)
END$$


-- ------------------------------------------------------------
-- sp_aceitar_corrida
--   Motorista aceita uma corrida disponível.
--   Chamada em: procedureExecutor.aceitarCorrida()
--
--   Vincula o motorista à corrida e muda o status.
--   Só aceita corridas com status AGUARDANDO para evitar
--   que dois motoristas aceitem a mesma corrida ao mesmo tempo.
--   Se a corrida já foi aceita por outro motorista, dispara erro.
-- ------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_aceitar_corrida$$
CREATE PROCEDURE sp_aceitar_corrida(
    IN p_id_corrida   INT,
    IN p_id_motorista INT
)
BEGIN
    UPDATE corridas
    SET
        id_motorista = p_id_motorista,
        status       = 'EM_ANDAMENTO'
    WHERE id_corrida = p_id_corrida
      AND status     = 'AGUARDANDO';  -- Condição de segurança: só aceita se ainda disponível

    -- ROW_COUNT() retorna quantas linhas foram afetadas pelo UPDATE.
    -- Se for 0, significa que outra pessoa já aceitou antes.
    IF ROW_COUNT() = 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Corrida nao disponivel para aceite.';
    END IF;
END$$


-- ------------------------------------------------------------
-- sp_recusar_corrida
--   Motorista recusa uma corrida, cancelando-a.
--   Chamada em: procedureExecutor.recusarCorrida()
--
--   Só cancela corridas com status AGUARDANDO.
-- ------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_recusar_corrida$$
CREATE PROCEDURE sp_recusar_corrida(
    IN p_id_corrida INT
)
BEGIN
    UPDATE corridas
    SET status = 'CANCELADA'
    WHERE id_corrida = p_id_corrida
      AND status     = 'AGUARDANDO';
END$$


-- ------------------------------------------------------------
-- sp_atualizar_disponibilidade
--   Alterna o status ONLINE/OFFLINE do motorista.
--   Chamada em: procedureExecutor.atualizarDisponibilidade()
--
--   p_disponivel: 1 = ONLINE (aceita corridas) | 0 = OFFLINE
--   A condição AND tipo = 'MOTORISTA' evita alterar passageiros
--   mesmo que um ID inválido seja enviado por engano.
-- ------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_atualizar_disponibilidade$$
CREATE PROCEDURE sp_atualizar_disponibilidade(
    IN p_id_motorista INT,
    IN p_disponivel   TINYINT(1)
)
BEGIN
    UPDATE usuarios
    SET disponivel = p_disponivel
    WHERE id_usuario = p_id_motorista
      AND tipo       = 'MOTORISTA';  -- Proteção: só motoristas podem ter disponibilidade alterada
END$$


-- ------------------------------------------------------------
-- proc_avaliar_corrida
--   Registra avaliação de uma corrida CONCLUÍDA.
--   Chamada em: procedureExecutor.avaliarCorrida()
--
--   Parâmetros:
--     p_id_corrida  - corrida avaliada
--     p_avaliador   - usuário que faz a avaliação
--     p_avaliado    - usuário que recebe a avaliação
--     p_nota        - nota entre 1 e 5
--     p_comentario  - texto opcional da avaliação
--     p_mensagem    - mensagem de retorno para o app
-- ------------------------------------------------------------
DROP PROCEDURE IF EXISTS proc_avaliar_corrida$$
CREATE PROCEDURE proc_avaliar_corrida(
    IN  p_id_corrida INT,
    IN  p_avaliador  INT,
    IN  p_avaliado   INT,
    IN  p_nota       INT,
    IN  p_comentario TEXT,
    OUT p_mensagem   VARCHAR(255)
)
BEGIN
    DECLARE v_status_corrida VARCHAR(15);
    DECLARE v_id_motorista    INT;
    DECLARE v_id_passageiro   INT;
    DECLARE v_corrida_existente INT DEFAULT 0;

    SELECT COUNT(*)
    INTO   v_corrida_existente
    FROM   corridas
    WHERE  id_corrida = p_id_corrida;

    IF v_corrida_existente = 0 THEN
        SET p_mensagem = 'Corrida nao encontrada.';
    ELSE
        SELECT status, id_motorista, id_passageiro
        INTO   v_status_corrida, v_id_motorista, v_id_passageiro
        FROM   corridas
        WHERE  id_corrida = p_id_corrida
        LIMIT 1;

        IF v_status_corrida <> 'CONCLUIDA' THEN
            SET p_mensagem = 'Apenas corridas concluídas podem ser avaliadas.';
        ELSEIF NOT ((p_avaliador = v_id_passageiro AND p_avaliado = v_id_motorista)
                OR (p_avaliador = v_id_motorista  AND p_avaliado = v_id_passageiro)) THEN
            SET p_mensagem = 'Avaliador ou avaliado inválido para esta corrida.';
        ELSEIF EXISTS (
            SELECT 1
            FROM avaliacoes
            WHERE id_corrida = p_id_corrida
              AND avaliador_id = p_avaliador
        ) THEN
            SET p_mensagem = 'Você já avaliou esta corrida.';
        ELSE
            INSERT INTO avaliacoes (
                id_corrida,
                avaliador_id,
                avaliado_id,
                nota,
                comentario
            ) VALUES (
                p_id_corrida,
                p_avaliador,
                p_avaliado,
                p_nota,
                p_comentario
            );
            SET p_mensagem = 'Avaliacao registrada com sucesso.';
        END IF;
    END IF;
END$$

DELIMITER ;


-- ============================================================
-- PASSO 4: DADOS DE TESTE
--   Espelham exatamente os dados do MockData.java para que
--   o comportamento com banco real seja idêntico ao mock.
-- ============================================================

-- Limpa os dados existentes respeitando as chaves estrangeiras
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE corridas;
TRUNCATE TABLE usuarios;
SET FOREIGN_KEY_CHECKS = 1;

-- ------------------------------------------------------------
-- Usuários de teste
-- Senha de todos: 123456 (em texto puro, somente para testes)
-- ------------------------------------------------------------
INSERT INTO usuarios (id_usuario, nome, email, senha, genero, tipo, disponivel) VALUES
    (1, 'Joao Silva',   'joao@email.com',   '123456', 'M', 'PASSAGEIRO', 0),
    (2, 'Maria Souza',  'maria@email.com',  '123456', 'F', 'PASSAGEIRO', 0),
    (3, 'Carlos Mota',  'carlos@email.com', '123456', 'M', 'MOTORISTA',  1),
    (4, 'Ana Ferreira', 'ana@email.com',    '123456', 'F', 'MOTORISTA',  1);

-- Garante que o próximo usuário cadastrado terá ID 5
ALTER TABLE usuarios AUTO_INCREMENT = 5;

-- ------------------------------------------------------------
-- Corridas históricas de exemplo
-- Preços inseridos manualmente (iguais ao MockData.java)
-- ------------------------------------------------------------
INSERT INTO corridas
    (id_corrida, origem, destino, status, preco, id_passageiro, id_motorista, genero_passageiro)
VALUES
    -- Corrida 1: Joao → Carlos, concluída
    (1, 'Rua das Flores, 10', 'Av. Paulista, 1000',     'CONCLUIDA',  18.50, 1, 3, 'M'),

    -- Corrida 2: Maria → Ana, concluída (regra de gênero aplicada: F → F)
    (2, 'Shopping Iguatemi',  'Aeroporto Internacional', 'CONCLUIDA',  45.00, 2, 4, 'F'),

    -- Corrida 3: Joao, cancelada, sem motorista
    (3, 'Centro Historico, 55', 'Av. Brasil, 200',       'CANCELADA',   0.00, 1, NULL, 'M');

-- Garante que a próxima corrida criada terá ID 4
ALTER TABLE corridas AUTO_INCREMENT = 4;


-- ============================================================
-- VERIFICAÇÃO FINAL
--   Estas queries permitem confirmar que tudo foi criado certo.
--   Execute-as selecionando só esta seção (Ctrl+Shift+Enter).
-- ============================================================

-- Mostra os usuários cadastrados
SELECT id_usuario, nome, email, genero, tipo, disponivel FROM usuarios;

-- Mostra as corridas com os nomes via JOIN (como as stored procedures retornam)
SELECT
    c.id_corrida,
    c.origem,
    c.destino,
    c.status,
    c.preco,
    u_p.nome AS nome_passageiro,
    IFNULL(u_m.nome, '—') AS nome_motorista
FROM  corridas c
JOIN  usuarios u_p ON c.id_passageiro = u_p.id_usuario
LEFT JOIN usuarios u_m ON c.id_motorista = u_m.id_usuario;

-- Testa o login do Joao (deve retornar 1 linha)
CALL sp_login_usuario('joao@email.com', '123456');

-- Testa o cálculo de preço
CALL sp_calcular_preco('Rua das Flores, 10', 'Av. Paulista, 1000', @preco);
SELECT @preco AS preco_calculado;

-- Testa corridas disponíveis para motorista feminina (deve retornar corridas de M e F)
CALL sp_corridas_disponiveis('F');

-- Testa corridas disponíveis para motorista masculino (só retorna corridas de passageiros M)
CALL sp_corridas_disponiveis('M');

-- Testa histórico do passageiro Joao (id=1)
CALL sp_corridas_passageiro(1);

-- Testa histórico do motorista Carlos (id=3)
CALL sp_corridas_motorista(3);
