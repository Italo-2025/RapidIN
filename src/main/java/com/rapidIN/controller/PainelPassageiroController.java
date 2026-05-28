package com.rapidIN.controller;

// ============================================================
// ARQUIVO: PainelPassageiroController.java
// RESPONSABILIDADE: Controlar o painel principal do passageiro.
//   Esta é a tela central de uso para quem tem perfil de
//   passageiro no sistema.
//
// O QUE O PASSAGEIRO PODE FAZER NESTA TELA:
//   1. Ver seu nome de boas-vindas no topo
//   2. Digitar origem e destino e calcular o preço estimado
//   3. Solicitar uma corrida (cria a corrida com status AGUARDANDO)
//   4. Ver o histórico completo de suas corridas numa tabela
//   5. Fazer logout e voltar para a tela de login
//
// REGRA DE NEGÓCIO PRESENTE AQUI:
//   Quando uma passageira feminina solicita uma corrida, a
//   mensagem exibida informa que ela será atendida por uma
//   motorista feminina — reforçando a política de segurança.
//
// O QUE É UMA TABLEVIEW?
//   TableView é um componente visual que exibe dados em
//   formato de tabela (como uma planilha). Cada linha é um
//   objeto corrida, e cada coluna exibe um campo específico
//   desse objeto. O JavaFX lida automaticamente com a
//   renderização de cada célula.
// ============================================================

// Importações necessárias
import com.rapidIN.App;                          // Para trocar de tela (logout)
import com.rapidIN.SessionManager;              // Para obter o usuário logado
import com.rapidIN.database.procedureExecutor; // Para operações com o banco
import com.rapidIN.model.corrida;               // Tipo dos itens da tabela
import com.rapidIN.model.Usuario;               // Tipo do usuário logado

// Coleções e componentes JavaFX
import javafx.collections.FXCollections;          // Converte listas Java para listas observáveis do JavaFX
import javafx.fxml.FXML;
import javafx.scene.control.*;                    // Label, TextField, TableView, TableColumn etc.
import javafx.scene.control.cell.PropertyValueFactory; // Liga colunas da tabela aos campos do objeto

import java.util.List;

public class PainelPassageiroController {

    // ── COMPONENTES VISUAIS ───────────────────────────────────────────────────

    @FXML private Label labelNomeUsuario;      // Exibe "Olá, [Nome]" no topo da tela
    @FXML private TextField campoOrigem;       // Campo para digitar o endereço de partida
    @FXML private TextField campoDestino;      // Campo para digitar o endereço de destino
    @FXML private Label labelPreco;            // Exibe o preço estimado após calcular
    @FXML private Label labelStatus;           // Exibe mensagens de feedback ao usuário
    @FXML private Label labelCorridaAtualInfo; // Exibe detalhes da corrida recém-solicitada

    // Tabela que exibe o histórico de corridas do passageiro
    @FXML private TableView<corrida> tabelaHistorico;

    // Colunas da tabela — cada uma exibe um campo diferente do objeto corrida
    @FXML private TableColumn<corrida, String> colOrigem;     // Coluna "Origem"
    @FXML private TableColumn<corrida, String> colDestino;    // Coluna "Destino"
    @FXML private TableColumn<corrida, String> colStatus;     // Coluna "Status"
    @FXML private TableColumn<corrida, String> colPreco;      // Coluna "Preço"
    @FXML private TableColumn<corrida, String> colMotorista;  // Coluna "Motorista"


    // ── REFERÊNCIA AO USUÁRIO LOGADO ─────────────────────────────────────────
    // Guardamos o usuário localmente para não precisar consultar o SessionManager
    // a todo momento — é mais eficiente.
    private Usuario usuario;


    // ── INICIALIZAÇÃO ─────────────────────────────────────────────────────────
    // Chamado automaticamente pelo JavaFX após injetar os componentes @FXML.
    // Configura a tabela e carrega os dados iniciais.
    @FXML
    public void initialize() {
        // Obtém o usuário que está logado no sistema
        usuario = SessionManager.getInstance().getUsuario();

        // Exibe a saudação personalizada com o nome do usuário
        if (usuario != null) {
            labelNomeUsuario.setText("Ola, " + usuario.getNome());
        }

        // Configura as colunas da tabela para saberem qual campo do objeto exibir.
        // PropertyValueFactory("origem") chama automaticamente o método getOrigem()
        // em cada objeto corrida da lista. A mesma lógica vale para as demais colunas.
        colOrigem.setCellValueFactory(new PropertyValueFactory<>("origem"));
        colDestino.setCellValueFactory(new PropertyValueFactory<>("destino"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colPreco.setCellValueFactory(new PropertyValueFactory<>("precoFormatado")); // Usa o preço formatado (R$ X,XX)
        colMotorista.setCellValueFactory(new PropertyValueFactory<>("nomeMotorista"));

        // Carrega o histórico de corridas já existentes do passageiro
        carregarHistorico();
    }


    // ── AÇÃO: BOTÃO "CALCULAR PREÇO" ─────────────────────────────────────────
    // Lê origem e destino, consulta o preço estimado e exibe no label.
    @FXML
    private void calcularPreco() {
        String origem  = campoOrigem.getText().trim();
        String destino = campoDestino.getText().trim();

        // Validação: os dois campos precisam estar preenchidos
        if (origem.isEmpty() || destino.isEmpty()) {
            labelStatus.setText("Preencha origem e destino para calcular o preco.");
            return;
        }

        // Consulta o preço via procedureExecutor (banco real ou mock)
        double preco = procedureExecutor.calcularPreco(origem, destino);

        // Formata e exibe o preço com duas casas decimais
        labelPreco.setText(String.format("R$ %.2f", preco));

        // Limpa qualquer mensagem de status anterior
        labelStatus.setText("");
    }


    // ── AÇÃO: BOTÃO "SOLICITAR CORRIDA" ──────────────────────────────────────
    // Registra a corrida no sistema e exibe confirmação ao passageiro.
    @FXML
    private void solicitarCorrida() {
        String origem  = campoOrigem.getText().trim();
        String destino = campoDestino.getText().trim();

        // Validação: os dois campos precisam estar preenchidos
        if (origem.isEmpty() || destino.isEmpty()) {
            labelStatus.setText("Preencha origem e destino.");
            return;
        }

        // Envia a solicitação para o banco. Retorna o ID da corrida criada.
        // Um ID positivo indica sucesso; -1 indica falha.
        int idCorrida = procedureExecutor.solicitarCorrida(usuario.getId(), origem, destino);

        if (idCorrida > 0) {
            // Monta a mensagem de confirmação
            String msg = "Corrida solicitada! Aguardando motorista...";

            // REGRA DE NEGÓCIO: se a passageira for feminina, informa que
            // ela será atendida exclusivamente por uma motorista feminina
            if ("F".equals(usuario.getGenero())) {
                msg = "Corrida solicitada! Sera atendida por uma motorista feminina.";
            }

            labelStatus.setText(msg);

            // Exibe um resumo da corrida solicitada
            labelCorridaAtualInfo.setText("Origem: " + origem + "\nDestino: " + destino + "\nStatus: AGUARDANDO");

            // Limpa os campos para a próxima solicitação
            campoOrigem.clear();
            campoDestino.clear();
            labelPreco.setText("R$ --");

            // Atualiza a tabela de histórico para incluir a nova corrida
            carregarHistorico();
        } else {
            labelStatus.setText("Erro ao solicitar corrida. Tente novamente.");
        }
    }


    // ── AÇÃO: BOTÃO "ATUALIZAR HISTÓRICO" ────────────────────────────────────
    // Recarrega a lista de corridas do passageiro a partir do banco.
    // Também é chamado internamente após solicitar uma nova corrida.
    @FXML
    private void carregarHistorico() {
        if (usuario == null) return; // Proteção: não tenta carregar sem usuário logado

        // Busca todas as corridas deste passageiro
        List<corrida> corridas = procedureExecutor.corridasPassageiro(usuario.getId());

        // FXCollections.observableArrayList converte a lista Java para um formato
        // que o JavaFX consegue "observar" e renderizar na tabela automaticamente
        tabelaHistorico.setItems(FXCollections.observableArrayList(corridas));
    }


    // ── AÇÃO: BOTÃO "SAIR" (LOGOUT) ──────────────────────────────────────────
    // Encerra a sessão do usuário e volta para a tela de login.
    @FXML
    private void logout() {
        // Apaga os dados do usuário logado do SessionManager
        SessionManager.getInstance().encerrarSessao();
        try {
            App.trocarTela("tela-inicial.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
