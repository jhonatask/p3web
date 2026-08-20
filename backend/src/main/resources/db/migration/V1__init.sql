-- Migration inicial: apenas valida que o Flyway esta corretamente configurado
-- contra o Postgres do projeto (compose.yaml / application.yml).
--
-- As migrations do modelo de dominio (Curso, Turma, Aluno, Usuario_Admin, Aula,
-- Presenca, Avaliacao, Interesse) serao adicionadas em tarefas futuras, uma por
-- entidade/feature, seguindo a convencao Vx__descricao.sql do Flyway.
select 1;
