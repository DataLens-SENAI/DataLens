package br.senai.datalens.exception;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import br.senai.datalens.dto.ErroDTO;

// Converte exceções em respostas JSON padronizadas ({"mensagem": "..."}), em português e sem stack trace.
// Herda de ResponseEntityExceptionHandler, que já dá o status certo aos erros do Spring (404, 405, JSON inválido...).
@RestControllerAdvice
public class TratadorDeErros extends ResponseEntityExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(TratadorDeErros.class);

	// campos inválidos no corpo da requisição (@Valid): junta as mensagens de cada campo
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException erro,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		String mensagem = erro.getBindingResult().getFieldErrors().stream()
				.map(campo -> campo.getField() + ": " + campo.getDefaultMessage())
				.collect(Collectors.joining("; "));
		return ResponseEntity.badRequest().body(new ErroDTO(mensagem));
	}

	// demais erros do Spring e erros previstos dos services (throw new ResponseStatusException(status, "mensagem")):
	// usa a mensagem do service, se houver; senão, uma mensagem padrão em português
	@Override
	protected ResponseEntity<Object> handleExceptionInternal(Exception erro, Object corpo, HttpHeaders headers,
			HttpStatusCode status, WebRequest request) {
		String mensagem = erro instanceof ResponseStatusException previsto && previsto.getReason() != null
				? previsto.getReason()
				: mensagemPadrao(status);
		return ResponseEntity.status(status).headers(headers).body(new ErroDTO(mensagem));
	}

	// qualquer erro não previsto: registra no log e devolve mensagem genérica (não vaza detalhes)
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErroDTO> erroInesperado(Exception erro) {
		log.error("Erro inesperado", erro);
		return ResponseEntity.internalServerError().body(new ErroDTO("Erro interno. Tente novamente mais tarde."));
	}

	// mensagem em português de acordo com o status HTTP
	private static String mensagemPadrao(HttpStatusCode status) {
		if (status.value() == HttpStatus.NOT_FOUND.value()) return "Recurso não encontrado.";
		if (status.value() == HttpStatus.METHOD_NOT_ALLOWED.value()) return "Método não permitido.";
		if (status.value() == HttpStatus.CONTENT_TOO_LARGE.value()) return "O arquivo ultrapassa o tamanho máximo de 50MB.";
		if (status.is4xxClientError()) return "Requisição inválida.";
		return "Erro interno. Tente novamente mais tarde.";
	}

}
