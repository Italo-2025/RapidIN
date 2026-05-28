Desenvolvimento de Aplicativo de Mobilidade UrbanaApresentação da Atividade


Bem-vindos ao desafio prático desta formação. Vocês irão desenvolver, em equipe, um aplicativo de transporte por aplicativo inspirado no modelo do Uber, com um diferencial importante de inclusão e segurança: quando uma passageira do sexo feminino solicitar uma corrida, o sistema deverá, obrigatoriamente, encaminhar a solicitação apenas para motoristas do sexo feminino cadastradas e disponíveis.
O objetivo desta atividade é integrar os conhecimentos adquiridos ao longo do curso em um projeto real, completo e funcional, cobrindo desde o planejamento até a entrega final.
Nome sugerido para o projeto
— ou o nome que a equipe preferir, desde que justificado na documentação.
Requisitos do Sistema
O aplicativo deve contemplar dois perfis de usuário: passageiro e motorista. Cada perfil terá funcionalidades específicas conforme descrito a seguir.
No perfil de passageiro, o sistema deve permitir cadastro com informações pessoais incluindo gênero, login com autenticação, solicitação de corrida informando origem e destino, visualização do motorista designado e do status da corrida, histórico de corridas realizadas e avaliação do motorista ao final da corrida.
No perfil de motorista, o sistema deve permitir cadastro com informações pessoais incluindo gênero, login com autenticação, definição de status de disponibilidade (online/offline), recebimento e aceite ou recusa de corridas, visualização do trajeto a ser realizado, histórico de corridas e avaliação do passageiro.
A regra de negócio central é a seguinte: quando o sistema identificar que o passageiro solicitante é do gênero feminino, o algoritmo de designação deve filtrar exclusivamente motoristas femininas disponíveis. Caso não haja motorista feminina disponível no momento, o sistema deve notificar a passageira e permitir que ela aguarde ou cancele a solicitação. Para passageiros do gênero masculino ou não especificado, o sistema designa qualquer motorista disponível normalmente.
Requisitos Técnicos
A equipe tem liberdade para escolher a stack tecnológica, mas o projeto deve obrigatoriamente conter uma interface gráfica funcional, seja ela desktop ou web, um banco de dados relacional para armazenamento de usuários, corridas e histórico, e lógica de negócio implementada no backend com as regras descritas acima.
Sugestões de tecnologias incluem: para backend, Java com Spring Boot, Python com Flask ou Django, ou Node.js com Express. Para banco de dados, MySQL, PostgreSQL ou SQLite. Para frontend, HTML/CSS/JavaScript, JavaFX, ou Tkinter caso optem por Python desktop.
Entregas Obrigatórias
As equipes devem entregar três itens ao final das 10 horas.
O primeiro é o software funcional, incluindo o código-fonte organizado, o banco de dados configurado e populado com dados de teste, e a aplicação rodando sem erros críticos com as funcionalidades principais operantes.
O segundo é a documentação do sistema, que deve conter uma visão geral do projeto e seus objetivos, o diagrama de casos de uso, o diagrama entidade-relacionamento do banco de dados, a descrição das regras de negócio implementadas, e os requisitos funcionais e não funcionais listados em prosa ou tabela.
O terceiro é o manual do usuário, escrito de forma clara e acessível para pessoas sem conhecimento técnico, explicando como realizar o cadastro, como solicitar uma corrida (passageiro) e como aceitar corridas (motorista), e como funciona a regra de encaminhamento feminino.
Organização das 10 Horas
Sugere-se a seguinte distribuição de tempo para orientar as equipes, lembrando que é uma referência e cada grupo pode adaptar conforme seu ritmo.
Da primeira à segunda hora, o foco deve ser no planejamento: definição da stack, divisão de tarefas, modelagem do banco de dados e esboço das telas. Da terceira à quinta hora, implementação do banco de dados, cadastro e autenticação de usuários. Da sexta à oitava hora, desenvolvimento da lógica de corridas com a regra de gênero e construção das telas principais. Da nona hora, integração e testes. Na décima hora, finalização da documentação e do manual do usuário.
Critérios de Avaliação
A avaliação considerará quatro dimensões de forma equilibrada.
A funcionalidade do sistema vale 40% da nota, considerando se o software executa sem erros graves, se a regra de encaminhamento por gênero funciona corretamente, e se os fluxos de cadastro, login, solicitação e aceite de corrida estão operantes.
A qualidade da documentação vale 25%, avaliando clareza, completude e organização do documento técnico entregue.
O manual do usuário vale 15%, considerando se está compreensível para um usuário leigo.
A organização do código e do banco de dados vale 20%, observando boas práticas de nomenclatura, estrutura de pastas e modelagem de dados.
Observações Finais para os Alunos
Gerenciem bem o tempo. Não busquem a perfeição absoluta nas primeiras horas — construam primeiro o que é essencial e refinem depois. A comunicação dentro da equipe é tão importante quanto o código. E lembrem: a documentação não é um detalhe, ela é parte do produto. Um software sem documentação é um produto incompleto.
Qualquer dúvida sobre os requisitos, consultem o instrutor nos momentos de checkpoint que ocorrerão ao final da segunda e da quinta hora.
Boa sorte e bom trabalho!