package br.senai.datalens.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import br.senai.datalens.config.SegurancaConfig;
import br.senai.datalens.dto.LoginDTO;
import br.senai.datalens.dto.TokenDTO;
import br.senai.datalens.model.Consultor;
import br.senai.datalens.repository.ConsultorRepository;

// Login: credencial certa gera token do consultor; errada ou inexistente dá 401 com a mesma mensagem
class AutenticacaoServiceTest {

	private final SegurancaConfig seguranca = new SegurancaConfig("segredo-de-teste-com-pelo-menos-32-caracteres");
	private final ConsultorRepository consultores = mock(ConsultorRepository.class);
	private final AutenticacaoService autenticacao = new AutenticacaoService(consultores, seguranca.jwtEncoder());

	@Test
	void loginValidoGeraTokenComIdDoConsultor() {
		Consultor ana = consultor();
		when(consultores.findByEmail("ana@exemplo.com")).thenReturn(Optional.of(ana));

		TokenDTO token = autenticacao.login(new LoginDTO("ana@exemplo.com", "senha-certa"));

		assertThat(token.idConsultor()).isEqualTo(7);
		assertThat(token.nome()).isEqualTo("Ana");
		assertThat(seguranca.jwtDecoder().decode(token.token()).getSubject()).isEqualTo("7");
	}

	@Test
	void senhaErradaDa401() {
		Consultor ana = consultor();
		when(consultores.findByEmail("ana@exemplo.com")).thenReturn(Optional.of(ana));

		assertNaoAutorizado(new LoginDTO("ana@exemplo.com", "senha-errada"));
	}

	@Test
	void emailInexistenteDa401() {
		when(consultores.findByEmail("ninguem@exemplo.com")).thenReturn(Optional.empty());

		assertNaoAutorizado(new LoginDTO("ninguem@exemplo.com", "qualquer"));
	}

	@Test
	void segredoCurtoImpedeSubida() {
		assertThatThrownBy(() -> new SegurancaConfig("curto"))
				.hasMessageContaining("32 caracteres");
	}

	private void assertNaoAutorizado(LoginDTO login) {
		assertThatThrownBy(() -> autenticacao.login(login))
				.isInstanceOfSatisfying(ResponseStatusException.class, erro -> {
					assertThat(erro.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
					assertThat(erro.getReason()).isEqualTo("Email ou senha inválidos.");
				});
	}

	// consultor fictício já salvo (id 7) com senha "senha-certa"
	private static Consultor consultor() {
		Consultor consultor = mock(Consultor.class);
		when(consultor.getIdConsultor()).thenReturn(7);
		when(consultor.getNome()).thenReturn("Ana");
		when(consultor.autenticar("senha-certa")).thenReturn(true);
		return consultor;
	}

}
