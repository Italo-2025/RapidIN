<div align="center">

<img src="https://readme-typing-svg.demolab.com?font=Fira+Code&weight=900&size=42&pause=1000&color=E53935&center=true&vCenter=true&width=700&lines=🚗+RapidIN;Plataforma+de+Mobilidade+Urbana;Segurança+%26+Velocidade" alt="Typing SVG" />

<br/>

![Java](https://img.shields.io/badge/Java-17-E53935?style=for-the-badge&logo=openjdk&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-21-FF6F00?style=for-the-badge&logo=java&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.3-FF6D00?style=for-the-badge&logo=mysql&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.11-E91E63?style=for-the-badge&logo=apachemaven&logoColor=white)
![Status](https://img.shields.io/badge/Status-Em%20Desenvolvimento-FFD600?style=for-the-badge)

<br/>

> 🔴 **App de mobilidade urbana** com foco em **segurança feminina** — passageiras mulheres são atendidas **exclusivamente** por motoristas mulheres.

---

</div>

## 🎨 Paleta de Cores do Projeto

<div align="center">

| 🔴 Vermelho Principal | 🟠 Laranja Vibrante | 🟡 Ouro Destaque | 🌸 Rosa Acento | 🟣 Roxo Profundo | ⚫ Preto Elegante |
|:---:|:---:|:---:|:---:|:---:|:---:|
| `#E53935` | `#FF6F00` | `#FFD600` | `#E91E63` | `#7B1FA2` | `#212121` |
| Ação principal | Avisos | Destaques | Feminino | Autoridade | Texto base |

</div>

---

## 🚀 O que é o RapidIN?

O **RapidIN** é uma plataforma de corridas urbanas desenvolvida como projeto acadêmico em **10 horas** de desenvolvimento intenso. Inspirado no modelo do Uber, o sistema conecta passageiros a motoristas com uma diferença crucial:

```
🛡️  REGRA DE OURO DO RAPIDIN
═══════════════════════════════════════════════════════
  Passageira FEMININA  →  somente Motorista FEMININA
  Passageiro MASCULINO →  qualquer motorista disponível
═══════════════════════════════════════════════════════
```

> 💡 **Por que essa regra existe?**
> A segurança da passageira é prioridade absoluta. O sistema filtra automaticamente as corridas disponíveis para o motorista com base no gênero, sem que nenhum dos dois precise fazer nada — a lógica está no banco de dados, nas stored procedures.

---

## 🧱 Stack Tecnológica

<div align="center">

```
┌─────────────────────────────────────────────────────┐
│                   🖥️  CAMADA VISUAL                  │
│              JavaFX 21  +  FXML  +  CSS              │
├─────────────────────────────────────────────────────┤
│                  ⚙️  CAMADA DE LÓGICA                 │
│          Java 17  |  Padrão MVC  |  Maven            │
├─────────────────────────────────────────────────────┤
│                  🗄️  CAMADA DE DADOS                  │
│        MySQL 8  |  JDBC  |  Stored Procedures        │
└─────────────────────────────────────────────────────┘
```

</div>

| Tecnologia | Versão | Para que serve |
|---|---|---|
| ☕ **Java** | 17 LTS | Linguagem principal — toda a lógica do app |
| 🎨 **JavaFX** | 21 | Interface gráfica (janelas, botões, tabelas) |
| 📄 **FXML** | — | Define a estrutura visual das telas em XML |
| 🎨 **CSS** | — | Estiliza a interface (cores, fontes, espaçamentos) |
| 🗄️ **MySQL** | 8.3 | Banco de dados relacional com stored procedures |
| 🔌 **JDBC** | — | Driver que conecta o Java ao MySQL |
| 🏗️ **Maven** | 3.11 | Gerencia dependências e build do projeto |

---

## 🗂️ Estrutura de Pacotes — O Mapa do Código

```
📁 com.rapidIN/
│
├── 📄 Main.java              ← 🚪 Porta de entrada do programa
├── 📄 App.java               ← 🏠 Gerencia a janela e troca de telas
├── 📄 SessionManager.java    ← 🪪 Guarda quem está logado (Singleton)
│
├── 📁 model/                 ← 🧬 Moldes dos dados
│   ├── Usuario.java          ← 👤 Representa um usuário (passageiro ou motorista)
│   └── corrida.java          ← 🚗 Representa uma corrida
│
├── 📁 database/              ← 🗄️ Tudo que fala com o banco
│   ├── conexao.java          ← 🔌 Singleton de conexão MySQL (abre/fecha)
│   ├── procedureExecutor.java← ⚙️  Executa todas as stored procedures
│   └── MockData.java         ← 🎭 Dados falsos para testar sem banco real
│
└── 📁 controller/            ← 🕹️  Controlam cada tela
    ├── TelaInicialController.java      ← Login
    ├── CadastroController.java         ← Novo cadastro
    ├── PainelPassageiroController.java ← Tela do passageiro
    ├── PainelMotoristaController.java  ← Tela do motorista
    └── SolicitarCorridaController.java ← Solicitação de corrida
```

---

## 🧠 Padrão MVC — Por que o código é dividido assim?

> O projeto usa o padrão **Model-View-Controller**, que separa responsabilidades em três camadas. Isso evita que o código vire um "macarrão" impossível de manter. 🍝

<div align="center">

```
  📄 FXML (View)          🕹️ Controller           🧬 Model
  ──────────────          ─────────────           ────────
  O usuário vê         Recebe o clique         Dados puros:
  botões, campos  ──▶  e decide o que   ──▶   Usuario.java
  e tabelas            fazer com ele          corrida.java
       ▲                      │
       └──────────────────────┘
          Atualiza a tela com
          o resultado
```

</div>

| Camada | Arquivos | Responsabilidade |
|---|---|---|
| 🎨 **View** | `*.fxml`, `estilo.css` | Como a tela parece |
| 🕹️ **Controller** | `*Controller.java` | O que acontece quando o usuário clica |
| 🧬 **Model** | `Usuario.java`, `corrida.java` | Como os dados são estruturados |

---

## 🗄️ Banco de Dados — Stored Procedures

> 🤔 **O que são Stored Procedures?**
> São "receitas prontas" guardadas **dentro do banco de dados**. Em vez do Java escrever SQL complexo, ele simplesmente chama uma função — como pedir uma pizza pelo app sem precisar saber a receita. 🍕

### 📋 Procedures disponíveis

| 🔴 Procedure | Parâmetros | O que faz |
|---|---|---|
| `proc_login_usuario` | email, senha_hash | Autentica o usuário |
| `proc_cadastrar_usuario` | nome, email, senha, cpf... | Cria conta de passageiro ou motorista |
| `proc_solicitar_corrida` | passageiro_id, origem, destino | Cria uma nova corrida |
| `proc_aceitar_corrida` | corrida_id, motorista_id | Motorista aceita uma corrida |
| `proc_iniciar_corrida` | corrida_id, motorista_id | Corrida começa |
| `proc_finalizar_corrida` | corrida_id, motorista_id, preco, km | Corrida termina |
| `proc_cancelar_corrida` | corrida_id, cancelado_por, ator_id | Cancela uma corrida |
| `proc_corridas_disponiveis` | genero_motorista | Lista corridas filtradas por gênero |
| `proc_corridas_passageiro` | passageiro_id | Histórico do passageiro |
| `proc_corridas_motorista` | motorista_id | Histórico do motorista |
| `proc_calcular_preco` | origem, destino | Retorna preço estimado |
| `proc_estatisticas_motorista` | motorista_id | Ganhos, corridas, nota média |
| `proc_estatisticas_passageiro` | passageiro_id | Gastos e histórico |
| `proc_desativar_usuario` | usuario_id | Desativa conta |
| `proc_avaliar_corrida` | corrida_id, nota, comentario | Avaliação pós-corrida |

### 🔄 Ciclo de vida de uma corrida

```
  🟡 SOLICITADA
      │
      ▼
  🟠 ACEITA          ◀── Motorista aceita na lista
      │
      ▼
  🔴 EM_ANDAMENTO    ◀── Motorista clica "Iniciar"
      │
      ├──▶ ✅ CONCLUIDA    ◀── Motorista clica "Finalizar"
      │
      └──▶ ❌ CANCELADA    ◀── Passageiro ou motorista cancela
```

---

## 🔐 Segurança — Como as senhas são protegidas?

```java
// ❌ ERRADO — nunca fazer isso:
stmt.setString(2, senha); // texto puro no banco = desastre

// ✅ CERTO — o que o RapidIN faz:
stmt.setString(2, hashSenha(senha)); // SHA-256 antes de enviar
```

> 🛡️ **SHA-256** é uma função matemática de "mão única" — dado o hash, é impossível descobrir a senha original. O banco nunca armazena sua senha real, apenas a "impressão digital" dela.

```
Senha digitada: "minha123"
         │
         ▼  SHA-256
Hash:    "a7f3c2e9d1b..."  ← isso vai pro banco
```

---

## 🎭 Mock Mode — Desenvolvimento sem banco de dados

O `procedureExecutor.java` tem uma chave mágica:

```java
// procedureExecutor.java — linha 50
public static final boolean MOCK_MODE = false; // 🔴 Banco real
//                                    = true;  // 🎭 Dados falsos
```

| Modo | Valor | Quando usar |
|---|---|---|
| 🎭 **Mock** | `true` | Desenvolvendo sem MySQL instalado |
| 🗄️ **Real** | `false` | Demo final ou com banco configurado |

---

## ⚙️ Padrão Singleton — Uma conexão só, sem desperdício

> 🤔 **Por que Singleton?**
> Abrir uma conexão com banco de dados é caro (lento). Se cada operação abrisse uma nova conexão, o sistema travaria. O Singleton garante que existe **apenas uma conexão** reutilizada por todo o app.

```java
// conexao.java — a "linha dedicada" com o banco
public static Connection getConexao() {
    if (conexaoAtiva == null || conexaoAtiva.isClosed()) {
        conexaoAtiva = DriverManager.getConnection(URL, USUARIO, SENHA);
        // ☝️ Só conecta se ainda não tem conexão aberta
    }
    return conexaoAtiva; // Sempre devolve a mesma
}
```

O mesmo padrão é usado no `SessionManager.java` para guardar o usuário logado.

---

## 🖥️ Telas do Sistema

| Tela | Arquivo FXML | Controller | Acesso |
|---|---|---|---|
| 🔐 **Login** | `tela-inicial.fxml` | `TelaInicialController` | Todos |
| 📝 **Cadastro** | `cadastro.fxml` | `CadastroController` | Todos |
| 👤 **Painel Passageiro** | `painel-passageiro.fxml` | `PainelPassageiroController` | Passageiro |
| 🚗 **Painel Motorista** | `painel-motorista.fxml` | `PainelMotoristaController` | Motorista |

---

## ▶️ Como rodar o projeto

### 1️⃣ Pré-requisitos

```
✅ Java 17+        → https://adoptium.net
✅ MySQL 8+        → https://dev.mysql.com/downloads
✅ Maven 3.8+      → https://maven.apache.org
✅ IntelliJ IDEA   → recomendado (ou VS Code com extensão Java)
```

### 2️⃣ Configurar o banco de dados

```sql
-- Abra o MySQL Workbench e execute:
-- File > Open SQL Script > banco_de_dados/rapidin_completo.sql
-- Clique no ⚡ (Execute Script)
```

### 3️⃣ Configurar a conexão Java

```java
// src/main/java/com/rapidIN/database/conexao.java
private static final String URL     = "jdbc:mysql://localhost:3306/rapdin";
private static final String USUARIO = "root";          // ← seu usuário MySQL
private static final String SENHA   = "sua_senha";     // ← sua senha MySQL
```

### 4️⃣ Rodar o projeto

```bash
# Via Maven (terminal)
mvn javafx:run

# Via IntelliJ
# Run > Run 'Main'
```

---

## 👥 Usuários de Teste

> 🔑 Senha de todos: **`123456`**

| 👤 Nome | 📧 Email | 🚻 Gênero | 🏷️ Tipo |
|---|---|---|---|
| Ana Silva | `ana.silva@email.com` | Feminino | 👩 Passageira |
| Beatriz Costa | `beatriz.costa@email.com` | Feminino | 👩 Passageira |
| Carlos Mendes | `carlos.mendes@email.com` | Masculino | 👨 Passageiro |
| Fernanda Lima | `fernanda.lima@email.com` | Feminino | 👩‍✈️ Motorista 🟢 |
| Gabriela Souza | `gabriela.souza@email.com` | Feminino | 👩‍✈️ Motorista 🟢 |
| Henrique Dias | `henrique.dias@email.com` | Masculino | 👨‍✈️ Motorista 🟢 |

---

## 🏗️ Estrutura do Banco de Dados

```
┌──────────┐     ┌────────────┐     ┌──────────────┐
│ usuarios │──┬──│ passageiros│     │  motoristas  │
│          │  └──│            │     │              │
│ id       │     │ usuario_id │     │ usuario_id   │
│ nome     │  ┌──│ pagamento  │     │ cnh / modelo │
│ email    │  │  └─────┬──────┘     │ status_online│
│ senha_   │  │        │            └──────┬───────┘
│   hash   │  │  ┌─────▼────────────────────▼──────┐
│ genero   │  │  │             corridas              │
│ tipo_    │  │  │ passageiro_id   motorista_id      │
│   user   │  │  │ origem_id       destino_id        │
│ ativo    │  └──│ status          preco             │
└──────────┘     │ solicitada_em   encerrada_em      │
                 └────────────────────────────────────┘
```

---

## 🎯 Funcionalidades por Perfil

### 👤 Passageiro
- ✅ Solicitar corrida com origem e destino em texto livre
- ✅ Calcular preço estimado antes de solicitar
- ✅ Cancelar corrida ativa
- ✅ Ver histórico completo de corridas
- ✅ Visualizar estatísticas (total gasto, corridas concluídas)
- ✅ Desativar conta

### 🚗 Motorista
- ✅ Alternar entre ONLINE / OFFLINE
- ✅ Ver lista de corridas disponíveis (filtrada por gênero automaticamente)
- ✅ Aceitar ou recusar corridas
- ✅ Iniciar e finalizar corridas
- ✅ Ver histórico e estatísticas (ganhos, km rodados, nota média)
- ✅ Desativar conta

---

## 🔍 Curiosidades Técnicas

<details>
<summary>🤔 Por que existe um <code>Main.java</code> E um <code>App.java</code>?</summary>

O `App.java` herda de `Application` (JavaFX). Quando rodado diretamente de algumas IDEs, o JavaFX exige que a classe com `main()` **não** herde de `Application` — senão lança `"JavaFX runtime components are missing"`. O `Main.java` resolve isso: é um intermediário simples que apenas chama `App.main()`.

</details>

<details>
<summary>🤔 O que é o <code>MOCK_MODE</code> e por que ele existia?</summary>

Durante o desenvolvimento, o banco de dados real ainda não estava pronto. Em vez de travar o trabalho, criamos o `MockData.java` com dados falsos em memória RAM. Com `MOCK_MODE = true`, o app funciona completamente sem MySQL. Quando o banco ficou pronto, bastou mudar para `false`.

</details>

<details>
<summary>🛡️ Por que SHA-256 e não bcrypt para as senhas?</summary>

BCrypt é mais seguro (tem salt embutido), mas exige bibliotecas externas. Para o escopo acadêmico do projeto, SHA-256 com a `java.security` padrão do Java foi suficiente — sem dependências extras. Em produção real, use BCrypt ou Argon2.

</details>

<details>
<summary>🔌 Por que Singleton na conexão com o banco?</summary>

Abrir uma conexão de banco de dados é uma operação cara e lenta. Se cada método que precisa de dados abrisse uma nova conexão, o sistema ficaria lento e sobrecarregaria o servidor MySQL. O padrão Singleton garante que existe **uma única conexão** aberta durante toda a execução, reutilizada por todas as operações.

</details>

---

## 📊 Diagrama de Fluxo — Do Login à Corrida

```
  👤 Usuário                    🖥️ App                     🗄️ Banco
      │                            │                           │
      │── email + senha ──────────▶│                           │
      │                            │── SHA-256(senha) ────────▶│
      │                            │                    proc_login_usuario
      │                            │◀── Usuario object ────────│
      │◀── Painel carregado ───────│                           │
      │                            │                           │
      │── Solicitar corrida ──────▶│                           │
      │                            │── origem, destino ───────▶│
      │                            │                   proc_solicitar_corrida
      │                            │◀── id da corrida ─────────│
      │◀── "Aguardando motorista" ─│                           │
      │                            │                           │
      │     (Motorista aceita)      │                           │
      │                            │── aceitar corrida ───────▶│
      │                            │                   proc_aceitar_corrida
      │◀── status "ACEITA" ───────│                           │
```

---

<div align="center">

---

**🔴 RapidIN** — Feito com ☕ Java, 🎨 JavaFX e muito trabalho em equipe

![Made with Java](https://img.shields.io/badge/Feito%20com-Java%2017-E53935?style=for-the-badge&logo=openjdk&logoColor=white)
![Powered by MySQL](https://img.shields.io/badge/Banco-MySQL%208-FF6F00?style=for-the-badge&logo=mysql&logoColor=white)
![Pattern MVC](https://img.shields.io/badge/Padrão-MVC-E91E63?style=for-the-badge)
![Security](https://img.shields.io/badge/Senha-SHA--256-7B1FA2?style=for-the-badge&logo=letsencrypt&logoColor=white)

*Projeto acadêmico — Formação em Desenvolvimento de Software*

</div>
