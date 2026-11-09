package br.senai.datalens.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import br.senai.datalens.dto.ImportacaoDTO;
import br.senai.datalens.dto.ProblemaDTO;
import br.senai.datalens.dto.RegistroPlanilhaDTO;
import br.senai.datalens.dto.ResultadoProcessamentoDTO;
import br.senai.datalens.dto.ServicoContratadoDTO;
import br.senai.datalens.model.Consultor;
import br.senai.datalens.model.Telemetria;
import br.senai.datalens.repository.ConsultorRepository;
import br.senai.datalens.repository.TelemetriaRepository;

// Fluxo do upload com Python, gravação e banco simulados: cada caminho gera a telemetria certa
class ImportacaoServiceTest {

	private static final MockMultipartFile PLANILHA = new MockMultipartFile("arquivo", "carteira.xlsx", null, new byte[] { 1 });

	private final ProcessamentoClient processamento = mock(ProcessamentoClient.class);
	private final GravacaoPlanilhaService gravacao = mock(GravacaoPlanilhaService.class);
	private final ConsultorRepository consultores = mock(ConsultorRepository.class);
	private final TelemetriaRepository telemetrias = mock(TelemetriaRepository.class);
	private final ImportacaoService importacao = new ImportacaoService(processamento, gravacao, consultores, telemetrias);

	private final Consultor ana = new Consultor();

	// o consultor logado (id 1) existe, e a "Ana" da planilha também
	@BeforeEach
	void prepararConsultores() {
		when(consultores.findById(1)).thenReturn(Optional.of(ana));
		when(consultores.findFirstByNomeIgnoreCaseOrEmailIgnoreCase("Ana", "Ana")).thenReturn(Optional.of(ana));
		when(consultores.findFirstByNomeIgnoreCaseOrEmailIgnoreCase("Fulano", "Fulano")).thenReturn(Optional.empty());
	}

	@Test
	void planilhaValidaEGravadaComSucesso() {
		when(processamento.processar(PLANILHA)).thenReturn(resultado(true, List.of(registro("Ana")), List.of()));

		ImportacaoDTO resposta = importacao.importar(PLANILHA, 1);

		assertThat(resposta.status()).isEqualTo(ImportacaoService.SUCESSO);
		assertThat(resposta.registrosProcessados()).isEqualTo(3);
		verify(gravacao).gravar(anyList(), anyList(), any());
		assertThat(telemetriaSalva().getStatus()).isEqualTo(ImportacaoService.SUCESSO);
		assertThat(telemetriaSalva().getNomeArquivo()).isEqualTo("carteira.xlsx");
	}

	@Test
	void problemasDoPythonNaoGravamNada() {
		ProblemaDTO problema = new ProblemaDTO(2, "segmento", "Segmento não reconhecido.", "Indstria", "Indústria");
		when(processamento.processar(PLANILHA)).thenReturn(resultado(false, List.of(), List.of(problema)));

		ImportacaoDTO resposta = importacao.importar(PLANILHA, 1);

		assertThat(resposta.status()).isEqualTo(ImportacaoService.FALHA_VALIDACAO);
		assertThat(resposta.problemas()).containsExactly(problema);
		verify(gravacao, never()).gravar(anyList(), anyList(), anyMap());
		assertThat(telemetriaSalva().getStatus()).isEqualTo(ImportacaoService.FALHA_VALIDACAO);
	}

	@Test
	void consultorNaoCadastradoViraProblema() {
		when(processamento.processar(PLANILHA)).thenReturn(resultado(true, List.of(registro("Fulano")), List.of()));

		ImportacaoDTO resposta = importacao.importar(PLANILHA, 1);

		assertThat(resposta.status()).isEqualTo(ImportacaoService.FALHA_VALIDACAO);
		assertThat(resposta.problemas()).containsExactly(
				new ProblemaDTO(2, "consultor", "Consultor não cadastrado no sistema.", "Fulano", null));
		verify(gravacao, never()).gravar(anyList(), anyList(), anyMap());
	}

	@Test
	void pythonForaDoArRegistraFalha() {
		when(processamento.processar(PLANILHA))
				.thenThrow(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "indisponível"));

		assertThatThrownBy(() -> importacao.importar(PLANILHA, 1)).isInstanceOf(ResponseStatusException.class);
		assertThat(telemetriaSalva().getStatus()).isEqualTo(ImportacaoService.FALHA_PROCESSAMENTO);
	}

	@Test
	void formatoNaoAceitoNemChegaAoPython() {
		MockMultipartFile pdf = new MockMultipartFile("arquivo", "carteira.pdf", null, new byte[] { 1 });

		assertThatThrownBy(() -> importacao.importar(pdf, 1))
				.hasMessageContaining("Formato não suportado");
		verify(processamento, never()).processar(any());
	}

	@Test
	void arquivoVazioEErro() {
		MockMultipartFile vazio = new MockMultipartFile("arquivo", "carteira.xlsx", null, new byte[0]);

		assertThatThrownBy(() -> importacao.importar(vazio, 1)).hasMessageContaining("Envie um arquivo.");
	}

	// pega a telemetria passada para o save
	private Telemetria telemetriaSalva() {
		ArgumentCaptor<Telemetria> captor = ArgumentCaptor.forClass(Telemetria.class);
		verify(telemetrias).save(captor.capture());
		return captor.getValue();
	}

	private static ResultadoProcessamentoDTO resultado(boolean valido, List<RegistroPlanilhaDTO> registros,
			List<ProblemaDTO> problemas) {
		return new ResultadoProcessamentoDTO(valido, 3, registros, problemas, List.of());
	}

	// cliente da linha 2 atendido pelo consultor informado
	private static RegistroPlanilhaDTO registro(String consultor) {
		return new RegistroPlanilhaDTO(2, consultor, "1001", "Metalúrgica", "Indústria", 'A', new BigDecimal("200000"),
				List.of(new ServicoContratadoDTO("Cloud", null)));
	}

}
