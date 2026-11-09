package br.senai.datalens.dto;

// Um problema encontrado na planilha (linha nula = problema do arquivo inteiro; valorSugerido pode ser nulo)
public record ProblemaDTO(Integer linha, String campo, String mensagem, String valorOriginal, String valorSugerido) {
}
