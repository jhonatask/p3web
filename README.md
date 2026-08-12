# Plataforma P3

Backend da **Plataforma P3**: um monolito modular unico, organizado por
pacotes de dominio, que nasceu como a API de controle de presenca (chamada)
da Escola de Hombridade e esta evoluindo para ser a base de todos os
ministerios da Family que giram em torno de um cadastro unico por CPF
(Escola de Hombridade, Homem ao Maximo, Primeiros Passos, Servos, GCs de
Homens — ver "Participante e Historico" abaixo). O nome do repositorio ficou
o mesmo por continuidade historica, mas o escopo ja e maior que "chamada".

A decisao de arquitetura, o modelo de dados unificado e o roadmap dos
proximos modulos estao documentados em
`../relatorio_arquitetura_plataforma_p3.pdf`, a partir dos mapas de execucao
de produto em `../proposta/`. A documentacao de arquitetura e modelo de
dados especifico da Escola de Hombridade (o primeiro modulo, ja
implementado) esta em `../parte-tecnica-app-chamada.md`, um nivel acima
deste diretorio. Este README cobre o setup e as convencoes deste
repositorio backend — inclui, alem da Escola de Hombridade, a base de
cadastro `Participante`/`HistoricoItem` compartilhada pelos futuros modulos.

Regra central que atravessa todo o backend: **nenhum modulo tem coluna
propria de nome/CPF/celular** — todos referenciam `Participante` (a
entidade "Pessoa" do relatorio de arquitetura) e so gravam o que e seu
(matricula, presenca, conclusao, papel). CPF ja cadastrado **atualiza**,
nunca duplica.

## Stack

- Java 21
- Spring Boot 4.1.0 (Web MVC, Data JPA, Security, Validation, WebSocket/STOMP)
- PostgreSQL 16
- Flyway (migrations de schema)
- ZXing (`core`) para geracao da imagem do QR code dinamico das aulas

## Como rodar localmente

O projeto usa `spring-boot-docker-compose`, entao o Postgres definido em
`compose.yaml` sobe automaticamente ao iniciar a aplicacao:

```
.\mvnw.cmd spring-boot:run
```

Nao e necessario subir o `docker compose` manualmente. Credenciais de
desenvolvimento (banco `chamada`, usuario `chamada`, senha `chamada`) estao
definidas tanto no `compose.yaml` quanto em `application.yml` — servem apenas
para ambiente local.

## Collection de requisicoes (Postman)

`postman/chamada-backend.postman_collection.json` tem todos os endpoints
prontos, organizados em pastas (Auth, Curso, Turma, Aluno, Aula, Presenca,
Avaliacao, Interesse, Dashboard). Importe o arquivo no Postman (ou em qualquer
cliente compativel com o formato de collection do Postman v2.1, como Insomnia
ou Bruno).

Rode as pastas em ordem — a collection encadeia as variaveis automaticamente
via scripts de teste:
1. **Auth > Login** — usa o usuario semente (`admin@hombridade.local`) e guarda
   o token JWT numa variavel de collection (`token`), reaproveitada em todas as
   requisicoes protegidas seguintes.
2. **Curso > Criar Curso** — guarda o `id` criado em `cursoId`.
3. **Turma > Criar Turma** — usa `cursoId`, guarda `turmaId` e o
   `tokenCadastro` gerado em `tokenCadastroTurma`.
4. **Aluno > Cadastro Publico de Aluno** — usa `tokenCadastroTurma` (rota
   publica, sem `Authorization`); vem com um CPF de teste com digitos
   verificadores validos (`529.982.247-25`).
5. **Aula > Criar Aula** — usa `turmaId`, guarda `aulaId` e o `qrToken`
   gerado. Ajuste `janelaAbertura`/`janelaFechamento` no corpo da requisicao
   para o horario do teste antes de rodar.
6. **Presenca > Check-in por QR Code** — rota publica, usa `qrToken` e o
   mesmo CPF de teste do passo 4. **Marcar Presenca Manual** e **Listar
   Presencas da Aula** exigem `Authorization` como as demais rotas internas.
7. **Avaliacao > Registrar Avaliacao** e **Interesse > Registrar Interesse**
   — rotas publicas, exigem que o aluno (mesmo CPF) ja tenha feito check-in na
   aula (passo 6) antes de avaliar ou registrar interesse. **Listar Avaliacoes
   da Aula** e **Listar Interesses da Turma** exigem `Authorization`.
8. **Dashboard > Dashboard da Turma** — exige `Authorization`, agrega
   frequencia/nota/NPS medios da turma e por aula.

`baseUrl` aponta para `http://localhost:8080` por padrao (ajustavel na
variavel de collection).

## Convencao de pacotes

Pacote raiz: `br.com.familychurch.p3`.

O projeto segue **package by feature, com subpacotes por camada dentro de
cada feature**. Para cada entidade/feature de dominio (Curso, Turma, Aluno,
Usuario_Admin, Aula, Presenca, Avaliacao, Interesse), o pacote raiz da feature
contem a entidade JPA (e enums fortemente acoplados a ela) direto na raiz, e
subpacotes `.controller`, `.service`, `.repository`, `.dto` e `.exception`
para as respectivas camadas, por exemplo:

```
br.com.familychurch.p3.curso.Curso
br.com.familychurch.p3.curso.controller.CursoController
br.com.familychurch.p3.curso.service.CursoService / CursoServiceImpl
br.com.familychurch.p3.curso.repository.CursoRepository
br.com.familychurch.p3.curso.dto.CursoRequest / CursoResponse
br.com.familychurch.p3.curso.exception.CursoJaExisteException / CursoNaoEncontradoException
```

O pacote `br.com.familychurch.p3.usuarioadmin` contem **apenas** a entidade
`UsuarioAdmin`, o enum `Papel` e o `UsuarioAdminRepository` — ainda nao tem
controller/service/dto proprios porque a gestao de usuarios administrativos
(criar, editar, trocar papel) e escopo de uma fase futura do roadmap ("Fase 4
— Gerenciar acessos"); quando essa fase chegar, seguira a mesma convencao de
subpacotes por camada.

A autenticacao/JWT fica num pacote `br.com.familychurch.p3.auth` **separado**
de `usuarioadmin`, deliberadamente: hoje o unico jeito de logar e por
email/senha de `UsuarioAdmin`, mas o modelo de dominio tem outros tipos de
usuario (`Aluno`, por exemplo) que podem ganhar login proprio no futuro
(possivelmente por CPF). Mantendo `JwtService`, `JwtProperties` e
`JwtAuthenticationFilter` genericos (nao dependem de `UsuarioAdmin`,
`JwtService.gerarToken` recebe `subject`/`papel`/`nome` como `String`, e o
filtro depende da interface `UserDetailsService`, nao de uma implementacao
concreta) esse pacote fica pronto para ser reaproveitado por um novo fluxo de
login sem precisar ser desacoplado depois. `UsuarioAdminDetails` e
`UsuarioAdminDetailsService` sao a unica parte de `auth` que hoje conhece
`UsuarioAdmin` especificamente (fazem a ponte entre a entidade e o Spring
Security) e ficariam ao lado de um eventual equivalente para `Aluno` no
futuro:

```
br.com.familychurch.p3.auth.JwtProperties
br.com.familychurch.p3.auth.JwtService
br.com.familychurch.p3.auth.JwtAuthenticationFilter
br.com.familychurch.p3.auth.UsuarioAdminDetails
br.com.familychurch.p3.auth.controller.AuthController
br.com.familychurch.p3.auth.service.AuthService / AuthServiceImpl / UsuarioAdminDetailsService
br.com.familychurch.p3.auth.dto.LoginRequest / LoginResponse
br.com.familychurch.p3.auth.exception.CredenciaisInvalidasException
```

Pacotes transversais:

- `config` — configuracao cross-cutting (seguranca, etc.). Contem
  `SecurityConfig`, que registra o `PasswordEncoder` (BCrypt), o filtro JWT e
  a `SecurityFilterChain`: `/api/auth/**` e `/api/publico/**` sao publicos, o
  restante de `/api/**` exige token valido, sessao stateless. `/api/publico/**`
  existe para o cadastro publico de `Aluno` (ver secao "Aluno" abaixo) e
  qualquer outro fluxo futuro que precise ser acessado sem autenticacao.
- `common` / `shared` — excecoes e utilitarios cross-cutting. Serao criados
  quando a primeira necessidade real aparecer nas proximas tarefas.

## Banco de dados em testes

Agora que as 4 features da Fase 1 (`Curso`, `Turma`, `Aluno`, autenticacao)
tem entidades JPA e repositorios Spring Data reais, a estrategia anterior
(desabilitar DataSource/JPA/Flyway no profile `test`) deixou de fazer sentido:
qualquer teste que suba o contexto Spring precisa de um `DataSource` real para
instanciar os repositorios. Ela foi substituida por **Testcontainers com
Postgres real** (mesma imagem `postgres:16` usada em `compose.yaml`).

`src/test/resources/application-test.yml` hoje so garante que
`ddl-auto: validate` e o Flyway continuam ativos em teste (o schema dos testes
de integracao e sempre criado pelas migrations reais, nunca pelo Hibernate).
O `DataSource` em si nao e configurado ali: quem resolve isso e a classe base
`br.com.familychurch.p3.IntegrationTestSupport`
(`src/test/java/br/com/familychurch/p3/IntegrationTestSupport.java`), que
sobe um container Postgres 16 via Testcontainers e o registra automaticamente
como `DataSource` da aplicacao usando `@ServiceConnection`
(`org.springframework.boot.testcontainers.service.connection.ServiceConnection`,
recurso nativo do Spring Boot desde a serie 3.1, confirmado disponivel e
funcional no Spring Boot 4.1.0 usado neste projeto) — sem precisar de
`@DynamicPropertySource` manual para expor host/porta/credenciais do
container.

Motivo de manter Postgres real em vez de H2 em memoria: o mesmo registrado
antes desta mudanca continua valido — o modelo de dados usa tipos e
comportamento especificos do Postgres (e vai usar ainda mais nas proximas
fases), e um banco em memoria mascararia incompatibilidades em vez de
preveni-las.

### Container compartilhado entre as classes de teste

O container Postgres e um campo `static` em `IntegrationTestSupport`,
iniciado uma unica vez em um bloco `static { }` e reutilizado por **todas** as
classes de teste de integracao que estendem essa classe base (`CursoControllerIT`,
`TurmaControllerIT`, `AlunoControllerIT`, `AuthControllerIT`, `AulaControllerIT`,
`PresencaControllerIT`, `AvaliacaoControllerIT`, `InteresseControllerIT`,
`ParticipanteControllerIT`, `P3ApplicationTests`) — pratica comum do
Testcontainers ("singleton container") para nao pagar o custo de subir um
container Postgres novo a cada classe de teste, o que deixaria a suite lenta.
Cada classe ainda limpa os dados que criou (via `deleteAll()` nos
repositorios, respeitando a ordem das foreign keys — hoje `aluno` tambem
depende de `participante`, entao `ParticipanteControllerIT` precisa limpar
`avaliacao`/`interesse`/`presenca`/`aula`/`aluno` antes de `participante`,
mesmo sem criar esses registros diretamente, porque outras classes do mesmo
container podem ter deixado linhas para tras) em um `@BeforeEach`, garantindo
isolamento entre os testes mesmo com o banco compartilhado.

### Testes de integracao (`*IT`) rodando junto com `mvn test`

Por padrao, o Maven Surefire (fase `test`) so roda classes terminadas em
`*Test`/`*Tests`; o sufixo `*IT` e a convencao do Failsafe (fase
`integration-test`), plugin que este projeto nao usa. Para atender ao
requisito de rodar a suite inteira com um unico `.\mvnw.cmd test` (sem exigir
`docker compose up -d` manual nem uma fase Maven separada), o
`maven-surefire-plugin` foi configurado explicitamente em `pom.xml` para
incluir tambem `**/*IT.java`, mantendo a nomenclatura `*IT` (mais idiomatica
para sinalizar "teste de integracao, sobe contexto Spring completo") mas sem
depender do Failsafe.

### Autenticacao real nos testes de integracao

Os testes que exercitam endpoints protegidos por JWT nao mockam o Spring
Security: fazem login de verdade contra `POST /api/auth/login` usando o
usuario admin semente (`admin@hombridade.local` / `hombridade2026`, inserido
pela migration `V4__seed_usuario_admin.sql`), obtendo um token JWT real para
enviar no header `Authorization` das chamadas seguintes. O metodo utilitario
`IntegrationTestSupport.obterTokenAdmin()` centraliza esse fluxo. Isso testa a
integracao completa (login -> geracao de token -> filtro JWT -> autorizacao),
em vez de assumir que a autenticacao funciona.

## Autenticacao (Spring Security + JWT)

A API usa autenticacao stateless via JWT. O endpoint `POST /api/auth/login`
recebe `email` e `senha` e devolve um token JWT (alem do nome e do papel do
usuario, para o front-end decidir o que exibir). Esse token deve ser enviado
nas demais chamadas em `/api/**` no header:

```
Authorization: Bearer <token>
```

`/api/auth/**` e publico; todo o restante de `/api/**` exige token valido.

### Configuracao do JWT

`segredo` e tempo de expiracao ficam em `application.yml`, sob `app.jwt`, com
valores de desenvolvimento e possibilidade de sobrescrita por variavel de
ambiente:

```yaml
app:
  jwt:
    secret: ${JWT_SECRET:chave-secreta-de-desenvolvimento-nao-usar-em-producao-1234567890}
    expiracao-minutos: ${JWT_EXPIRACAO_MINUTOS:60}
```

Em producao, `JWT_SECRET` (e opcionalmente `JWT_EXPIRACAO_MINUTOS`) devem vir
de variavel de ambiente da plataforma de deploy, nunca do valor default deste
arquivo — conforme ja registrado na secao de infraestrutura do documento
tecnico (`../parte-tecnica-app-chamada.md`, secao 11, "Segredos").

### Usuario admin semente (apenas ambiente local/dev)

Esta tarefa nao inclui endpoint de cadastro de administrador — gestao de
acessos entre lideranças ("convidar" Professor/Admin/Visualizador) e escopo da
Fase 4 do roadmap ("Gerenciar acessos"). Para permitir testar o login
localmente, a migration `V4__seed_usuario_admin.sql` insere um usuario ADMIN
fixo:

| Campo | Valor |
|---|---|
| email | `admin@hombridade.local` |
| senha | `hombridade2026` |
| papel | `ADMIN` |

O hash BCrypt salvo no banco corresponde a essa senha. **Use essas
credenciais apenas em ambiente local/dev** — a gestao "de verdade" de usuarios
administrativos (criacao, edicao, troca de papel) sera implementada em uma
fase futura.

### Exemplo de uso local

```
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "admin@hombridade.local",
  "senha": "hombridade2026"
}
```

A resposta traz o `token` a ser usado no header `Authorization: Bearer` para
acessar, por exemplo, `GET /api/cursos`.

## Turma

`Turma` pertence a um `Curso` (`@ManyToOne` obrigatorio) e representa uma
turma concreta (ex: "Campus 85 - Turma 13"). Segue a mesma estrutura de
pacotes de `Curso` (`br.com.familychurch.p3.turma` com subpacotes
`.controller`, `.service`, `.repository`, `.dto`, `.exception`).

### Link unico da chamada

Toda `Turma` recebe, na criacao, um `tokenCadastro` unico usado para montar o
link publico que a lideranca compartilha com os alunos (ex:
`http://localhost:3000/chamada/<token>` em dev). O nome do campo
(`tokenCadastro`/`linkCadastro`) e uma sobra do desenho original (cadastro de
`Aluno` por turma) - a partir da Fase A da chamada, esse mesmo link passou a
apontar para o fluxo de chamada (`/chamada/[token]` no frontend), nao mais
para um cadastro por turma.

O token e uma string curta de 8 caracteres alfanumericos (alfabeto reduzido,
sem caracteres ambiguos como `0`/`O` e `1`/`I`/`L`), gerada com
`SecureRandom` em `TurmaServiceImpl`, com nova tentativa em caso de colisao
(`TurmaRepository.existsByTokenCadastro`). Um UUID completo (como usado para
o `id` das entidades) foi descartado para esse campo especificamente: o token
e digitado/lido por humanos e compartilhado via WhatsApp, entao um valor
longo prejudicaria a usabilidade do link sem trazer beneficio de seguranca
proporcional (o token nao e segredo criptografico, apenas precisa ser dificil
de adivinhar e unico).

`TurmaResponse` expõe tanto o `tokenCadastro` isolado quanto o `linkCadastro`
ja montado (token + base URL configurada em `app.chamada.base-url`,
sobrescrevivel por `CHAMADA_BASE_URL`, default `http://localhost:3000/chamada`
para bater com a porta padrao do `p3web` em dev), para o front-end escolher o
formato mais conveniente sem precisar remontar a URL.

### Modulo atual

`moduloAtual` e um campo `String` livre, e nao um enum. A especificacao cita
os modulos "Provisao/Protecao/Plataforma" da Escola de Hombridade, mas esses
nomes sao especificos desse curso — outros cursos que a instituicao venha a
cadastrar no futuro (a entidade `Curso` existe justamente para permitir
reuso da mesma base por outros programas) teriam modulos com nomes
diferentes. Fixar um enum aqui seria modelar a excecao de hoje como se fosse
regra geral do dominio.

### Reuso de excecao entre `curso` e `turma`

A criacao de uma `Turma` valida que o `Curso` informado existe e esta ativo.
Para "curso nao existe", `TurmaServiceImpl` reaproveita
`br.com.familychurch.p3.curso.exception.CursoNaoEncontradoException` (ja
publica) em vez de duplicar uma excecao equivalente em `turma.exception`:
`Turma` ja depende de `Curso` na propria entidade (`@ManyToOne`) e no
`CursoRepository`, entao depender tambem da excecao de dominio de `Curso` nao
introduz acoplamento novo, apenas reflete uma dependencia que ja existe. Para
"curso existe mas esta inativo" — uma regra que pertence a `Turma`, nao a
`Curso` — foi criada `CursoInativoException` em `turma.exception`, ja que
essa validacao nao existe do lado de `curso`.

`TurmaNaoEncontradaException` (tambem em `turma.exception`) segue o mesmo
padrao de `CursoNaoEncontradoException`: usada quando a propria `Turma`
buscada por id nao existe.

## Participante e Historico (cadastro base multi-ministerio)

`Participante` (`br.com.familychurch.p3.participante`) e a base de cadastro
**unica por CPF**, compartilhada por todos os ministerios da Family — nao
presa a uma turma/curso especifico como `Aluno`. Guarda identidade, endereco
(geocodificado) e os dados de "vida na Family"/comunhao. E o equivalente,
neste repositorio, ao que o relatorio de arquitetura da Plataforma P3 chamava
de `Pessoa`: a entidade central da qual `Aluno` (ver secao seguinte) passa a
ser so um registro de matricula.

```
POST /api/publico/participantes    (permitAll, cadastro publico via link)
GET  /api/participantes/{cpf}      (exige JWT — lideranca consulta o perfil completo)
```

Segue a mesma estrutura de pacotes das demais features
(`.controller`, `.service`, `.repository`, `.dto`, `.exception`).

### Apenas nome/cpf sao obrigatorios no banco

O cadastro de `Aluno` (ver abaixo) tambem cria/reaproveita um `Participante`,
mas so coleta nome/cpf/celular — nao o formulario completo (data de
nascimento, endereco, vida na Family etc). Por isso, no banco, **so `nome` e
`cpf` sao `NOT NULL`**; todos os demais campos ficam nullable. A
obrigatoriedade descrita no documento de produto do cadastro base e aplicada
via Bean Validation em `ParticipanteCadastroRequest`, camada de aplicacao —
nao como constraint de schema.

### Endereco embutido (`@Embeddable`) e geocoding atras de interface

`Endereco` e um `@Embeddable` dentro da propria tabela `participante` (relacao
1:1 real, sem necessidade de join). `latitude`/`longitude` nunca sao aceitos
do cliente — sao calculados no servidor a partir do endereco completo, via
`GeocodingService` (`participante.service`). Hoje so existe
`GeocodingServiceStub`, que sempre retorna vazio (cadastro salva normalmente,
sem coordenadas) porque ainda nao ha chave do Google Geocoding disponivel;
trocar para uma implementacao real (HTTP via `RestClient`, ja disponivel via
`spring-boot-starter-webmvc`) e questao de adicionar um novo `@Service` que
implemente a mesma interface, sem tocar em `ParticipanteServiceImpl`. O
preenchimento do endereco a partir do CEP (ViaCEP) e responsabilidade do
formulario (fora deste repositorio) — o backend so recebe os campos ja
resolvidos.

**Cuidado ao ler `participante.getEndereco()` de uma entidade carregada do
banco:** o Hibernate materializa um `@Embedded` como `null` (nao como uma
instancia com todos os campos `null`) quando **todas** as colunas mapeadas
estao `null` — exatamente o caso de um `Participante` minimo criado pelo
fluxo de `Aluno`. `ParticipanteServiceImpl` sempre atribui uma nova instancia
de `Endereco` em vez de reaproveitar a existente, e `ParticipanteResponse`
trata esse campo como opcional na resposta.

### Upsert por CPF, nunca duplica

`ParticipanteServiceImpl.cadastrar` busca por CPF primeiro: se ja existe,
**atualiza** todos os campos do perfil com os valores enviados (reenviar o
mesmo CPF corrige/completa o cadastro, nunca cria um segundo registro).

### Historico de cursos/eventos (`br.com.familychurch.p3.historico`)

As opcoes de multipla escolha do formulario ("Cursos realizados", "Eventos
que participou", + os campos livres "Outros") **nao viram colunas** em
`Participante`. Cada item marcado gera uma linha em `HistoricoItem`, ligada
ao participante — assim, um curso novo (outra turma, outro ministerio) entra
sem alterar o schema. Ao reenviar o mesmo cadastro, `HistoricoItemRepository`
evita duplicar a mesma linha (`participante_id + item + tipo + origem`).

A leitura pivotada por curso (visao "Nao iniciado / Em andamento /
Concluido" por coluna, que o lider ve na consulta) e o gatilho automatico de
conclusao via chamada de presenca sao explicitamente a proxima entrega
("chamada") no documento de produto — nao implementados aqui.
`GET /api/participantes/{cpf}` ja devolve a lista crua de `HistoricoItem` do
participante, suficiente para "o lider ver o homem inteiro" nesta fase.

### Validacoes condicionais no service, nao em anotacao

`quantidadeFilhos` (exigido se `temFilhos=true`) e `quaisGcs` (exigido se
`participaGc=true`) sao verificados imperativamente em
`ParticipanteServiceImpl`, no mesmo espirito do
`ConsentimentoLgpdObrigatorioException` ja usado em `Aluno` — Bean
Validation puro nao expressa bem regras condicionais entre dois campos do
mesmo request.

### Cursos e eventos sao opcionais, apesar do mapa marcar como obrigatorio

O mapa de execucao marca "Cursos realizados na Family" e "Eventos que
participou na Family" como obrigatorios, mas isso nao reflete a realidade:
quem esta se cadastrando pela primeira vez pode legitimamente nao ter feito
nenhum curso nem participado de nenhum evento ainda. Por decisao explicita
do usuario (ver historico do projeto), `cursosRealizados`/`cursosOutros` e
`eventosParticipou`/`eventosOutros` sao tratados como totalmente opcionais
no `ParticipanteServiceImpl` — nenhuma validacao exige selecao. `Set<String>`
vazio ou `null` e aceito normalmente; `registrarHistorico` so grava o que
foi de fato informado.

### Consentimento LGPD com prova de aceite

Assim como em `Aluno`, `Participante.consentimentoLgpd` +
`consentimentoLgpdTimestamp` (migration `V14`) guardam a prova do aceite —
`ParticipanteServiceImpl` grava `true` + `LocalDateTime.now()` no momento do
cadastro/atualizacao, depois de `validarConsentimentoLgpd` confirmar que
`aceiteLgpd=true` veio no request (400 caso contrario). Antes da `V14`, o
request exigia o aceite mas nao guardava nenhuma prova dele em
`Participante` — falha que so nao existia em `Aluno`.

### Fora de escopo desta entrega

Busca/roteamento por regiao (bairro) ate o GC ou curso mais proximo por
distancia exigiria modelar `GC` como entidade propria, o que nao existe
ainda — o documento de produto trata isso como parte de uma entrega futura.
Esta entrega cobre o cadastro, o modelo de dados completo e a consulta por
CPF.

## Aluno

`Aluno` segue a mesma estrutura de pacotes de `Curso`/`Turma`
(`br.com.familychurch.p3.aluno` com subpacotes `.controller`, `.service`,
`.repository`, `.dto`, `.exception`). O validador de CPF (`@Cpf`/`CpfValidator`)
foi movido para `br.com.familychurch.p3.common.validation` quando `Participante`
passou a precisar dele tambem — `common` e o pacote documentado desde a Fase 1
para utilitarios cross-cutting "quando a primeira necessidade real aparecer".

`Aluno` **e uma matricula na Escola de Hombridade, nao a identidade da
pessoa**: `nome`/`cpf`/`celular` vivem em `Participante` (ver secao acima),
referenciado por `Aluno.participante` (`@ManyToOne`, FK `participante_id`,
`unique`). `AlunoResponse`/`AlunoCadastroResponse` continuam expondo
`nome`/`cpf`/`celular` no mesmo formato JSON de antes — apenas buscam os
valores via `aluno.getParticipante()` por baixo, sem quebrar quem ja consome
esses endpoints.

### Cadastro publico via link da turma

O aluno se cadastra **uma unica vez**, atraves de um link publico montado a
partir do `tokenCadastro` da `Turma` (o mesmo token usado no `linkCadastro` de
`TurmaResponse`), sem precisar instalar o app nem se autenticar:

```
POST /api/publico/turmas/{token}/alunos
```

Esse endpoint resolve a `Turma` pelo `tokenCadastro` recebido na URL — nunca
pelo id interno da turma, ja que quem acessa o link publico so conhece o
token. Para isso, o prefixo `/api/publico/**` foi liberado em
`SecurityConfig` (`permitAll`, adicionado antes da regra geral de `/api/**`
autenticado), sem alterar o restante da `SecurityFilterChain`.

`AlunoServiceImpl.cadastrar` faz *find-or-create* em `Participante` por CPF
antes de criar a matricula: se ja existe um `Participante` com esse CPF (por
exemplo, criado antes pelo cadastro base completo), **reaproveita sem
sobrescrever** nome/celular — evita que um cadastro simples de turma
degrade um perfil ja mais completo. Se nao existe, cria um `Participante`
minimo (nome/cpf/celular, resto vazio).

A listagem de alunos de uma turma, por outro lado, e uso interno da
lideranca e segue o padrao de autenticacao ja existente:

```
GET /api/turmas/{turmaId}/alunos   (exige JWT, como o restante de /api/**)
```

Os dois endpoints ficam no mesmo `AlunoController`, mesmo com prefixos de URL
diferentes, porque pertencem a mesma feature de dominio (`aluno`).

### Matricula unica por participante

O modelo de dados tem `Aluno.turma_id` como FK simples para uma unica turma
(um aluno pertence a uma turma so). Por isso a unicidade e **global por
participante** (`uk_aluno_participante` na tabela, equivalente ao antigo
`uk_aluno_cpf` — mesmo comportamento, so mudou onde a unicidade e verificada:
`AlunoRepository.existsByParticipanteCpf` em vez de `existsByCpf`), nao
apenas dentro da turma — o mesmo CPF nao pode se matricular em nenhuma turma
se ja existe em outra.

### CPF guardado em texto puro (em `Participante`)

O documento tecnico (`../parte-tecnica-app-chamada.md`, secao 3, LGPD) sugere
"considerar" guardar o CPF com hash ou criptografia. Essa tarefa optou por
manter o CPF (hoje em `Participante.cpf`) em **texto puro** no banco, decisao
deliberada: a Fase 2 do
roadmap (identificacao/check-in por CPF e busca manual do professor por CPF
na marcacao de presenca) depende de buscar e exibir o CPF diretamente. Um
hash impediria a busca por igualdade sem guardar o CPF em paralelo em outro
formato, e criptografia reversivel exigiria decifrar em toda consulta sem
ganho de seguranca real (a chave ficaria acessivel a mesma aplicacao). A
ressalva do documento tecnico permanece valida como trabalho futuro:
criptografia em repouso a **nivel de infraestrutura/banco** (ex: disco
criptografado, `pgcrypto` com criptografia transparente, ou um servico de
banco gerenciado com encryption at rest) pode ser avaliada depois, sem
impedir a busca por igualdade que a Fase 2 exige — diferente de criptografar
a coluna do lado da aplicacao.

### Validacao de CPF

CPF nao e validado so por tamanho/regex. Existe uma anotacao Bean Validation
customizada, `@Cpf` (`br.com.familychurch.p3.common.validation.Cpf` +
`CpfValidator` — pacote compartilhado, ver secao "Participante" acima), que
implementa o algoritmo padrao de digito verificador do CPF brasileiro (modulo
11 sobre os 9 primeiros digitos, calculado duas vezes) e rejeita tambem os
classicos CPFs com todos os digitos iguais (ex: `111.111.111-11`), que passam
no calculo do modulo 11 mas sao invalidos na pratica.
`AlunoCadastroRequest.cpf`/`ParticipanteCadastroRequest.cpf` usam `@Cpf`
diretamente, entao um CPF invalido e rejeitado automaticamente pelo mecanismo
padrao de `@Valid` (400), sem logica extra no controller. O CPF e persistido
apenas com digitos (a mascara `xxx.xxx.xxx-xx`, se enviada, e normalizada
antes de salvar).

### Consentimento LGPD

`AlunoCadastroRequest.aceiteLgpd` precisa vir `true`; se vier `false` ou
ausente (o tipo primitivo `boolean` default para `false`), o cadastro e
rejeitado com `ConsentimentoLgpdObrigatorioException` (400) — nunca passa
silenciosamente. O timestamp do consentimento (`consentimentoLgpdTimestamp`)
e gerado no servidor no momento do processamento, assim como `dataCadastro`;
nenhum dos dois e aceito do cliente, exatamente como pede a especificacao
("prova" do momento do aceite).

### Regras de negocio do cadastro (`AlunoServiceImpl`)

Na ordem aplicada: (1) resolve a `Turma` pelo `tokenCadastro`, lancando
`aluno.exception.TurmaNaoEncontradaException` se nao existir — uma variante
local que recebe o token (`String`), diferente de
`turma.exception.TurmaNaoEncontradaException` (que recebe o `id` da turma),
ja que aqui a busca publica so conhece o token; (2) se a turma estiver
`ENCERRADA`, rejeita com `TurmaEncerradaException` (409) — regra de negocio
do cadastro de aluno, por isso vive em `aluno.exception` mesmo `Turma` sendo
de outro pacote; (3) verifica duplicidade de CPF, `AlunoJaCadastradoException`
(409); (4) valida o consentimento LGPD. A validacao de formato do CPF
acontece antes, via `@Valid` no controller.

### Migrations (Flyway)

Migrations ficam em `src/main/resources/db/migration`, seguindo a convencao
`Vx__descricao.sql`. `V1__init.sql` e um baseline minimo que apenas valida que
o Flyway esta corretamente configurado; as migrations de dominio sao
adicionadas nas proximas tarefas, uma por entidade/feature (`V2__create_curso_table.sql`
e a primeira delas, seguida por `V3__create_usuario_admin_table.sql` e
`V4__seed_usuario_admin.sql` para autenticacao, `V5__create_turma_table.sql`
para a tabela de `Turma`, com FK para `curso` e constraint de unicidade em
`token_cadastro`, e `V6__create_aluno_table.sql` para a tabela de `Aluno`, com
FK para `turma` e constraint de unicidade global em `cpf`).
`V11__create_participante_table.sql` e `V12__create_historico_item_table.sql`
criam as tabelas do cadastro base (`participante`, `participante_gc`,
`historico_item`; ver secao "Participante e Historico" acima).
`V13__extrai_participante_de_aluno.sql` e a migration de dados que extrai
`nome`/`cpf`/`celular` de `aluno` para `participante` (via
`INSERT ... SELECT` + `UPDATE`, funciona com 0 ou N linhas existentes),
adiciona `aluno.participante_id` e remove as colunas antigas — o refactor que
transforma `Aluno` em matricula. `V14__add_consentimento_lgpd_participante.sql`
adiciona `consentimento_lgpd`/`consentimento_lgpd_timestamp` em `participante`
(ver secao "Consentimento LGPD com prova de aceite" acima).

`spring.jpa.hibernate.ddl-auto` esta como `validate`: o schema e sempre gerenciado
pelo Flyway, nunca gerado automaticamente pelo Hibernate.

## Aula

`Aula` (`br.com.familychurch.p3.aula`, subpacotes `.controller`, `.service`,
`.repository`, `.dto`, `.exception`) representa uma sessao concreta de uma
`Turma` (ex: "Aula 1 - Provisao"), com uma janela de tempo
(`janelaAbertura`/`janelaFechamento`) que delimita quando o check-in (QR code
ou marcacao manual) e aceito.

```
POST /api/turmas/{turmaId}/aulas   (exige JWT)
GET  /api/turmas/{turmaId}/aulas   (exige JWT)
GET  /api/aulas/{id}               (exige JWT)
GET  /api/aulas/{id}/qrcode        (exige JWT, retorna image/png)
```

A criacao de aula reaproveita a mesma validacao de `Turma` de `AlunoServiceImpl`
(turma existe e nao esta `ENCERRADA`), com copias locais de
`TurmaNaoEncontradaException`/`TurmaEncerradaException` em `aula.exception`,
seguindo o mesmo padrao ja estabelecido de nao acoplar excecoes de dominio
entre features.

### QR code dinamico

Cada `Aula` recebe, na criacao, um `qrToken` unico (mesmo esquema de
`SecureRandom` + retry usado no `tokenCadastro` de `Turma`, alfabeto reduzido
sem caracteres ambiguos). O QR code exibido em sala (projetor/TV) so codifica
esse token — quem escaneia (ou digita o CPF na tela do check-in) manda o
`qrToken` de volta ao backend, nunca o id interno da aula.

`GET /api/aulas/{id}/qrcode` gera a imagem PNG do QR code sob demanda (via
ZXing `QRCodeWriter`, convertida para `BufferedImage` e codificada com
`ImageIO`, sem depender do modulo `zxing:javase`) — a imagem nao e
persistida, e recalculada a cada requisicao a partir do `qrToken` salvo.

## Presenca (check-in, WebSocket, marcacao manual)

`Presenca` (`br.com.familychurch.p3.presenca`) registra o check-in de um
`Aluno` numa `Aula`, por um dos dois metodos (`MetodoPresenca`: `QRCODE` ou
`MANUAL`). Um aluno so pode ter uma presenca por aula
(`uk_presenca_aluno_aula`), garantindo que reenviar o mesmo check-in nunca
duplica o registro.

### Identificacao e check-in por CPF

```
POST /api/publico/checkin
```

Rota publica (`/api/publico/**`, sem JWT), pensada para a tela de check-in do
aluno: recebe `qrToken` (lido do QR code exibido em sala) e `cpf` (que o
aluno ja sabe de cor, sem precisar de login). Regras aplicadas em
`PresencaServiceImpl.checkin`, na ordem: (1) resolve a `Aula` pelo `qrToken`,
404 (`QrCodeInvalidoException`) se nao existir; (2) valida que o momento
atual esta dentro de `janelaAbertura`/`janelaFechamento`, 400
(`JanelaAulaFechadaException`) caso contrario; (3) resolve o `Aluno` pelo CPF
(normalizado, so digitos), 404 (`AlunoNaoEncontradoException`) se nao
cadastrado; (4) valida que o aluno pertence a mesma `Turma` da aula, 400
(`AlunoNaoPertenceATurmaException`) caso contrario; (5) valida que ainda nao
existe presenca desse aluno nessa aula, 409 (`PresencaDuplicadaException`).

### Marcacao manual (fallback)

```
POST /api/aulas/{aulaId}/presencas/manual   (exige JWT)
```

Usada pelo professor/admin para quem esqueceu o celular ou ainda nao tem o
app (fase de transicao do Forms). Recebe o `cpf` do aluno (mesma busca por
CPF do check-in — a especificacao pede "busca aluno por nome ou CPF"; CPF foi
escolhido por ja ser a chave de busca unica usada em todo o resto da Fase 2).
`validadoPor` e preenchido com o `UsuarioAdmin` autenticado (extraido do
`UsuarioAdminDetails` via `@AuthenticationPrincipal`, sem endpoint dedicado
para isso), nunca aceito do cliente.

### Lista de presenca ao vivo (WebSocket)

```
GET  /api/aulas/{aulaId}/presencas   (exige JWT — snapshot inicial da lista)
STOMP topic: /topic/aulas/{aulaId}/presencas
STOMP endpoint (SockJS): /ws
```

A tela do professor carrega o estado inicial via `GET .../presencas` e depois
assina o topico STOMP `/topic/aulas/{aulaId}/presencas` para receber cada
novo check-in (QR code ou manual) em tempo real, sem polling. Configurado em
`config.WebSocketConfig` (`@EnableWebSocketMessageBroker`, broker simples em
memoria em `/topic`, endpoint SockJS em `/ws`); `PresencaServiceImpl` publica
o mesmo `PresencaResponse` da API REST via `SimpMessagingTemplate` logo apos
salvar cada presenca (check-in ou manual), então quem esta ouvindo o topico
ve o mesmo formato de dado que a API retorna. `/ws/**` foi liberado em
`SecurityConfig` (`permitAll`) porque o handshake HTTP inicial do SockJS nao
carrega o header `Authorization` da mesma forma que uma chamada REST; a
avaliacao de quem pode ver a lista ao vivo de uma aula especifica fica para
uma fase futura caso vire requisito (hoje equivale ao mesmo nivel de exposicao
de quem consegue ver o QR code projetado em sala).

### Migrations (Aula/Presenca)

`V7__create_aula_table.sql` cria `aula` (FK para `turma`, `uk_aula_qr_token`
unico). `V8__create_presenca_table.sql` cria `presenca` (FK para `aluno`,
`aula` e `usuario_admin` via `validado_por`, nullable; `uk_presenca_aluno_aula`
garante um check-in por aluno por aula).

## Avaliacao e Interesse (pos check-in)

`Avaliacao` (`br.com.familychurch.p3.avaliacao`) e `Interesse`
(`br.com.familychurch.p3.interesse`) sao features irmas, mas com pacotes
proprios (entidade, `.controller`, `.service`, `.repository`, `.dto`,
`.exception` cada uma), seguindo a mesma convencao das demais features — nao
foi criado um pacote generico compartilhado entre as duas, mesmo com a regra
de negocio de pre-requisito (check-in) sendo identica nas duas, com excecoes
de dominio proprias e duplicadas em cada pacote (`CheckinNaoRealizadoException`
em `avaliacao.exception` e outra em `interesse.exception`), no mesmo espirito
de `presenca.exception`/`aula.exception` nao compartilharem excecoes entre si.

```
POST /api/publico/aulas/{aulaId}/avaliacoes   (publico)
GET  /api/aulas/{aulaId}/avaliacoes           (exige JWT)

POST /api/publico/aulas/{aulaId}/interesses   (publico)
GET  /api/turmas/{turmaId}/interesses         (exige JWT)
```

Ambos os endpoints publicos identificam o aluno pelo `cpf` (mesmo padrao de
`PresencaServiceImpl.buscarAlunoDaTurma`, reimplementado localmente em cada
servico) e validam, nesta ordem: (1) a `Aula` existe; (2) o aluno pertence a
turma da aula; (3) o aluno ja tem uma `Presenca` registrada para essa aula
(`PresencaRepository.existsByAlunoIdAndAulaId`), 404
(`CheckinNaoRealizadoException`) caso contrario — a tela de avaliacao/interesse
so aparece para quem ja fez check-in; (4) o aluno ainda nao avaliou/registrou
interesse nessa aula, 409 (`AvaliacaoDuplicadaException`/
`InteresseDuplicadoException`) caso contrario, com constraint unica
`(aluno_id, aula_id)` no banco reforcando a regra.

`Avaliacao.nota` e `Avaliacao.nps` sao validados tanto via Bean Validation
(`@Min(0) @Max(10)` no `AvaliacaoRequest`) quanto via `check` constraint na
migration, para o banco nunca aceitar um valor invalido mesmo se outra rota de
escrita for adicionada no futuro. `Avaliacao.didatica` e o enum
`DidaticaAvaliacao` (`OTIMA`, `BOA`, `REGULAR`, `RUIM`). `Interesse` tem tres
flags booleanas (`gcHomens`, `proximoAwake`, `discipulado`), sem validacao
adicional alem de exigirem um valor (mesmo que `false`).

A listagem de interesses e por **turma**, nao por aula (`GET
/api/turmas/{turmaId}/interesses`, via `InteresseRepository.findByAulaTurmaId`,
que o Spring Data deriva automaticamente pela associacao `interesse.aula.turma`),
porque a tela de "gestao de interesse" do admin precisa ver quem demonstrou
interesse em GC/Awake/Discipulado ao longo de toda a turma, nao aula a aula —
diferente da listagem de avaliacoes, que e por aula (uso do professor
avaliando uma aula especifica logo apos ela acontecer).

### Migrations (Avaliacao/Interesse)

`V9__create_avaliacao_table.sql` cria `avaliacao` (FK para `aluno` e `aula`,
`uk_avaliacao_aluno_aula` unico, `check` constraints em `nota` e `nps`).
`V10__create_interesse_table.sql` cria `interesse` (FK para `aluno` e `aula`,
`uk_interesse_aluno_aula` unico, tres colunas boolean com `default false`).

## Dashboard da turma

`GET /api/turmas/{turmaId}/dashboard` (exige JWT) fica dentro da propria
feature `turma` (`TurmaController`/`TurmaServiceImpl`) em vez de um pacote
novo, por ser uma visao agregada sobre dados que ja pertencem a `Turma` — sem
entidade ou tabela propria. `TurmaServiceImpl` passou a injetar tambem
`AulaRepository`, `PresencaRepository`, `AlunoRepository` e
`AvaliacaoRepository` so para esse calculo.

`TurmaDashboardResponse` traz `totalAlunos` (matriculados na turma agora,
`AlunoRepository.countByTurmaId`), a lista `porAula` (uma `AulaDashboardItem`
por `Aula` da turma, ordenada por `numero`, com `totalPresencas`
(`PresencaRepository.countByAulaId`), `percentualPresenca` (`totalPresencas /
totalAlunos * 100`, `0` se a turma nao tiver alunos ainda) e `notaMedia`/
`npsMedia` daquela aula especifica, calculadas com `avg()` via `@Query` em
`AvaliacaoRepository` — `null` enquanto nenhuma avaliacao daquela aula existir,
para o front-end distinguir "sem dado" de "nota zero"), e os agregados da
turma inteira: `frequenciaMedia` (media aritmetica simples do
`percentualPresenca` de cada aula, `0` se a turma nao tem aulas ainda) e
`notaMedia`/`npsMedia` da turma (media das medias por aula que nao sao nulas,
tambem `null` se nenhuma aula tem avaliacao ainda). E um calculo deliberamente
simples (media de medias, nao ponderada por numero de avaliacoes por aula),
suficiente para o MVP do dashboard basico do roadmap; um calculo estatistico
mais rigoroso fica para uma fase futura caso vire requisito.

## Escolha de chave primaria

As entidades de dominio usam `UUID` como chave primaria (gerado pela aplicacao
via Hibernate `@UuidGenerator`, nao pelo banco). Motivo: varias entidades do
modelo (`Turma`, e por consequencia links de cadastro/check-in expostos
publicamente) serao referenciadas por identificadores que acabam visiveis em
URLs fora do app — UUID evita expor contagem/sequencia de registros e evita
colisao ao combinar dados de ambientes diferentes (staging/producao) no
futuro. O padrao estabelecido em `Curso` deve ser replicado pelas proximas
entidades para manter consistencia.
