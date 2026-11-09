package br.senai.datalens.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

// Regras de negócio que ficam nas próprias entidades
class RegrasModelTest {

	// status do cliente: risco vence oportunidade; sem insight = ativo
	@Test
	void statusPriorizaRisco() {
		Cliente cliente = new Cliente();
		assertEquals("ativo", cliente.status());

		cliente.getInsights().add(insight("oportunidade"));
		assertEquals("oportunidade", cliente.status());

		cliente.getInsights().add(insight("RISCO"));
		assertEquals("risco", cliente.status());
	}

	// meta e ticket médio consideram só contratos ativos
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

	// cria um insight só com o tipo
	private static Insight insight(String tipo) {
		Insight insight = new Insight();
		insight.setTipo(tipo);
		return insight;
	}

	// cria um contrato iniciado ontem, sem data de fim
	private static Contrato contrato(String valor, String status) {
		Contrato contrato = new Contrato();
		contrato.setDataInicio(LocalDate.now().minusDays(1));
		contrato.setValor(new BigDecimal(valor));
		contrato.setStatus(status);
		return contrato;
	}

}
