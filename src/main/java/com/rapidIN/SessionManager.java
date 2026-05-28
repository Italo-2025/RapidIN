package com.rapidIN;

// ============================================================
// ARQUIVO: SessionManager.java
// RESPONSABILIDADE: Guardar as informações do usuário logado
//   enquanto ele estiver usando o sistema.
//
// O QUE É UMA "SESSÃO"?
//   Sessão é o período entre o login e o logout. Enquanto você
//   está usando o sistema, o programa precisa saber quem você é
//   para mostrar seu nome, suas corridas, suas permissões etc.
//   O SessionManager é o "crachá digital" do usuário — ele é
//   verificado em vários pontos do sistema.
//
// PADRÃO SINGLETON:
//   Este arquivo usa um padrão de projeto chamado "Singleton".
//   Isso garante que exista APENAS UMA instância (cópia) do
//   SessionManager em toda a execução do programa.
//   Por quê? Porque não faz sentido ter dois "crachás"
//   diferentes circulando — isso causaria inconsistências.
//
// ANALOGIA: Imagine um vestiário de academia com um armário
//   especial que guarda o crachá de quem está dentro. Só cabe
//   um crachá de cada vez. Quando alguém faz login, o crachá
//   entra. Quando faz logout, o crachá é retirado.
// ============================================================

// Importa a "receita" do objeto Usuario para que possamos
// guardar e recuperar os dados do usuário logado.
import com.rapidIN.model.Usuario;

public class SessionManager {

    // ── Atributos ────────────────────────────────────────────────────────────

    // "instancia" guarda a única cópia do SessionManager que existirá.
    // "static" significa que pertence à classe, não a um objeto específico.
    // Começa como "null" (vazia) e só é criada na primeira chamada de getInstance().
    private static SessionManager instancia;

    // "usuarioLogado" guarda os dados (nome, tipo, gênero etc.) do
    // usuário que está atualmente usando o sistema.
    // Será "null" quando ninguém estiver logado.
    private Usuario usuarioLogado;

    // ── Construtor privado ────────────────────────────────────────────────────
    // O construtor "private" impede que outros arquivos criem um
    // SessionManager diretamente com "new SessionManager()".
    // A única forma de obter o SessionManager é pelo metodo getInstance().
    // Isso é o que garante a unicidade do Singleton.
    private SessionManager() {}

    // ── getInstance ──────────────────────────────────────────────────────────
    // Metodo estático que retorna a única instância do SessionManager.
    // Se ainda não existe, cria uma. Se já existe, retorna a mesma.
    // Assim, qualquer parte do sistema que chamar getInstance()
    // sempre receberá o mesmo objeto.
    public static SessionManager getInstance() {
        if (instancia == null) instancia = new SessionManager();
        return instancia;
    }

    // ── setUsuario ───────────────────────────────────────────────────────────
    // Chamado após o login bem-sucedido para "preencher o crachá"
    // com os dados do usuário que acabou de entrar.
    public void setUsuario(Usuario u) { usuarioLogado = u; }

    // ── getUsuario ───────────────────────────────────────────────────────────
    // Permite que outras telas saibam quem está logado.
    // Ex.: o painel do passageiro chama este metodo para saber
    // o nome a exibir no topo da tela.
    public Usuario getUsuario() { return usuarioLogado; }

    // ── encerrarSessao ───────────────────────────────────────────────────────
    // Chamado quando o usuário clica em "Sair" (logout).
    // Define o usuário logado como null, "limpando o crachá"
    // e encerrando a sessão. O próximo usuário precisará
    // fazer login novamente.
    public void encerrarSessao() { usuarioLogado = null; }
}
