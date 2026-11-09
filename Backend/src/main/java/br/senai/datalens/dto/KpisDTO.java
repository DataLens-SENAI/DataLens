package br.senai.datalens.dto;

// Indicadores do topo do dashboard (faturamentoMedio nulo = sem dados)
public record KpisDTO(long totalClientes, long totalConsultores, Double faturamentoMedio) {
}
