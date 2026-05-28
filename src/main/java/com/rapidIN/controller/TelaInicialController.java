package com.rapidIN.controller;

// ============================================================
// ARQUIVO: TelaInicialController.java
// RESPONSABILIDADE: Controlar a tela de login — a primeira
//   tela que o usuário vê ao abrir o sistema.
//
// O QUE É UM "CONTROLLER"?
//   No padrão MVC (Model-View-Controller), o Controller é
//   o intermediário entre a tela visual (View, arquivo .fxml)
//   e os dados (Model/banco de dados).
//   Ele "escuta" as ações do usuário na tela (clicar em botões,
//   digitar campos) e decide o que fazer com elas.
//
// O QUE ESTA TELA FAZ:
//   1. Recebe e-mail e senha digitados pelo usuário
//   2. Valida se os campos não estão vazios
//   3. Consulta o banco (ou MockData) para verificar as credenciais
//   4. Se válido: salva o usuário na sessão e navega para o painel correto
//      - Motoristas vão para painel-motorista.fxml
//      - Passageiros vão para painel-passageiro.fxml
//   5. Se inválido: exibe mensagem de erro na tela
//
// COMO A TELA SE CONECTA AO CONTROLLER?
//   O arquivo tela-inicial.fxml referencia esta classe em seu
//   atributo fx:controller. Os campos e botões marcados com
//   fx:id no .fxml são injetados aqui pela anotação @FXML.
// ============================================================

// Importações necessárias para este controller
import com.rapidIN.App;                          // Para trocar de tela
import com.rapidIN.SessionManager;              // Para guardar o usuário logado
import com.rapidIN.database.procedureExecutor; // Para chamar o login no banco
import com.rapidIN.model.Usuario;               // Tipo retornado pelo login

// Componentes visuais do JavaFX que usamos nesta tela
import javafx.fxml.FXML;              // Anotação que vincula campos Java ao arquivo .fxml
import javafx.scene.control.Label;        // Componente de texto (para exibir erros)
import javafx.scene.control.PasswordField; // Campo de senha (exibe ●●●● em vez de texto)
import javafx.scene.control.TextField;    // Campo de texto simples

public class TelaInicialController {

    // ── COMPONENTES VISUAIS ───────────────────────────────────────────────────
    // @FXML indica que este campo será preenchido automaticamente pelo JavaFX
    // com o componente de mesmo fx:id no arquivo tela-inicial.fxml.
    // Não precisamos criar esses objetos manualmente — o JavaFX os injeta.

    @FXML private TextField campoEmail;       // Campo onde o usuário digita o e-mail
    @FXML private PasswordField campoSenha;   // Campo de senha (texto oculto)
    @FXML private Label mensagemErro;         // Texto de erro exibido abaixo do formulário


    // ── AÇÃO: BOTÃO "ENTRAR" ──────────────────────────────────────────────────
    // @FXML significa que este método está vinculado ao botão "Entrar"
    // no arquivo .fxml (via atributo onAction).
    // É chamado automaticamente quando o usuário clica no botão.
    @FXML
    private void fazerLogin() {
        // Lê o que o usuário digitou. ".trim()" remove espaços extras no início e fim.
        String email = campoEmail.getText().trim();
        String senha = campoSenha.getText().trim();

        // Validação básica: verifica se os campos não estão vazios
        if (email.isEmpty() || senha.isEmpty()) {
            mostrarErro("Preencha e-mail e senha!");
            return; // "return" interrompe o método aqui, não continua para o login
        }

        // Consulta o banco de dados (ou MockData) para verificar as credenciais.
        // Retorna um objeto Usuario se as credenciais estiverem corretas, ou null se não.
        Usuario usuario = procedureExecutor.fazerLogin(email, senha);

        if (usuario == null) {
            // Credenciais inválidas: exibe mensagem de erro e fica na mesma tela
            mostrarErro("E-mail ou senha incorretos.");
        } else {
            // Login bem-sucedido!
            // Guarda o usuário no SessionManager para que outras telas saibam quem está logado
            SessionManager.getInstance().setUsuario(usuario);

            try {
                // Decide para qual painel navegar com base no tipo do usuário
                // Operador ternário: se for motorista, vai para painel-motorista; senão, painel-passageiro
                String proxima = usuario.isMotorista() ? "painel-motorista.fxml" : "painel-passageiro.fxml";
                App.trocarTela(proxima);
            } catch (Exception e) {
                mostrarErro("Erro ao abrir o painel.");
                e.printStackTrace(); // Imprime detalhes do erro no console para diagnóstico
            }
        }
    }


    // ── AÇÃO: LINK "CADASTRE-SE" ──────────────────────────────────────────────
    // Chamado quando o usuário clica no link/botão de cadastro.
    // Navega para a tela de cadastro de novos usuários.
    @FXML
    private void irParaCadastro() {
        try {
            App.trocarTela("cadastro.fxml");
        } catch (Exception e) {
            mostrarErro("Erro ao abrir cadastro.");
        }
    }


    // ── MÉTODO AUXILIAR: EXIBIR ERRO ─────────────────────────────────────────
    // Centraliza a lógica de mostrar mensagens de erro na tela.
    // Define o texto e torna o label visível.
    // Sem este método, teríamos que repetir essas duas linhas em vários lugares.
    private void mostrarErro(String msg) {
        mensagemErro.setText(msg);      // Define o texto da mensagem
        mensagemErro.setVisible(true);  // Torna o label visível (pode estar oculto por padrão)
    }
}
