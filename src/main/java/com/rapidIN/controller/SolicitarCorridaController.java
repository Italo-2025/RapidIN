package com.rapidIN.controller;

// ============================================================
// ARQUIVO: SolicitarCorridaController.java
// RESPONSABILIDADE: Controlar a tela de solicitação de corrida.
//
// CONTEXTO:
//   Esta tela era usada em uma versão anterior do sistema como
//   tela dedicada para o fluxo de solicitar corrida.
//   Atualmente o PainelPassageiroController já incorpora essa
//   funcionalidade diretamente no painel do passageiro.
//   Este controller existe para o caso de a tela
//   "solicitar-corrida.fxml" ser exibida de forma separada.
//
// O QUE ESTA TELA FAZ:
//   1. Recebe origem e destino digitados pelo passageiro
//   2. Permite calcular o preço estimado antes de solicitar
//   3. Registra a corrida com status AGUARDANDO
//   4. Exibe o histórico de corridas do passageiro em uma tabela
//   5. Tem um botão "Voltar" para retornar ao painel do passageiro
//
// DIFERENÇA PARA O PainelPassageiroController:
//   Esta tela é mais simples e focada somente na ação de solicitar.
//   O painel do passageiro integra tudo em uma única tela.
// ============================================================

// Importações necessárias
import com.rapidIN.App;                          // Para trocar de tela
import com.rapidIN.SessionManager;              // Para obter o usuário logado
import com.rapidIN.database.procedureExecutor; // Para calcular preço e solicitar corrida
import com.rapidIN.model.corrida;               // Tipo dos itens da tabela
import com.rapidIN.model.Usuario;               // Tipo do usuário logado

// Coleções e componentes JavaFX
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class SolicitarCorridaController {

    // ── COMPONENTES VISUAIS ───────────────────────────────────────────────────
    // Vinculados ao arquivo solicitar-corrida.fxml via @FXML

    @FXML private TextField campoOrigem;       // Campo para digitar o endereço de partida
    @FXML private TextField campoDestino;      // Campo para digitar o endereço de chegada
    @FXML private Label labelPreco;            // Exibe o preço estimado após calcular
    @FXML private Label labelStatus;           // Exibe mensagens de feedback ao usuário

    // Tabela que exibe o histórico de corridas do passageiro
    @FXML private TableView<corrida> tabelaCorridas;

    // Colunas da tabela — cada uma mostra um campo diferente do objeto corrida
    @FXML private TableColumn<corrida, String> colunaOrigem;   // Coluna "Origem"
    @FXML private TableColumn<corrida, String> colunaDestino;  // Coluna "Destino"
    @FXML private TableColumn<corrida, String> colunaStatus;   // Coluna "Status"
    @FXML private TableColumn<corrida, String> colunaPreco;    // Coluna "Preço"


    // ── REFERÊNCIA AO USUÁRIO LOGADO ─────────────────────────────────────────
    private Usuario usuario;


    // ── INICIALIZAÇÃO ─────────────────────────────────────────────────────────
    // Chamado automaticamente pelo JavaFX após injetar os componentes @FXML.
    @FXML
    public void initialize() {
        // Obtém o passageiro logado via SessionManager
        usuario = SessionManager.getInstance().getUsuario();

        // Configura cada coluna para exibir o campo correto do objeto corrida.
        // PropertyValueFactory("origem") faz o JavaFX chamar getOrigem()
        // em cada objeto corrida automaticamente.
        colunaOrigem.setCellValueFactory(new PropertyValueFactory<>("origem"));
        colunaDestino.setCellValueFactory(new PropertyValueFactory<>("destino"));
        colunaStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colunaPreco.setCellValueFactory(new PropertyValueFactory<>("precoFormatado")); // Ex.: "R$ 18,50"

        // Carrega o histórico de corridas ao abrir a tela
        carregarHistorico();
    }


    // ── AÇÃO: BOTÃO "CALCULAR PREÇO" ─────────────────────────────────────────
    // Consulta o preço estimado para a rota e exibe no label.
    @FXML
    private void calcularPreco() {
        String origem  = campoOrigem.getText().trim();
        String destino = campoDestino.getText().trim();

        // Ambos os campos devem estar preenchidos para calcular
        if (origem.isEmpty() || destino.isEmpty()) {
            labelStatus.setText("Preencha origem e destino!");
            return;
        }

        // Consulta o preço via banco (real ou mock)
        double preco = procedureExecutor.calcularPreco(origem, destino);

        // Formata com duas casas decimais e exibe
        labelPreco.setText(String.format("R$ %.2f", preco));
    }


    // ── AÇÃO: BOTÃO "SOLICITAR CORRIDA" ──────────────────────────────────────
    // Registra a corrida no sistema com status AGUARDANDO.
    @FXML
    private void solicitarCorrida() {
        // Verifica se a sessão ainda está ativa (proteção contra expiração)
        if (usuario == null) {
            labelStatus.setText("Sessao expirada.");
            return;
        }

        String origem  = campoOrigem.getText().trim();
        String destino = campoDestino.getText().trim();

        if (origem.isEmpty() || destino.isEmpty()) {
            labelStatus.setText("Preencha origem e destino!");
            return;
        }

        // Envia a solicitação para o banco e recebe o ID da corrida criada.
        // ID positivo = sucesso; -1 = falha.
        int id = procedureExecutor.solicitarCorrida(usuario.getId(), origem, destino);

        if (id > 0) {
            // Corrida criada com sucesso
            labelStatus.setText("Corrida solicitada! Aguardando motorista...");

            // Limpa os campos para uma possível nova solicitação
            campoOrigem.clear();
            campoDestino.clear();
            labelPreco.setText("R$ --");

            // Atualiza a tabela para mostrar a nova corrida no histórico
            carregarHistorico();
        } else {
            labelStatus.setText("Erro ao solicitar corrida.");
        }
    }


    // ── CARREGAMENTO DO HISTÓRICO ─────────────────────────────────────────────
    // Busca as corridas do passageiro e preenche a tabela.
    // Chamado na inicialização e após cada nova solicitação.
    private void carregarHistorico() {
        if (usuario == null) return; // Não tenta carregar sem usuário logado

        // Busca a lista de corridas do passageiro
        List<corrida> corridas = procedureExecutor.corridasPassageiro(usuario.getId());

        // FXCollections.observableArrayList converte a lista Java para
        // um formato que o JavaFX consegue monitorar e renderizar na tabela
        tabelaCorridas.setItems(FXCollections.observableArrayList(corridas));
    }


    // ── AÇÃO: BOTÃO "VOLTAR" ──────────────────────────────────────────────────
    // Retorna para o painel principal do passageiro.
    @FXML
    private void voltar() {
        try {
            App.trocarTela("painel-passageiro.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
