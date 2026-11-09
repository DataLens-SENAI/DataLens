package br.senai.datalens.dto;

import java.time.LocalDate;

// Um serviço contratado por um cliente da planilha (dataContratacao nula = planilha não informou)
public record ServicoContratadoDTO(String nome, LocalDate dataContratacao) {
}
