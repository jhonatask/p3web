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

A partir da infraestrutura configurada nesta tarefa, o projeto segue
**package by feature**: cada entidade/feature de dominio (Curso, Turma, Aluno,
Usuario_Admin, Aula, Presenca, Avaliacao, Interesse) tera seu proprio
subpacote com `controller`, `service`, `repository` e `dto` quando as tarefas
seguintes os criarem, por exemplo `br.com.familychurch.p3.turma`.

O pacote `br.com.familychurch.p3.auth` contem a entidade `UsuarioAdmin` e toda
a infraestrutura de autenticacao/JWT (service de login, `JwtService`, filtro,
`UserDetailsService`), ja que sao fortemente acoplados entre si.

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

## Migrations (Flyway)

Migrations ficam em `src/main/resources/db/migration`, seguindo a convencao
`Vx__descricao.sql`. `V1__init.sql` e um baseline minimo que apenas valida que
o Flyway esta corretamente configurado; as migrations de dominio sao
adicionadas nas proximas tarefas, uma por entidade/feature (`V2__create_curso_table.sql`
e a primeira delas, seguida por `V3__create_usuario_admin_table.sql` e
`V4__seed_usuario_admin.sql` para autenticacao).

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
