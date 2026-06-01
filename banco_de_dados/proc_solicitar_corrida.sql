-- ################################################################
-- RAPDIN - Procedure simplificada: proc_solicitar_corrida
-- ################################################################
-- O que ela faz:
--   1. Descobre o genero da passageira pelo passageiro_id
--   2. Se genero = 'F', busca APENAS motoristas mulheres online
--   3. Se genero = 'M' ou 'NE', aceita qualquer motorista online
--   4. Se nao tiver motorista F disponivel para passageira F,
--      registra a corrida com status 'SEM_MOTORISTA_FEMININA'
--   5. Insere a corrida na tabela e retorna o id + uma mensagem
-- ################################################################

USE rapdin;

DELIMITER $$

DROP PROCEDURE IF EXISTS proc_solicitar_corrida$$

CREATE PROCEDURE proc_solicitar_corrida(
    IN  p_passageiro_id       INT,          -- id da tabela passageiros
    IN  p_origem_endereco_id  INT,          -- id do endereco de origem
    IN  p_destino_endereco_id INT,          -- id do endereco de destino
    OUT p_corrida_id          INT,          -- retorna o id da corrida criada
    OUT p_mensagem            VARCHAR(100)  -- retorna uma mensagem de resultado
)
BEGIN
    -- variaveis locais
    DECLARE v_genero       ENUM('F','M','NE');  -- genero da passageira
    DECLARE v_motorista_id INT DEFAULT NULL;    -- motorista encontrado (pode ser nulo)
    DECLARE v_status       VARCHAR(40) DEFAULT 'SOLICITADA'; -- status inicial da corrida

    -- ---------------------------------------------------------------
    -- PASSO 1: descobre o genero da passageira
    -- Juntamos passageiros + usuarios pra pegar o genero
    -- ---------------------------------------------------------------
    SELECT u.genero
    INTO   v_genero
    FROM   passageiros p
    JOIN   usuarios    u ON u.id = p.usuario_id
    WHERE  p.id = p_passageiro_id
    LIMIT  1;

    -- ---------------------------------------------------------------
    -- PASSO 2: busca um motorista disponivel respeitando a regra
    --          de genero do app
    -- ---------------------------------------------------------------
    IF v_genero = 'F' THEN
        -- passageira mulher: somente motorista mulher (visao ja filtra)
        SELECT motorista_id
        INTO   v_motorista_id
        FROM   visao_motoristas_femininas_online
        LIMIT  1;

        -- sem motorista feminina: muda o status e avisa
        IF v_motorista_id IS NULL THEN
            SET v_status   = 'SEM_MOTORISTA_FEMININA';
            SET p_mensagem = 'Nenhuma motorista feminina disponivel no momento.';
        ELSE
            SET p_mensagem = 'Motorista feminina encontrada. Corrida solicitada.';
        END IF;

    ELSE
        -- passageiro M ou NE: qualquer motorista online serve
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

    -- ---------------------------------------------------------------
    -- PASSO 3: insere a corrida com os dados montados acima
    -- O gatilho gat_corridas_apos_insert vai registrar no log
    -- automaticamente apos este INSERT.
    -- ---------------------------------------------------------------
    INSERT INTO corridas (
        passageiro_id,
        motorista_id,
        origem_endereco_id,
        destino_endereco_id,
        status,
        solicitada_em
    ) VALUES (
        p_passageiro_id,
        v_motorista_id,       -- pode ser NULL se nao achou motorista
        p_origem_endereco_id,
        p_destino_endereco_id,
        v_status,
        NOW()
    );

    -- retorna o id da linha recem inserida
    SET p_corrida_id = LAST_INSERT_ID();

END$$

DELIMITER ;

-- ################################################################
-- Como testar no MySQL Workbench:
--
-- CALL proc_solicitar_corrida(1, 1, 2, @corrida, @situacao);
-- SELECT @corrida AS corrida_criada, @situacao AS situacao;
--
-- (1 = Ana Silva, passageira F -> deve pegar motorista F)
-- ################################################################
