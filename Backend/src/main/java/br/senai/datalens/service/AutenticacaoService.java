package br.senai.datalens.service;

import java.time.Duration;
import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import br.senai.datalens.dto.LoginDTO;
import br.senai.datalens.dto.TokenDTO;
import br.senai.datalens.model.Consultor;
import br.senai.datalens.repository.ConsultorRepository;

// Login do consultor: confere email e senha e gera o token JWT
@Service
public class AutenticacaoService {

	// tempo de validade do token (um dia de trabalho)
	private static final Duration VALIDADE_TOKEN = Duration.ofHours(8);

	private final ConsultorRepository consultores;
	private final JwtEncoder jwtEncoder;

	public AutenticacaoService(ConsultorRepository consultores, JwtEncoder jwtEncoder) {
		this.consultores = consultores;
		this.jwtEncoder = jwtEncoder;
	}

	// mesma mensagem para email inexistente e senha errada: não revela quais emails existem
	public TokenDTO login(LoginDTO login) {
		Consultor consultor = consultores.findByEmail(login.email())
				.filter(encontrado -> encontrado.autenticar(login.senha()))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email ou senha inválidos."));
		return new TokenDTO(gerarToken(consultor), consultor.getIdConsultor(), consultor.getNome());
	}

	// token com o id do consultor no "subject", usado depois para saber quem fez a requisição
	private String gerarToken(Consultor consultor) {
		Instant agora = Instant.now();
		JwtClaimsSet dados = JwtClaimsSet.builder()
				.subject(consultor.getIdConsultor().toString())
				.claim("nome", consultor.getNome())
				.issuedAt(agora)
				.expiresAt(agora.plus(VALIDADE_TOKEN))
				.build();
		JwsHeader cabecalho = JwsHeader.with(MacAlgorithm.HS256).build();
		return jwtEncoder.encode(JwtEncoderParameters.from(cabecalho, dados)).getTokenValue();
	}

}
