package br.senai.datalens.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// Dados enviados na tela de login
public record LoginDTO(
		@NotBlank(message = "é obrigatório") @Email(message = "formato inválido") String email,
		@NotBlank(message = "é obrigatória") String senha) {
}
