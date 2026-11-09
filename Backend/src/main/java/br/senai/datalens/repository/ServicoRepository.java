package br.senai.datalens.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import br.senai.datalens.dto.ContagemDTO;
import br.senai.datalens.model.Servico;

// Acesso aos serviços no banco
public interface ServicoRepository extends JpaRepository<Servico, Integer> {

	// busca o serviço pelo nome padronizado (usado na importação)
	Optional<Servico> findByNomeIgnoreCase(String nome);

	// serviços mais contratados, do maior para o menor (idConsultor nulo = carteira inteira)
	@Query("""
			select new br.senai.datalens.dto.ContagemDTO(c.servico.nome, count(c))
			from Contrato c
			where :idConsultor is null or c.cliente.consultor.idConsultor = :idConsultor
			group by c.servico.nome
			order by count(c) desc
			""")
	List<ContagemDTO> contarContratosPorServico(Integer idConsultor);

}
