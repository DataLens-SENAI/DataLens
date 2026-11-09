package br.senai.datalens.dto;

import java.time.LocalDateTime;

import br.senai.datalens.model.Insight;

// Insight como a API devolve (cards do dashboard)
public record InsightDTO(
		Integer idInsight,
		String tipo,
		String descricao,
		String tag,
		Double valor,
		LocalDateTime dataGeracao) {

	// monta o DTO a partir da entidade
	public static InsightDTO de(Insight insight) {
		return new InsightDTO(
				insight.getIdInsight(),
				insight.getTipo(),
				insight.getDescricao(),
				insight.getTag(),
				insight.getValor(),
				insight.getDataGeracao());
	}

}
