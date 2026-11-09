package br.senai.datalens.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.senai.datalens.model.Consultor;

// Acesso aos consultores no banco
public interface ConsultorRepository extends JpaRepository<Consultor, Integer> {

	// busca o consultor pelo email (usado no login)
	Optional<Consultor> findByEmail(String email);

	// acha o consultor responsável da planilha, que pode vir pelo nome ou pelo email
	Optional<Consultor> findFirstByNomeIgnoreCaseOrEmailIgnoreCase(String nome, String email);

}
