package rapdin.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import rapdin.database.ProcedureExecutor;
import rapdin.database.ProcedureExecutor.ResultadoCorrida;

/**
 * SolicitarCorridaController
 *
 * Responsabilidade: receber os dados digitados na tela (FXML),
 * chamar o ProcedureExecutor e exibir o resultado de volta na tela.
 *
 * O JavaFX liga automaticamente os campos @FXML aos elementos
 * do arquivo .fxml pelo atributo fx:id.
 */
public class SolicitarCorridaController {

    // -------------------------------------------------------
    // Campos ligados ao FXML (fx:id deve ser igual ao nome)
    // -------------------------------------------------------
    @FXML private TextField campoPassageiroId;
    @FXML private TextField campoOrigemId;
    @FXML private TextField campoDestinoId;
    @FXML private Label     labelResultado;

    /**
     * Chamado pelo botao "Solicitar Corrida" no FXML
     * via onAction="#handleSolicitarCorrida"
     */
    @FXML
    private void handleSolicitarCorrida() {

        // 1) le e valida os campos da tela
        String txtPassageiro = campoPassageiroId.getText().trim();
        String txtOrigem     = campoOrigemId.getText().trim();
        String txtDestino    = campoDestinoId.getText().trim();

        if (txtPassageiro.isEmpty() || txtOrigem.isEmpty() || txtDestino.isEmpty()) {
            labelResultado.setText("Preencha todos os campos.");
            labelResultado.setStyle("-fx-text-fill: red;");
            return;
        }

        int passageiroId, origemId, destinoId;
        try {
            passageiroId = Integer.parseInt(txtPassageiro);
            origemId     = Integer.parseInt(txtOrigem);
            destinoId    = Integer.parseInt(txtDestino);
        } catch (NumberFormatException e) {
            labelResultado.setText("Os IDs precisam ser numeros inteiros.");
            labelResultado.setStyle("-fx-text-fill: red;");
            return;
        }

        // 2) chama o banco via ProcedureExecutor
        try {
            ResultadoCorrida resultado = ProcedureExecutor.solicitarCorrida(
                    passageiroId, origemId, destinoId);

            // 3) exibe o resultado na tela
            labelResultado.setText(
                    "Corrida #" + resultado.corridaId + " criada!\n" + resultado.mensagem);
            labelResultado.setStyle("-fx-text-fill: green;");

        } catch (Exception e) {
            // banco fora, dados invalidos, etc.
            labelResultado.setText("Erro ao conectar ao banco: " + e.getMessage());
            labelResultado.setStyle("-fx-text-fill: red;");
        }
    }
}
