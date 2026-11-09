package br.senai.datalens.dto;

import java.math.BigDecimal;
import java.util.List;

// Um cliente da planilha já validado e padronizado pelo serviço Python.
// consultor = nome ou email do consultor responsável, como veio na planilha.
public record RegistroPlanilhaDTO(
		Integer linha,
		String consultor,
		String codigoCliente,
		String nomeCliente,
		String segmento,
		Character nivel,
		BigDecimal faturamento,
		List<ServicoContratadoDTO> servicos) {
}
