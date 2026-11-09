package br.senai.datalens.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import br.senai.datalens.dto.ResultadoProcessamentoDTO;

// Cliente HTTP do serviço Python: envia a planilha e recebe o resultado da validação/padronização
@Component
public class ProcessamentoClient {

	private final RestClient http;

	public ProcessamentoClient(@Value("${processamento.url}") String url) {
		this.http = RestClient.create(url);
	}

	// POST /processar com o arquivo no campo "arquivo"; serviço fora do ar ou com erro = 503
	public ResultadoProcessamentoDTO processar(MultipartFile arquivo) {
		MultiValueMap<String, Object> corpo = new LinkedMultiValueMap<>();
		corpo.add("arquivo", arquivo.getResource());
		try {
			return http.post()
					.uri("/processar")
					.contentType(MediaType.MULTIPART_FORM_DATA)
					.body(corpo)
					.retrieve()
					.body(ResultadoProcessamentoDTO.class);
		} catch (RestClientException erro) {
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
					"O serviço de processamento está indisponível. Tente novamente em instantes.", erro);
		}
	}

}
