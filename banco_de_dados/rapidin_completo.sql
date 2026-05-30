-- ###################################################################
-- PROJETO RAPDIN - banco completo (MySQL)  [VERSAO FINAL]
-- app de mobilidade urbana com prioridade pra seguranca de passageiras
-- ###################################################################
--
-- Esse arquivo junta tudo na ordem certa de rodar.
-- E so abrir no Workbench, colar e dar play. Ele cria o banco do zero.
--
-- Combinados do grupo:
--   banco: rapdin / tudo em portugues / snake_case
--   FK com sufixo _id (usuario_id, motorista_id...)
--   genero: F, M, NE
--   tipo_usuario: PASSAGEIRO, MOTORISTA
--   corrida finalizada = CONCLUIDA
--
-- Padrao de nomes (pt-br):
--   proc_   -> procedures
--   visao_  -> views
--   idx_    -> indices
--   gat_    -> gatilhos (triggers)
--   fk_ / chk_ / uq_ -> restricoes
--   p_      -> parametros de procedure
--   v_      -> variaveis locais
-- ###################################################################


-- ===================  banco  =======================================
DROP DATABASE IF EXISTS rapdin;
CREATE DATABASE rapdin CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE rapdin;


-- ===================  usuarios + login  ============================
CREATE TABLE usuarios (
                          id INT PRIMARY KEY AUTO_INCREMENT,
                          nome VARCHAR(150) NOT NULL,
                          email VARCHAR(150) NOT NULL UNIQUE,
                          senha_hash VARCHAR(255) NOT NULL,
                          cpf VARCHAR(14) NOT NULL UNIQUE,
                          telefone VARCHAR(20),
                          genero ENUM('F','M','NE') NOT NULL,
                          tipo_usuario ENUM('PASSAGEIRO','MOTORISTA') NOT NULL,
                          data_nascimento DATE,
                          ativo BOOLEAN DEFAULT TRUE,
                          criado_em DATETIME DEFAULT CURRENT_TIMESTAMP,
                          atualizado_em DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- guarda as tentativas de login (pra seguranca)
CREATE TABLE logs_autenticacao (
                                   id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                   usuario_id INT,
                                   email_tentado VARCHAR(150) NOT NULL,
                                   endereco_ip VARCHAR(45) NOT NULL,
                                   sucesso BOOLEAN NOT NULL,
                                   tentativa_em DATETIME DEFAULT CURRENT_TIMESTAMP,
                                   FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL
) ENGINE=InnoDB;


-- ===================  passageiros + motoristas  ====================
CREATE TABLE passageiros (
                             id INT PRIMARY KEY AUTO_INCREMENT,
                             usuario_id INT NOT NULL UNIQUE,
                             forma_pagamento ENUM('CARTAO_CREDITO','CARTAO_DEBITO','PIX','DINHEIRO') NOT NULL DEFAULT 'PIX',
                             criado_em DATETIME DEFAULT CURRENT_TIMESTAMP,
                             FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
) ENGINE=InnoDB;

CREATE TABLE motoristas (
                            id INT PRIMARY KEY AUTO_INCREMENT,
                            usuario_id INT NOT NULL UNIQUE,
                            numero_cnh VARCHAR(20) NOT NULL UNIQUE,
                            modelo_veiculo VARCHAR(80) NOT NULL,
                            placa_veiculo VARCHAR(8) NOT NULL UNIQUE,
                            status_online ENUM('ONLINE','OFFLINE','EM_CORRIDA') NOT NULL DEFAULT 'OFFLINE',
                            latitude_atual DECIMAL(10,8),
                            longitude_atual DECIMAL(11,8),
                            nota_media DECIMAL(3,2) NOT NULL DEFAULT 5.00,
                            criado_em DATETIME DEFAULT CURRENT_TIMESTAMP,
                            atualizado_em DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
) ENGINE=InnoDB;


-- ===================  enderecos + corridas  ========================
CREATE TABLE enderecos (
                           id INT PRIMARY KEY AUTO_INCREMENT,
                           usuario_id INT NOT NULL,
                           apelido VARCHAR(50),
                           logradouro VARCHAR(150) NOT NULL,
                           cidade VARCHAR(100) NOT NULL,
                           latitude DECIMAL(10,8) NOT NULL,
                           longitude DECIMAL(11,8) NOT NULL,
                           criado_em DATETIME DEFAULT CURRENT_TIMESTAMP,
                           FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
) ENGINE=InnoDB;

-- corrida e o nucleo do sistema. motorista_id pode ficar nulo
-- enquanto ninguem aceita. o status SEM_MOTORISTA_FEMININA e o
-- que registra quando a passageira pede e nao tem motorista mulher.
CREATE TABLE corridas (
                          id INT PRIMARY KEY AUTO_INCREMENT,
                          passageiro_id INT NOT NULL,
                          motorista_id INT,
                          origem_endereco_id INT NOT NULL,
                          destino_endereco_id INT NOT NULL,
                          status ENUM('SOLICITADA','ACEITA','EM_ANDAMENTO','CONCLUIDA','CANCELADA','SEM_MOTORISTA_FEMININA')
        NOT NULL DEFAULT 'SOLICITADA',
                          preco DECIMAL(10,2),
                          distancia_km DECIMAL(6,3),
                          solicitada_em DATETIME DEFAULT CURRENT_TIMESTAMP,
                          aceita_em DATETIME,
                          iniciada_em DATETIME,
                          encerrada_em DATETIME,
                          motivo_cancelamento VARCHAR(200),
                          FOREIGN KEY (passageiro_id) REFERENCES passageiros(id),
                          FOREIGN KEY (motorista_id) REFERENCES motoristas(id),
                          FOREIGN KEY (origem_endereco_id) REFERENCES enderecos(id),
                          FOREIGN KEY (destino_endereco_id) REFERENCES enderecos(id)
) ENGINE=InnoDB;


-- ===================  avaliacoes + logs de status  =================
CREATE TABLE avaliacoes (
                            id INT PRIMARY KEY AUTO_INCREMENT,
                            corrida_id INT NOT NULL,
                            avaliador_id INT NOT NULL,
                            avaliado_id INT NOT NULL,
                            nota TINYINT NOT NULL,
                            comentario TEXT,
                            criado_em DATETIME DEFAULT CURRENT_TIMESTAMP,
                            CONSTRAINT fk_avaliacoes_corrida   FOREIGN KEY (corrida_id)   REFERENCES corridas(id),
                            CONSTRAINT fk_avaliacoes_avaliador FOREIGN KEY (avaliador_id) REFERENCES usuarios(id),
                            CONSTRAINT fk_avaliacoes_avaliado  FOREIGN KEY (avaliado_id)  REFERENCES usuarios(id),
                            CONSTRAINT chk_nota CHECK (nota BETWEEN 1 AND 5),
                            CONSTRAINT uq_avaliacao_por_papel UNIQUE (corrida_id, avaliador_id)
) ENGINE=InnoDB;

CREATE TABLE logs_status_corrida (
                                     id INT PRIMARY KEY AUTO_INCREMENT,
                                     corrida_id INT NOT NULL,
                                     status_anterior VARCHAR(40),
                                     status_novo VARCHAR(40) NOT NULL,
                                     criado_em DATETIME DEFAULT CURRENT_TIMESTAMP,
                                     CONSTRAINT fk_logs_corrida FOREIGN KEY (corrida_id) REFERENCES corridas(id)
) ENGINE=InnoDB;


-- ===================  views  =======================================
-- view geral + view que filtra a partir dela (mais limpo)
CREATE VIEW visao_todos_motoristas_online AS
SELECT
    m.id AS motorista_id,
    u.id AS usuario_id,
    u.nome,
    u.genero,
    m.modelo_veiculo,
    m.placa_veiculo,
    m.latitude_atual,
    m.longitude_atual,
    m.nota_media,
    m.status_online
FROM motoristas m
         INNER JOIN usuarios u ON u.id = m.usuario_id
WHERE m.status_online = 'ONLINE' AND u.ativo = TRUE;

-- view das motoristas mulheres online (aproveita a view de cima)
CREATE VIEW visao_motoristas_femininas_online AS
SELECT *
FROM visao_todos_motoristas_online
WHERE genero = 'F';

-- historico pronto pra mostrar na tela
CREATE VIEW visao_historico_corridas AS
SELECT c.id AS corrida_id, up.nome AS passageiro_nome, up.genero AS passageiro_genero,
       um.nome AS motorista_nome, um.genero AS motorista_genero,
       c.passageiro_id, c.motorista_id,
       eo.logradouro AS origem, ed.logradouro AS destino,
       c.status, c.preco, c.distancia_km, c.solicitada_em, c.encerrada_em
FROM corridas c
         JOIN passageiros p ON c.passageiro_id = p.id
         JOIN usuarios up ON p.usuario_id = up.id
         LEFT JOIN motoristas m ON c.motorista_id = m.id
         LEFT JOIN usuarios um ON m.usuario_id = um.id
         JOIN enderecos eo ON c.origem_endereco_id = eo.id
         JOIN enderecos ed ON c.destino_endereco_id = ed.id;


-- ===================  triggers  ====================================
-- fazem coisas acontecerem automaticamente

DELIMITER $$

-- quando uma corrida e criada, ja registra no log
DROP TRIGGER IF EXISTS gat_corridas_apos_insert$$
CREATE TRIGGER gat_corridas_apos_insert
    AFTER INSERT ON corridas
    FOR EACH ROW
BEGIN
    INSERT INTO logs_status_corrida (corrida_id, status_anterior, status_novo)
    VALUES (NEW.id, NULL, NEW.status);
    END$$

-- quando o status da corrida muda: loga e mexe no motorista
    DROP TRIGGER IF EXISTS gat_corridas_apos_update$$
    CREATE TRIGGER gat_corridas_apos_update
        AFTER UPDATE ON corridas
        FOR EACH ROW
    BEGIN
        IF OLD.status <> NEW.status THEN
        INSERT INTO logs_status_corrida (corrida_id, status_anterior, status_novo)
        VALUES (NEW.id, OLD.status, NEW.status);

        IF NEW.motorista_id IS NOT NULL THEN
            IF NEW.status = 'ACEITA' THEN
        UPDATE motoristas SET status_online = 'EM_CORRIDA' WHERE id = NEW.motorista_id;
        ELSEIF NEW.status IN ('CONCLUIDA', 'CANCELADA') THEN
        UPDATE motoristas SET status_online = 'ONLINE' WHERE id = NEW.motorista_id;
    END IF;
END IF;
END IF;
END$$

-- quando alguem avalia: recalcula a media do motorista
DROP TRIGGER IF EXISTS gat_avaliacoes_apos_insert$$
CREATE TRIGGER gat_avaliacoes_apos_insert
    AFTER INSERT ON avaliacoes
    FOR EACH ROW
BEGIN
    DECLARE v_tipo_usuario VARCHAR(20);

    SELECT tipo_usuario INTO v_tipo_usuario
    FROM usuarios WHERE id = NEW.avaliado_id;

    IF v_tipo_usuario = 'MOTORISTA' THEN
    UPDATE motoristas
    SET nota_media = (SELECT AVG(nota) FROM avaliacoes WHERE avaliado_id = NEW.avaliado_id)
    WHERE usuario_id = NEW.avaliado_id;
END IF;
END$$

DELIMITER ;


-- ###################################################################
-- PROCEDURES
-- ###################################################################

DELIMITER $$

-- ------------------------------------------------------------------
-- 1. proc_solicitar_corrida
--    Regra principal: passageira F, somente motorista F online.
--    M ou NE, qualquer motorista online.
--    Sem motorista feminina disponivel, SEM_MOTORISTA_FEMININA.
-- ------------------------------------------------------------------
DROP PROCEDURE IF EXISTS proc_solicitar_corrida$$

CREATE PROCEDURE proc_solicitar_corrida(
    IN  p_passageiro_id       INT,
    IN  p_origem_endereco_id  INT,
    IN  p_destino_endereco_id INT,
    OUT p_corrida_id          INT,
    OUT p_mensagem            VARCHAR(100)
)
BEGIN
    DECLARE v_usuario_id     INT;
    DECLARE v_genero         ENUM('F','M','NE');
    DECLARE v_motorista_id   INT DEFAULT NULL;
    DECLARE v_status_corrida ENUM(
        'SOLICITADA','ACEITA','EM_ANDAMENTO',
        'CONCLUIDA','CANCELADA','SEM_MOTORISTA_FEMININA'
    ) DEFAULT 'SOLICITADA';

    -- Descobre o genero do passageiro
SELECT u.id, u.genero
INTO   v_usuario_id, v_genero
FROM   passageiros p
           JOIN   usuarios    u ON u.id = p.usuario_id
WHERE  p.id = p_passageiro_id
    LIMIT  1;

IF v_genero = 'F' THEN
SELECT motorista_id
INTO   v_motorista_id
FROM   visao_motoristas_femininas_online
           LIMIT  1;

IF v_motorista_id IS NULL THEN
            SET v_status_corrida = 'SEM_MOTORISTA_FEMININA';
            SET p_mensagem = 'Nenhuma motorista feminina disponivel no momento.';
ELSE
            SET p_mensagem = 'Motorista feminina encontrada. Corrida solicitada.';
END IF;

ELSE
SELECT motorista_id
INTO   v_motorista_id
FROM   visao_todos_motoristas_online
           LIMIT  1;

IF v_motorista_id IS NULL THEN
            SET p_mensagem = 'Nenhum motorista disponivel no momento. Aguardando...';
ELSE
            SET p_mensagem = 'Motorista encontrado. Corrida solicitada.';
END IF;
END IF;

INSERT INTO corridas (
    passageiro_id,
    motorista_id,
    origem_endereco_id,
    destino_endereco_id,
    status,
    solicitada_em
) VALUES (
             p_passageiro_id,
             v_motorista_id,
             p_origem_endereco_id,
             p_destino_endereco_id,
             v_status_corrida,
             NOW()
         );

SET p_corrida_id = LAST_INSERT_ID();
END$$


-- ------------------------------------------------------------------
-- 2. proc_aceitar_corrida
--    Motorista aceita a corrida SOLICITADA.
-- ------------------------------------------------------------------
DROP PROCEDURE IF EXISTS proc_aceitar_corrida$$

CREATE PROCEDURE proc_aceitar_corrida(
    IN  p_corrida_id   INT,
    IN  p_motorista_id INT,
    OUT p_mensagem     VARCHAR(100)
)
BEGIN
    DECLARE v_status_atual      VARCHAR(40);
    DECLARE v_motorista_corrida INT;

SELECT status, motorista_id
INTO   v_status_atual, v_motorista_corrida
FROM   corridas
WHERE  id = p_corrida_id;

IF v_status_atual IS NULL THEN
        SET p_mensagem = 'Corrida nao encontrada.';

    ELSEIF v_status_atual != 'SOLICITADA' THEN
        SET p_mensagem = CONCAT('Corrida nao pode ser aceita. Status atual: ', v_status_atual);

    ELSEIF v_motorista_corrida IS NOT NULL AND v_motorista_corrida != p_motorista_id THEN
        SET p_mensagem = 'Este motorista nao esta designado para esta corrida.';

ELSE
UPDATE corridas
SET    motorista_id = p_motorista_id,
       status       = 'ACEITA',
       aceita_em    = NOW()
WHERE  id = p_corrida_id;

SET p_mensagem = 'Corrida aceita com sucesso.';
END IF;
END$$


-- ------------------------------------------------------------------
-- 3. proc_iniciar_corrida
--    Motorista inicia a corrida ACEITA.
-- ------------------------------------------------------------------
DROP PROCEDURE IF EXISTS proc_iniciar_corrida$$

CREATE PROCEDURE proc_iniciar_corrida(
    IN  p_corrida_id   INT,
    IN  p_motorista_id INT,
    OUT p_mensagem     VARCHAR(100)
)
BEGIN
    DECLARE v_status_atual      VARCHAR(40);
    DECLARE v_motorista_corrida INT;

SELECT status, motorista_id
INTO   v_status_atual, v_motorista_corrida
FROM   corridas
WHERE  id = p_corrida_id;

IF v_status_atual IS NULL THEN
        SET p_mensagem = 'Corrida nao encontrada.';

    ELSEIF v_status_atual != 'ACEITA' THEN
        SET p_mensagem = CONCAT('Corrida nao pode ser iniciada. Status atual: ', v_status_atual);

    ELSEIF v_motorista_corrida != p_motorista_id THEN
        SET p_mensagem = 'Este motorista nao esta designado para esta corrida.';

ELSE
UPDATE corridas
SET    status      = 'EM_ANDAMENTO',
       iniciada_em = NOW()
WHERE  id = p_corrida_id;

SET p_mensagem = 'Corrida iniciada com sucesso.';
END IF;
END$$


-- ------------------------------------------------------------------
-- 4. proc_finalizar_corrida
--    Finaliza a corrida EM_ANDAMENTO. Grava preco e distancia.
-- ------------------------------------------------------------------
DROP PROCEDURE IF EXISTS proc_finalizar_corrida$$

CREATE PROCEDURE proc_finalizar_corrida(
    IN  p_corrida_id   INT,
    IN  p_motorista_id INT,
    IN  p_preco        DECIMAL(10,2),
    IN  p_distancia_km DECIMAL(6,3),
    OUT p_mensagem     VARCHAR(100)
)
BEGIN
    DECLARE v_status_atual      VARCHAR(40);
    DECLARE v_motorista_corrida INT;

SELECT status, motorista_id
INTO   v_status_atual, v_motorista_corrida
FROM   corridas
WHERE  id = p_corrida_id;

IF v_status_atual IS NULL THEN
        SET p_mensagem = 'Corrida nao encontrada.';

    ELSEIF v_status_atual != 'EM_ANDAMENTO' THEN
        SET p_mensagem = CONCAT('Corrida nao pode ser finalizada. Status atual: ', v_status_atual);

    ELSEIF v_motorista_corrida != p_motorista_id THEN
        SET p_mensagem = 'Este motorista nao esta designado para esta corrida.';

ELSE
UPDATE corridas
SET    status       = 'CONCLUIDA',
       encerrada_em = NOW(),
       preco        = p_preco,
       distancia_km = p_distancia_km
WHERE  id = p_corrida_id;

SET p_mensagem = 'Corrida finalizada com sucesso.';
END IF;
END$$


-- ------------------------------------------------------------------
-- 5. proc_alternar_status_motorista
--    Alterna ONLINE <-> OFFLINE. Bloqueia se EM_CORRIDA.
-- ------------------------------------------------------------------
DROP PROCEDURE IF EXISTS proc_alternar_status_motorista$$

CREATE PROCEDURE proc_alternar_status_motorista(
    IN  p_motorista_id INT,
    OUT p_novo_status  VARCHAR(20),
    OUT p_mensagem     VARCHAR(100)
)
BEGIN
    DECLARE v_status_atual ENUM('ONLINE','OFFLINE','EM_CORRIDA');

SELECT status_online
INTO   v_status_atual
FROM   motoristas
WHERE  id = p_motorista_id;

IF v_status_atual IS NULL THEN
        SET p_mensagem    = 'Motorista nao encontrado.';
        SET p_novo_status = NULL;

    ELSEIF v_status_atual = 'EM_CORRIDA' THEN
        SET p_mensagem    = 'Impossivel alterar status: corrida em andamento.';
        SET p_novo_status = 'EM_CORRIDA';

    ELSEIF v_status_atual = 'ONLINE' THEN
UPDATE motoristas SET status_online = 'OFFLINE' WHERE id = p_motorista_id;
SET p_novo_status = 'OFFLINE';
        SET p_mensagem    = 'Motorista agora esta offline.';

ELSE
UPDATE motoristas SET status_online = 'ONLINE' WHERE id = p_motorista_id;
SET p_novo_status = 'ONLINE';
        SET p_mensagem    = 'Motorista agora esta online.';
END IF;
END$$


-- ------------------------------------------------------------------
-- 6. proc_cancelar_corrida
--    Passageiro ou motorista cancela a corrida.
--    Statuses canceláveis: SOLICITADA e ACEITA.
--    Devolve o motorista para ONLINE se havia um designado.
--    p_cancelado_por: 'PASSAGEIRO' ou 'MOTORISTA'
-- ------------------------------------------------------------------
DROP PROCEDURE IF EXISTS proc_cancelar_corrida$$

CREATE PROCEDURE proc_cancelar_corrida(
    IN  p_corrida_id    INT,
    IN  p_cancelado_por VARCHAR(20),
    IN  p_ator_id       INT,
    OUT p_mensagem      VARCHAR(100)
)
BEGIN
    DECLARE v_status        VARCHAR(40);
    DECLARE v_motorista_id  INT;
    DECLARE v_passageiro_id INT;

SELECT status, motorista_id, passageiro_id
INTO   v_status, v_motorista_id, v_passageiro_id
FROM   corridas
WHERE  id = p_corrida_id;

IF v_status IS NULL THEN
        SET p_mensagem = 'Corrida nao encontrada.';

    ELSEIF v_status NOT IN ('SOLICITADA', 'ACEITA') THEN
        SET p_mensagem = CONCAT('Corrida nao pode ser cancelada. Status atual: ', v_status);

    ELSEIF p_cancelado_por = 'PASSAGEIRO' AND v_passageiro_id != p_ator_id THEN
        SET p_mensagem = 'Este passageiro nao e o dono da corrida.';

    ELSEIF p_cancelado_por = 'MOTORISTA' AND v_motorista_id != p_ator_id THEN
        SET p_mensagem = 'Este motorista nao esta designado para esta corrida.';

ELSE
UPDATE corridas
SET    status              = 'CANCELADA',
       motivo_cancelamento = CONCAT('Cancelada por: ', p_cancelado_por)
WHERE  id = p_corrida_id;

-- obs: o gatilho gat_corridas_apos_update ja devolve o motorista
-- para ONLINE, mas mantemos aqui por seguranca caso a regra mude.
IF v_motorista_id IS NOT NULL THEN
UPDATE motoristas
SET    status_online = 'ONLINE'
WHERE  id = v_motorista_id;
END IF;

        SET p_mensagem = CONCAT('Corrida cancelada por: ', p_cancelado_por, '.');
END IF;
END$$


-- ------------------------------------------------------------------
-- 7. proc_avaliar_corrida
--    Insere nota (1-5) apos corrida CONCLUIDA.
--    Bloqueia avaliacao duplicada.
--    O gatilho gat_avaliacoes_apos_insert atualiza nota_media
--    automaticamente apos o INSERT em avaliacoes.
-- ------------------------------------------------------------------
DROP PROCEDURE IF EXISTS proc_avaliar_corrida$$

CREATE PROCEDURE proc_avaliar_corrida(
    IN  p_corrida_id   INT,
    IN  p_avaliador_id INT,
    IN  p_avaliado_id  INT,
    IN  p_nota         TINYINT,
    IN  p_comentario   TEXT,
    OUT p_mensagem     VARCHAR(100)
)
BEGIN
    DECLARE v_status      VARCHAR(40);
    DECLARE v_ja_avaliado INT DEFAULT 0;

SELECT status
INTO   v_status
FROM   corridas
WHERE  id = p_corrida_id;

IF v_status IS NULL THEN
        SET p_mensagem = 'Corrida nao encontrada.';

    ELSEIF v_status != 'CONCLUIDA' THEN
        SET p_mensagem = 'So e possivel avaliar corridas concluidas.';

    ELSEIF p_nota < 1 OR p_nota > 5 THEN
        SET p_mensagem = 'Nota invalida. Use um valor entre 1 e 5.';

ELSE
SELECT COUNT(*)
INTO   v_ja_avaliado
FROM   avaliacoes
WHERE  corrida_id   = p_corrida_id
  AND  avaliador_id = p_avaliador_id;

IF v_ja_avaliado > 0 THEN
            SET p_mensagem = 'Voce ja avaliou esta corrida.';
ELSE
            INSERT INTO avaliacoes (corrida_id, avaliador_id, avaliado_id, nota, comentario, criado_em)
            VALUES (p_corrida_id, p_avaliador_id, p_avaliado_id, p_nota, p_comentario, NOW());

            SET p_mensagem = 'Avaliacao registrada com sucesso.';
END IF;
END IF;
END$$


-- ------------------------------------------------------------------
-- 8. proc_estatisticas_motorista
--    Retorna: total de corridas, concluidas, canceladas,
--    ganho total (R$), distancia total (km) e nota media.
-- ------------------------------------------------------------------
DROP PROCEDURE IF EXISTS proc_estatisticas_motorista$$

CREATE PROCEDURE proc_estatisticas_motorista(
    IN p_motorista_id INT
)
BEGIN
SELECT
    m.id                                                      AS motorista_id,
    u.nome                                                    AS nome,
    COUNT(c.id)                                               AS total_corridas,
    SUM(c.status = 'CONCLUIDA')                               AS corridas_concluidas,
    SUM(c.status = 'CANCELADA')                               AS corridas_canceladas,
    COALESCE(SUM(CASE WHEN c.status = 'CONCLUIDA'
                          THEN c.preco END), 0)                   AS ganho_total_brl,
    COALESCE(SUM(CASE WHEN c.status = 'CONCLUIDA'
                          THEN c.distancia_km END), 0)            AS distancia_total_km,
    m.nota_media                                              AS nota_media
FROM  motoristas m
          JOIN  usuarios   u  ON u.id = m.usuario_id
          LEFT JOIN corridas c ON c.motorista_id = m.id
WHERE m.id = p_motorista_id
GROUP BY m.id, u.nome, m.nota_media;
END$$


-- ------------------------------------------------------------------
-- 9. proc_estatisticas_passageiro
--    Retorna: corridas solicitadas, concluidas, canceladas,
--    vezes sem motorista feminina disponivel e gasto total.
-- ------------------------------------------------------------------
DROP PROCEDURE IF EXISTS proc_estatisticas_passageiro$$

CREATE PROCEDURE proc_estatisticas_passageiro(
    IN p_passageiro_id INT
)
BEGIN
SELECT
    p.id                                                              AS passageiro_id,
    u.nome                                                            AS nome,
    u.genero                                                          AS genero,
    COUNT(c.id)                                                       AS total_corridas_solicitadas,
    SUM(c.status = 'CONCLUIDA')                                       AS corridas_concluidas,
    SUM(c.status = 'CANCELADA')                                       AS corridas_canceladas,
    SUM(c.status = 'SEM_MOTORISTA_FEMININA')                          AS vezes_sem_motorista_feminina,
    COALESCE(SUM(CASE WHEN c.status = 'CONCLUIDA'
                          THEN c.preco END), 0)                           AS gasto_total_brl
FROM  passageiros p
          JOIN  usuarios    u  ON u.id = p.usuario_id
          LEFT JOIN corridas c ON c.passageiro_id = p.id
WHERE p.id = p_passageiro_id
GROUP BY p.id, u.nome, u.genero;
END$$


-- ------------------------------------------------------------------
-- 10. proc_cadastrar_usuario
--     Cria usuario + passageiro OU motorista numa transacao.
--     Valida e-mail e CPF duplicados antes de comecar.
-- ------------------------------------------------------------------
DROP PROCEDURE IF EXISTS proc_cadastrar_usuario$$

CREATE PROCEDURE proc_cadastrar_usuario(
    IN  p_nome               VARCHAR(150),
    IN  p_email              VARCHAR(150),
    IN  p_senha_hash         VARCHAR(255),
    IN  p_cpf                VARCHAR(14),
    IN  p_telefone           VARCHAR(20),
    IN  p_genero             ENUM('F','M','NE'),
    IN  p_tipo_usuario       ENUM('PASSAGEIRO','MOTORISTA'),
    IN  p_forma_pagamento    ENUM('CARTAO_CREDITO','CARTAO_DEBITO','PIX','DINHEIRO'),
    IN  p_numero_cnh         VARCHAR(20),
    IN  p_modelo_veiculo     VARCHAR(80),
    IN  p_placa_veiculo      VARCHAR(8),
    OUT p_novo_usuario_id    INT,
    OUT p_mensagem           VARCHAR(100)
        )
BEGIN
    DECLARE v_email_existe INT DEFAULT 0;
    DECLARE v_cpf_existe   INT DEFAULT 0;

SELECT COUNT(*) INTO v_email_existe FROM usuarios WHERE email = p_email;
SELECT COUNT(*) INTO v_cpf_existe   FROM usuarios WHERE cpf   = p_cpf;

IF v_email_existe > 0 THEN
        SET p_mensagem        = 'E-mail ja cadastrado.';
        SET p_novo_usuario_id = NULL;

    ELSEIF v_cpf_existe > 0 THEN
        SET p_mensagem        = 'CPF ja cadastrado.';
        SET p_novo_usuario_id = NULL;

ELSE
        START TRANSACTION;

INSERT INTO usuarios (nome, email, senha_hash, cpf, telefone, genero, tipo_usuario, ativo, criado_em)
VALUES (p_nome, p_email, p_senha_hash, p_cpf, p_telefone, p_genero, p_tipo_usuario, 1, NOW());

SET p_novo_usuario_id = LAST_INSERT_ID();

        IF p_tipo_usuario = 'PASSAGEIRO' THEN
            INSERT INTO passageiros (usuario_id, forma_pagamento)
            VALUES (p_novo_usuario_id, COALESCE(p_forma_pagamento, 'PIX'));

        ELSEIF p_tipo_usuario = 'MOTORISTA' THEN
            INSERT INTO motoristas (usuario_id, numero_cnh, modelo_veiculo, placa_veiculo, status_online, nota_media)
            VALUES (p_novo_usuario_id, p_numero_cnh, p_modelo_veiculo, p_placa_veiculo, 'OFFLINE', 5.00);
END IF;

COMMIT;
SET p_mensagem = CONCAT(p_tipo_usuario, ' cadastrado com sucesso. usuario_id = ', p_novo_usuario_id);
END IF;
END$$


-- ------------------------------------------------------------------
-- 11. proc_desativar_usuario
--     Desativa o usuario (ativo = 0).
--     Motorista e forcado para OFFLINE.
--     Bloqueia se houver corrida ativa.
-- ------------------------------------------------------------------
DROP PROCEDURE IF EXISTS proc_desativar_usuario$$

CREATE PROCEDURE proc_desativar_usuario(
    IN  p_usuario_id INT,
    OUT p_mensagem   VARCHAR(100)
)
BEGIN
    DECLARE v_tipo_usuario    ENUM('PASSAGEIRO','MOTORISTA');
    DECLARE v_motorista_id    INT DEFAULT NULL;
    DECLARE v_passageiro_id   INT DEFAULT NULL;
    DECLARE v_corridas_ativas INT DEFAULT 0;

SELECT tipo_usuario
INTO   v_tipo_usuario
FROM   usuarios
WHERE  id = p_usuario_id;

IF v_tipo_usuario IS NULL THEN
        SET p_mensagem = 'Usuario nao encontrado.';

ELSE
        IF v_tipo_usuario = 'MOTORISTA' THEN
SELECT id INTO v_motorista_id FROM motoristas WHERE usuario_id = p_usuario_id LIMIT 1;

SELECT COUNT(*)
INTO   v_corridas_ativas
FROM   corridas
WHERE  motorista_id = v_motorista_id
  AND  status IN ('SOLICITADA','ACEITA','EM_ANDAMENTO');
ELSE
SELECT id INTO v_passageiro_id FROM passageiros WHERE usuario_id = p_usuario_id LIMIT 1;

SELECT COUNT(*)
INTO   v_corridas_ativas
FROM   corridas
WHERE  passageiro_id = v_passageiro_id
  AND  status IN ('SOLICITADA','ACEITA','EM_ANDAMENTO');
END IF;

        IF v_corridas_ativas > 0 THEN
            SET p_mensagem = 'Usuario possui corrida em andamento. Desativacao bloqueada.';
ELSE
UPDATE usuarios SET ativo = 0 WHERE id = p_usuario_id;

IF v_tipo_usuario = 'MOTORISTA' AND v_motorista_id IS NOT NULL THEN
UPDATE motoristas SET status_online = 'OFFLINE' WHERE id = v_motorista_id;
END IF;

            SET p_mensagem = 'Usuario desativado com sucesso.';
END IF;
END IF;
END$$


-- ------------------------------------------------------------------
-- 12. proc_atribuir_motorista
--     Tenta designar um motorista a uma corrida SOLICITADA
--     que ainda nao tem motorista_id.
--     Respeita a regra de genero para passageiras F.
-- ------------------------------------------------------------------
DROP PROCEDURE IF EXISTS proc_atribuir_motorista$$

CREATE PROCEDURE proc_atribuir_motorista(
    IN  p_corrida_id INT,
    OUT p_mensagem   VARCHAR(100)
)
BEGIN
    DECLARE v_status        VARCHAR(40);
    DECLARE v_passageiro_id INT;
    DECLARE v_motorista_id  INT DEFAULT NULL;
    DECLARE v_genero        ENUM('F','M','NE');

SELECT status, passageiro_id, motorista_id
INTO   v_status, v_passageiro_id, v_motorista_id
FROM   corridas
WHERE  id = p_corrida_id;

IF v_status IS NULL THEN
        SET p_mensagem = 'Corrida nao encontrada.';

    ELSEIF v_status != 'SOLICITADA' THEN
        SET p_mensagem = CONCAT('Corrida nao esta no status SOLICITADA. Status atual: ', v_status);

    ELSEIF v_motorista_id IS NOT NULL THEN
        SET p_mensagem = 'Esta corrida ja possui motorista designado.';

ELSE
SELECT u.genero
INTO   v_genero
FROM   passageiros p
           JOIN   usuarios    u ON u.id = p.usuario_id
WHERE  p.id = v_passageiro_id;

IF v_genero = 'F' THEN
SELECT motorista_id INTO v_motorista_id FROM visao_motoristas_femininas_online LIMIT 1;
ELSE
SELECT motorista_id INTO v_motorista_id FROM visao_todos_motoristas_online    LIMIT 1;
END IF;

        IF v_motorista_id IS NULL THEN
            SET p_mensagem = CASE v_genero
                WHEN 'F' THEN 'Nenhuma motorista feminina disponivel no momento.'
                ELSE          'Nenhum motorista disponivel no momento.'
END;
ELSE
UPDATE corridas SET motorista_id = v_motorista_id WHERE id = p_corrida_id;
SET p_mensagem = CONCAT('Motorista ', v_motorista_id, ' designado para a corrida ', p_corrida_id, '.');
END IF;
END IF;
END$$

DELIMITER ;


-- ===================  indices  =====================================
-- deixam as buscas mais rapidas
CREATE INDEX idx_usuarios_genero    ON usuarios(genero);
CREATE INDEX idx_usuarios_tipo      ON usuarios(tipo_usuario);
CREATE INDEX idx_motoristas_status  ON motoristas(status_online);
CREATE INDEX idx_corridas_status    ON corridas(status);
CREATE INDEX idx_corridas_passageiro ON corridas(passageiro_id);
CREATE INDEX idx_corridas_motorista ON corridas(motorista_id);


-- ###################################################################
-- DADOS DE TESTE
-- ###################################################################

-- usuarios
INSERT INTO usuarios (nome, email, senha_hash, cpf, telefone, genero, tipo_usuario, data_nascimento) VALUES
                                                                                                         ('Ana Silva',      'ana.silva@email.com',      '$2y$10$fakehash', '111.111.111-11', '11999991111', 'F',  'PASSAGEIRO', '1995-03-15'),
                                                                                                         ('Beatriz Costa',  'beatriz.costa@email.com',  '$2y$10$fakehash', '222.222.222-22', '11999992222', 'F',  'PASSAGEIRO', '1998-07-22'),
                                                                                                         ('Carlos Mendes',  'carlos.mendes@email.com',  '$2y$10$fakehash', '333.333.333-33', '11999993333', 'M',  'PASSAGEIRO', '1990-05-20'),
                                                                                                         ('Diego Rocha',    'diego.rocha@email.com',    '$2y$10$fakehash', '444.444.444-44', '11999994444', 'M',  'PASSAGEIRO', '1993-09-30'),
                                                                                                         ('Alex Oliveira',  'alex.oliveira@email.com',  '$2y$10$fakehash', '555.555.555-55', '11999995555', 'NE', 'PASSAGEIRO', '1997-02-14'),
                                                                                                         ('Fernanda Lima',  'fernanda.lima@email.com',  '$2y$10$fakehash', '666.666.666-66', '11999996666', 'F',  'MOTORISTA',  '1985-04-25'),
                                                                                                         ('Gabriela Souza', 'gabriela.souza@email.com', '$2y$10$fakehash', '777.777.777-77', '11999997777', 'F',  'MOTORISTA',  '1988-08-12'),
                                                                                                         ('Henrique Dias',  'henrique.dias@email.com',  '$2y$10$fakehash', '888.888.888-88', '11999998888', 'M',  'MOTORISTA',  '1982-12-05'),
                                                                                                         ('Igor Martins',   'igor.martins@email.com',   '$2y$10$fakehash', '999.999.999-99', '11999999999', 'M',  'MOTORISTA',  '1987-06-18'),
                                                                                                         ('Jordan Santos',  'jordan.santos@email.com',  '$2y$10$fakehash', '000.000.000-00', '11999990000', 'NE', 'MOTORISTA',  '1991-10-08');

-- passageiros (usuarios 1 a 5)
INSERT INTO passageiros (usuario_id, forma_pagamento) VALUES
                                                          (1, 'PIX'), (2, 'CARTAO_CREDITO'), (3, 'DINHEIRO'), (4, 'PIX'), (5, 'CARTAO_DEBITO');

-- motoristas (usuarios 6 a 10) -- Fernanda e Gabriela sao as mulheres
INSERT INTO motoristas (usuario_id, numero_cnh, modelo_veiculo, placa_veiculo, status_online, latitude_atual, longitude_atual) VALUES
                                                                                                                                   (6,  'CNH0000001', 'Honda Civic',    'ABC1D23', 'ONLINE',  -19.916810, -43.934930),
                                                                                                                                   (7,  'CNH0000002', 'Toyota Corolla', 'XYZ9K87', 'ONLINE',  -19.920000, -43.940000),
                                                                                                                                   (8,  'CNH0000003', 'VW Gol',         'GHI4F56', 'ONLINE',  -19.925000, -43.938000),
                                                                                                                                   (9,  'CNH0000004', 'Fiat Argo',      'JKL7M89', 'OFFLINE', -19.930000, -43.932000),
                                                                                                                                   (10, 'CNH0000005', 'Chevrolet Onix', 'PQR2S34', 'ONLINE',  -19.918000, -43.945000);

-- enderecos (usuarios 1 a 5)
INSERT INTO enderecos (usuario_id, apelido, logradouro, cidade, latitude, longitude) VALUES
                                                                                         (1, 'Casa',     'Rua das Flores, 10',     'Belo Horizonte', -19.916700, -43.934500),
                                                                                         (1, 'Trabalho', 'Av. Afonso Pena, 1500',  'Belo Horizonte', -19.925000, -43.938900),
                                                                                         (2, 'Casa',     'Rua da Bahia, 200',      'Belo Horizonte', -19.921000, -43.941000),
                                                                                         (3, 'Casa',     'Rua Espirito Santo, 50', 'Belo Horizonte', -19.930000, -43.932000),
                                                                                         (4, 'Casa',     'Rua Paraiba, 320',       'Belo Horizonte', -19.935000, -43.927000),
                                                                                         (5, 'Casa',     'Av. Brasil, 900',        'Belo Horizonte', -19.942000, -43.935000);

-- corridas (uma de cada status)
INSERT INTO corridas (passageiro_id, motorista_id, origem_endereco_id, destino_endereco_id, status, solicitada_em) VALUES
    (3, NULL, 4, 5, 'SOLICITADA', NOW());

INSERT INTO corridas (passageiro_id, motorista_id, origem_endereco_id, destino_endereco_id, status, solicitada_em, aceita_em) VALUES
    (1, 1, 1, 2, 'ACEITA', NOW() - INTERVAL 3 MINUTE, NOW());

INSERT INTO corridas (passageiro_id, motorista_id, origem_endereco_id, destino_endereco_id, status, solicitada_em, aceita_em, iniciada_em) VALUES
    (3, 3, 4, 5, 'EM_ANDAMENTO', NOW() - INTERVAL 10 MINUTE, NOW() - INTERVAL 8 MINUTE, NOW() - INTERVAL 5 MINUTE);

INSERT INTO corridas (passageiro_id, motorista_id, origem_endereco_id, destino_endereco_id, status, preco, distancia_km, solicitada_em, aceita_em, iniciada_em, encerrada_em) VALUES
    (1, 1, 1, 2, 'CONCLUIDA', 18.50, 4.200, NOW() - INTERVAL 1 HOUR, NOW() - INTERVAL 58 MINUTE, NOW() - INTERVAL 55 MINUTE, NOW() - INTERVAL 40 MINUTE);

INSERT INTO corridas (passageiro_id, motorista_id, origem_endereco_id, destino_endereco_id, status, solicitada_em, aceita_em) VALUES
    (3, 3, 4, 5, 'CANCELADA', NOW() - INTERVAL 2 HOUR, NOW() - INTERVAL 118 MINUTE);

INSERT INTO corridas (passageiro_id, motorista_id, origem_endereco_id, destino_endereco_id, status, solicitada_em) VALUES
    (2, NULL, 3, 1, 'SEM_MOTORISTA_FEMININA', NOW() - INTERVAL 30 MINUTE);


-- ###################################################################
-- TESTES (roda depois pra ver se ta tudo certo)
-- ###################################################################

-- login: busca a Ana pra conferir senha
SELECT id, senha_hash, genero, tipo_usuario, ativo
FROM usuarios WHERE email = 'ana.silva@email.com';

-- regra feminina: Ana (passageira F) pede corrida -> tem que vir motorista F
CALL proc_solicitar_corrida(1, 1, 2, @corrida, @situacao);
SELECT @corrida AS corrida_criada, @situacao AS situacao;

-- passageiro homem pede corrida -> qualquer motorista
CALL proc_solicitar_corrida(3, 4, 5, @corrida, @situacao);
SELECT @corrida AS corrida_criada, @situacao AS situacao;

-- ve as motoristas mulheres online (tem que aparecer Fernanda e Gabriela)
SELECT * FROM visao_motoristas_femininas_online;

-- historico completo
SELECT * FROM visao_historico_corridas ORDER BY solicitada_em DESC;


-- ===================================================================
-- TESTE DO CICLO DE VIDA DA CORRIDA (usando as procedures)
-- mostra todas as triggers funcionando: log de status, mudanca de
-- status do motorista (EM_CORRIDA / ONLINE) e calculo da nota media
-- ===================================================================

-- pega os ids primeiro (Ana = passageiro 1, Fernanda = motorista 1)
SET @passageiro_id := (SELECT id FROM passageiros WHERE usuario_id = 1);
SET @motorista_id  := (SELECT id FROM motoristas  WHERE usuario_id = 6);
SET @origem_id     := (SELECT id FROM enderecos   WHERE usuario_id = 1 LIMIT 1);
SET @destino_id    := (SELECT id FROM enderecos   WHERE usuario_id = 1 LIMIT 1 OFFSET 1);

-- 1) passageira solicita (regra feminina escolhe motorista F)
CALL proc_solicitar_corrida(@passageiro_id, @origem_id, @destino_id, @corrida_id, @situacao);
SELECT 'APOS SOLICITAR' AS etapa, @corrida_id AS corrida, @situacao AS situacao;
SELECT 'LOGS APOS SOLICITAR' AS etapa FROM logs_status_corrida WHERE corrida_id = @corrida_id;

-- garante motorista designado pra continuar o teste do ciclo
CALL proc_atribuir_motorista(@corrida_id, @msg);
SELECT 'ATRIBUIR' AS etapa, @msg AS mensagem;

-- 2) motorista aceita
CALL proc_aceitar_corrida(@corrida_id, @motorista_id, @msg);
SELECT 'APOS ACEITAR' AS etapa, @msg AS mensagem;
SELECT 'MOTORISTA APOS ACEITAR' AS etapa, status_online FROM motoristas WHERE id = @motorista_id;
SELECT 'LOGS APOS ACEITAR' AS etapa, * FROM logs_status_corrida WHERE corrida_id = @corrida_id;

-- 3) motorista inicia
CALL proc_iniciar_corrida(@corrida_id, @motorista_id, @msg);
SELECT 'APOS INICIAR' AS etapa, @msg AS mensagem;
SELECT 'LOGS APOS INICIAR' AS etapa, * FROM logs_status_corrida WHERE corrida_id = @corrida_id;

-- 4) motorista finaliza
CALL proc_finalizar_corrida(@corrida_id, @motorista_id, 25.50, 8.300, @msg);
SELECT 'APOS FINALIZAR' AS etapa, @msg AS mensagem;
SELECT 'MOTORISTA APOS FINALIZAR' AS etapa, status_online FROM motoristas WHERE id = @motorista_id;
SELECT 'LOGS COMPLETOS' AS etapa, * FROM logs_status_corrida WHERE corrida_id = @corrida_id ORDER BY id;

-- 5) avaliacoes (passageiro avalia motorista e vice-versa)
CALL proc_avaliar_corrida(@corrida_id, 1, 6, 5, 'Motorista muito atenciosa', @msg);
SELECT 'AVALIACAO 1' AS etapa, @msg AS mensagem;
CALL proc_avaliar_corrida(@corrida_id, 6, 1, 5, 'Passageira tranquila', @msg);
SELECT 'AVALIACAO 2' AS etapa, @msg AS mensagem;

SELECT 'MEDIA DA MOTORISTA' AS etapa, nota_media FROM motoristas WHERE id = @motorista_id;

-- 6) corrida final completa
SELECT 'CORRIDA FINAL' AS etapa, id, status, aceita_em, iniciada_em, encerrada_em, preco
FROM corridas WHERE id = @corrida_id;

-- 7) estatisticas
CALL proc_estatisticas_motorista(@motorista_id);
CALL proc_estatisticas_passageiro(@passageiro_id);

-- ---------------------------------------------------------------
-- PROCEDURES COMPLEMENTARES
-- Criadas para alinhar com o procedureExecutor.java
-- ---------------------------------------------------------------

DELIMITER $$

DROP PROCEDURE IF EXISTS proc_login_usuario$$

CREATE PROCEDURE proc_login_usuario(
    IN email VARCHAR (100),
    IN senha_hash VARCHAR (255)
)
BEGIN
    SELECT
        id AS usuario_id,
        nome,
        email,
        genero,
        tipo_usuario AS tipo,
        ativo AS disponivel
    FROM usuarios WHERE email =p.email AND senha_hash = p.senha_hash ANF ativo = TRUE;
    LIMIT 1;
END$$


DROP PROCEDURE IF EXISTS proc_solicitar_corrida$$

CREATE PROCEDURE proc_solicitar_corrida(
    IN p_passageiro_id INT,
    IN p_origem VARCHAR(150),
    IN p_destino VARCHAR(150)
    OUT p_corrida_id INT,
    OUT p_mensagem VARCHAR(100)
)
BEGIN
    DECLARE v_origem_id INT;
    DECLARE v_destino_id INT;

    INSERT INTO enderecos (usuario_id, logradouro, cidade,latitude, longitude)
    VALUES (p_passageiro_id, p_origem, 'nao informada', 00, 00)
    SET v_origem_id = LAST_INSERT_ID();

    INSERT INTO enderecos (usuario_id, logradouro, cidade,latitude, longitude)
    VALUES (p_passageiro_id, p_destino, 'nao informada', 00, 00)
    SET v_destino_id = LAST_INSERT_ID();

    INSERT INTO corridas (passageiro_id, origem_endereco_id, destino_endereco_id)
    VALUES (p_passageiro_id, v_origem_id, v_destino_id)

    SET p_corrida_id = LAST_INSERT_ID();
    SET p_mensagem = 'Corrida solicitada com sucesso!';
END$$

DROP PROCEDURE IF EXISTS proc_corridas_disponiveis$$

CREATE PROCEDURE proc_corridas_disponiveis(
    IN p_genero_motorista VARCHAR (2)
)
    BEGIN
    SELECT
        c.id                AS id_corrida,
        eo.logradouro       AS origem,
        ed.logradouro       AS destino,
        c.status,
        c.preco,
        c.passageiro_id     AS id_passageiro,
        c.motorista_id      AS id_motorista,
        up.nome             AS nome_passageiro,
        um.nome             AS nome_motorista,
        up.genero           AS genero_passageiro
    FROM corridas c
             JOIN passageiros p  ON p.id       = c.passageiro_id
             JOIN usuarios up    ON up.id      = p.usuario_id
             LEFT JOIN motoristas m  ON m.id   = c.motorista_id
             LEFT JOIN usuarios um   ON um.id  = m.usuario_id
             JOIN enderecos eo   ON eo.id      = c.origem_endereco_id
             JOIN enderecos ed   ON ed.id      = c.destino_endereco_id
    WHERE c.status = 'SOLICITADA'
      AND (p_genero_motorista != 'F' OR up.genero = 'F');
DELIMITER;

