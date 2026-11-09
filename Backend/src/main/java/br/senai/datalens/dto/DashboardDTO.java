package br.senai.datalens.dto;

import java.util.List;

// Tudo que a tela de Dashboard precisa em uma única resposta
public record DashboardDTO(
		KpisDTO kpis,
		List<ContagemDTO> clientesPorSegmento,
		List<ContagemDTO> clientesPorNivel,
		List<ContratacoesMesDTO> contratacoesPorMes,
		List<ContagemDTO> servicosMaisContratados,
		List<InsightDTO> insights) {
}
