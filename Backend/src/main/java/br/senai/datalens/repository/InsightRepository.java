package br.senai.datalens.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.senai.datalens.model.Insight;

// Acesso aos insights no banco
public interface InsightRepository extends JpaRepository<Insight, Integer> {
}
