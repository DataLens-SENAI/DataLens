package br.senai.datalens.dto;

// Um item de gráfico: rótulo (ex: segmento, nível) e quantos registros tem
public record ContagemDTO(String rotulo, Long total) {
}
