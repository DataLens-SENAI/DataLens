package br.senai.datalens.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import br.senai.datalens.dto.ContratacoesMesDTO;
import br.senai.datalens.model.Contrato;

// Acesso aos contratos e evolução das contratações
public interface ContratoRepository extends JpaRepository<Contrato, Integer> {

	// contratos com data agrupados pelo mês de início, em ordem cronológica (idConsultor nulo = todos)
	@Query("""
			select new br.senai.datalens.dto.ContratacoesMesDTO(
				extract(year from c.dataInicio), extract(month from c.dataInicio), count(c))
			from Contrato c
			where c.dataInicio is not null
				and (:idConsultor is null or c.cliente.consultor.idConsultor = :idConsultor)
			group by extract(year from c.dataInicio), extract(month from c.dataInicio)
			order by extract(year from c.dataInicio), extract(month from c.dataInicio)
			""")
	List<ContratacoesMesDTO> contarPorMes(Integer idConsultor);

	// total de contratos
	@Query("""
			select count(c) from Contrato c
			where :idConsultor is null or c.cliente.consultor.idConsultor = :idConsultor
			""")
	long contarContratos(Integer idConsultor);

	// contratos sem data de início: ficam fora do gráfico de evolução e são informados ao usuário
	@Query("""
			select count(c) from Contrato c
			where c.dataInicio is null
				and (:idConsultor is null or c.cliente.consultor.idConsultor = :idConsultor)
			""")
	long contarSemData(Integer idConsultor);

}
