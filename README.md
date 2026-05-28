# RapidIN — Plataforma de Mobilidade Urbana

Aplicativo desktop de transporte por aplicativo desenvolvido como projeto educacional de formação. Inspirado no modelo do Uber, com uma regra de negócio central de segurança: **passageiras do gênero feminino são atendidas exclusivamente por motoristas femininas.**

---

## O que o sistema faz

O RapidIN conecta dois tipos de usuário em tempo real:

| Perfil | O que pode fazer |
|---|---|
| **Passageiro** | Cadastrar-se, fazer login, calcular o preço de uma corrida, solicitar corridas e acompanhar o histórico |
| **Motorista** | Cadastrar-se, fazer login, alternar entre online/offline, visualizar corridas disponíveis, aceitar ou recusar corridas e acompanhar o histórico |

### Fluxo de uma corrida

```
Passageiro solicita corrida
        ↓
Sistema cria corrida com status: AGUARDANDO
        ↓
Motorista(s) compatível(is) veem a corrida disponível
        ↓
Motorista aceita → status muda para: EM_ANDAMENTO
        ↓
Corrida concluída → status muda para: CONCLUIDA
```

### Regra de gênero

```
Passageiro masculino  →  qualquer motorista pode aceitar
Passageira feminina   →  apenas motoristas femininas podem aceitar
```

Essa regra é aplicada tanto no filtro do banco de dados (`sp_corridas_disponiveis`) quanto na interface, que informa a passageira feminina sobre o encaminhamento exclusivo.

---

## Tecnologias utilizadas

### Linguagem
- **Java 17** — linguagem principal do projeto

### Interface gráfica
- **JavaFX 21** — framework para criação de interfaces desktop
- **FXML** — arquivos XML que descrevem o layout visual de cada tela separando design de lógica
- **CSS** — estilização das telas (tema escuro com paleta roxa/vermelha)

### Banco de dados
- **MySQL 8.0+** — banco de dados relacional onde usuários e corridas são persistidos
- **Stored Procedures** — toda a lógica de acesso ao banco encapsulada no próprio banco, chamadas via JDBC
- **JDBC** — API padrão do Java para comunicação com bancos relacionais (`CallableStatement`, `ResultSet`)

### Build e dependências
- **Maven** — gerenciador de dependências e build do projeto (`pom.xml`)
- **MySQL Connector/J 8.3** — driver JDBC que permite o Java "falar" com o MySQL

### IDE recomendada
- **IntelliJ IDEA** (Community ou Ultimate)

---

## Arquitetura do projeto

O projeto segue o padrão **MVC (Model — View — Controller)**:

```
Model      → define os dados (o que é um Usuário? o que é uma Corrida?)
View       → define a aparência (arquivos .fxml + .css)
Controller → define o comportamento (o que acontece ao clicar em cada botão)
```

### Estrutura de pastas

```
RapidIN/
│
├── banco_de_dados/
│   └── rapidin_completo.sql          ← Script SQL completo (tabelas + procedures + dados)
│
├── src/main/
│   ├── java/com/rapidIN/
│   │   │
│   │   ├── Main.java                 ← Ponto de entrada do programa
│   │   ├── App.java                  ← Inicializa a janela e gerencia a troca de telas
│   │   ├── SessionManager.java       ← Mantém os dados do usuário logado (padrão Singleton)
│   │   │
│   │   ├── model/
│   │   │   ├── Usuario.java          ← Molde de dados de um usuário (passageiro ou motorista)
│   │   │   └── corrida.java          ← Molde de dados de uma corrida
│   │   │
│   │   ├── database/
│   │   │   ├── conexao.java          ← Abre e mantém a conexão com o MySQL (Singleton)
│   │   │   ├── procedureExecutor.java← Chama todas as stored procedures do banco
│   │   │   └── MockData.java         ← Dados fictícios para desenvolvimento sem banco
│   │   │
│   │   └── controller/
│   │       ├── TelaInicialController.java     ← Lógica da tela de login
│   │       ├── CadastroController.java        ← Lógica da tela de cadastro
│   │       ├── PainelPassageiroController.java← Lógica do painel do passageiro
│   │       ├── PainelMotoristaController.java ← Lógica do painel do motorista
│   │       └── SolicitarCorridaController.java← Lógica da tela de solicitar corrida
│   │
│   └── resources/com/corridas/
│       ├── FXML/
│       │   ├── tela-inicial.fxml      ← Tela de login
│       │   ├── cadastro.fxml          ← Tela de cadastro
│       │   ├── painel-passageiro.fxml ← Painel principal do passageiro
│       │   ├── painel-motorista.fxml  ← Painel principal do motorista
│       │   └── solicitar-corrida.fxml ← Tela de solicitação de corrida
│       └── css/
│           └── estilo.css             ← Tema visual (cores, fontes, botões)
│
└── pom.xml                            ← Dependências Maven (JavaFX + MySQL Connector)
```

### Banco de dados

```
┌──────────────┐         ┌──────────────────┐
│   usuarios   │         │     corridas      │
├──────────────┤         ├──────────────────┤
│ id_usuario   │◄────────│ id_passageiro    │
│ nome         │◄──┐     │ id_motorista     │
│ email        │   └─────│ (FK opcional)    │
│ senha        │         │ id_corrida       │
│ genero       │         │ origem           │
│ tipo         │         │ destino          │
│ disponivel   │         │ status           │
└──────────────┘         │ preco            │
                         │ genero_passageiro│
                         └──────────────────┘
```

**Stored Procedures disponíveis:**

| Procedure | O que faz |
|---|---|
| `sp_login_usuario` | Valida e-mail e senha, retorna dados do usuário |
| `sp_cadastrar_usuario` | Insere novo usuário, rejeita e-mail duplicado |
| `sp_calcular_preco` | Retorna preço estimado da corrida (parâmetro OUT) |
| `sp_solicitar_corrida` | Cria uma nova corrida com status AGUARDANDO |
| `sp_corridas_passageiro` | Retorna o histórico de corridas de um passageiro |
| `sp_corridas_motorista` | Retorna o histórico de corridas de um motorista |
| `sp_corridas_disponiveis` | Lista corridas aguardando, aplicando a regra de gênero |
| `sp_aceitar_corrida` | Vincula motorista à corrida, muda status para EM_ANDAMENTO |
| `sp_recusar_corrida` | Muda status da corrida para CANCELADA |
| `sp_atualizar_disponibilidade` | Alterna o motorista entre ONLINE e OFFLINE |

---

## Como executar

### Pré-requisitos

- Java 17 ou superior instalado
- IntelliJ IDEA (Community ou Ultimate)
- MySQL Server 8.0+ instalado e rodando
- MySQL Workbench (para executar o script SQL)

### 1. Clonar ou abrir o projeto

Abra a pasta `RapidIN` diretamente no IntelliJ IDEA via **File → Open**.

### 2. Configurar o banco de dados

No MySQL Workbench:

1. Abra o arquivo `banco_de_dados/rapidin_completo.sql`
2. Execute com **Ctrl + Shift + Enter**
3. O script cria o banco `RapidIN`, as tabelas, todas as stored procedures e os dados de teste

### 3. Configurar a conexão no Java

Abra `src/main/java/com/rapidIN/database/conexao.java` e edite as três constantes:

```java
private static final String URL     = "jdbc:mysql://localhost:3306/RapidIN";
private static final String USUARIO = "root";         // seu usuário MySQL
private static final String SENHA   = "sua_senha";    // sua senha MySQL
```

### 4. Ativar o banco real

Abra `src/main/java/com/rapidIN/database/procedureExecutor.java` e mude:

```java
// DE:
public static final boolean MOCK_MODE = true;

// PARA:
public static final boolean MOCK_MODE = false;
```

> Enquanto `MOCK_MODE = true`, o sistema usa dados fictícios embutidos no código (`MockData.java`) e **não acessa o banco de dados**. Útil para desenvolvimento sem banco disponível.

### 5. Rodar o projeto

No IntelliJ, configure a **Main Class** como `com.rapidIN.Main` e clique em Run, ou use o Maven:

```bash
mvn javafx:run
```

---

## Usuários de teste (MOCK_MODE ou banco real)

| E-mail | Senha | Perfil | Gênero |
|---|---|---|---|
| `joao@email.com` | `123456` | Passageiro | Masculino |
| `maria@email.com` | `123456` | Passageira | Feminino |
| `carlos@email.com` | `123456` | Motorista | Masculino |
| `ana@email.com` | `123456` | Motorista | Feminino |

> **Dica para testar a regra de gênero:** faça login como `maria@email.com` e solicite uma corrida — apenas `ana@email.com` verá a corrida disponível no painel de motoristas.

---

## Erros mais comuns

| Erro | Causa provável | Solução |
|---|---|---|
| `Communications link failure` | MySQL não está rodando | Inicie o serviço MySQL no seu computador |
| `Access denied for user 'root'` | Senha errada em `conexao.java` | Corrija a variável `SENHA` |
| `Unknown database 'RapidIN'` | Script SQL não foi executado | Execute `rapidin_completo.sql` no Workbench |
| `JavaFX runtime components are missing` | Classe de entrada errada | Confirme que a Main Class está em `com.rapidIN.Main` |
| Tela em branco / `NullPointerException` | `fx:id` no FXML diferente do `@FXML` no Controller | Verifique se os nomes são idênticos |

---

## Avaliação do projeto

| Critério | Peso |
|---|---|
| Funcionalidade (regra de gênero, login, corridas) | 40% |
| Qualidade da documentação | 25% |
| Manual do usuário | 15% |
| Organização do código e do banco | 20% |

---

## Equipe

Projeto desenvolvido durante formação de 10 horas como atividade prática integradora.
