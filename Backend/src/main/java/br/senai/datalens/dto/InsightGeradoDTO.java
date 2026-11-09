package br.senai.datalens.dto;

// Insight calculado pelo serviço Python (codigoCliente nulo = insight da carteira toda)
public record InsightGeradoDTO(String tipo, String tag, String descricao, Double valor, String codigoCliente) {
}
