package com.rapidIN;

// ============================================================
// ARQUIVO: Main.java
// RESPONSABILIDADE: Ponto de entrada técnico do programa.
//
// POR QUE ESTE ARQUIVO EXISTE?
//   Quando rodamos o sistema diretamente pelo IntelliJ IDEA
//   (nossa IDE), às vezes aparece o erro:
//   "JavaFX runtime components are missing"
//   Isso acontece porque o IntelliJ precisa que a classe
//   inicial não herde diretamente do JavaFX.
//
//   A solução é simples: este arquivo é um "intermediário"
//   que apenas chama o App.java, sem herdar nada do JavaFX.
//   Assim o IntelliJ fica satisfeito e o programa inicia.
//
// CONFIGURAÇÃO NECESSÁRIA:
//   Na IDE, configure esta classe (Main) como a "Main Class"
//   nas Run Configurations (configurações de execução).
//
// ANALOGIA: É como um botão de liga/desliga externo que
//   aciona o motor principal (App.java) sem tocá-lo diretamente.
// ============================================================
public class Main {

    // Este é o metodo de entrada do programa em Java.
    // Toda aplicação Java começa aqui.
    public static void main(String[] args) {
        // Simplesmente repassa o controle para App.java,
        // que é quem realmente inicializa o sistema.
        App.main(args);
    }
}
