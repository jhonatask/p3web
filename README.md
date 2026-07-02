# Cadastro P3 — página pública de cadastro de aluno

Página web pública (Next.js) onde o aluno se cadastra numa turma a partir do link único gerado pelo
backend (`app.com/cadastro/{token}`). Não exige instalar nenhum app — o aluno abre o link no celular,
preenche os dados uma única vez e pronto.

Este projeto nasceu para a Escola de Hombridade, mas é genérico o suficiente para servir outros
cursos/programas da igreja, já que a turma e o curso vêm dinamicamente do backend.

## Como rodar

```bash
npm install
npm run dev
```

Acesse `http://localhost:3000`. A rota principal é `/cadastro/[token]`, por exemplo:

```
http://localhost:3000/cadastro/ABC12345
```

## Variável de ambiente

Copie `.env.local.example` para `.env.local` e ajuste se necessário:

```
NEXT_PUBLIC_API_URL=http://localhost:8080
```

Todas as chamadas ao backend usam essa variável (nunca há URL de backend hardcoded no código).

## Rota principal

`app/cadastro/[token]/page.tsx` — fluxo:

1. Busca `GET /api/publico/turmas/{token}` no servidor (Server Component).
   - Token inexistente (404) → tela de link inválido.
   - Turma encerrada (409) → tela informando que o cadastro não está mais disponível, com a mensagem
     vinda do backend.
   - Sucesso (200) → mostra o cabeçalho com curso/turma e o formulário de cadastro.
2. O formulário (`components/FormularioCadastro.tsx`) valida nome, CPF (algoritmo real de dígito
   verificador, em `lib/cpf.ts`), celular e campus no client antes de enviar, e exige o checkbox de
   consentimento LGPD marcado (vem desmarcado por padrão) para habilitar o botão de envio.
3. Ao enviar, chama `POST /api/publico/turmas/{token}/alunos`. Erros de validação de campo (400 com
   `erros`) são exibidos ao lado do campo correspondente; erros de regra de negócio (CPF duplicado,
   turma encerrada) aparecem como alerta geral no topo do formulário, sem apagar o que já foi
   preenchido. Sucesso (201) mostra a tela de confirmação de cadastro.

## Estrutura

- `lib/api.ts` — chamadas HTTP ao backend e tipos de request/response.
- `lib/cpf.ts` — validação de CPF (dígito verificador via módulo 11 + rejeição de dígitos repetidos).
- `lib/masks.ts` — máscaras de digitação de CPF e celular.
- `lib/texto.ts` — utilitário compartilhado de normalização de texto.
- `components/` — componentes de UI (formulário, telas de erro/sucesso, cartão de página).
- `app/cadastro/[token]/page.tsx` — rota pública de cadastro.

## Testando de ponta a ponta

Para testar o fluxo completo é preciso ter o backend rodando:

```bash
cd ../backend
docker compose up -d
./mvnw.cmd spring-boot:run
```

Depois, crie uma turma de teste (login, curso, turma) usando a collection Postman
`../backend/postman/chamada-backend.postman_collection.json` (pastas Auth → Curso → Turma, nessa
ordem) para obter um `tokenCadastro` válido. Acesse
`http://localhost:3000/cadastro/{tokenCadastro}` para ver o formulário real.
