package com.rapidIN.controller;

// ============================================================
// ARQUIVO: PainelMotoristaController.java
// RESPONSABILIDADE: Controlar o painel principal do motorista.
//   Esta é a tela central de uso para quem tem perfil de
//   motorista no sistema.
//
// O QUE O MOTORISTA PODE FAZER NESTA TELA:
//   1. Ver seu nome de boas-vindas no topo
//   2. Alternar entre ONLINE (disponível) e OFFLINE (indisponível)
//   3. Ver a lista de corridas disponíveis para aceitar
//   4. Aceitar uma corrida selecionada na tabela
//   5. Recusar uma corrida (ela será cancelada)
//   6. Ver o histórico de suas corridas já realizadas
//   7. Fazer logout
//
// REGRA DE GÊNERO NESTA TELA:
//   As corridas exibidas na lista "disponíveis" já chegam
//   filtradas com base no gênero do motorista. Se for motorista
//   feminina, verá todas as corridas. Se for masculino, não verá
//   corridas de passageiras femininas. O filtro acontece em
//   procedureExecutor.corridasDisponiveis(genero).
//
// O QUE É O TOGGLEBUTTON?
//   ToggleButton é um botão que alterna entre dois estados:
//   pressionado (ONLINE) e não-pressionado (OFFLINE).
//   Funciona como um interruptor de luz.
// ============================================================

// Importações necessárias
import com.rapidIN.App;                          // Para trocar de tela
import com.rapidIN.SessionManager;              // Para obter o motorista logado
import com.rapidIN.database.procedureExecutor; // Para operações com o banco
import com.rapidIN.model.corrida;               // Tipo dos itens das tabelas
import com.rapidIN.model.Usuario;               // Tipo do motorista logado

// Coleções e componentes JavaFX
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class PainelMotoristaController {

    // ── COMPONENTES VISUAIS ───────────────────────────────────────────────────

    @FXML private Label labelNomeMotorista;   // Exibe "Olá, [Nome]" no topo
    @FXML private ToggleButton toggleDisponivel; // Botão interruptor ONLINE/OFFLINE
    @FXML private Label labelAcaoStatus;      // Exibe feedback das ações (aceitar, recusar etc.)
    @FXML private Label labelTotalCorridas;
    @FXML private Label labelConcluidas;
    @FXML private Label labelCanceladas;
    @FXML private Label labelGanhoTotal;
    @FXML private Label labelDistanciaTotal;
    @FXML private Label labelNotaMedia;
    @FXML private Label labelDesativar;

    // ── Tabela de corridas disponíveis ──────────────────────────────────────
    // Exibe corridas com status AGUARDANDO que o motorista pode aceitar
    @FXML private TableView<corrida> tabelaDisponiveis;
    @FXML private TableColumn<corrida, String> dColOrigem;      // Coluna "Origem"
    @FXML private TableColumn<corrida, String> dColDestino;     // Coluna "Destino"
    @FXML private TableColumn<corrida, String> dColPassageiro;  // Coluna "Passageiro"
    @FXML private TableColumn<corrida, String> dColPreco;       // Coluna "Preço"

    // ── Tabela de histórico do motorista ────────────────────────────────────
    // Exibe corridas que o motorista já aceitou/realizou/recusou
    @FXML private TableView<corrida> tabelaHistoricoMotorista;
    @FXML private TableColumn<corrida, String> hColOrigem;      // Coluna "Origem"
    @FXML private TableColumn<corrida, String> hColDestino;     // Coluna "Destino"
    @FXML private TableColumn<corrida, String> hColPassageiro;  // Coluna "Passageiro"
    @FXML private TableColumn<corrida, String> hColStatus;      // Coluna "Status"
    @FXML private TableColumn<corrida, String> hColPreco;       // Coluna "Preço"


    // ── REFERÊNCIA AO MOTORISTA LOGADO ───────────────────────────────────────
    private Usuario motorista;


    // ── INICIALIZAÇÃO ─────────────────────────────────────────────────────────
    // Chamado automaticamente pelo JavaFX após injetar os componentes @FXML.
    @FXML
    public void initialize() {
        // Obtém o motorista que está logado
        motorista = SessionManager.getInstance().getUsuario();

        if (motorista != null) {
            // Exibe a saudação com o nome do motorista
            labelNomeMotorista.setText("Ola, " + motorista.getNome());

            // Inicializa o toggle com o status de disponibilidade salvo no banco
            // (o motorista pode ter saído ONLINE da última sessão)
            toggleDisponivel.setSelected(motorista.isDisponivel());

            // Atualiza o texto do botão conforme o estado atual
            atualizarTextoToggle();
        }

        // Configura as colunas da tabela de corridas disponíveis.
        // PropertyValueFactory("nomePassageiro") chama getNomePassageiro()
        // em cada objeto corrida automaticamente.
        dColOrigem.setCellValueFactory(new PropertyValueFactory<>("origem"));
        dColDestino.setCellValueFactory(new PropertyValueFactory<>("destino"));
        dColPassageiro.setCellValueFactory(new PropertyValueFactory<>("nomePassageiro"));
        dColPreco.setCellValueFactory(new PropertyValueFactory<>("precoFormatado"));

        // Configura as colunas da tabela de histórico do motorista
        hColOrigem.setCellValueFactory(new PropertyValueFactory<>("origem"));
        hColDestino.setCellValueFactory(new PropertyValueFactory<>("destino"));
        hColPassageiro.setCellValueFactory(new PropertyValueFactory<>("nomePassageiro"));
        hColStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        hColPreco.setCellValueFactory(new PropertyValueFactory<>("precoFormatado"));

        // Carrega os dados iniciais em ambas as tabelas
        carregarCorridasDisponiveis();
        carregarHistorico();
    }


    // ── AÇÃO: TOGGLE ONLINE/OFFLINE ───────────────────────────────────────────
    // Chamado quando o motorista clica no botão de disponibilidade.
    // Alterna entre ONLINE (aceita corridas) e OFFLINE (não aparece para corridas).
    @FXML
    private void alternarDisponibilidade() {
        // Lê o novo estado do toggle (true = pressionado = ONLINE)
        boolean novoStatus = toggleDisponivel.isSelected();

        // Salva o novo status no banco de dados
        procedureExecutor.atualizarDisponibilidade(motorista.getId(), novoStatus);

        // Atualiza o objeto local para refletir a mudança
        motorista.setDisponivel(novoStatus);

        // Atualiza o texto exibido no botão (ONLINE ou OFFLINE)
        atualizarTextoToggle();

        // Exibe mensagem de feedback e, se ficou ONLINE, carrega corridas disponíveis
        if (novoStatus) {
            carregarCorridasDisponiveis(); // Traz corridas para o motorista agora ONLINE
            labelAcaoStatus.setText("Voce esta ONLINE. Corridas disponiveis carregadas.");
        } else {
            labelAcaoStatus.setText("Voce esta OFFLINE.");
        }
    }


    // ── AÇÃO: BOTÃO "ATUALIZAR CORRIDAS" ─────────────────────────────────────
    // Recarrega a lista de corridas disponíveis a partir do banco.
    // Filtra automaticamente pelo gênero do motorista.
    @FXML
    private void carregarCorridasDisponiveis() {
        if (motorista == null) return;

        // Busca corridas com status AGUARDANDO, já filtradas pela regra de gênero
        List<corrida> corridas = procedureExecutor.corridasDisponiveis(motorista.getGenero());

        // Preenche a tabela com os dados obtidos
        tabelaDisponiveis.setItems(FXCollections.observableArrayList(corridas));
    }


    // ── AÇÃO: BOTÃO "ACEITAR CORRIDA" ─────────────────────────────────────────
    // Vincula o motorista à corrida selecionada e muda seu status para EM_ANDAMENTO.
    @FXML
    private void aceitarCorrida() {
        // Obtém a corrida que o motorista clicou/selecionou na tabela
        corrida selecionada = tabelaDisponiveis.getSelectionModel().getSelectedItem();

        // Verifica se o motorista realmente selecionou uma corrida
        if (selecionada == null) {
            labelAcaoStatus.setText("Selecione uma corrida na tabela.");
            return;
        }

        // Tenta aceitar a corrida no banco de dados
        // Retorna false se a corrida já foi aceita por outro motorista entre
        // o carregamento da lista e o clique (condição de corrida)
        boolean ok = procedureExecutor.aceitarCorrida(selecionada.getId(), motorista.getIdMotorista());

        if (ok) {
            // Aceito com sucesso — informa o endereço de origem para o motorista ir buscar
            labelAcaoStatus.setText("Corrida aceita! Va ao local de origem: " + selecionada.getOrigem());

            // Atualiza ambas as tabelas: a corrida sai de "disponíveis" e entra no histórico
            carregarCorridasDisponiveis();
            carregarHistorico();
        } else {
            // Outro motorista aceitou antes — situação normal em ambiente com múltiplos motoristas
            labelAcaoStatus.setText("Nao foi possivel aceitar. Corrida ja foi aceita por outro motorista.");
        }
    }

    @FXML
    private void iniciarCorrida() {
        // Obtém a corrida que o motorista clicou/selecionou na tabela
        corrida selecionada = tabelaHistoricoMotorista.getSelectionModel().getSelectedItem();

        // Verifica se o motorista realmente selecionou uma corrida
        if (selecionada == null) {
            labelAcaoStatus.setText("Selecione uma corrida na tabela.");
            return;
        }

        // Tenta aceitar a corrida no banco de dados
        // Retorna false se a corrida já foi aceita por outro motorista entre
        // o carregamento da lista e o clique (condição de corrida)
        boolean ok = procedureExecutor.iniciarCorrida(selecionada.getId(), motorista.getIdMotorista());

        if (ok) {
            // Iniciada com sucesso — informa o endereço de destino para o motorista levar
            labelAcaoStatus.setText("Corrida iniciada! Indo até:" + selecionada.getDestino());

            // Atualiza ambas as tabelas: a corrida sai de "disponíveis" e entra no histórico
            carregarCorridasDisponiveis();
            carregarHistorico();
        } else {
            // Outro motorista aceitou antes — situação normal em ambiente com múltiplos motoristas
            labelAcaoStatus.setText("Nao foi possivel iniciar. Corrida ja foi aceita por outro motorista.");
        }
    }

    @FXML
    private void finalizarCorrida() {
        // Obtém a corrida que o motorista clicou/selecionou na tabela
        corrida selecionada = tabelaHistoricoMotorista.getSelectionModel().getSelectedItem();

        // Verifica se o motorista realmente selecionou uma corrida
        if (selecionada == null) {
            labelAcaoStatus.setText("Selecione uma corrida na tabela.");
            return;
        }

        // Calcula preço e distância
        double preco = procedureExecutor.calcularPreco(
                selecionada.getOrigem(),
                selecionada.getDestino()
        );
        double distancia = 1 + (Math.random() * 20);

        // Tenta aceitar a corrida no banco de dados
        // Retorna false se a corrida já foi aceita por outro motorista entre
        // o carregamento da lista e o clique (condição de corrida)
        boolean ok = procedureExecutor.finalizarCorrida(selecionada.getId(), motorista.getIdMotorista(), preco, distancia);

        if (ok) {
            // Iniciada com sucesso — informa o endereço de destino para o motorista levar
            labelAcaoStatus.setText("Corrida finalizad! De: " + selecionada.getOrigem() +" até: "+selecionada.getDestino());

            // Atualiza ambas as tabelas: a corrida sai de "disponíveis" e entra no histórico
            carregarCorridasDisponiveis();
            carregarHistorico();
        } else {
            // Outro motorista aceitou antes — situação normal em ambiente com múltiplos motoristas
            labelAcaoStatus.setText("Nao foi possivel iniciar.");
        }
    }




    // ── AÇÃO: BOTÃO "RECUSAR CORRIDA" ─────────────────────────────────────────
    // Cancela a corrida selecionada (muda status para CANCELADA).
    @FXML
    private void recusarCorrida() {
        // Obtém a corrida selecionada na tabela
        corrida selecionada = tabelaDisponiveis.getSelectionModel().getSelectedItem();

        if (selecionada == null) {
            labelAcaoStatus.setText("Selecione uma corrida na tabela.");
            return;
        }

        // Recusa a corrida no banco — muda status para CANCELADA
        procedureExecutor.recusarCorrida(selecionada.getId());
        labelAcaoStatus.setText("Corrida recusada.");

        // Atualiza a tabela de disponíveis (a corrida recusada some da lista)
        carregarCorridasDisponiveis();
    }


    // ── AÇÃO: BOTÃO "ATUALIZAR HISTÓRICO" ────────────────────────────────────
    // Recarrega o histórico de corridas do motorista.
    @FXML
    private void carregarHistorico() {
        if (motorista == null) return;

        List<corrida> corridas = procedureExecutor.corridasMotorista(motorista.getIdMotorista());
        tabelaHistoricoMotorista.setItems(FXCollections.observableArrayList(corridas));
    }


    // ── AÇÃO: BOTÃO "SAIR" (LOGOUT) ──────────────────────────────────────────
    // Encerra a sessão do motorista e volta para a tela de login.
    @FXML
    private void logout() {
        SessionManager.getInstance().encerrarSessao();
        try {
            App.trocarTela("tela-inicial.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void carregarEstatisticas() {
        try {
            ResultSet rs = procedureExecutor.estatisticasMotorista(motorista.getIdMotorista());
            if (rs != null && rs.next()) {
                labelTotalCorridas.setText("Total de corridas: "  + rs.getInt("total_corridas"));
                labelConcluidas.setText("Corridas concluidas: "   + rs.getInt("corridas_concluidas"));
                labelCanceladas.setText("Corridas canceladas: "   + rs.getInt("corridas_canceladas"));
                labelGanhoTotal.setText("Ganho total: R$ "        + rs.getDouble("ganho_total_brl"));
                labelDistanciaTotal.setText("Distancia total: "   + rs.getDouble("distancia_total_km") + " km");
                labelNotaMedia.setText("Nota media: "             + rs.getDouble("nota_media"));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao carregar estatisticas: " + e.getMessage());
        }
    }

    @FXML
    private void desativarConta() {
        boolean ok = procedureExecutor.desativarUsuario(motorista.getId());
        if (ok) {
            labelDesativar.setText("Conta desativada com sucesso!");
            SessionManager.getInstance().encerrarSessao();
            try { App.trocarTela("tela-inicial.fxml"); }
            catch (Exception e) { e.printStackTrace(); }
        } else {
            labelDesativar.setText("Nao foi possivel desativar. Verifique se ha corrida ativa.");
        }
    }

    // ── MÉTODO AUXILIAR: ATUALIZAR TEXTO DO TOGGLE ───────────────────────────
    // Atualiza o rótulo do botão ONLINE/OFFLINE conforme o estado atual.
    // Chamado sempre que o status de disponibilidade muda.
    private void atualizarTextoToggle() {
        // Operador ternário: se estiver disponível, exibe "ONLINE"; senão "OFFLINE"
        toggleDisponivel.setText(motorista.isDisponivel() ? "ONLINE" : "OFFLINE");
    }
}
