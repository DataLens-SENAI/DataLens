package br.senai.datalens.dto;

import java.util.List;

// Resposta do upload: status da importação, quantos clientes foram gravados e os problemas (se houver)
public record ImportacaoDTO(Integer idTelemetria, String status, int registrosProcessados, List<ProblemaDTO> problemas) {
}
