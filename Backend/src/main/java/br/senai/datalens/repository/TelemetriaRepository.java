package br.senai.datalens.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.senai.datalens.model.Telemetria;

// Acesso ao histórico de uploads no banco
public interface TelemetriaRepository extends JpaRepository<Telemetria, Integer> {

	// últimos 20 uploads, mais recentes primeiro
	List<Telemetria> findTop20ByOrderByDataUploadDesc();

}
