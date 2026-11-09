package br.senai.datalens.dto;

import java.time.LocalDateTime;

import br.senai.datalens.model.Telemetria;

// Um upload do histórico de importações
public record TelemetriaDTO(
		Integer idTelemetria,
		String nomeArquivo,
		LocalDateTime dataUpload,
		Integer registrosProcessados,
		String status,
		String consultor) {

	// monta o DTO a partir da entidade
	public static TelemetriaDTO de(Telemetria telemetria) {
		return new TelemetriaDTO(
				telemetria.getIdTelemetria(),
				telemetria.getNomeArquivo(),
				telemetria.getDataUpload(),
				telemetria.getRegistrosProcessados(),
				telemetria.getStatus(),
				telemetria.getConsultor().getNome());
	}

}
