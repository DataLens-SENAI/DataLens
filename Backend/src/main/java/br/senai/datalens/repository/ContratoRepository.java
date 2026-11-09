package br.senai.datalens.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import br.senai.datalens.dto.ContratacoesMesDTO;
import br.senai.datalens.model.Contrato;

// Acesso aos contratos e evolução das contratações
public interface ContratoRepository extends JpaRepository<Contrato, Integer> {

	// contratos agrupados pelo mês de início, em ordem cronológica (idConsultor nulo = todos)
	@Query("""
			select new br.senai.datalens.dto.ContratacoesMesDTO(
				extract(year from c.dataInicio), extract(month from c.dataInicio), count(c))
			from Contrato c
			where :idConsultor is null or c.cliente.consultor.idConsultor = :idConsultor
			group by extract(year from c.dataInicio), extract(month from c.dataInicio)
			order by extract(year from c.dataInicio), extract(month from c.dataInicio)
			""")
	List<ContratacoesMesDTO> contarPorMes(Integer idConsultor);

}
