🚗 Guia Completo — Plataforma de Corridas em JavaFX + MySQL
Para quem: iniciantes que nunca configuraram um projeto JavaFX com banco de dados
Stack: Java 17+, JavaFX 21, Maven, MySQL Connector/J, IntelliJ IDEA
Objetivo: configurar o projeto do zero, conectar ao MySQL e chamar stored procedures

Índice
Pré-requisitos — o que instalar antes de tudo
Criando o projeto no IntelliJ
Configurando o pom.xml (Maven)
Estrutura de pastas do projeto
Configurando a conexão com o MySQL
Classe para chamar Stored Procedures
Estrutura base do JavaFX (App.java + FXML)
Estilização com CSS no JavaFX
Tela de exemplo: Solicitar Corrida
Como rodar o projeto
Erros mais comuns e como resolver
1. Pré-requisitos
   Instale os itens abaixo antes de abrir o IntelliJ. Instale nessa ordem.

✅ 1.1 — Java Development Kit (JDK) 17 ou superior
Acesse: https://adoptium.net
Baixe o JDK 17 (versão LTS) — escolha o instalador do seu sistema operacional
Execute o instalador e clique em "Next" em tudo
Para conferir se instalou certo, abra o Prompt de Comando (Windows) ou Terminal (Mac/Linux) e digite:
java -version
Deve aparecer algo como: openjdk version "17.x.x"
✅ 1.2 — IntelliJ IDEA
Acesse: https://www.jetbrains.com/idea/download
Baixe a versão Community (gratuita) ou Ultimate (paga, 30 dias grátis)
Instale normalmente
✅ 1.3 — MySQL Server e MySQL Workbench
Acesse: https://dev.mysql.com/downloads/mysql/
Baixe o MySQL Installer para Windows (ou o pacote do seu SO)
Durante a instalação, escolha "Developer Default" — instala o MySQL Server e o Workbench juntos
Anote a senha do usuário root que você criar — você vai precisar dela
2. Criando o projeto no IntelliJ
   Passo a passo completo:
   2.1. Abra o IntelliJ IDEA → na tela inicial clique em "New Project"

2.2. Na coluna da esquerda, clique em "New Project" (não em "Maven" ou "JavaFX" ainda)

2.3. Preencha os campos:

Name: plataforma-corridas (sem espaço, sem acento)
Location: escolha uma pasta de fácil acesso (ex: C:\Projetos\plataforma-corridas)
Language: Java
Build system: Maven ← MUITO IMPORTANTE
JDK: selecione o JDK 17 que você instalou. Se não aparecer, clique em "Add SDK → Download SDK" e baixe o JDK 17
2.4. Expanda a seção "Advanced Settings" (parte de baixo da tela):

GroupId: com.corridas (identificador do projeto, como um nome de pacote)
ArtifactId: plataforma-corridas
2.5. Clique em "Create"

O IntelliJ vai criar a pasta do projeto com uma estrutura básica Maven. Aguarde carregar.

3. Configurando o pom.xml
   O pom.xml é o arquivo que diz ao Maven quais bibliotecas baixar automaticamente.
   Ele já existe na raiz do projeto. Vamos substituir o conteúdo dele.

3.1. No painel esquerdo (Project), clique duas vezes em pom.xml para abrir

3.2. Apague todo o conteúdo e cole o seguinte:

<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
http://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <!-- Identificação do projeto -->
    <groupId>com.corridas</groupId>
    <artifactId>plataforma-corridas</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <!-- Versão do Java -->
    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <javafx.version>21.0.2</javafx.version>
    </properties>

    <dependencies>

        <!-- ========== JavaFX ========== -->
        <!-- Módulo principal de janelas e controles -->
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-controls</artifactId>
            <version>${javafx.version}</version>
        </dependency>

        <!-- Módulo FXML (telas em arquivo .fxml) -->
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-fxml</artifactId>
            <version>${javafx.version}</version>
        </dependency>

        <!-- ========== MySQL Connector/J ========== -->
        <!-- Driver JDBC para conectar ao banco MySQL -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <version>8.3.0</version>
        </dependency>

    </dependencies>

    <build>
        <plugins>

            <!-- Plugin do Maven para compilar o projeto -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <source>17</source>
                    <target>17</target>
                </configuration>
            </plugin>

            <!-- Plugin do JavaFX para rodar o projeto com mvn javafx:run -->
            <plugin>
                <groupId>org.openjfx</groupId>
                <artifactId>javafx-maven-plugin</artifactId>
                <version>0.0.8</version>
                <configuration>
                    <!-- Esse é o caminho da sua classe principal (Main) -->
                    <mainClass>com.corridas.App</mainClass>
                </configuration>
            </plugin>

        </plugins>
    </build>

</project>
3.3. Salve o arquivo (Ctrl+S)

3.4. Um ícone de elefante (Maven) aparecerá no canto superior direito do editor → clique em "Load Maven Changes" (ou no ícone 🔄). O Maven vai baixar todas as bibliotecas automaticamente. Aguarde terminar — pode demorar alguns minutos na primeira vez.

4. Estrutura de Pastas
   Após configurar o Maven, crie as pastas abaixo manualmente.
   No painel Project do IntelliJ, clique com o botão direito para criar pacotes e arquivos.

Estrutura final que vamos criar:
plataforma-corridas/
├── pom.xml
└── src/
└── main/
├── java/
│   └── com/
│       └── corridas/
│           ├── App.java                  ← Ponto de entrada do programa
│           ├── database/
│           │   ├── Conexao.java           ← Gerencia a conexão com MySQL
│           │   └── ProcedureExecutor.java ← Chama os stored procedures
│           ├── controller/
│           │   ├── TelaInicialController.java
│           │   └── SolicitarCorrida Controller.java
│           └── model/
│               ├── Usuario.java           ← Dados do usuário
│               └── Corrida.java           ← Dados de uma corrida
└── resources/
└── com/
└── corridas/
├── fxml/
│   ├── tela-inicial.fxml      ← Layout da tela inicial
│   └── solicitar-corrida.fxml ← Layout da tela de corrida
└── css/
└── estilo.css             ← Estilos visuais
Como criar as pastas no IntelliJ:
4.1. No painel Project, expanda: src → main → java

4.2. Clique com botão direito em java → New → Package
Digite: com.corridas e pressione Enter

4.3. Clique com botão direito em com.corridas → New → Package
Repita para criar os sub-pacotes: com.corridas.database, com.corridas.controller, com.corridas.model

4.4. Para a pasta de recursos, clique com botão direito em main → New → Directory
Selecione resources na lista que aparecer (ou digite manualmente)

4.5. Dentro de resources, crie as pastas:

com/corridas/fxml
com/corridas/css
💡 Dica: A pasta resources deve ser marcada como "Resources Root". Clique com botão direito nela → Mark Directory as → Resources Root

5. Configurando a Conexão com MySQL
   Arquivo: Conexao.java
   Crie esse arquivo dentro do pacote com.corridas.database:

5.1. Clique com botão direito em com.corridas.database → New → Java Class
Nome: Conexao

5.2. Cole o conteúdo abaixo:

package com.corridas.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
* Gerencia a conexão com o banco de dados MySQL.
* Usa o padrão Singleton — só existe UMA conexão ativa por vez.
  */
  public class Conexao {

  // ==========================================
  // CONFIGURAÇÕES DO BANCO — ALTERE AQUI
  // ==========================================
  private static final String URL      = "jdbc:mysql://localhost:3306/corridas_db";
  //                                                              ^^^^^^^^^^^
  //                                      Troque por: IP do servidor do back-end
  //                                      Ex: "jdbc:mysql://192.168.1.10:3306/corridas_db"

  private static final String USUARIO  = "root";         // usuário do MySQL
  private static final String SENHA    = "sua_senha";    // senha do MySQL

  // Guarda a instância única da conexão
  private static Connection conexaoAtiva = null;

  // Construtor privado: ninguém cria Conexao com "new Conexao()"
  private Conexao() {}

  /**
    * Retorna a conexão ativa. Se não existir (ou estiver fechada), cria uma nova.
      */
      public static Connection getConexao() {
      try {
      // Se não há conexão ou ela foi fechada, cria uma nova
      if (conexaoAtiva == null || conexaoAtiva.isClosed()) {
      conexaoAtiva = DriverManager.getConnection(URL, USUARIO, SENHA);
      System.out.println("✅ Conexão com banco de dados estabelecida!");
      }
      } catch (SQLException e) {
      System.err.println("❌ Erro ao conectar ao banco: " + e.getMessage());
      // Em produção, lance uma exceção em vez de só imprimir
      }
      return conexaoAtiva;
      }

  /**
    * Fecha a conexão com o banco. Chame isso ao fechar o programa.
      */
      public static void fecharConexao() {
      try {
      if (conexaoAtiva != null && !conexaoAtiva.isClosed()) {
      conexaoAtiva.close();
      System.out.println("🔒 Conexão encerrada.");
      }
      } catch (SQLException e) {
      System.err.println("Erro ao fechar conexão: " + e.getMessage());
      }
      }
      }
      🔑 Importante: Troque "sua_senha" pela senha do MySQL e "corridas_db" pelo nome real do banco que o time de back-end criar.

6. Chamando Stored Procedures
   Arquivo: ProcedureExecutor.java
   Crie dentro de com.corridas.database:

package com.corridas.database;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
* Executa Stored Procedures do banco de dados.
*
* Como funciona:
* - CallableStatement é a classe JDBC para chamar procedures
* - A sintaxe é sempre: {CALL nome_da_procedure(?, ?, ?)}
* - Cada "?" é um parâmetro (entrada ou saída)
    */
    public class ProcedureExecutor {

// -----------------------------------------------------------
// EXEMPLO 1: Procedure que INSERE dados (sem retorno)
// Equivalente a: CALL sp_solicitar_corrida(origem, destino, id_usuario)
// -----------------------------------------------------------
public static void solicitarCorrida(String origem, String destino, int idUsuario) {
// Pega a conexão do banco
Connection conn = Conexao.getConexao();

     // Sintaxe JDBC para chamar procedure: {CALL nome(?, ?, ?)}
     String sql = "{CALL sp_solicitar_corrida(?, ?, ?)}";

     try (CallableStatement stmt = conn.prepareCall(sql)) {

         // Define os valores dos parâmetros na ordem da procedure
         stmt.setString(1, origem);    // 1º parâmetro = origem
         stmt.setString(2, destino);   // 2º parâmetro = destino
         stmt.setInt(3, idUsuario);    // 3º parâmetro = id_usuario

         // Executa a procedure
         stmt.execute();
         System.out.println("✅ Corrida solicitada com sucesso!");

     } catch (SQLException e) {
         System.err.println("❌ Erro ao solicitar corrida: " + e.getMessage());
     }
}


    // -----------------------------------------------------------
    // EXEMPLO 2: Procedure que RETORNA dados (ResultSet)
    // Equivalente a: CALL sp_buscar_corridas_usuario(id_usuario)
    // -----------------------------------------------------------
    public static ResultSet buscarCorridasUsuario(int idUsuario) {
        Connection conn = Conexao.getConexao();
        String sql = "{CALL sp_buscar_corridas_usuario(?)}";

        try {
            CallableStatement stmt = conn.prepareCall(sql);
            stmt.setInt(1, idUsuario);

            // executeQuery() retorna os dados como tabela (ResultSet)
            ResultSet resultado = stmt.executeQuery();
            return resultado;

        } catch (SQLException e) {
            System.err.println("❌ Erro ao buscar corridas: " + e.getMessage());
            return null;
        }
    }


    // -----------------------------------------------------------
    // EXEMPLO 3: Procedure com parâmetro de SAÍDA (OUT parameter)
    // O banco devolve um valor calculado — ex: preço estimado
    // Equivalente a: CALL sp_calcular_preco(origem, destino, OUT preco)
    // -----------------------------------------------------------
    public static double calcularPreco(String origem, String destino) {
        Connection conn = Conexao.getConexao();
        String sql = "{CALL sp_calcular_preco(?, ?, ?)}";

        try (CallableStatement stmt = conn.prepareCall(sql)) {

            // Parâmetros de entrada
            stmt.setString(1, origem);
            stmt.setString(2, destino);

            // Parâmetro de saída: registra que o 3º parâmetro
            // será devolvido pelo banco como DOUBLE
            stmt.registerOutParameter(3, Types.DOUBLE);

            stmt.execute();

            // Lê o valor que o banco devolveu
            double preco = stmt.getDouble(3);
            return preco;

        } catch (SQLException e) {
            System.err.println("❌ Erro ao calcular preço: " + e.getMessage());
            return 0.0;
        }
    }


    // -----------------------------------------------------------
    // EXEMPLO 4: Procedure de LOGIN
    // Retorna dados do usuário se login for válido
    // -----------------------------------------------------------
    public static Map<String, String> fazerLogin(String email, String senha) {
        Connection conn = Conexao.getConexao();
        String sql = "{CALL sp_login_usuario(?, ?)}";

        Map<String, String> dadosUsuario = new HashMap<>();

        try (CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, senha);

            ResultSet rs = stmt.executeQuery();

            // Se houver resultado, o login foi bem-sucedido
            if (rs.next()) {
                // Lê as colunas devolvidas pela procedure
                dadosUsuario.put("id",     rs.getString("id_usuario"));
                dadosUsuario.put("nome",   rs.getString("nome"));
                dadosUsuario.put("email",  rs.getString("email"));
            }

        } catch (SQLException e) {
            System.err.println("❌ Erro no login: " + e.getMessage());
        }

        return dadosUsuario; // Vazio se login falhou
    }
}
7. Estrutura Base JavaFX
   7.1 — Arquivo principal: App.java
   Crie em com.corridas:

package com.corridas;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.corridas.database.Conexao;

/**
* Ponto de entrada do programa JavaFX.
* O método start() é chamado automaticamente quando o app inicia.
  */
  public class App extends Application {

  // Stage é a "janela" principal do programa
  private static Stage janelaPrincipal;

  @Override
  public void start(Stage stage) throws Exception {
  janelaPrincipal = stage;

       // Carrega a tela inicial a partir do arquivo FXML
       trocarTela("fxml/tela-inicial.fxml");

       // Configurações da janela
       stage.setTitle("🚗 Plataforma de Corridas");
       stage.setMinWidth(800);
       stage.setMinHeight(600);
       stage.show();
  }

  /**
    * Método auxiliar para trocar de tela.
    * Chame esse método de qualquer Controller para navegar entre telas.
    *
    * Exemplo de uso em um Controller:
    *   App.trocarTela("fxml/solicitar-corrida.fxml");
        */
        public static void trocarTela(String caminhoFxml) throws Exception {
        // Monta o caminho completo do arquivo FXML dentro de resources
        FXMLLoader loader = new FXMLLoader(
        App.class.getResource("/com/corridas/" + caminhoFxml)
        );

    Parent tela = loader.load();
    Scene cena = new Scene(tela);

    // Aplica o CSS global em todas as telas
    cena.getStylesheets().add(
    App.class.getResource("/com/corridas/css/estilo.css").toExternalForm()
    );

    janelaPrincipal.setScene(cena);
    }

  @Override
  public void stop() {
  // Fecha a conexão com o banco quando o programa é encerrado
  Conexao.fecharConexao();
  }

  public static void main(String[] args) {
  // Inicia o JavaFX
  launch(args);
  }
  }
  7.2 — Tela Inicial: tela-inicial.fxml
  Crie em resources/com/corridas/fxml/:

<?xml version="1.0" encoding="UTF-8"?>

<?import javafx.scene.layout.*?>
<?import javafx.scene.control.*?>
<?import javafx.scene.image.*?>

<!-- fx:controller aponta para a classe Java que controla essa tela -->
<VBox xmlns:fx="http://javafx.com/fxml"
fx:controller="com.corridas.controller.TelaInicialController"
styleClass="tela-inicial"
alignment="CENTER"
spacing="20">

    <!-- Título do app -->
    <Label text="🚗 Plataforma de Corridas" styleClass="titulo-principal"/>

    <!-- Subtítulo -->
    <Label text="Bem-vindo! Faça login para continuar." styleClass="subtitulo"/>

    <!-- Campos de login -->
    <VBox styleClass="card-login" spacing="12" alignment="CENTER">

        <Label text="E-mail" styleClass="label-campo"/>
        <!-- fx:id conecta esse campo ao atributo "campoEmail" no Controller -->
        <TextField fx:id="campoEmail"
                   promptText="seu@email.com"
                   styleClass="campo-texto"/>

        <Label text="Senha" styleClass="label-campo"/>
        <PasswordField fx:id="campoSenha"
                       promptText="••••••••"
                       styleClass="campo-texto"/>

        <!-- onAction chama o método no Controller quando clicado -->
        <Button text="Entrar"
                onAction="#fazerLogin"
                styleClass="botao-primario"/>

        <Label fx:id="mensagemErro"
               styleClass="mensagem-erro"
               visible="false"/>

    </VBox>

</VBox>
7.3 — Controller da Tela Inicial: TelaInicialController.java
Crie em com.corridas.controller:

package com.corridas.controller;

import com.corridas.App;
import com.corridas.database.ProcedureExecutor;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.Map;

/**
* Controller da Tela Inicial (Login).
*
* A anotação @FXML conecta os atributos aqui com os
* elementos fx:id no arquivo .fxml correspondente.
  */
  public class TelaInicialController {

  // @FXML liga esse campo ao <TextField fx:id="campoEmail"/> do FXML
  @FXML private TextField campoEmail;
  @FXML private PasswordField campoSenha;
  @FXML private Label mensagemErro;

  /**
    * Chamado quando o botão "Entrar" é clicado.
    * O nome do método deve bater com o onAction="#fazerLogin" do FXML.
      */
      @FXML
      private void fazerLogin() {
      String email = campoEmail.getText().trim();
      String senha = campoSenha.getText().trim();

      // Validação simples antes de ir ao banco
      if (email.isEmpty() || senha.isEmpty()) {
      mostrarErro("Preencha e-mail e senha!");
      return;
      }

      // Chama a procedure de login
      Map<String, String> usuario = ProcedureExecutor.fazerLogin(email, senha);

      if (usuario.isEmpty()) {
      mostrarErro("E-mail ou senha incorretos.");
      } else {
      // Login bem-sucedido → vai para a tela de corridas
      System.out.println("Login OK! Usuário: " + usuario.get("nome"));
      try {
      App.trocarTela("fxml/solicitar-corrida.fxml");
      } catch (Exception e) {
      mostrarErro("Erro ao abrir tela de corridas.");
      e.printStackTrace();
      }
      }
      }

  /** Exibe uma mensagem de erro na tela */
  private void mostrarErro(String mensagem) {
  mensagemErro.setText(mensagem);
  mensagemErro.setVisible(true);
  }
  }
8. Estilização CSS
   Arquivo: estilo.css
   Crie em resources/com/corridas/css/:

/* ============================================
PLATAFORMA DE CORRIDAS — Tema Escuro
Paleta: fundo escuro, destaque laranja/amarelo
============================================ */


/* ── Fonte base de toda a aplicação ── */
.root {
-fx-font-family: "Segoe UI", "Arial", sans-serif;
-fx-font-size: 14px;
-fx-background-color: #1a1a2e; /* fundo roxo escuro */
}

/* ── Tela de Login ── */
.tela-inicial {
-fx-background-color: #1a1a2e;
-fx-padding: 60px;
}

/* ── Título principal ── */
.titulo-principal {
-fx-font-size: 32px;
-fx-font-weight: bold;
-fx-text-fill: #e94560;     /* vermelho/rosa de destaque */
}

/* ── Subtítulo ── */
.subtitulo {
-fx-font-size: 15px;
-fx-text-fill: #a0a0c0;     /* cinza azulado claro */
}

/* ── Card branco que envolve o formulário ── */
.card-login {
-fx-background-color: #16213e; /* azul escuro */
-fx-background-radius: 16px;
-fx-padding: 40px;
-fx-min-width: 380px;
-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 20, 0, 0, 6);
}

/* ── Labels dos campos ── */
.label-campo {
-fx-text-fill: #c0c0d8;
-fx-font-size: 13px;
-fx-font-weight: bold;
}

/* ── Campos de texto e senha ── */
.campo-texto {
-fx-background-color: #0f3460; /* azul médio */
-fx-text-fill: #ffffff;
-fx-prompt-text-fill: #607080;
-fx-background-radius: 8px;
-fx-border-color: #2a4a7f;
-fx-border-radius: 8px;
-fx-border-width: 1.5px;
-fx-padding: 10px 14px;
-fx-pref-width: 300px;
}

.campo-texto:focused {
-fx-border-color: #e94560; /* borda vermelha quando em foco */
-fx-effect: dropshadow(gaussian, #e9456055, 8, 0, 0, 0);
}

/* ── Botão principal (cor vibrante) ── */
.botao-primario {
-fx-background-color: #e94560;
-fx-text-fill: #ffffff;
-fx-font-size: 15px;
-fx-font-weight: bold;
-fx-background-radius: 8px;
-fx-padding: 12px 40px;
-fx-cursor: hand;
-fx-pref-width: 300px;
}

.botao-primario:hover {
-fx-background-color: #c73652; /* escurece ao passar o mouse */
-fx-scale-x: 1.02;
-fx-scale-y: 1.02;
}

.botao-primario:pressed {
-fx-background-color: #a02940;
-fx-scale-x: 0.98;
-fx-scale-y: 0.98;
}

/* ── Botão secundário (contorno) ── */
.botao-secundario {
-fx-background-color: transparent;
-fx-text-fill: #e94560;
-fx-border-color: #e94560;
-fx-border-radius: 8px;
-fx-background-radius: 8px;
-fx-padding: 10px 30px;
-fx-cursor: hand;
-fx-font-weight: bold;
}

.botao-secundario:hover {
-fx-background-color: #e9456015;
}

/* ── Mensagem de erro ── */
.mensagem-erro {
-fx-text-fill: #ff6b6b;
-fx-font-size: 13px;
}

/* ── Tela de Solicitar Corrida ── */
.tela-corrida {
-fx-background-color: #1a1a2e;
-fx-padding: 30px;
}

.painel-lateral {
-fx-background-color: #16213e;
-fx-padding: 24px;
-fx-background-radius: 12px;
-fx-pref-width: 340px;
}

/* ── Títulos de seção ── */
.titulo-secao {
-fx-font-size: 20px;
-fx-font-weight: bold;
-fx-text-fill: #ffffff;
}

/* ── Label de preço ── */
.label-preco {
-fx-font-size: 28px;
-fx-font-weight: bold;
-fx-text-fill: #4ecdc4; /* verde-azulado */
}

/* ── Status da corrida ── */
.status-aguardando  { -fx-text-fill: #f7dc6f; } /* amarelo */
.status-em-andamento { -fx-text-fill: #4ecdc4; } /* verde-azul */
.status-concluida    { -fx-text-fill: #58d68d; } /* verde */
.status-cancelada    { -fx-text-fill: #ff6b6b; } /* vermelho */

/* ── Separador ── */
.separator {
-fx-background-color: #2a4a7f;
-fx-pref-height: 1px;
}
9. Tela de Exemplo: Solicitar Corrida
   Arquivo: solicitar-corrida.fxml
<?xml version="1.0" encoding="UTF-8"?>

<?import javafx.scene.layout.*?>
<?import javafx.scene.control.*?>

<HBox xmlns:fx="http://javafx.com/fxml"
fx:controller="com.corridas.controller.SolicitarCorridaController"
styleClass="tela-corrida"
spacing="24">

    <!-- Coluna esquerda: formulário -->
    <VBox styleClass="painel-lateral" spacing="16">

        <Label text="Solicitar Corrida" styleClass="titulo-secao"/>

        <Label text="Local de Origem" styleClass="label-campo"/>
        <TextField fx:id="campoOrigem"
                   promptText="Ex: Rua das Flores, 123"
                   styleClass="campo-texto"/>

        <Label text="Local de Destino" styleClass="label-campo"/>
        <TextField fx:id="campoDestino"
                   promptText="Ex: Av. Paulista, 1000"
                   styleClass="campo-texto"/>

        <!-- Exibe o preço estimado -->
        <HBox spacing="8" alignment="CENTER_LEFT">
            <Label text="Preço estimado:" styleClass="label-campo"/>
            <Label fx:id="labelPreco" text="R$ --" styleClass="label-preco"/>
        </HBox>

        <Button text="Calcular Preço"
                onAction="#calcularPreco"
                styleClass="botao-secundario"/>

        <Button text="🚗  Solicitar Corrida"
                onAction="#solicitarCorrida"
                styleClass="botao-primario"/>

        <Label fx:id="labelStatus"
               text=""
               styleClass="status-aguardando"/>

    </VBox>

    <!-- Coluna direita: histórico de corridas -->
    <VBox styleClass="painel-lateral" spacing="12" HBox.hgrow="ALWAYS">

        <Label text="Minhas Corridas" styleClass="titulo-secao"/>

        <!-- TableView exibe dados em formato de tabela -->
        <TableView fx:id="tabelaCorridas" VBox.vgrow="ALWAYS">

            <columns>
                <TableColumn fx:id="colunaOrigem"  text="Origem"  prefWidth="140"/>
                <TableColumn fx:id="colunaDestino" text="Destino" prefWidth="140"/>
                <TableColumn fx:id="colunaStatus"  text="Status"  prefWidth="100"/>
                <TableColumn fx:id="colunaPreco"   text="Preço"   prefWidth="80"/>
            </columns>

        </TableView>

    </VBox>

</HBox>
Arquivo: SolicitarCorridaController.java
package com.corridas.controller;

import com.corridas.database.ProcedureExecutor;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SolicitarCorridaController {

    @FXML private TextField campoOrigem;
    @FXML private TextField campoDestino;
    @FXML private Label labelPreco;
    @FXML private Label labelStatus;

    @FXML private TableView<String[]> tabelaCorridas;
    @FXML private TableColumn<String[], String> colunaOrigem;
    @FXML private TableColumn<String[], String> colunaDestino;
    @FXML private TableColumn<String[], String> colunaStatus;
    @FXML private TableColumn<String[], String> colunaPreco;

    // ID do usuário logado (em produção, guarde isso em uma SessionManager)
    private final int ID_USUARIO = 1; // valor fixo por enquanto

    /** Chamado quando "Calcular Preço" é clicado */
    @FXML
    private void calcularPreco() {
        String origem  = campoOrigem.getText().trim();
        String destino = campoDestino.getText().trim();

        if (origem.isEmpty() || destino.isEmpty()) {
            labelStatus.setText("Preencha origem e destino!");
            return;
        }

        double preco = ProcedureExecutor.calcularPreco(origem, destino);
        labelPreco.setText(String.format("R$ %.2f", preco));
    }

    /** Chamado quando "Solicitar Corrida" é clicado */
    @FXML
    private void solicitarCorrida() {
        String origem  = campoOrigem.getText().trim();
        String destino = campoDestino.getText().trim();

        if (origem.isEmpty() || destino.isEmpty()) {
            labelStatus.setText("Preencha origem e destino!");
            return;
        }

        ProcedureExecutor.solicitarCorrida(origem, destino, ID_USUARIO);

        labelStatus.setText("✅ Corrida solicitada! Aguardando motorista...");
        labelStatus.getStyleClass().setAll("status-aguardando");

        // Limpa os campos após solicitar
        campoOrigem.clear();
        campoDestino.clear();
        labelPreco.setText("R$ --");
    }
}
10. Como Rodar o Projeto
    Há duas formas de rodar. Recomendamos a Forma 1.

Forma 1 — Pelo IntelliJ (mais simples)
Abra o painel Maven clicando no ícone de elefante na barra lateral direita
Expanda: plataforma-corridas → Plugins → javafx
Dê duplo clique em javafx:run
Forma 2 — Pelo Terminal dentro do IntelliJ
No menu superior: View → Tool Windows → Terminal
Digite:
mvn javafx:run
Se aparecer erro de módulos JavaFX:
Adicione as seguintes configurações de VM no IntelliJ:

Menu superior: Run → Edit Configurations...
Clique em + → Application
Em Main class: com.corridas.App
Em VM options cole:
--module-path ${PATH_TO_FX} --add-modules javafx.controls,javafx.fxml
Obs: com o Maven configurado corretamente (como mostramos), isso geralmente não é necessário. Use só se der erro.

11. Erros Comuns
    Erro	Causa	Solução
    Communications link failure	MySQL não está rodando ou URL errada	Abra o MySQL Workbench e verifique se o servidor está ativo
    Access denied for user 'root'	Senha errada na classe Conexao.java	Corrija a senha na variável SENHA
    Unknown database 'corridas_db'	Banco não foi criado ainda	Crie o banco no Workbench: CREATE DATABASE corridas_db;
    Location is not set (FXML)	Caminho do FXML errado	Verifique o caminho em App.trocarTela() e confirme que o arquivo existe em resources
    NullPointerException no Controller	Campo com @FXML não tem o fx:id correspondente no FXML	Confira se o nome do fx:id no FXML é igual ao nome da variável no Controller
    ClassNotFoundException: com.mysql.cj.jdbc.Driver	Maven não baixou o driver MySQL	Clique em 🔄 no pom.xml para forçar o download
    Checklist Final — Antes de Começar a Codar as Telas
    [ ] JDK 17 instalado e visível no IntelliJ
    [ ] Projeto criado com Maven (não Gradle)
    [ ] pom.xml configurado com JavaFX 21 e MySQL Connector
    [ ] Maven fez o download das dependências (sem erros em vermelho)
    [ ] Pastas criadas: database, controller, model, fxml, css
    [ ] resources marcado como Resources Root
    [ ] Conexao.java com os dados corretos do banco
    [ ] estilo.css criado em resources/com/corridas/css/
    [ ] Projeto roda com mvn javafx:run sem erros
    💬 Dica para o time: combinem com o time de back-end o nome exato de cada stored procedure e os parâmetros na ordem correta — isso evita muita dor de cabeça na integração. Peçam um documento com a assinatura de cada procedure antes de começar a codificar as telas.