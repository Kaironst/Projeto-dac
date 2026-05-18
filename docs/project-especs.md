# UFPR - TADS

# Trabalho de DS152 - DAC

# BANTADS - Internet Banking do TADS

## SUMÁRIO

### 1 REQUISITOS FUNCIONAIS............................................................................................................................. 2

### 2 DECOMPOSIÇÃO POR SUBDOMÍNIO............................................................................................................ 4

### 3 ARQUITETURA................................................................................................................................................. 5

### 4 DADOS PRÉ-CADASTRADOS........................................................................................................................ 7

### 5 REQUISITOS NÃO-FUNCIONAIS E COMENTÁRIOS..................................................................................... 9

### 6 NORMAS PARA O PROTÓTIPO.................................................................................................................... 11

### 7 O QUE DEVE SER ENTREGUE NO ARQUIVO .ZIP..................................................................................... 12

### 8 NORMAS PARA DEFESA FINAL................................................................................................................... 13


## 1 REQUISITOS FUNCIONAIS

O objetivo deste trabalho é o desenvolvimento de um sistema de Internet Banking usando
tecnologia Javascript (Angular, React ou Vue) e Java com Spring Boot, baseado na arquitetura de
microsserviços.

```
O sistema possui 3 perfis de acesso:
● Cliente : usuários com esse perfil são os clientes do banco BANTADS;
● Gerente : usuários com esse perfil são gerentes de contas dos clientes;
● Administrador : usuários com esse perfil são administradores do sistema.
```
Os requisitos funcionais são apresentados a seguir (CRUD significa - Inserir, Remover, Atualizar e
Listar todos).

#### PERFIL CLIENTE

```
● R1: Autocadastro - Uma pessoa só se torna cliente do BANTADS a partir do autocadastro,
que é feito a partir da página inicial, sem necessitar login.
○ Ao fazer o autocadastro o cliente irá para aprovação por um gerente
○ O gerente responsável pela sua aprovação e conta será o que possuir menos clientes
e será atribuído automaticamente pelo sistema. Em caso de mais de um gerente
com o número mínimo, escolhe um;
○ A senha do cliente só será enviada após a aprovação do gerente.
○ Cada cliente só pode ter uma conta no BANTADS;
○ Um cliente não pode se autocadastrar mais de uma vez, devendo retornar erro.
Mesmo que o cliente já esteja aguardando aprovação. A verificação é feita por CPF.
○ Na tela de autocadastro, ao final da solicitação, o sistema mostra uma mensagem
indicando que a solicitação foi enviada, pois o resto da operação é assíncrona.
○ Em caso de falha (algum processo interno da transação falhou), um e-mail deve ser
enviado ao cliente indicando que a solicitação não foi efetuada.
● R2: Login/Logout - Login com e-mail/senha, todas as demais funcionalidades não podem
ser acessadas sem um login com sucesso.
● R3: Tela Inicial de Cliente - Deve apresentar um menu, com as operações que podem ser
efetuadas pelo cliente, e seu saldo atual. Se estiver negativo deve ser com sinal de "-" e em
vermelho;
● R4: Alteração de Perfil - O cliente pode alterar seus dados de perfil, menos o CPF. Se
houver alteração no valor do seu salário, o novo limite deve ser calculado. Se o novo limite
for menor que o seu saldo negativo neste momento, então seu limite será ajustado para
seu saldo negativo. Ao final, devem ser apresentados os novos dados de perfil e também
seu saldo e o nome do seu gerente;
● R5: Depositar - Para simplificar, vamos assumir que ao fazer o depósito o dinheiro real é
adicionado na conta e, portanto, as bases de dados podem ser atualizadas; Você não pode
depositar na conta de outro cliente;
● R6: Saque - Para simplificar, vamos assumir que ao fazer um saque o dinheiro é retirado
da conta e está disponível para o cliente e, portanto, as bases de dados podem ser
atualizadas. Só pode haver saque se houver saldo suficiente na conta, contando com o
limite do cliente; Você não pode sacar da conta de outro cliente;
● R7: Transferência - O cliente informa o número da conta corrente destino e saldo para
transferência. A operação é automática e é registrada naquela data/hora em que foi
solicitada;
● R8: Consulta de extrato - O cliente pode, a qualquer momento, consultar seu extrato,
informando a data de início e a data de fim.
○ Devem ser apresentados: data/hora da transação, operação (transferência, depósito,
saque), cliente origem/destino (preencher em caso de transferência), valor.
○ Se o valor for de saída (transferência ou saque) o registro deve ser mostrado todo
em vermelho.
○ Se a operação for de entrada (transferência ou depósito) o registro deve ser
mostrado todo em azul.
○ A cada dia, desde a data inicial, deve-se mostrar o saldo consolidado naquele dia,
mesmo que não tenha movimentações;
```

#### PERFIL GERENTE

```
○ R9: Tela Inicial do Gerente - Deve apresentar todos os pedidos de autocadastro para
aprovação, como uma tabela, contendo o CPF, Nome e salário do cliente, junto com dois
botões "Aprovar" e "Recusar".
○ R10: Aprovar Cliente - A partir da tela inicial do gerente, este pode aprovar um cliente.
Quando isso acontece, um novo registro de conta é gerado, com uma string que é um
número aleatório de 4 dígitos. Também, um e-mail contendo a senha (aleatória) do cliente
deve ser enviada para seu e-mail.
■ Uma das informações é o salário mensal do cliente, que é usado para calcular seu
limite. Se o cliente ganha R$ 2.000,00 por mês, ou mais, então tem direito ao limite
em sua conta, que é exatamente igual a metade do seu salário.
■ A conta só é criada após a aprovação
■ Após aprovação a senha é enviada por e-mail
○ R11: Rejeitar Cliente - A partir da tela inicial do gerente, este pode rejeitar um cliente.
Deve-se cadastrar um motivo e um e-mail também deve ser enviado para o cliente,
contendo o motivo da reprovação. A data/hora em que a aprovação/reprovação for efetuada
também deve ser armazenada;
○ R12: Consultar Todos os Clientes - Deve apresentar em uma tabela todos os seus
clientes, contendo CPF, Nome, Cidade, Estado, Saldo da conta, Limite da conta. Deve ser
ordenado de forma crescente por Nome. Deve ser disponibilizado um campo de texto onde
o gerente pode pesquisar o cliente por CPF (ou parte dele) e Nome (ou parte dele). Cada
cliente deve possuir um link que, ao ser pressionado, vai para uma tela contendo todos os
dados do cliente e de sua conta;
○ R13: Consultar Cliente - Em uma tela em branco, o gerente deve informar em um campo
de texto o CPF, o sistema deve mostrar todos os dados do cliente, incluindo os dados de sua
conta (saldo e limite);
○ R14: Consultar 3 melhores clientes - Deve ser apresentada uma tela contendo somente
os clientes que possuem os 3 maiores saldos em conta (de qualquer gerente), mostrando
CPF, Nome, Cidade, Estado, Saldo da conta, ordenado de forma decrescente por saldo;
```
#### PERFIL ADMINISTRADOR

```
○ R15: Tela Inicial Administrador - Apresenta uma tela (pode ser em estilo dashboard)
mostrando todos os gerentes do banco, para cada gerente apresenta: quantos clientes
possui, a totalização (soma) de saldos positivo (0.0 conta como positivo) e a totalização
(soma) de saldos negativos. Deve ser mostrado os gerentes com maiores saldos positivos
primeiro;
○ R16: Relatório de Clientes - Deve apresentar em tela uma lista com todos os clientes do
banco contendo: CPF do cliente, Nome do cliente, E-mail do Cliente, Salário, Número da
conta, Saldo, Limite do cliente, CPF do gerente e Nome do gerente. Deve-se ordenar de
forma crescente por nome do cliente.
○ R17: (CRUD de Gerentes) Inserção de Gerente - Insere um novo gerente e a senha
deve ser informada no formulário (não é enviada por e-mail). Ao inserir o gerente ele
recebe automaticamente uma conta do gerente que possui mais contas atreladas. Se vários
gerentes tiverem a mesma quantidade de contas, considerando os gerentes com mais
contas, pega uma conta do gerente que possui menor saldo positivo. Se for o primeiro
gerente a ser cadastrado, ou se só houver somente mais um gerente e ele tiver somente
uma conta atrelado, este gerente fica sem nenhuma conta;
○ R18: (CRUD de Gerentes) Remoção de Gerente - Ao remover um gerente, as contas
que ele mantém devem ser atribuídas para o gerente que possua menos contas atreladas
naquele momento. Não permitir a remoção do último gerente do banco.
○ R19: (CRUD de Gerentes) Listagem de Gerentes - Apresenta a lista de todos os
gerentes ordenados de forma crescente por nome. Deve-se mostrar: Nome, CPF, E-mail e
Telefone.
○ R20: (CRUD de Gerentes) Alteração de Gerente - Deve-se permitir a alteração de
dados de gerente, nome, e-mail e senha somente.
```

## 2 DECOMPOSIÇÃO POR SUBDOMÍNIO

Em uma análise preliminar, o sistema foi decomposto em poucos subdomínios e foram determinados os
seguintes serviços a serem implementados, contendo seus dados (mínimos, podendo haver mais caso
vocês detectem alguma necessidade):

```
● Cliente : responsável pela manutenção de clientes;
○ Dados de Cliente : Nome, e-mail, CPF, Endereço, Estado, Telefone, Salário
○ Endereço de Cliente : Logradouro, número, complemento, CEP, Cidade, Estado
● Conta : responsável pela manutenção das contas dos clientes;
○ Dados da Conta : Cliente, Número da conta, Data da criação, Saldo, Limite, Gerente
○ Histórico de Movimentações : data/hora, tipo (depósito, saque, transferência), cliente
origem/destino (quando for transferência), valor da movimentação
● Gerente : responsável pela manutenção dos dados de gerentes de contas;
○ Dados do Gerente : Nome, e-mail, CPF, telefone
● Autenticação : responsável pela autenticação no BANTADS.
○ Dados de Usuário : Cliente ou Gerente, Tipo (cliente/gerente/admin), login, senha
```

## 3 ARQUITETURA

O BANTADS deve ser implementado usando-se os seguintes padrões de projeto de microsserviços:

```
● Arquitetura de Microsserviços : para desenvolver o software;
● Padrão API Gateway : para expor a API. Só deve ser implementado um ponto de acesso ao
sistema. O front-end NÃO deve acessar os microsserviços diretamente, só via API Gateway;
● Padrão Database per Service : para manter os dados, sendo que cada serviço só tem acesso ao
seu SGBD. Deve ser implementado o padrão schema-per-service ;
● Padrão CQRS : no microsserviço de Conta deve-se usar CQRS obrigatoriamente. A sincronização
dos dois bancos de dados deve ser feita por mensageria;
● Padrão SAGA Orquestrada : para transações que abrangem vários serviços. Cuidado para não
confundir com a coreografada, aqui você deve implementar a ORQUESTRADA.
● Padrão API Composition : para agregar resultados de consultas;
```
Você deverá usar 2 bancos de dados diferentes:

```
● PostgreSQL : para os serviços transacionais
● MongoDB : para o serviço de autenticação
```
Cada microsserviço, incluindo o API Gateway, deve ser executado em uma imagem Docker separada. O
PostgreSQL executa em um contêiner separado (com todos os _schemas_ ) e o MongoDB em outro.

A FIGURA 1 ilustra o esboço da arquitetura que deve ser implementada.


FIGURA 1: Arquitetura do Sistema

```
FONTE: O Autor (2022)
```

## 4 DADOS PRÉ-CADASTRADOS

Os seguintes dados devem estar pré-cadastrados na sua base de dados, com estes dados em específico (o
nome das colunas e localização pode variar conforme sua modelagem de dados):

```
● Cliente e Auth :
```
```
● Conta
```
```
cliente número
da conta
```
```
saldo limite gerente data criação
```
```
Catharyna 1291 R$ 800,00 R$ 5.000,00 Geniéve 01/01/
```
```
Cleuddônio 0950 R$ -10.000,00 R$ 10.000,00 Godophredo 10/10/
```
```
Catianna 8573 R$ -1.000,00 R$ 1.500,00 Gyândula 12/12/
```
```
Cutardo 5887 R$ 150.000,00 R$ 0,00 Geniéve 22/02/
```
```
Coândrya 7617 R$ 1.500,00 R$ 0,00 Godophredo 01/01/
```
```
● Movimentações
```
```
data/hora tipo cliente origem cliente destino valor
```
```
01/01/
10:
```
```
depósito Catharyna R$ 1.000,
```
#### 01/01/

#### 11:

```
depósito Catharyna R$ 900,
```
#### 01/01/

#### 12:

```
saque Catharyna R$ 550,
```
#### 01/01/

#### 13:

```
saque Catharyna R$ 350,
```
#### 10/01/

#### 15:

```
depósito Catharyna R$ 2.000,
```
#### 15/01/

#### 08:

```
saque Catharyna R$ 500,
```
#### 20/01/

#### 12:

```
transferência Catharyna Cleuddônio R$ 1.700,
```
```
cpf nome email senha salário dados de
endereço
```
```
12912861012 Catharyna cli1@bantads.com.br tads R$ 10.000,00 você escolhe
```
```
09506382000 Cleuddônio cli2@bantads.com.br tads R$ 20.000,00 você escolhe
```
```
85733854057 Catianna cli3@bantads.com.br tads R$ 3.000,00 você escolhe
```
```
58872160006 Cutardo cli4@bantads.com.br tads R$ 500,00 você escolhe
```
```
76179646090 Coândrya cli5@bantads.com.br tads R$ 1.500,00 você escolhe
```

#### 01/01/

#### 12:

```
depósito Cleuddônio R$ 1.000,
```
#### 02/01/

#### 10:

```
depósito Cleuddônio R$ 5.000,
```
#### 10/01/

#### 10:

```
saque Cleuddônio R$ 200,
```
#### 05/02/

#### 10:

```
depósito Cleuddônio R$ 7.000,
```
```
05/05/2025 depósito Catianna R$ 1.000,
```
```
06/05/2025 saque Catianna R$ 2.000,
```
```
01/06/2025 depósito Cutardo R$ 150.000,
```
```
01/07/2025 depósito Coândrya R$ 1.500,
```
**● Gerente/Administrador e Auth**

```
cpf nome email senha tipo
```
```
98574307084 Geniéve ger1@bantads.com.br tads gerente
```
```
64065268052 Godophredo ger2@bantads.com.br tads gerente
```
```
23862179060 Gyândula ger3@bantads.com.br tads gerente
```
```
40501740066 Adamântio adm1@bantads.com.
br
```
```
tads administrador
```

## 5 REQUISITOS NÃO-FUNCIONAIS E COMENTÁRIOS

T o d a e qualquer suposição, que não esteja definida aqui e que a equipe faça, deve ser devidamente
documentada e entregue em um arquivo **.pdf** que acompanha o trabalho.

```
● Devem ser usadas as tecnologias vistas na disciplina:
○ Front-end: Angular 13 (mínimo) ou React ou Vue.js
○ API Gateway: Node.js
○ Back-end: Spring-boot (Java ou Kotlin)
○ Acesso às bases de dados: Spring Data JPA
○ Banco de dados: PostgreSQL e MongoDB (para usuários)
○ Conteinerização: Docker
○ Mensageria: RabbitMQ.
● Será usada esta aplicação para testar seus end-points:
https://github.com/razeranthom/test_dac
● End-points que precisam de autenticação devem fazê-lo por meio de token JWT:
○ O end-point de login deve retornar: token , tipo do token , tipo do usuário e o usuário
{
"access_token": "XXXXX",
"token_type": "bearer",
"tipo": "CLIENTE",
"usuario": {
....
}
}
○ Os end-points que precisam de um usuário logado devem fazer a verificação;
○ As chamadas aos end-points que precisam de um usuário logado devem enviar juntamente
no Header
"Authorization" : "Bearer XXXXX"
● Os microsserviços são independentes e possuem bancos de dados separados, um microsserviço
não pode acessar o BD de outro;
○ V o c ê deve usar Schema-per-service , para manter a privacidade dos dados para cada
microsserviço;
○ O Banco de dados de autenticação deve ser MongoDB, os demais devem se PostgreSQL;
● Devem ser usados os padrões Arquitetura de Microsserviços, API Gateway , API Composition (se
necessário), Database Per Service, CQRS (Reserva), SAGA Orquestrada ;
● Cada microsserviço trata de um subdomínio específico. Pode ser criado mais algum microsserviço,
desde que validado com o professor;
● Os microsserviços devem estar em conformidade com o Modelo de Maturidade de Richardson
Nível 2.
● Todos os elementos dos sistemas devem ser conteinerizadas individualmente usando Docker: uma
imagem para o API Gateway, uma imagem para cada microsserviço e uma imagem para o banco
de dados;
● O Front-end só deve se comunicar com o API Gateway, via API HTTP-REST;
● O API Gateway deve se comunicar com seus microsserviços via API HTTP-REST;
● Os microsserviços, se precisarem, devem se comunicar entre si via mensageria (RabbitMQ);
● Entre o servidor e a aplicação em Angular, somente devem trafegar objetos de classes DTO (nunca
objetos persistentes);
● Transações distribuídas devem usar o padrão SAGA
○ A implementação das SAGAs deve ser feita com orquestração usando filas assíncrona com
RabbitMQ. Esse conteúdo não será passado em sala e faz parte do conteúdo de
PESQUISA que vocês devem aprender;
● No mínimo temos as SAGAs: Autocadastro, Alteração de Perfil, Remoção de Gerente, Inserção de
Gerente
○ Autocadastro:
```
```
■ MS Cliente: Criação do registro de Cliente
■ MS Autenticação: Criação do registro de autenticação do cliente
■ MS Conta: Descobrir o gerente com menos clientes
■ MS Gerente: Consulta de gerentes e contas para decidir o gerente que assume a
nova conta
○ Alteração de Perfil:
```

■ MS Cliente: Alteração dos dados do registro de Cliente
■ MS Conta: Cálculo e alteração do novo limite da Conta
○ Remoção de Gerente:
■ MS Gerente: Consultar o gerente com menos contas
■ MS Conta: Atribuição de um novo gerente
■ MS Gerente: Remoção do gerente
○ Inserção de Gerente:
■ MS Gerente: Consultar o gerente com mais contas
■ MS Gerente: Inserção do gerente
■ MS Conta: Atribuição da conta ao novo gerente
● O microsserviço de Conta deve ser implementado com o padrão CQRS, usando fila assíncrona com
RabbitMQ para atualização do banco de dados de consulta;
● O _build_ , geração das imagens e execução deve ser feita a partir de um _shell script_ automatizado;
● Senhas devem ser criptografadas (SHA256+SALT);
● O leiaute deve ser agradável, usando Bootstrap ou Material no Angular;
● Os valores monetários devem ser todos arredondados para 2 casas decimais;
● Todos os campos que precisarem devem ter validação;
● Todas as datas e valores monetários devem ser entrados e mostrados no formato brasileiro;
● Todos os campos que tiverem formatação devem possuir máscara;
● Os bancos de dados devem estar normalizados apropriadamente, exceto o banco de leitura do
microsserviço Conta (CQRS) que pode estar denormalizado;
● O sistema será testado usando o navegador FIREFOX, versão mais recente.


## 6 NORMAS PARA O PROTÓTIPO

### Vocês deverão gravar um vídeo de, no máximo, 20 minutos explicando cada requisito e mostrando no

### protótipo.

### O Vídeo deve estar em boa resolução e a resolução da gravação (notebook/computador) deve estar

### baixa para permitir a visualização das telas/códigos.

### Vídeos com resolução muito alta ou com conteúdo ilegível, receberão nota zero.

### Não há necessidade de todos falarem no vídeo.

### Deixem claro no início da explicação de cada requisito seu número e nome. Isso pode ser feito com

### elementos na tela, como textos indicativos.

### Deve ser explicado também a organização do código do front, com seus módulos, componentes, serviços,

### pipes , diretivas, models , etc.


## 7 O QUE DEVE SER ENTREGUE NO ARQUIVO .ZIP

1. Todos os fontes do projeto;
2. _Scripts_ de inicialização do banco de dados (criação e inserções);
3. _Scripts_ para construção das imagens e execução do projeto;
4. Arquivos de configuração, parâmetros, etc.

O arquivo compactado não deve passar de 50Mb.

Se passar, use seu compactador favorito para separar em partes de 50Mb e entregue todas as partes

### Cuidado para remover arquivos inúteis (executáveis, bibliotecas, diretório

### node_modules) antes da compactação.


## 8 NORMAS PARA DEFESA FINAL

Diretrizes:

- A defesa deve demorar uns 20 min por equipe.
- Vocês devem trazer suas máquinas para rodar a aplicação.
- No momento da defesa **tudo deve estar no ar:** Front, Back, BD, Containers, etc.
- A IDE de desenvolvimento com os códigos de front e back devem estar abertos
- A IDE de acesso ao banco de dados deve estar aberta
- O Console do RabbitMQ deve estar aberto
- No momento da defesa o **projeto de teste** deve estar instalado e será executado na hora
- Não serão aceitos projetos sem integração Front x Back, ou rodando com LocalStorage/json-server.
- Não serão aceitos projetos "rodando" somente no Postman.
- Não serão aceitos projetos sem a implementação dos microsserviços solicitados.
- Não serão aceitos projetos sem a implementação dos microsserviços solicitados.
- Todos os fontes, bancos de dados e scripts devem estar disponíveis para consulta e execução

A nota será individual, por aluno, que deverá responder aos questionamentos do professor, bem como
demonstrar fluência no código para explicá-lo, alterá-lo ou criar funcionalidades novas, no momento da
defesa.

As tecnologias permitidas são:

- Front-end: Angular, React, Vue.js
- Back-end: API Gateway em Node.js, Microsserviços com Spring Boot em Java ou Kotlin
- Bancos de dados: PostgreSQL e MongoDB para MS de Auth

**1) REQUISITOS PARA ENTREGA/DEFESA (Sem isso não há defesa)**

- Aplicação de Teste instalada e executando. Ela será executada na hora da defesa;
- Front-end implementado em Angular/React/Vue+Typescript e back-end em Spring Boot (Java ou Kotlin);
- Sistemas usando arquitetura de microsserviços;
- Front-end acessando somente o API Gateway via HTTP-REST;
- Front-end integrado com o back-end
- Não usar Local Storage nem json-server para armazenar as informações do sistema;
- Usar banco de dados distintos por microsserviço (ou _schema-per-service_ );
- Os seguintes requisitos implementados corretamente e de forma completa (arquitetura de MS solicitada,
mensageria, SAGA, etc):
- R01 - Autocadastro
- R02 - Efetuar Login/Logout
- R03 - Tela Inicial de Cliente
- R05 - Depósito
- R09 - Tela inicial do gerente
- R10 - Aprovar Cliente
- R15 - Tela inicial do administrador
- R17 - Inserção de gerentes
- R18 - Remoção de gerentes
- R19 - Listagem de gerentes
- R20 - Alteração de gerentes
- Uma SAGA completamente implementada;
- Uso de mensageria (RabbitMQ);
- API Gateway básico implementado;
- Sistemas devem possuir interface muito bem elaborada. (Não será permitida a entrega de sistemas em
HTML puro ou com interface ruim).


#### 2) O QUE DEVE SER ENTREGUE

Deve ser entregue em arquivo ZIP:

1. Todos os fontes do projeto;
2. _Scripts_ de inicialização do banco de dados (criação e inserções);
3. _Scripts_ para construção das imagens e execução do projeto;
4. Arquivos de configuração, parâmetros, etc.

O arquivo compactado não deve passar de 50Mb.

Se passar, use seu compactador favorito para separar em partes de 50Mb e entregue todas as partes

**!!!!! Cuidado para remover arquivos inúteis (executáveis, bibliotecas, diretório node_modules)
antes da compactação <= VOU DESCONTAR DE QUEM ENTREGAR NODE_MODULES e ARQUIVOS
COMPILADOS)**

#### 3) SOBRE O VÍDEO COM OS REQUISITOS FUNCIONAIS

Um Vídeo contendo a apresentação de todos os requisitos funcionais implementados.
No vídeo deve aparecer - de forma clara - a identificação do requisito (Número e nome, conforme a
especificação do trabalho) que está sendo testado e o teste efetivo de todos os aspectos do requisito.
O vídeo deve ter, no máximo, 20 minutos de duração.
Não há necessidade de todos os integrantes da equipe participarem do vídeo.
Só devem ser mostrados os requisitos funcionais que estão implementados integralmente (front-end e
back-end).
**Mantenha o banco de dados aberto para mostrar que o requisito funcionou, como uma
evidência do teste.**

#### 4) SOBRE A DEFESA DOS NÃO-FUNCIONAIS

Defesa dos requisitos não-funcionais. O sistema deve estar funcionando, todos os contêineres carregados.
Todos os fontes devem estar disponibilizados, bem como banco de dados e _scripts_.
O projeto de Testes deve estar instalado e funcionando. Ele será executado na hora da defesa.
A nota será individual, por aluno, que deverá responder aos questionamentos do professor, bem como
demonstrar fluência no código para explicá-lo, alterá-lo ou criar funcionalidades novas, no momento da
defesa.