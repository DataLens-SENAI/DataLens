package br.senai.datalens.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// Uma linha da planilha já validada e padronizada pelo serviço Python.
// PROVISÓRIO: campos sujeitos a mudança quando houver a planilha de exemplo (ver AGENTS.md §9).
public record RegistroPlanilhaDTO(
		Integer linha,
		String emailConsultor,
		String nomeCliente,
		String segmento,
		Character nivel,
		BigDecimal faturamento,
		List<String> servicos,
		LocalDate dataContratacao) {
}
