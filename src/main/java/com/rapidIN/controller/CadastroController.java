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
import com.rapidIN.App;
import com.rapidIN.database.procedureExecutor;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
@FXML private TextField     campoNome;
@FXML private TextField     campoEmail;
@FXML private PasswordField campoSenha;
@FXML private TextField     campoCpf;
@FXML private TextField     campoTelefone;

@FXML private RadioButton radioMasculino;
@FXML private RadioButton radioFeminino;
@FXML private RadioButton radioNaoEspecificado;

@FXML private RadioButton radioPassageiro;
@FXML private RadioButton radioMotorista;

// Painel e campo exclusivos de passageiro
@FXML private VBox             painelPassageiro;
@FXML private ComboBox<String> comboFormaPagamento;

// Painel e campos exclusivos de motorista
@FXML private VBox      painelMotorista;
@FXML private TextField campoCnh;
@FXML private TextField campoModeloVeiculo;
@FXML private TextField campoPlacaVeiculo;

@FXML private Label mensagem;

private final ToggleGroup grupoGenero = new ToggleGroup();
private final ToggleGroup grupoPerfil = new ToggleGroup();
    // ── INICIALIZAÇÃO ─────────────────────────────────────────────────────────
    // O JavaFX chama este método automaticamente logo após carregar o .fxml
    // e injetar os componentes com @FXML.
    // Aqui configuramos os grupos e definimos os valores padrão.
    @FXML
    public void initialize() {
        radioMasculino.setToggleGroup(grupoGenero);
        radioFeminino.setToggleGroup(grupoGenero);
        radioNaoEspecificado.setToggleGroup(grupoGenero);
        radioMasculino.setSelected(true);

        radioPassageiro.setToggleGroup(grupoPerfil);
        radioMotorista.setToggleGroup(grupoPerfil);
        radioPassageiro.setSelected(true);

        comboFormaPagamento.getItems().addAll(
                "PIX", "CARTAO_CREDITO", "CARTAO_DEBITO", "DINHEIRO"
        );

        // Mostra/oculta painel conforme o perfil selecionado
        grupoPerfil.selectedToggleProperty().addListener(
                (obs, antigo, novo) -> atualizarPainelPerfil()
        );

        // Garante estado inicial correto (passageiro selecionado por padrão)
        atualizarPainelPerfil();
    }

private void atualizarPainelPerfil() {
    boolean ehPassageiro = radioPassageiro.isSelected();

    painelPassageiro.setVisible(ehPassageiro);
    painelPassageiro.setManaged(ehPassageiro);
    painelMotorista.setVisible(!ehPassageiro);
    painelMotorista.setManaged(!ehPassageiro);

    if (ehPassageiro) {
        campoCnh.clear();
        campoModeloVeiculo.clear();
        campoPlacaVeiculo.clear();
    } else {
        comboFormaPagamento.setValue(null);
    }
}

    // ── AÇÃO: BOTÃO "CADASTRAR" ───────────────────────────────────────────────
    // Chamado ao clicar no botão de cadastro.
    // Lê os dados do formulário, valida e envia para o banco.
    @FXML
    private void cadastrar() {
        String nome     = campoNome.getText().trim();
        String email    = campoEmail.getText().trim();
        String senha    = campoSenha.getText().trim();
        String cpf      = campoCpf.getText().trim();
        String telefone = campoTelefone.getText().trim();

        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()
                || cpf.isEmpty() || telefone.isEmpty()) {
            mostrar("Preencha todos os campos!", false);
            return;
        }

        if (senha.length() < 4) {
            mostrar("Senha deve ter ao menos 4 caracteres.", false);
            return;
        }

        String genero;
        if (radioFeminino.isSelected())             genero = "F";
        else if (radioNaoEspecificado.isSelected()) genero = "NE";
        else                                        genero = "M";

        String tipo = radioMotorista.isSelected() ? "MOTORISTA" : "PASSAGEIRO";

        String formaPagamento = null;
        String numeroCnh      = null;
        String modeloVeiculo  = null;
        String placaVeiculo   = null;

        if ("PASSAGEIRO".equals(tipo)) {
            formaPagamento = comboFormaPagamento.getValue();
            if (formaPagamento == null) {
                mostrar("Selecione a forma de pagamento.", false);
                return;
            }
        } else {
            numeroCnh    = campoCnh.getText().trim();
            modeloVeiculo = campoModeloVeiculo.getText().trim();
            placaVeiculo  = campoPlacaVeiculo.getText().trim();

            if (numeroCnh.isEmpty() || modeloVeiculo.isEmpty() || placaVeiculo.isEmpty()) {
                mostrar("Preencha todos os dados do veiculo.", false);
                return;
            }
        }

        boolean ok = procedureExecutor.cadastrarUsuario(
                nome, email, senha, cpf, telefone,
                genero, tipo,
                formaPagamento,
                numeroCnh, modeloVeiculo, placaVeiculo
        );

        if (ok) {
            try { App.trocarTela("tela-inicial.fxml"); }
            catch (Exception e) { e.printStackTrace(); }
        } else {
            mostrar("E-mail ja cadastrado.", false);
        }
    }
