package br.senai.datalens.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.senai.datalens.dto.LoginDTO;
import br.senai.datalens.dto.TokenDTO;
import br.senai.datalens.service.AutenticacaoService;
import jakarta.validation.Valid;

// Endpoint de login
@RestController
@RequestMapping("/auth")
public class AutenticacaoController {

	private final AutenticacaoService autenticacao;

	public AutenticacaoController(AutenticacaoService autenticacao) {
		this.autenticacao = autenticacao;
	}

	// POST /auth/login {email, senha} -> {token, idConsultor, nome}
	@PostMapping("/login")
	public TokenDTO login(@Valid @RequestBody LoginDTO login) {
		return autenticacao.login(login);
	}

}
