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
public class Insight {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idInsight;

	@Column(nullable = false)
	private String tipo;

	@Column(length = 1000)
	private String descricao;

	private Double valor;

	private LocalDateTime dataGeracao;

	@ManyToOne(optional = false)
	@JoinColumn(name = "id_cliente")
	private Cliente cliente;

	@ManyToOne(optional = false)
	@JoinColumn(name = "id_contrato")
	private Contrato contrato;

	// chamado automaticamente pelo JPA ao salvar
	@PrePersist
	public void gerar() {
		dataGeracao = LocalDateTime.now();
	}

	public Integer getIdInsight() {
		return idInsight;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public Double getValor() {
		return valor;
	}

	public void setValor(Double valor) {
		this.valor = valor;
	}

	public LocalDateTime getDataGeracao() {
		return dataGeracao;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public Contrato getContrato() {
		return contrato;
	}

	public void setContrato(Contrato contrato) {
		this.contrato = contrato;
	}

}
