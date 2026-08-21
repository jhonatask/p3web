package br.com.familychurch.p3.auth;

import java.util.UUID;

/**
 * pessoaId e o pessoa_id do core.pessoa - resolvido uma unica vez no
 * conversor do JWT (nao a cada acao), porque sobrevive a recriacao da conta
 * do Supabase Auth (o sub nao). Nullable: uma conta pode existir sem Pessoa
 * vinculada ainda (core.pessoa_da_conta retorna NULL nesse caso).
 */
public record PessoaPrincipal(UUID pessoaId, String sub) {
}
