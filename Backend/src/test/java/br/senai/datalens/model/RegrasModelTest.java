package br.senai.datalens.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class RegrasModelTest {

	@Test
	void nivelSegueFaturamento() {
		Cliente cliente = new Cliente();
		cliente.classificar();
		assertNull(cliente.getNivel());

		cliente.setFaturamento(new BigDecimal("150000"));
		cliente.classificar();
		assertEquals('A', cliente.getNivel());

		cliente.setFaturamento(new BigDecimal("70000"));
		cliente.classificar();
		assertEquals('B', cliente.getNivel());

		cliente.setFaturamento(new BigDecimal("69999.99"));
		cliente.classificar();
		assertEquals('C', cliente.getNivel());
	}

	@Test
	void statusPriorizaRisco() {
		Cliente cliente = new Cliente();
		assertEquals("ativo", cliente.status());

		cliente.getInsights().add(insight("oportunidade"));
		assertEquals("oportunidade", cliente.status());

		cliente.getInsights().add(insight("RISCO"));
		assertEquals("risco", cliente.status());
	}

	@Test
	void situacaoMetaUsaContratosAtivos() {
		Consultor consultor = new Consultor();
		Cliente cliente = new Cliente();
		consultor.getCarteira().add(cliente);
		cliente.getContratos().add(contrato("600", "ATIVO"));
		cliente.getContratos().add(contrato("400", "ATIVO"));
		cliente.getContratos().add(contrato("9999", "CANCELADO"));

		assertEquals(1, consultor.clientesAtivos());
		assertEquals(new BigDecimal("500.00"), consultor.ticketMedio());

		consultor.setMeta(new BigDecimal("900"));
		assertEquals("acima", consultor.situacaoMeta());
		consultor.setMeta(new BigDecimal("1000"));
		assertEquals("na-meta", consultor.situacaoMeta());
		consultor.setMeta(new BigDecimal("1200"));
		assertEquals("atencao", consultor.situacaoMeta());
	}

	private static Insight insight(String tipo) {
		Insight insight = new Insight();
		insight.setTipo(tipo);
		return insight;
	}

	private static Contrato contrato(String valor, String status) {
		Contrato contrato = new Contrato();
		contrato.setDataInicio(LocalDate.now().minusDays(1));
		contrato.setValor(new BigDecimal(valor));
		contrato.setStatus(status);
		return contrato;
	}

}
