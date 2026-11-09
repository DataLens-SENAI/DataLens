package br.senai.datalens.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;

import br.senai.datalens.dto.ClienteDTO;
import br.senai.datalens.dto.ContagemDTO;
import br.senai.datalens.dto.ContratacoesMesDTO;
import br.senai.datalens.dto.DashboardDTO;
import br.senai.datalens.dto.KpisDTO;
import br.senai.datalens.model.Cliente;
import br.senai.datalens.model.Consultor;
import br.senai.datalens.model.Contrato;
import br.senai.datalens.model.Insight;
import br.senai.datalens.model.Servico;
import br.senai.datalens.service.CarteiraService;
import br.senai.datalens.service.DashboardService;
import jakarta.persistence.EntityManager;

// Consultas e services do dashboard com dados fictícios e resultado calculado à mão:
//   Ana:   Indústria/A/200 mil, Indústria/B/100 mil, Comércio/C/30 mil
//   Bruno: Serviços/A/170 mil
//   Contratos de Cloud: jan/2026 x3 (2 da Ana, 1 do Bruno), mar/2026 x1 (Ana)
//   Contrato de Telefonia IP sem data (Ana) -> fora do gráfico de evolução
//   Insights: um da carteira toda e um de risco do cliente do Bruno
@DataJpaTest
@Import({ DashboardService.class, CarteiraService.class })
@AutoConfigureTestDatabase(replace = Replace.NONE) // usa o H2 configurado em test/resources (modo PostgreSQL)
class AgregacoesDashboardTest {

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
	@Autowired
	private DashboardService dashboard;
	@Autowired
	private CarteiraService carteira;
	@Autowired
	private EntityManager banco;

	private Consultor ana;
	private Consultor bruno;

	// monta a carteira fictícia descrita acima
	@BeforeEach
	void popularBanco() {
		ana = consultor("Ana", "ana@exemplo.com");
		bruno = consultor("Bruno", "bruno@exemplo.com");
		Servico cloud = servico("Cloud");
		Servico telefonia = servico("Telefonia IP");

		Cliente metalurgica = cliente(ana, "Indústria", 'A', "200000");
		Cliente fabrica = cliente(ana, "Indústria", 'B', "100000");
		cliente(ana, "Comércio", 'C', "30000");
		Cliente consultoria = cliente(bruno, "Serviços", 'A', "170000");

		contrato(metalurgica, cloud, LocalDate.of(2026, 1, 10));
		contrato(metalurgica, cloud, LocalDate.of(2026, 1, 20));
		contrato(fabrica, cloud, LocalDate.of(2026, 3, 5));
		contrato(consultoria, cloud, LocalDate.of(2026, 1, 15));
		contrato(metalurgica, telefonia, null);

		insight(null);
		insight(consultoria);

		// grava e limpa a sessão para as consultas lerem do banco (com os relacionamentos completos)
		banco.flush();
		banco.clear();
	}

	// segmentos: carteira toda e filtrado pela Ana
	@Test
	void contaClientesPorSegmento() {
		assertThat(clientes.contarPorSegmento(null)).containsExactlyInAnyOrder(
				new ContagemDTO("Indústria", 2L), new ContagemDTO("Comércio", 1L), new ContagemDTO("Serviços", 1L));
		assertThat(clientes.contarPorSegmento(null).getFirst()).isEqualTo(new ContagemDTO("Indústria", 2L));

		assertThat(clientes.contarPorSegmento(ana.getIdConsultor())).containsExactly(
				new ContagemDTO("Indústria", 2L), new ContagemDTO("Comércio", 1L));
	}

	// níveis em ordem A, B, C
	@Test
	void contaClientesPorNivel() {
		assertThat(clientes.contarPorNivel(null)).containsExactly(
				new ContagemDTO("A", 2L), new ContagemDTO("B", 1L), new ContagemDTO("C", 1L));
		assertThat(clientes.contarPorNivel(ana.getIdConsultor())).containsExactly(
				new ContagemDTO("A", 1L), new ContagemDTO("B", 1L), new ContagemDTO("C", 1L));
	}

	// KPIs: total e média = (200 + 100 + 30 + 170) / 4 = 125 mil; só Ana = 330 / 3 = 110 mil
	@Test
	void calculaKpis() {
		assertThat(clientes.contarClientes(null)).isEqualTo(4);
		assertThat(clientes.contarClientes(ana.getIdConsultor())).isEqualTo(3);
		assertThat(clientes.faturamentoMedio(null)).isEqualTo(125000.0);
		assertThat(clientes.faturamentoMedio(ana.getIdConsultor())).isEqualTo(110000.0);
	}

	// contratações agrupadas por mês, em ordem cronológica
	@Test
	void contaContratacoesPorMes() {
		assertThat(contratos.contarPorMes(null)).containsExactly(
				new ContratacoesMesDTO(2026, 1, 3L), new ContratacoesMesDTO(2026, 3, 1L));
		assertThat(contratos.contarPorMes(ana.getIdConsultor())).containsExactly(
				new ContratacoesMesDTO(2026, 1, 2L), new ContratacoesMesDTO(2026, 3, 1L));
	}

	// contratos sem data contam no total, mas não no gráfico; serviços mais contratados primeiro
	@Test
	void contaContratosEServicos() {
		assertThat(contratos.contarContratos(null)).isEqualTo(5);
		assertThat(contratos.contarSemData(null)).isEqualTo(1);
		assertThat(contratos.contarSemData(bruno.getIdConsultor())).isZero();
		assertThat(servicos.contarContratosPorServico(null)).containsExactly(
				new ContagemDTO("Cloud", 4L), new ContagemDTO("Telefonia IP", 1L));
		assertThat(servicos.contarContratosPorServico(ana.getIdConsultor())).containsExactly(
				new ContagemDTO("Cloud", 3L), new ContagemDTO("Telefonia IP", 1L));
	}

	// filtro por consultor mantém os insights da carteira toda e tira os de clientes de outros consultores
	@Test
	void filtraInsightsPorConsultor() {
		assertThat(insights.listar(null)).hasSize(2);
		assertThat(insights.listar(ana.getIdConsultor())).singleElement()
				.satisfies(insight -> assertThat(insight.getCliente()).isNull());
		assertThat(insights.listar(bruno.getIdConsultor())).hasSize(2);
	}

	// dashboard da Ana: 3 clientes, 1 consultor, média 110 mil, 4 contratos sendo 1 sem data
	@Test
	void montaDashboardFiltrado() {
		DashboardDTO resultado = dashboard.montar(ana.getIdConsultor());

		assertThat(resultado.kpis()).isEqualTo(new KpisDTO(3, 1, 110000.0, 4, 1));
		assertThat(resultado.insights()).hasSize(1);
		assertThat(dashboard.montar(null).kpis().totalConsultores()).isEqualTo(2);
	}

	// listagem da Ana em ordem alfabética, com contagem de serviços; cliente do Bruno fica em risco
	@Test
	void listaCarteira() {
		List<ClienteDTO> daAna = carteira.listarClientes(ana.getIdConsultor());

		assertThat(daAna).extracting(ClienteDTO::nome)
				.containsExactly("Cliente Comércio C", "Cliente Indústria A", "Cliente Indústria B");
		assertThat(daAna.get(1).servicos()).isEqualTo(3);
		assertThat(carteira.listarClientes(bruno.getIdConsultor())).singleElement()
				.satisfies(cliente -> assertThat(cliente.status()).isEqualTo("risco"));
		assertThat(carteira.listarConsultores()).hasSize(2);
	}

	private Consultor consultor(String nome, String email) {
		Consultor consultor = new Consultor();
		consultor.setNome(nome);
		consultor.setEmail(email);
		consultor.setSenha("senha-teste");
		return consultores.save(consultor);
	}

	private Servico servico(String nome) {
		Servico servico = new Servico();
		servico.setNome(nome);
		return servicos.save(servico);
	}

	private Cliente cliente(Consultor consultor, String segmento, char nivel, String faturamento) {
		Cliente cliente = new Cliente();
		cliente.setNome("Cliente " + segmento + " " + nivel);
		cliente.setConsultor(consultor);
		cliente.setSegmento(segmento);
		cliente.setNivel(nivel);
		cliente.setFaturamento(new BigDecimal(faturamento));
		return clientes.save(cliente);
	}

	private void contrato(Cliente cliente, Servico servico, LocalDate inicio) {
		Contrato contrato = new Contrato();
		contrato.setCliente(cliente);
		contrato.setServico(servico);
		contrato.setDataInicio(inicio);
		contratos.save(contrato);
	}

	// insight de risco ligado ao cliente, ou da carteira toda se cliente for nulo
	private void insight(Cliente cliente) {
		Insight insight = new Insight();
		insight.setTipo("risco");
		insight.setDescricao("insight de teste");
		insight.setCliente(cliente);
		insights.save(insight);
	}

}
