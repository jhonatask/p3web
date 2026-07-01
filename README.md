# chamada-backend

API de controle de presenca (chamada) da Escola de Hombridade.

A documentacao de arquitetura e modelo de dados completo esta em
`../parte-tecnica-app-chamada.md` (um nivel acima deste diretorio). Este README
cobre apenas o setup e as convencoes deste repositorio backend.

## Stack

- Java 21
- Spring Boot 4.1.0 (Web MVC, Data JPA, Security, Validation)
- PostgreSQL 16
- Flyway (migrations de schema)

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
  a `SecurityFilterChain`: `/api/auth/**` e publico, o restante de `/api/**`
  exige token valido, sessao stateless.
- `common` / `shared` — excecoes e utilitarios cross-cutting. Serao criados
  quando a primeira necessidade real aparecer nas proximas tarefas.

## Banco de dados em testes

O profile `test` (`src/test/resources/application-test.yml`) desabilita as
autoconfiguracoes de datasource, JPA e Flyway, permitindo que o contexto Spring
suba em testes sem depender do Postgres do `compose.yaml` estar no ar.

Essa e uma decisao deliberada: como o modelo de dados vai usar tipos e
funcionalidades especificas do Postgres, um banco em memoria (H2) mascararia
incompatibilidades em vez de preveni-las. Quando as proximas tarefas
adicionarem entidades JPA e repositorios, a estrategia de teste de integracao
com banco real (via Testcontainers com Postgres) deve ser decidida e
implementada naquele momento.

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

### Link unico de cadastro

Toda `Turma` recebe, na criacao, um `tokenCadastro` unico usado para montar o
link publico que a lideranca compartilha com os alunos (ex:
`app.com/cadastro/<token>`, que sera consumido pelo futuro endpoint de
cadastro publico de `Aluno`).

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
ja montado (token + base URL configurada em `app.cadastro.base-url`, sobre
escrevivel por `CADASTRO_BASE_URL`), para o front-end escolher o formato mais
conveniente sem precisar remontar a URL.

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

## Migrations (Flyway)

Migrations ficam em `src/main/resources/db/migration`, seguindo a convencao
`Vx__descricao.sql`. `V1__init.sql` e um baseline minimo que apenas valida que
o Flyway esta corretamente configurado; as migrations de dominio sao
adicionadas nas proximas tarefas, uma por entidade/feature (`V2__create_curso_table.sql`
e a primeira delas, seguida por `V3__create_usuario_admin_table.sql` e
`V4__seed_usuario_admin.sql` para autenticacao, e `V5__create_turma_table.sql`
para a tabela de `Turma`, com FK para `curso` e constraint de unicidade em
`token_cadastro`).

`spring.jpa.hibernate.ddl-auto` esta como `validate`: o schema e sempre gerenciado
pelo Flyway, nunca gerado automaticamente pelo Hibernate.

## Escolha de chave primaria

As entidades de dominio usam `UUID` como chave primaria (gerado pela aplicacao
via Hibernate `@UuidGenerator`, nao pelo banco). Motivo: varias entidades do
modelo (`Turma`, e por consequencia links de cadastro/check-in expostos
publicamente) serao referenciadas por identificadores que acabam visiveis em
URLs fora do app — UUID evita expor contagem/sequencia de registros e evita
colisao ao combinar dados de ambientes diferentes (staging/producao) no
futuro. O padrao estabelecido em `Curso` deve ser replicado pelas proximas
entidades para manter consistencia.
