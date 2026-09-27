package br.senai.datalens.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
public class Consultor {

	private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idConsultor;

	@Column(nullable = false)
	private String nome;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(nullable = false)
	private String senhaHash;

	private String area;

	// atende: 1 consultor -> 0..* clientes
	@OneToMany(mappedBy = "consultor")
	private List<Cliente> carteira = new ArrayList<>();

	// realiza upload: 1 consultor -> 0..* telemetrias
	@OneToMany(mappedBy = "consultor")
	private List<Telemetria> telemetrias = new ArrayList<>();

	public boolean autenticar(String senha) {
		return senha != null && ENCODER.matches(senha, senhaHash);
	}

	public void setSenha(String senha) {
		this.senhaHash = ENCODER.encode(senha);
	}

	public List<Cliente> getCarteira() {
		return carteira;
	}

	public List<Telemetria> getTelemetrias() {
		return telemetrias;
	}

	public Integer getIdConsultor() {
		return idConsultor;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getSenhaHash() {
		return senhaHash;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

}
