package br.senai.datalens.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;

@Entity
public class Telemetria {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idTelemetria;

	private LocalDateTime dataUpload;

	@Column(nullable = false)
	private String nomeArquivo;

	private Integer registrosProcessados = 0;

	private String status = "PENDENTE";

	@ManyToOne(optional = false)
	@JoinColumn(name = "id_consultor")
	private Consultor consultor;

	// chamado automaticamente pelo JPA ao salvar
	@PrePersist
	public void registrar() {
		dataUpload = LocalDateTime.now();
	}

	public Integer getIdTelemetria() {
		return idTelemetria;
	}

	public LocalDateTime getDataUpload() {
		return dataUpload;
	}

	public String getNomeArquivo() {
		return nomeArquivo;
	}

	public void setNomeArquivo(String nomeArquivo) {
		this.nomeArquivo = nomeArquivo;
	}

	public Integer getRegistrosProcessados() {
		return registrosProcessados;
	}

	public void setRegistrosProcessados(Integer registrosProcessados) {
		this.registrosProcessados = registrosProcessados;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Consultor getConsultor() {
		return consultor;
	}

	public void setConsultor(Consultor consultor) {
		this.consultor = consultor;
	}

}
