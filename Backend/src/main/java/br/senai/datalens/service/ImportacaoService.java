package br.senai.datalens.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import br.senai.datalens.dto.ImportacaoDTO;
import br.senai.datalens.dto.ProblemaDTO;
import br.senai.datalens.dto.RegistroPlanilhaDTO;
import br.senai.datalens.dto.ResultadoProcessamentoDTO;
import br.senai.datalens.dto.TelemetriaDTO;
import br.senai.datalens.model.Consultor;
import br.senai.datalens.model.Telemetria;
import br.senai.datalens.repository.ConsultorRepository;
import br.senai.datalens.repository.TelemetriaRepository;

// Fluxo do upload: confere o arquivo, processa no Python, confere os consultores, grava e registra a telemetria
@Service
public class ImportacaoService {

	public static final String SUCESSO = "SUCESSO";
	public static final String FALHA_VALIDACAO = "FALHA_VALIDACAO";
	public static final String FALHA_PROCESSAMENTO = "FALHA_PROCESSAMENTO";
	public static final String FALHA_GRAVACAO = "FALHA_GRAVACAO";

	private static final Set<String> EXTENSOES = Set.of("xlsx", "xls", "csv");

	private final ProcessamentoClient processamento;
	private final GravacaoPlanilhaService gravacao;
	private final ConsultorRepository consultores;
	private final TelemetriaRepository telemetrias;

	public ImportacaoService(ProcessamentoClient processamento, GravacaoPlanilhaService gravacao,
			ConsultorRepository consultores, TelemetriaRepository telemetrias) {
		this.processamento = processamento;
		this.gravacao = gravacao;
		this.consultores = consultores;
		this.telemetrias = telemetrias;
	}

	// importa a planilha enviada pelo consultor logado; todo resultado gera uma telemetria
	public ImportacaoDTO importar(MultipartFile arquivo, Integer idResponsavel) {
		validarArquivo(arquivo);
		Consultor responsavel = consultores.findById(idResponsavel)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Consultor não encontrado."));
		String nomeArquivo = arquivo.getOriginalFilename();

		ResultadoProcessamentoDTO resultado = processar(arquivo, responsavel, nomeArquivo);
		List<ProblemaDTO> problemas = new ArrayList<>(resultado.problemas());
		Map<String, Consultor> consultoresDaPlanilha = buscarConsultores(resultado.registros(), problemas);
		if (!problemas.isEmpty()) {
			return registrar(responsavel, nomeArquivo, FALHA_VALIDACAO, resultado.registrosLidos(), problemas);
		}

		gravar(resultado, consultoresDaPlanilha, responsavel, nomeArquivo);
		return registrar(responsavel, nomeArquivo, SUCESSO, resultado.registrosLidos(), List.of());
	}

	// últimos uploads (tela de Importação)
	public List<TelemetriaDTO> historico() {
		return telemetrias.findTop20ByOrderByDataUploadDesc().stream().map(TelemetriaDTO::de).toList();
	}

	// barra arquivo vazio ou de formato não aceito antes de chamar o Python (o tamanho é limitado pelo Spring)
	private void validarArquivo(MultipartFile arquivo) {
		if (arquivo == null || arquivo.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Envie um arquivo.");
		}
		String nome = String.valueOf(arquivo.getOriginalFilename()).toLowerCase();
		String extensao = nome.substring(nome.lastIndexOf('.') + 1);
		if (!nome.contains(".") || !EXTENSOES.contains(extensao)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Formato não suportado. Envie um arquivo .xlsx, .xls ou .csv.");
		}
	}

	// chama o Python; se ele estiver fora do ar, registra a falha antes de devolver o erro
	private ResultadoProcessamentoDTO processar(MultipartFile arquivo, Consultor responsavel, String nomeArquivo) {
		try {
			return processamento.processar(arquivo);
		} catch (ResponseStatusException erro) {
			registrar(responsavel, nomeArquivo, FALHA_PROCESSAMENTO, 0, List.of());
			throw erro;
		}
	}

	// acha no banco cada consultor citado na planilha; quem não existe vira problema na linha
	private Map<String, Consultor> buscarConsultores(List<RegistroPlanilhaDTO> registros, List<ProblemaDTO> problemas) {
		Map<String, Consultor> encontrados = new HashMap<>();
		for (RegistroPlanilhaDTO registro : registros) {
			String nomeOuEmail = registro.consultor();
			if (!encontrados.containsKey(nomeOuEmail)) {
				consultores.findFirstByNomeIgnoreCaseOrEmailIgnoreCase(nomeOuEmail, nomeOuEmail)
						.ifPresent(consultor -> encontrados.put(nomeOuEmail, consultor));
			}
			if (!encontrados.containsKey(nomeOuEmail)) {
				problemas.add(new ProblemaDTO(registro.linha(), "consultor", "Consultor não cadastrado no sistema.",
						nomeOuEmail, null));
			}
		}
		return encontrados;
	}

	// grava tudo; se o banco falhar, registra a falha antes de devolver o erro
	private void gravar(ResultadoProcessamentoDTO resultado, Map<String, Consultor> consultoresDaPlanilha,
			Consultor responsavel, String nomeArquivo) {
		try {
			gravacao.gravar(resultado.registros(), resultado.insights(), consultoresDaPlanilha);
		} catch (RuntimeException erro) {
			registrar(responsavel, nomeArquivo, FALHA_GRAVACAO, resultado.registrosLidos(), List.of());
			throw erro;
		}
	}

	// salva a telemetria do upload e monta a resposta
	private ImportacaoDTO registrar(Consultor responsavel, String nomeArquivo, String status, int registros,
			List<ProblemaDTO> problemas) {
		Telemetria telemetria = new Telemetria();
		telemetria.setConsultor(responsavel);
		telemetria.setNomeArquivo(nomeArquivo);
		telemetria.setStatus(status);
		telemetria.setRegistrosProcessados(registros);
		telemetrias.save(telemetria);
		return new ImportacaoDTO(telemetria.getIdTelemetria(), status, registros, problemas);
	}

}
