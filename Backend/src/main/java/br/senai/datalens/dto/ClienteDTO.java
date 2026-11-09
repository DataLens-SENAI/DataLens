package br.senai.datalens.dto;

import java.math.BigDecimal;

import br.senai.datalens.model.Cliente;

// Cliente como a API devolve (tela de Análises)
public record ClienteDTO(
		Integer idCliente,
		String nome,
		String segmento,
		Character nivel,
		String consultor,
		int servicos,
		BigDecimal faturamento,
		String status) {

	// monta o DTO a partir da entidade (chamar dentro de transação: lê contratos e insights)
	public static ClienteDTO de(Cliente cliente) {
		return new ClienteDTO(
				cliente.getIdCliente(),
				cliente.getNome(),
				cliente.getSegmento(),
				cliente.getNivel(),
				cliente.getConsultor().getNome(),
				cliente.getContratos().size(),
				cliente.getFaturamento(),
				cliente.status());
	}

}
