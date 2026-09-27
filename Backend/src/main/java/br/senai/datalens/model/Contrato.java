package br.senai.datalens.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

@Entity
public class Contrato {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idContrato;

	@Column(nullable = false)
	private LocalDate dataInicio;

	// null = contrato sem data de término
	private LocalDate dataFim;

	@Column(precision = 15, scale = 2)
	private BigDecimal valor;

	private String status = "ATIVO";

	@ManyToOne(optional = false)
	@JoinColumn(name = "id_cliente")
	private Cliente cliente;

	// refere-se a: 0..* contratos -> 1 serviço
	@ManyToOne(optional = false)
	@JoinColumn(name = "id_servico")
	private Servico servico;

	// alimenta: 1 contrato -> 0..* insights
	@OneToMany(mappedBy = "contrato")
	private List<Insight> insights = new ArrayList<>();

	public boolean estaAtivo() {
		LocalDate hoje = LocalDate.now();
		return "ATIVO".equalsIgnoreCase(status)
				&& !hoje.isBefore(dataInicio)
				&& (dataFim == null || !hoje.isAfter(dataFim));
	}

	public List<Insight> getInsights() {
		return insights;
	}

	public Integer getIdContrato() {
		return idContrato;
	}

	public LocalDate getDataInicio() {
		return dataInicio;
	}

	public void setDataInicio(LocalDate dataInicio) {
		this.dataInicio = dataInicio;
	}

	public LocalDate getDataFim() {
		return dataFim;
	}

	public void setDataFim(LocalDate dataFim) {
		this.dataFim = dataFim;
	}

	public BigDecimal getValor() {
		return valor;
	}

	public void setValor(BigDecimal valor) {
		this.valor = valor;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public Servico getServico() {
		return servico;
	}

	public void setServico(Servico servico) {
		this.servico = servico;
	}

}
