package br.com.familychurch.p3.auth;

public record LoginResponse(String token, String nome, Papel papel) {
}
