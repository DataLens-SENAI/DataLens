package br.senai.datalens.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import br.senai.datalens.model.Insight;

// Acesso aos insights no banco
public interface InsightRepository extends JpaRepository<Insight, Integer> {

	// left join: mantém os insights sem cliente (carteira toda) junto com os dos clientes do consultor;
	// mais recentes primeiro (idConsultor nulo = todos)
	@Query("""
			select i from Insight i
			left join i.cliente c
			where :idConsultor is null or c is null or c.consultor.idConsultor = :idConsultor
			order by i.dataGeracao desc
			""")
	List<Insight> listar(Integer idConsultor);

}
