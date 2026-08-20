# Documentação — módulo P3 / Escola da Hombridade

| # | Documento | Para quê |
|---|---|---|
| 01 | [Como a Family desenvolve](01-como-a-family-desenvolve.md) | O resumo: domínios, infra, git, banco, padrões. 5 min |
| 02 | [Contrato do núcleo](02-nucleo-contrato.md) | O contrato oficial entre o núcleo e os módulos |
| 03 | [Integrar um serviço Java](03-integrar-servico-java.md) | JWT, papéis, Flyway, ambiente local. **É o seu caminho** |
| — | [`schema/core-referencia.sql`](schema/core-referencia.sql) | O DDL do núcleo, para consulta. **Não rode este arquivo** — o `core` já existe; é só referência |

**Se você só vai ler um agora:** o **03**.

O **02** é o contrato oficial e foi escrito pensando em módulos dentro do app Next.js — a
parte de TypeScript não se aplica a você, mas as regras de fronteira sim. O **03** é o
complemento que traduz aquilo para serviço separado.
