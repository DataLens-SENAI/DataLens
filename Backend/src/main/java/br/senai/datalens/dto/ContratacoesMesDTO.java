package br.senai.datalens.dto;

// Quantos contratos começaram em um mês/ano (gráfico de evolução)
public record ContratacoesMesDTO(Integer ano, Integer mes, Long total) {
}
