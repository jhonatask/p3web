# P3 — Escola da Hombridade

Módulo do ministério P3 da Family Church: cadastro base, turmas, aulas, presença,
avaliação, formatura e histórico.

**Backend:** Java 21 + Spring Boot · PostgreSQL · Flyway
**Dono:** Jhonata Nazareno

## Comece por aqui

Antes da primeira linha de integração, leia **[`docs/`](docs/)** — principalmente o
[03 — Integrar um serviço Java](docs/03-integrar-servico-java.md).

A regra que organiza tudo:

> O núcleo da Family responde **quem é a pessoa e o que ela pode fazer**.
> Este módulo responde **todo o resto**.

Identidade, login e papel vêm do núcleo (`core.pessoa`). Curso, turma, aula, presença,
avaliação, formatura e histórico são deste módulo, no schema `p3`.
