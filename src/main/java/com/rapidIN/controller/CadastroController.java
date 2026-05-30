package com.rapidIN.controller;

// ============================================================
// ARQUIVO: CadastroController.java
// RESPONSABILIDADE: Controlar a tela de cadastro de novos
//   usuários no sistema.
//
// O QUE ESTA TELA FAZ:
//   1. Exibe um formulário com: nome, e-mail, senha,
//      seleção de gênero (Masculino/Feminino) e
//      seleção de perfil (Passageiro/Motorista)
//   2. Valida os campos preenchidos
//   3. Envia os dados para o banco de dados
//   4. Em caso de sucesso: volta para a tela de login
//   5. Em caso de erro (e-mail duplicado): exibe mensagem
//
// CAMPOS COM SELEÇÃO ÚNICA (RadioButton):
//   RadioButtons agrupados num ToggleGroup garantem que apenas
//   UMA opção pode estar selecionada por vez. É como as opções
//   de um formulário físico onde você marca apenas um círculo.
//   Ex.: você não pode ser Masculino E Feminino ao mesmo tempo.
// ============================================================

// Importações necessárias
import com.rapidIN.App;                          // Para trocar de tela
import com.rapidIN.database.procedureExecutor; // Para cadastrar no banco

// Componentes visuais do JavaFX
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class CadastroController {

    // ── COMPONENTES VISUAIS ───────────────────────────────────────────────────
    // @FXML vincula cada variável ao componente de mesmo id no arquivo cadastro.fxml

    @FXML private TextField campoNome;         // Campo de texto para o nome completo
    @FXML private TextField campoEmail;        // Campo de texto para o e-mail
    @FXML private PasswordField campoSenha;    // Campo de senha (mostra ●●●● enquanto digita)
    @FXML private TextField campoCpf;
    @FXML private TextField campoTelefone;
    @FXML private TextField campoCnh;
    @FXML private TextField campoModelo;
    @FXML private TextField campoPlaca;
    @FXML private ComboBox<String> comboFormaPagamento;

    @FXML private RadioButton radioMasculino;  // Opção "Masculino" para gênero
    @FXML private RadioButton radioFeminino;   // Opção "Feminino" para gênero

    @FXML private RadioButton radioPassageiro; // Opção "Passageiro" para tipo de conta
    @FXML private RadioButton radioMotorista;  // Opção "Motorista" para tipo de conta

    @FXML private Label mensagem;              // Label para exibir erros ou confirmações
    @FXML private Label labelCnh;
    @FXML private Label labelModelo;
    @FXML private Label labelPlaca;


    // ── GRUPOS DE SELEÇÃO EXCLUSIVA ───────────────────────────────────────────
    // ToggleGroup garante que apenas um RadioButton de cada grupo
    // possa estar selecionado ao mesmo tempo.

    // Grupo de gênero: Masculino OU Feminino (nunca os dois)
    private final ToggleGroup grupoGenero = new ToggleGroup();

    // Grupo de perfil: Passageiro OU Motorista (nunca os dois)
    private final ToggleGroup grupoPerfil  = new ToggleGroup();


    // ── INICIALIZAÇÃO ─────────────────────────────────────────────────────────
    // O JavaFX chama este método automaticamente logo após carregar o .fxml
    // e injetar os componentes com @FXML.
    // Aqui configuramos os grupos e definimos os valores padrão.
    @FXML
    public void initialize() {
        // Associa os RadioButtons de gênero ao mesmo grupo
        radioMasculino.setToggleGroup(grupoGenero);
        radioFeminino.setToggleGroup(grupoGenero);
        radioMasculino.setSelected(true);  // "Masculino" vem selecionado por padrão

        // Associa os RadioButtons de perfil ao mesmo grupo
        radioPassageiro.setToggleGroup(grupoPerfil);
        radioMotorista.setToggleGroup(grupoPerfil);
        radioPassageiro.setSelected(true); // "Passageiro" vem selecionado por padrão

        comboFormaPagamento.getItems().addAll(
                "PIX", "CARTAO_CREDITO", "CARTAO_DEBITO", "DINHEIRO"
        );
        comboFormaPagamento.setValue("PIX"); // opção padrão

        radioPassageiro.selectedProperty().addListener((obs, old, novo) -> {
            boolean motorista = !novo;
            campoCnh.setVisible(motorista);
            campoModelo.setVisible(motorista);
            campoPlaca.setVisible(motorista);
            labelCnh.setVisible(motorista);
            labelModelo.setVisible(motorista);
            labelPlaca.setVisible(motorista);
        });

        campoCnh.setVisible(false);
        campoModelo.setVisible(false);
        campoPlaca.setVisible(false);
        labelCnh.setVisible(false);
        labelModelo.setVisible(false);
        labelPlaca.setVisible(false);    }


    // ── AÇÃO: BOTÃO "CADASTRAR" ───────────────────────────────────────────────
    // Chamado ao clicar no botão de cadastro.
    // Lê os dados do formulário, valida e envia para o banco.
    @FXML
    private void cadastrar() {
        // Lê os valores dos campos de texto e remove espaços desnecessários
        String nome  = campoNome.getText().trim();
        String email = campoEmail.getText().trim();
        String senha = campoSenha.getText().trim();
        String cpf = campoCpf.getText().trim();
        String telefone = campoTelefone.getText().trim();
        String cnh = campoCnh.getText().trim();
        String modelo = campoModelo.getText().trim();
        String placa = campoPlaca.getText().trim();
        String formaPagamento = comboFormaPagamento.getValue();



        // Validação 1: todos os campos de texto devem estar preenchidos
        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty() || cpf.isEmpty() || telefone.isEmpty()) {
            mostrar("Preencha todos os campos!", false); // false = estilo de erro
            return; // Interrompe sem enviar ao banco
        }
        if (radioMotorista.isSelected() && cnh.isEmpty() || modelo.isEmpty() || placa.isEmpty()) {
            mostrar("Preencha todos os campos!", false); // false = estilo de erro
            return; // Interrompe sem enviar ao banco
        }

        // Validação 2: senha deve ter no mínimo 4 caracteres
        if (senha.length() < 4) {
            mostrar("Senha deve ter ao menos 4 caracteres.", false);
            return;
        }

        // Lê qual RadioButton está selecionado e converte para o código do banco
        // Operador ternário: se "radioFeminino" estiver marcado, usa "F"; senão, "M"
        String genero = radioFeminino.isSelected() ? "F" : "M";

        // Mesmo princípio para o perfil de conta
        String tipo   = radioMotorista.isSelected() ? "MOTORISTA" : "PASSAGEIRO";

        // Tenta cadastrar no banco de dados (ou MockData)
        // Retorna true se deu certo, false se o e-mail já existe
        boolean ok = procedureExecutor.cadastrarUsuario(
                nome, email, senha, cpf, telefone,
                genero, tipo, formaPagamento,
                cnh, modelo, placa
        );

        if (ok) {
            // Cadastro realizado com sucesso — volta para a tela de login
            try {
                App.trocarTela("tela-inicial.fxml");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Falha: e-mail já está cadastrado no sistema
            mostrar("E-mail ja cadastrado.", false);
        }


    }


    // ── AÇÃO: BOTÃO/LINK "VOLTAR" ─────────────────────────────────────────────
    // Volta para a tela de login sem realizar nenhum cadastro.
    @FXML
    private void voltarLogin() {
        try {
            App.trocarTela("tela-inicial.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // ── MÉTODO AUXILIAR: EXIBIR MENSAGEM ─────────────────────────────────────
    // Exibe uma mensagem no label de feedback da tela.
    // O parâmetro "sucesso" determina o estilo visual:
    //   true  → estilo de sucesso (texto verde, por exemplo)
    //   false → estilo de erro (texto vermelho, por exemplo)
    // Os estilos "mensagem-erro" e "mensagem-sucesso" são definidos no CSS.
    private void mostrar(String msg, boolean sucesso) {
        mensagem.setText(msg);
        // Remove os estilos anteriores para não acumular
        mensagem.getStyleClass().removeAll("mensagem-erro", "mensagem-sucesso");
        // Adiciona o estilo correto conforme o tipo da mensagem
        mensagem.getStyleClass().add(sucesso ? "mensagem-sucesso" : "mensagem-erro");
        mensagem.setVisible(true); // Garante que o label esteja visível
    }
}
