package br.senai.datalens.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.context.annotation.Import;

import br.senai.datalens.dto.InsightGeradoDTO;
import br.senai.datalens.dto.RegistroPlanilhaDTO;
import br.senai.datalens.dto.ServicoContratadoDTO;
import br.senai.datalens.model.Cliente;
import br.senai.datalens.model.Consultor;
import br.senai.datalens.repository.ClienteRepository;
import br.senai.datalens.repository.ConsultorRepository;
import br.senai.datalens.repository.ContratoRepository;
import br.senai.datalens.repository.InsightRepository;
import br.senai.datalens.repository.ServicoRepository;

// Gravação no banco (H2): cria, atualiza sem duplicar e substitui os insights
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import(GravacaoPlanilhaService.class)
class GravacaoPlanilhaServiceTest {

	@Autowired
	private GravacaoPlanilhaService gravacao;
	@Autowired
	private ConsultorRepository consultores;
	@Autowired
	private ClienteRepository clientes;
	@Autowired
	private ServicoRepository servicos;
	@Autowired
	private ContratoRepository contratos;
	@Autowired
	private InsightRepository insights;

	private Map<String, Consultor> consultoresDaPlanilha;

	// a planilha cita a consultora "Ana", já cadastrada
	@BeforeEach
	void cadastrarConsultora() {
		Consultor ana = new Consultor();
		ana.setNome("Ana");
		ana.setEmail("ana@exemplo.com");
		ana.setSenha("senha-teste");
		consultoresDaPlanilha = Map.of("Ana", consultores.save(ana));
	}

	@Test
	void gravaClienteContratosEServicosNovos() {
		gravacao.gravar(List.of(registro("200000", 'A', "Cloud", "Telefonia IP")), List.of(), consultoresDaPlanilha);

		Cliente cliente = clientes.findByCodigoInterno("1001").orElseThrow();
		assertThat(cliente.getNome()).isEqualTo("Metalúrgica");
		assertThat(cliente.getNivel()).isEqualTo('A');
		assertThat(cliente.getConsultor().getNome()).isEqualTo("Ana");
		assertThat(servicos.count()).isEqualTo(2);
		assertThat(contratos.count()).isEqualTo(2);
		assertThat(contratos.contarSemData(null)).isEqualTo(1); // Telefonia IP veio sem data
	}

	@Test
	void reenvioAtualizaClienteSemDuplicar() {
		gravacao.gravar(List.of(registro("200000", 'A', "Cloud")), List.of(), consultoresDaPlanilha);
		gravacao.gravar(List.of(registro("60000", 'C', "Cloud", "Telefonia IP")), List.of(), consultoresDaPlanilha);

		assertThat(clientes.count()).isEqualTo(1);
		assertThat(clientes.findByCodigoInterno("1001").orElseThrow().getNivel()).isEqualTo('C');
		assertThat(contratos.count()).isEqualTo(2); // Cloud não foi contratado de novo
	}

	@Test
	void insightsDoUltimoUploadSubstituemOsAnteriores() {
		InsightGeradoDTO antigo = new InsightGeradoDTO("risco", "Concentração", "antigo", 80.0, null);
		InsightGeradoDTO doCliente = new InsightGeradoDTO("oportunidade", "Cross-sell", "novo", 200000.0, "1001");

		gravacao.gravar(List.of(registro("200000", 'A', "Cloud")), List.of(antigo), consultoresDaPlanilha);
		gravacao.gravar(List.of(registro("200000", 'A', "Cloud")), List.of(doCliente), consultoresDaPlanilha);

		assertThat(insights.findAll()).singleElement().satisfies(insight -> {
			assertThat(insight.getDescricao()).isEqualTo("novo");
			assertThat(insight.getCliente().getCodigoInterno()).isEqualTo("1001");
		});
	}

	// cliente 1001 da Ana; o primeiro serviço tem data, os demais não
	private static RegistroPlanilhaDTO registro(String faturamento, char nivel, String... nomesServicos) {
		List<ServicoContratadoDTO> contratados = new ArrayList<>();
		for (int i = 0; i < nomesServicos.length; i++) {
			contratados.add(new ServicoContratadoDTO(nomesServicos[i], i == 0 ? LocalDate.of(2026, 1, 10) : null));
		}
		return new RegistroPlanilhaDTO(2, "Ana", "1001", "Metalúrgica", "Indústria", nivel, new BigDecimal(faturamento),
				contratados);
	}

}
