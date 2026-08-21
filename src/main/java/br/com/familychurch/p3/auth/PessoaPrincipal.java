package br.com.familychurch.p3.auth;

import java.util.UUID;

public record PessoaPrincipal(UUID pessoaId, String sub) {
}
