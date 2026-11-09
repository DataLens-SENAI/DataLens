package br.senai.datalens.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;

import br.senai.datalens.dto.ContagemDTO;
import br.senai.datalens.dto.ContratacoesMesDTO;
import br.senai.datalens.model.Cliente;
import br.senai.datalens.model.Consultor;
import br.senai.datalens.model.Contrato;
import br.senai.datalens.model.Servico;

// Consultas do dashboard com dados fictícios e resultado calculado à mão:
//   Ana:   Indústria/A/200 mil, Indústria/B/100 mil, Comércio/C/30 mil
//   Bruno: Serviços/A/170 mil
//   Contratos: jan/2026 x3 (2 da Ana, 1 do Bruno), mar/2026 x1 (Ana)
@DataJpaTest
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

	private Consultor ana;

	// monta a carteira fictícia descrita acima
	@BeforeEach
	void popularBanco() {
		ana = consultor("Ana", "ana@exemplo.com");
		Consultor bruno = consultor("Bruno", "bruno@exemplo.com");
		Servico cloud = servico("Cloud");

		Cliente metalurgica = cliente(ana, "Indústria", 'A', "200000");
		Cliente fabrica = cliente(ana, "Indústria", 'B', "100000");
		cliente(ana, "Comércio", 'C', "30000");
		Cliente consultoria = cliente(bruno, "Serviços", 'A', "170000");

		contrato(metalurgica, cloud, LocalDate.of(2026, 1, 10));
		contrato(metalurgica, cloud, LocalDate.of(2026, 1, 20));
		contrato(fabrica, cloud, LocalDate.of(2026, 3, 5));
		contrato(consultoria, cloud, LocalDate.of(2026, 1, 15));
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

}
