package br.senai.datalens.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

import br.senai.datalens.model.Consultor;
import br.senai.datalens.repository.ConsultorRepository;

// Regras de acesso de ponta a ponta: login público, demais rotas só com token válido
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SegurancaConfigTest {

	private static final String LOGIN_VALIDO = """
			{"email": "teste@exemplo.com", "senha": "senha-teste"}""";

	@Autowired
	private MockMvc mvc;
	@Autowired
	private ConsultorRepository consultores;

	// cria o consultor usado no login (desfeito ao fim de cada teste pelo @Transactional)
	@BeforeEach
	void criarConsultor() {
		Consultor consultor = new Consultor();
		consultor.setNome("Teste");
		consultor.setEmail("teste@exemplo.com");
		consultor.setSenha("senha-teste");
		consultores.save(consultor);
	}

	@Test
	void loginValidoDevolveToken() throws Exception {
		mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(LOGIN_VALIDO))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").isNotEmpty())
				.andExpect(jsonPath("$.nome").value("Teste"));
	}

	@Test
	void loginInvalidoDa401ComMensagem() throws Exception {
		mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\": \"teste@exemplo.com\", \"senha\": \"errada\"}"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.mensagem").value("Email ou senha inválidos."));
	}

	@Test
	void rotaProtegidaSemTokenDa401() throws Exception {
		mvc.perform(get("/qualquer-rota")).andExpect(status().isUnauthorized());
	}

	@Test
	void tokenValidoLiberaAcesso() throws Exception {
		String resposta = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(LOGIN_VALIDO))
				.andReturn().getResponse().getContentAsString();
		String token = JsonPath.read(resposta, "$.token");

		// passa da segurança e chega no Spring, que responde 404 porque a rota não existe
		mvc.perform(get("/qualquer-rota").header("Authorization", "Bearer " + token))
				.andExpect(status().isNotFound());
	}

	@Test
	void tokenFalsoDa401() throws Exception {
		mvc.perform(get("/qualquer-rota").header("Authorization", "Bearer token.falso.qualquer"))
				.andExpect(status().isUnauthorized());
	}

}
