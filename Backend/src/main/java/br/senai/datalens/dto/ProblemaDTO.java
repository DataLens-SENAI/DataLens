package br.senai.datalens.dto;

// Um problema encontrado na planilha pelo serviço Python (valorSugerido pode ser nulo)
public record ProblemaDTO(Integer linha, String campo, String valorOriginal, String valorSugerido) {
}
