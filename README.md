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

Pacotes transversais:

- `config` — configuracao cross-cutting (seguranca, etc.). Ja contem
  `SecurityConfig`, uma `SecurityFilterChain` minima que libera todas as
  requisicoes (`permitAll`) e serve de ponto de extensao para a tarefa futura
  de autenticacao JWT.
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

## Migrations (Flyway)

Migrations ficam em `src/main/resources/db/migration`, seguindo a convencao
`Vx__descricao.sql`. `V1__init.sql` e um baseline minimo que apenas valida que
o Flyway esta corretamente configurado; as migrations de dominio serao
adicionadas nas proximas tarefas.

`spring.jpa.hibernate.ddl-auto` esta como `validate`: o schema e sempre gerenciado
pelo Flyway, nunca gerado automaticamente pelo Hibernate.
