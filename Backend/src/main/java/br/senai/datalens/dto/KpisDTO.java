package br.senai.datalens.dto;

// Indicadores do topo do dashboard.
// faturamentoMedio nulo = sem dados; contratosSemData = contratos fora do gráfico de evolução (planilha sem data)
public record KpisDTO(
		long totalClientes,
		long totalConsultores,
		Double faturamentoMedio,
		long totalContratos,
		long contratosSemData) {
}
