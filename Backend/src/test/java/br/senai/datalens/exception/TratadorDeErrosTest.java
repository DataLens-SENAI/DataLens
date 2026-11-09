package br.senai.datalens.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

// Cada tipo de erro vira {"mensagem": "..."} em português, com o status certo
class TratadorDeErrosTest {

	// controller falso só para disparar os erros
	@RestController
	static class ControllerDeTeste {

		record Pedido(@NotBlank(message = "é obrigatório") String nome) {
		}

		@GetMapping("/previsto")
		void previsto() {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado.");
		}

		@GetMapping("/inesperado")
		void inesperado() {
			throw new IllegalStateException("detalhe interno que não pode vazar");
		}

		@PostMapping("/validar")
		void validar(@Valid @RequestBody Pedido pedido) {
		}

	}

	private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new ControllerDeTeste())
			.setControllerAdvice(new TratadorDeErros())
			.build();

	@Test
	void erroPrevistoUsaMensagemDoService() throws Exception {
		mvc.perform(get("/previsto"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.mensagem").value("Cliente não encontrado."));
	}

	@Test
	void erroInesperadoNaoVazaDetalhes() throws Exception {
		mvc.perform(get("/inesperado"))
				.andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.mensagem").value("Erro interno. Tente novamente mais tarde."));
	}

	@Test
	void campoInvalidoInformaOCampo() throws Exception {
		mvc.perform(post("/validar").contentType(MediaType.APPLICATION_JSON).content("{\"nome\": \"\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.mensagem").value("nome: é obrigatório"));
	}

	@Test
	void erroDoSpringGanhaMensagemEmPortugues() throws Exception {
		mvc.perform(post("/previsto"))
				.andExpect(status().isMethodNotAllowed())
				.andExpect(jsonPath("$.mensagem").value("Método não permitido."));
	}

	@Test
	void jsonMalformadoERequisicaoInvalida() throws Exception {
		mvc.perform(post("/validar").contentType(MediaType.APPLICATION_JSON).content("{quebrado"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.mensagem").value("Requisição inválida."));
	}

}
