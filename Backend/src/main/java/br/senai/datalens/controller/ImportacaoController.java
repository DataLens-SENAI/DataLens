package br.senai.datalens.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import br.senai.datalens.dto.ImportacaoDTO;
import br.senai.datalens.dto.TelemetriaDTO;
import br.senai.datalens.service.ImportacaoService;

// Upload de planilhas e histórico de importações
@RestController
@RequestMapping("/importacoes")
public class ImportacaoController {

	private final ImportacaoService importacao;

	public ImportacaoController(ImportacaoService importacao) {
		this.importacao = importacao;
	}

	// POST /importacoes (multipart, campo "arquivo"); o consultor responsável vem do token
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ImportacaoDTO importar(@RequestParam("arquivo") MultipartFile arquivo, @AuthenticationPrincipal Jwt token) {
		return importacao.importar(arquivo, Integer.valueOf(token.getSubject()));
	}

	// GET /importacoes -> últimos 20 uploads
	@GetMapping
	public List<TelemetriaDTO> historico() {
		return importacao.historico();
	}

}
