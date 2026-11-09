package br.senai.datalens.dto;

import java.math.BigDecimal;

import br.senai.datalens.model.Consultor;

// Consultor como a API devolve (nunca inclui senha nem hash)
public record ConsultorDTO(
		Integer idConsultor,
		String nome,
		String email,
		String area,
		long clientesAtivos,
		BigDecimal ticketMedio,
		String situacaoMeta) {

	// monta o DTO a partir da entidade (chamar dentro de transação: lê a carteira)
	public static ConsultorDTO de(Consultor consultor) {
		return new ConsultorDTO(
				consultor.getIdConsultor(),
				consultor.getNome(),
				consultor.getEmail(),
				consultor.getArea(),
				consultor.clientesAtivos(),
				consultor.ticketMedio(),
				consultor.situacaoMeta());
	}

}
