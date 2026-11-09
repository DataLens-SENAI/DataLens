package br.senai.datalens.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.senai.datalens.model.Telemetria;

// Acesso ao histórico de uploads no banco
public interface TelemetriaRepository extends JpaRepository<Telemetria, Integer> {
}
