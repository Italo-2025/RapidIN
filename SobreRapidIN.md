Nenhum selecionado

Pular para o conteúdo
Como usar o E-mail de Serviço Nacional de Aprendizagem Industrial com leitores de tela

1 de 53
(sem assunto)
Caixa de entrada

ANTÔNIA QUINTINO NEVES
28 de mai. de 2026, 19:25 (há 4 dias)
DOCUMENTO 3 — Manual do Usuário Capa sugerida PLATAFORMA DE CORRIDAS Manual do Usuário — v1.0 Este manual foi escrito para você que vai usar o sistema pela prim

ANTÔNIA QUINTINO NEVES
Anexos
19:32 (há 1 hora)
Em qui., 28 de mai. de 2026 às 19:25, ANTÔNIA QUINTINO NEVES <0001194717@senaimgaluno.com.br> escreveu: DOCUMENTO 3 — Manual do Usuário Capa sugerida PLATAFORMA

ANTÔNIA QUINTINO NEVES
Anexos
21:16 (há 1 minuto)
para mim

1 anexo
•  Verificados pelo Gmail
# RapidIN — Documentação do Sistema

**Disciplina:** Programação de Aplicativos  
**Curso:** Desenvolvimento de Sistemas  
**Instituição:** Senai CTTI  
**Integrantes:** Antônia Quintino, Arthur, Italo Adriano, Mainã Rodrigues, Rafael Rezende, Ricardo Luiz, Thales Rodrigues  
**Data:** 05/2026

---

## 1. Visão Geral do Projeto

### 1.1 Introdução

A mobilidade urbana é um desafio crescente nas cidades brasileiras. Diante disso, este projeto propõe o desenvolvimento de uma plataforma de solicitação de corridas que conecta passageiros a motoristas cadastrados de forma ágil e segura. O sistema foi desenvolvido como projeto acadêmico da disciplina de Programação de Aplicativos, utilizando JavaFX para a interface e MySQL para a persistência de dados.

### 1.2 Objetivos

**Objetivo Geral:**  
Desenvolver uma plataforma desktop de solicitação de corridas que permita o gerenciamento de usuários, corridas e motoristas por meio de uma interface gráfica intuitiva integrada a um banco de dados MySQL.

**Objetivos Específicos:**
- Permitir o cadastro de passageiros e motoristas com perfis distintos
- Possibilitar a solicitação de corridas com origem e destino definidos pelo passageiro
- Implementar um mecanismo de encaminhamento de corrida ao motorista disponível mais próximo
- Aplicar a regra de encaminhamento feminino: passageiras do gênero feminino podem optar por motoristas exclusivamente femininas
- Registrar o histórico de corridas de cada usuário
- Garantir a segurança de acesso via autenticação por e-mail e senha

### 1.3 Escopo do Sistema

| O sistema FAZ | O sistema NÃO FAZ |
|---|---|
| Cadastro e login de usuários | Pagamento em tempo real |
| Solicitação e aceite de corridas | Rastreamento GPS em tempo real |
| Filtro de motoristas por gênero | Avaliação de motoristas/passageiros (v1) |
| Histórico de corridas | Integração com mapas externos |
| Gerenciamento via stored procedures | Aplicativo mobile |

### 1.4 Tecnologias Utilizadas

| Camada | Tecnologia | Versão |
|---|---|---|
| Interface (Front-end) | JavaFX + FXML | 21.0.2 |
| Estilização | CSS (JavaFX CSS) | — |
| Linguagem | Java | 17 |
| Banco de Dados | MySQL | 8.x |
| Lógica de negócio | Stored Procedures (MySQL) | — |
| Gerenciador de dependências | Maven | 3.x |
| IDE | IntelliJ IDEA | — |

---

## 2. Diagrama de Casos de Uso

### 2.1 Atores do Sistema

| Ator | Descrição |
|---|---|
| Passageiro | Usuário que solicita corridas |
| Motorista | Usuário que aceita e realiza corridas |
| Administrador | Gerencia usuários e visualiza relatórios (opcional) |
| Sistema | Realiza encaminhamentos e cálculos automáticos |

### 2.2 Lista de Casos de Uso

**Passageiro:**
- UC01 — Realizar cadastro
- UC02 — Fazer login
- UC03 — Solicitar corrida
- UC04 — Informar preferência de motorista feminina
- UC05 — Acompanhar status da corrida
- UC06 — Visualizar histórico de corridas
- UC07 — Cancelar corrida (antes do aceite)

**Motorista:**
- UC08 — Realizar cadastro
- UC09 — Fazer login
- UC10 — Definir disponibilidade (online/offline)
- UC11 — Visualizar corridas disponíveis
- UC12 — Aceitar corrida
- UC13 — Iniciar corrida
- UC14 — Finalizar corrida
- UC15 — Visualizar histórico de corridas realizadas

**Sistema (automático):**
- UC16 — Calcular preço estimado da corrida
- UC17 — Encaminhar corrida ao motorista mais próximo disponível
- UC18 — Aplicar filtro de gênero no encaminhamento

### 2.3 Descrição dos Casos de Uso Principais

#### UC03 — Solicitar Corrida

| Campo | Conteúdo |
|---|---|
| Nome | Solicitar Corrida |
| Ator principal | Passageiro |
| Pré-condição | Passageiro autenticado no sistema |
| Pós-condição | Corrida registrada no banco com status "Aguardando" |
| Fluxo principal | 1. Passageiro informa origem e destino. 2. Sistema calcula preço estimado (UC16). 3. Passageiro confirma a solicitação. 4. Sistema encaminha ao motorista disponível (UC17/UC18). |
| Fluxo alternativo | Se nenhum motorista estiver disponível: sistema exibe mensagem e mantém corrida como "Aguardando". |
| Regra de negócio | Se a passageira ativou o filtro feminino, apenas motoristas do gênero feminino recebem o encaminhamento. |

#### UC12 — Aceitar Corrida

| Campo | Conteúdo |
|---|---|
| Nome | Aceitar Corrida |
| Ator principal | Motorista |
| Pré-condição | Motorista autenticado e com status "Disponível" |
| Pós-condição | Status da corrida atualizado para "Em andamento" |
| Fluxo principal | 1. Sistema exibe corridas disponíveis para o motorista. 2. Motorista seleciona e aceita uma corrida. 3. Status é atualizado no banco. 4. Passageiro é notificado. |

---

## 3. Diagrama Entidade-Relacionamento (DER)

### 3.1 Entidades e Atributos

**USUARIO**
```sql
usuario (
  id_usuario     INT PK AUTO_INCREMENT,
  nome           VARCHAR(100) NOT NULL,
  email          VARCHAR(150) UNIQUE NOT NULL,
  senha_hash     VARCHAR(255) NOT NULL,
  telefone       VARCHAR(20),
  tipo           ENUM('passageiro', 'motorista') NOT NULL,
  genero         ENUM('masculino', 'feminino', 'outro'),
  data_cadastro  DATETIME DEFAULT NOW()
)
```

**MOTORISTA** *(extensão do usuário)*
```sql
motorista (
  id_motorista   INT PK,
  id_usuario     INT FK → usuario,
  cnh            VARCHAR(20) UNIQUE NOT NULL,
  veiculo_modelo VARCHAR(80),
  veiculo_placa  VARCHAR(10),
  disponivel     BOOLEAN DEFAULT FALSE
)
```

**CORRIDA**
```sql
corrida (
  id_corrida        INT PK AUTO_INCREMENT,
  id_passageiro     INT FK → usuario,
  id_motorista      INT FK → motorista (nullable),
  origem            VARCHAR(200) NOT NULL,
  destino           VARCHAR(200) NOT NULL,
  preco_estimado    DECIMAL(8,2),
  preco_final       DECIMAL(8,2),
  status            ENUM('aguardando','em_andamento','concluida','cancelada'),
  filtro_feminino   BOOLEAN DEFAULT FALSE,
  data_solicitacao  DATETIME DEFAULT NOW(),
  data_inicio       DATETIME,
  data_fim          DATETIME
)
```

### 3.2 Relacionamentos

```
USUARIO ──1────1── MOTORISTA
USUARIO ──1────N── CORRIDA  (como passageiro)
MOTORISTA ──1────N── CORRIDA
```

---

## 4. Regras de Negócio

| Código | Descrição | Onde é aplicada |
|---|---|---|
| RN01 | Um usuário só pode ser passageiro OU motorista, não ambos | Cadastro → `sp_cadastrar_usuario` |
| RN02 | Uma corrida só pode ser aceita por motoristas com `disponivel = TRUE` | Encaminhamento → `sp_encaminhar_corrida` |
| RN03 | Se `filtro_feminino = TRUE`, apenas motoristas com `genero = 'feminino'` são encaminhadas | Encaminhamento → `sp_encaminhar_corrida` |
| RN04 | O status segue o fluxo: `aguardando → em_andamento → concluida` ou `aguardando → cancelada` | Atualização → `sp_atualizar_status` |
| RN05 | Uma corrida não pode ser cancelada após ter o status `em_andamento` | Cancelamento → `sp_cancelar_corrida` |
| RN06 | O preço é calculado automaticamente com base em uma tarifa por km | Cálculo → `sp_calcular_preco` |
| RN07 | Não é permitido cadastrar dois usuários com o mesmo e-mail | Cadastro → constraint `UNIQUE` no banco |
| RN08 | O motorista só pode ter uma corrida com status `em_andamento` por vez | Encaminhamento → verificação na procedure |

---

## 5. Requisitos Funcionais e Não Funcionais

### 5.1 Requisitos Funcionais

| Código | Requisito | Prioridade |
|---|---|---|
| RF01 | O sistema deve permitir o cadastro de usuários do tipo passageiro e motorista | Alta |
| RF02 | O sistema deve autenticar usuários por e-mail e senha | Alta |
| RF03 | O passageiro deve poder informar origem e destino para solicitar uma corrida | Alta |
| RF04 | O sistema deve calcular e exibir o preço estimado da corrida antes da confirmação | Média |
| RF05 | O passageiro deve poder ativar o filtro de encaminhamento feminino | Alta |
| RF06 | O sistema deve encaminhar a corrida automaticamente ao motorista disponível compatível | Alta |
| RF07 | O motorista deve visualizar corridas disponíveis e poder aceitar uma delas | Alta |
| RF08 | O sistema deve registrar data/hora de início e fim de cada corrida | Média |
| RF09 | Tanto passageiro quanto motorista devem poder consultar o histórico de corridas | Média |
| RF10 | O passageiro deve poder cancelar uma corrida com status "Aguardando" | Média |

### 5.2 Requisitos Não Funcionais

| Código | Requisito | Categoria |
|---|---|---|
| RNF01 | O sistema deve responder às ações do usuário em no máximo 3 segundos | Desempenho |
| RNF02 | As senhas devem ser armazenadas em formato hash (não em texto puro) | Segurança |
| RNF03 | A interface deve ser intuitiva e utilizável sem treinamento técnico | Usabilidade |
| RNF04 | Toda lógica de negócio deve ser implementada em stored procedures no MySQL | Arquitetura |
| RNF05 | O sistema deve rodar em Windows 10 ou superior com JDK 17 instalado | Portabilidade |
| RNF06 | O banco de dados deve ser normalizado até a 3ª Forma Normal (3FN) | Qualidade de dados |
| RNF07 | O código-fonte deve seguir as convenções de nomenclatura Java (camelCase, PascalCase) | Manutenibilidade |
SobreRapidIN.md
Exibindo SobreRapidIN.md.