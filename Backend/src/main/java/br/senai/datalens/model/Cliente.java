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
public class Cliente {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idCliente;

	@Column(nullable = false)
	private String nome;

	private String segmento;

	@Column(length = 1)
	private Character nivel;

	@Column(precision = 15, scale = 2)
	private BigDecimal faturamento;

	private LocalDate dataCadastro = LocalDate.now();

	@ManyToOne(optional = false)
	@JoinColumn(name = "id_consultor")
	private Consultor consultor;

	// firma: 1 cliente -> 0..* contratos
	@OneToMany(mappedBy = "cliente")
	private List<Contrato> contratos = new ArrayList<>();

	// origina: 1 cliente -> 0..* insights
	@OneToMany(mappedBy = "cliente")
	private List<Insight> insights = new ArrayList<>();

	public List<Contrato> getContratos() {
		return contratos;
	}

	public List<Insight> getInsights() {
		return insights;
	}

	public Integer getIdCliente() {
		return idCliente;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getSegmento() {
		return segmento;
	}

	public void setSegmento(String segmento) {
		this.segmento = segmento;
	}

	public Character getNivel() {
		return nivel;
	}

	public void setNivel(Character nivel) {
		this.nivel = nivel;
	}

	public BigDecimal getFaturamento() {
		return faturamento;
	}

	public void setFaturamento(BigDecimal faturamento) {
		this.faturamento = faturamento;
	}

	public LocalDate getDataCadastro() {
		return dataCadastro;
	}

	public void setDataCadastro(LocalDate dataCadastro) {
		this.dataCadastro = dataCadastro;
	}

	public Consultor getConsultor() {
		return consultor;
	}

	public void setConsultor(Consultor consultor) {
		this.consultor = consultor;
	}

}
