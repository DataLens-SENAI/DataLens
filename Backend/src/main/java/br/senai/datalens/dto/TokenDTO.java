package br.senai.datalens.dto;

// Resposta do login: token para as próximas requisições e dados básicos do consultor
public record TokenDTO(String token, Integer idConsultor, String nome) {
}
