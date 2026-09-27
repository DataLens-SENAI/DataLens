package br.senai.datalens.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
public class Servico {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idServico;

	@Column(nullable = false)
	private String nome;

	private String descricao;

	private String categoria;

	@OneToMany(mappedBy = "servico")
	private List<Contrato> contratos = new ArrayList<>();

	// ponytail: carrega todos os contratos; troque por ContratoRepository.countByServico se ficar lento
	public int totalContratacoes() {
		return contratos.size();
	}

	public List<Contrato> getContratos() {
		return contratos;
	}

	public Integer getIdServico() {
		return idServico;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public String getCategoria() {
		return categoria;
	}

	public void setCategoria(String categoria) {
		this.categoria = categoria;
	}

}
