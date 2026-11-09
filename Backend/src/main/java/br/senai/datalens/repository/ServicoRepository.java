package br.senai.datalens.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.senai.datalens.model.Servico;

// Acesso aos serviços no banco
public interface ServicoRepository extends JpaRepository<Servico, Integer> {
}
