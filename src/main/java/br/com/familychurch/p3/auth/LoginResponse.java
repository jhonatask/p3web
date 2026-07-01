package br.com.familychurch.p3.auth;

import br.com.familychurch.p3.usuarioadmin.Papel;

public record LoginResponse(String token, String nome, Papel papel) {
}
