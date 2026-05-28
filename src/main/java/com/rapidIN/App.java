package com.rapidIN;

// ============================================================
// ARQUIVO: App.java
// RESPONSABILIDADE: Ponto de entrada visual do sistema.
//   Este é o arquivo que "acende a tela" quando o programa
//   é iniciado. Ele cria a janela principal e define como
//   trocar de uma tela para outra (ex.: da tela de login
//   para o painel do passageiro).
//
// ANALOGIA: Pense neste arquivo como o gerente de um cinema.
//   Ele abre as portas (inicia a janela), coloca o filme
//   certo em cartaz (carrega a tela correta) e fecha tudo
//   ao final (encerra a conexão com o banco).
// ============================================================

// "import" é como pegar uma ferramenta de uma caixa de ferramentas.
// Cada linha abaixo traz uma ferramenta já pronta que vamos usar.
import javafx.application.Application;   // Base para criar aplicações com janelas
import javafx.fxml.FXMLLoader;           // Lê arquivos .fxml (que descrevem a aparência das telas)
import javafx.scene.Parent;              // Representa o conteúdo visual de uma tela
import javafx.scene.Scene;              // Representa a "cena" (tela visível) dentro da janela
import javafx.stage.Stage;              // Representa a janela do sistema operacional
import com.rapidIN.database.conexao;    // Nossa classe que conecta ao banco de dados

// "public class App" declara a "receita" (classe) principal do aplicativo.
// "extends Application" significa que App herda todo o comportamento básico
// de um aplicativo JavaFX — assim não precisamos programar do zero.
public class App extends Application {

    // "janelaPrincipal" guarda a referência para a única janela que o
    // sistema usa. Ao trocar de tela, reutilizamos sempre essa mesma janela.
    // "static" significa que esta variável pertence à classe inteira,
    // não a um objeto específico — qualquer parte do programa pode acessá-la.
    private static Stage janelaPrincipal;

    // ── Método start ────────────────────────────────────────────────────────
    // O JavaFX chama automaticamente este método quando o programa inicia.
    // É o equivalente a "ligar o aparelho".
    //
    // Parâmetro "stage": a janela que o JavaFX criou automaticamente para nós.
    // "throws Exception": avisa que este método pode dar erro (ex.: arquivo não encontrado).
    @Override
    public void start(Stage stage) throws Exception {
        janelaPrincipal = stage;                           // Guarda a janela para uso futuro
        trocarTela("tela-inicial.fxml");                   // Exibe a tela de login ao iniciar
        stage.setTitle("RapidIN — Plataforma de Corridas"); // Define o texto na barra de título
        stage.setMinWidth(900);                            // Tamanho mínimo da janela (largura)
        stage.setMinHeight(660);                           // Tamanho mínimo da janela (altura)
        stage.show();                                      // Torna a janela visível
    }

    // ── Método trocarTela ────────────────────────────────────────────────────
    // Responsável por navegar entre telas.
    // Recebe o nome do arquivo .fxml da tela desejada e substitui
    // o conteúdo atual da janela pelo novo.
    //
    // Como funciona, passo a passo:
    //   1. Encontra o arquivo .fxml dentro da pasta de recursos
    //   2. Carrega e "monta" a tela a partir desse arquivo
    //   3. Aplica o arquivo de estilos (CSS) para deixar a tela bonita
    //   4. Coloca essa nova tela na janela principal
    //   5. Centraliza a janela na tela do monitor
    public static void trocarTela(String arquivo) throws Exception {
        // FXMLLoader é como um "leitor de planta baixa" — lê o arquivo .fxml
        // e constrói a tela descrita nele
        FXMLLoader loader = new FXMLLoader(
            App.class.getResource("/com/corridas/FXML/" + arquivo)
        );

        // "loader.load()" de fato constrói os botões, campos de texto,
        // tabelas etc. descritos no arquivo .fxml
        Parent tela = loader.load();

        // "Scene" é a "moldura" que envolve o conteúdo visual
        Scene cena = new Scene(tela);

        // Vincula o arquivo de estilos (CSS) à nova tela
        // CSS define cores, fontes, tamanhos — a "roupa" da tela
        cena.getStylesheets().add(
            App.class.getResource("/css/estilo.css").toExternalForm()
        );

        // Substitui o conteúdo da janela pela nova tela
        janelaPrincipal.setScene(cena);

        // Garante que a janela fique centralizada no monitor após a troca
        janelaPrincipal.centerOnScreen();
    }

    // ── Método stop ─────────────────────────────────────────────────────────
    // O JavaFX chama este método automaticamente quando o usuário fecha
    // a janela (clica no X). É o equivalente a "desligar o aparelho".
    // Aqui encerramos a conexão com o banco de dados corretamente,
    // evitando que ela fique "presa" no servidor.
    @Override
    public void stop() {
        conexao.fecharConexao();
    }

    // ── Método main ─────────────────────────────────────────────────────────
    // Em Java, todo programa começa no método "main".
    // Aqui ele simplesmente diz ao JavaFX: "pode começar!".
    // O argumento "args" permite que o programa receba parâmetros
    // pela linha de comando (não utilizamos isso aqui).
    public static void main(String[] args) {
        launch(args);
    }
}