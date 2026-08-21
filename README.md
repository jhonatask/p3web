# p3web — frontend público da Plataforma P3

Frontend público (Next.js) da **Plataforma P3** (cadastro único por CPF, compartilhado por vários
módulos/ministérios — ver `../relatorio_arquitetura_plataforma_p3.pdf` e os mapas em `../proposta/`).
Este projeto se chamava `cadastro-web`; foi renomeado para `p3web` porque deixou de ser só a tela de
cadastro — agora também é a chamada da Escola de Hombridade, e vai ganhar a área logada do líder.

Nenhuma das rotas exige instalar app: o homem abre o link no celular e resolve tudo pelo navegador.

## Como rodar

```bash
npm install
npm run dev
```

Acesse `http://localhost:3000`.

## Variáveis de ambiente

Copie `.env.local.example` para `.env.local` e ajuste se necessário:

```
NEXT_PUBLIC_API_URL=http://localhost:8080
NEXT_PUBLIC_SUPABASE_URL=
NEXT_PUBLIC_SUPABASE_ANON_KEY=
```

Todas as chamadas ao backend usam `NEXT_PUBLIC_API_URL` (nunca há URL de backend hardcoded no
código). O login (`/login`) é a exceção: vai direto no Supabase Auth com `NEXT_PUBLIC_SUPABASE_URL`
+ `NEXT_PUBLIC_SUPABASE_ANON_KEY`, e o backend só valida o token via JWKS — o P3 não emite nem
guarda credencial.

## Rotas

### `/cadastro-base` — cadastro único por CPF (Entrega 1)

Link único e permanente. Formulário completo (identidade, endereço, vida na Family, história de
cursos/eventos, comunhão). Ao enviar, chama `POST /api/publico/participantes` — se o CPF já existir na
base, atualiza o cadastro existente em vez de duplicar.

### `/chamada/[token]` — chamada da Escola de Hombridade (Entrega 2)

Link exclusivo por turma. Fluxo:

1. Busca `GET /api/publico/turmas/{token}` e `GET /api/publico/turmas/{token}/aula-ativa` no servidor.
   - Token inexistente (404) → link inválido. Turma encerrada (409) → aviso. Sem aula ativa hoje (404) →
     aviso — a aula do dia é decidida pela agenda da turma, o aluno nunca escolhe.
2. O aluno informa o CPF (`components/FormularioCpfChamada.tsx`), que chama
   `POST /api/publico/turmas/{token}/chamada`.
   - CPF já cadastrado → presença registrada na hora, mostra "Olá, Fulano" e vai direto para a
     avaliação da aula.
   - CPF sem cadastro (404) → abre o formulário completo do cadastro base
     (`components/CadastroBaseCliente.tsx`, reaproveitado, com o CPF já preenchido) antes de liberar a
     avaliação. A chamada nunca recadastra quem já tem cadastro — só localiza pelo CPF.
3. Avaliação da aula (`components/FormularioAvaliacao.tsx`): nota (0–10), didática, sugestão, NPS
   (0–10) — sempre. Nas aulas 03/06/09 (3ª aula de cada módulo), aparecem também "deseja servir?" e "o
   que você sabe fazer?", que fazem write-back direto na base do homem pelo CPF.

## Estrutura

- `lib/api.ts` — chamadas HTTP ao backend e tipos de request/response.
- `lib/cpf.ts` — validação de CPF (dígito verificador via módulo 11 + rejeição de dígitos repetidos).
- `lib/masks.ts` — máscaras de digitação de CPF, CEP e celular.
- `lib/texto.ts` — utilitário compartilhado de normalização de texto.
- `lib/viacep.ts` — auto-preenchimento de endereço a partir do CEP.
- `components/Campo*.tsx` — campos de formulário reaproveitáveis (texto, textarea, escala 0–10,
  seleção única, múltipla escolha, sim/não).
- `components/CartaoPagina.tsx` — moldura visual comum a todas as páginas.
- `app/cadastro-base/` — rota do cadastro único.
- `app/chamada/[token]/` — rota da chamada por turma.

## Testando de ponta a ponta

Para testar o fluxo completo é preciso ter o backend rodando:

```bash
cd ../backend
docker compose up -d
./mvnw.cmd spring-boot:run
```

Depois, use a collection Postman `../backend/postman/chamada-backend.postman_collection.json` (ou
`curl`) para: logar como admin, criar/achar o curso "Escola de Hombridade", criar uma turma, e definir a
agenda de pelo menos uma aula com `data` de hoje (`PUT /api/turmas/{turmaId}/agenda`) para que
`/api/publico/turmas/{token}/aula-ativa` responda. Acesse `http://localhost:3000/chamada/{tokenCadastro}`
para ver o fluxo real.
