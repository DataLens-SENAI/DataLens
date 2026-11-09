package br.senai.datalens.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "consultores")
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

	// meta de receita em contratos ativos (mesma unidade de Contrato.valor)
	@Column(precision = 15, scale = 2)
	private BigDecimal meta;

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

	public long clientesAtivos() {
		return carteira.stream()
				.filter(cliente -> cliente.getContratos().stream().anyMatch(Contrato::estaAtivo))
				.count();
	}

	public BigDecimal ticketMedio() {
		List<BigDecimal> valores = valoresAtivos();
		if (valores.isEmpty()) return BigDecimal.ZERO;
		return soma(valores).divide(BigDecimal.valueOf(valores.size()), 2, RoundingMode.HALF_UP);
	}

	// "acima" | "na-meta" | "atencao"
	// ponytail: faixas fixas (>= 110% acima, >= 90% na meta); vira configuração se o negócio pedir
	public String situacaoMeta() {
		if (meta == null || meta.signum() == 0) return "na-meta";
		BigDecimal realizado = soma(valoresAtivos());
		if (realizado.compareTo(meta.multiply(new BigDecimal("1.1"))) >= 0) return "acima";
		if (realizado.compareTo(meta.multiply(new BigDecimal("0.9"))) >= 0) return "na-meta";
		return "atencao";
	}

	// ponytail: percorre a carteira em memória; troque por consulta agregada no repository se ficar lento
	private List<BigDecimal> valoresAtivos() {
		return carteira.stream()
				.flatMap(cliente -> cliente.getContratos().stream())
				.filter(Contrato::estaAtivo)
				.map(Contrato::getValor)
				.filter(Objects::nonNull)
				.toList();
	}

	private static BigDecimal soma(List<BigDecimal> valores) {
		return valores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
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

	public BigDecimal getMeta() {
		return meta;
	}

	public void setMeta(BigDecimal meta) {
		this.meta = meta;
	}

}
