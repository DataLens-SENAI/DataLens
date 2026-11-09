package br.senai.datalens.config;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

// Segurança da API: login livre, todo o resto exige token JWT no header Authorization: Bearer <token>
@Configuration
public class SegurancaConfig {

	private final SecretKey chave;

	// monta a chave HMAC a partir do segredo; falha logo na subida se o segredo for curto demais
	public SegurancaConfig(@Value("${jwt.segredo}") String segredo) {
		byte[] bytes = segredo.getBytes(StandardCharsets.UTF_8);
		if (bytes.length < 32) {
			throw new IllegalStateException("JWT_SEGREDO precisa ter pelo menos 32 caracteres.");
		}
		this.chave = new SecretKeySpec(bytes, "HmacSHA256");
	}

	// regras de acesso: API sem sessão (stateless), sem CSRF (não usa cookie), só /auth/login é público
	@Bean
	SecurityFilterChain regrasDeAcesso(HttpSecurity http) throws Exception {
		return http
				.csrf(csrf -> csrf.disable())
				.sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(rotas -> rotas
						.requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
						.requestMatchers("/error").permitAll()
						.anyRequest().authenticated())
				.oauth2ResourceServer(recurso -> recurso.jwt(jwt -> {}))
				.build();
	}

	// assina os tokens gerados no login
	@Bean
	public JwtEncoder jwtEncoder() {
		return NimbusJwtEncoder.withSecretKey(chave).build();
	}

	// confere a assinatura e a validade dos tokens recebidos
	@Bean
	public JwtDecoder jwtDecoder() {
		return NimbusJwtDecoder.withSecretKey(chave).macAlgorithm(MacAlgorithm.HS256).build();
	}

}
