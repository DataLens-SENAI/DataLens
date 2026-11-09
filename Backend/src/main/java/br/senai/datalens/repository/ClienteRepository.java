package br.senai.datalens.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import br.senai.datalens.dto.ContagemDTO;
import br.senai.datalens.model.Cliente;

// Acesso aos clientes e agregações do dashboard.
// Em todas as consultas, idConsultor nulo = carteira inteira.
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

	// busca o cliente pelo código da planilha (usado na importação)
	Optional<Cliente> findByCodigoInterno(String codigoInterno);

	// clientes em ordem alfabética (tela de Análises)
	@Query("""
			select c from Cliente c
			where :idConsultor is null or c.consultor.idConsultor = :idConsultor
			order by c.nome
			""")
	List<Cliente> listar(Integer idConsultor);

	// quantidade de clientes por segmento, do maior para o menor
	@Query("""
			select new br.senai.datalens.dto.ContagemDTO(c.segmento, count(c))
			from Cliente c
			where :idConsultor is null or c.consultor.idConsultor = :idConsultor
			group by c.segmento
			order by count(c) desc
			""")
	List<ContagemDTO> contarPorSegmento(Integer idConsultor);

	// quantidade de clientes por nível (A, B, C)
	@Query("""
			select new br.senai.datalens.dto.ContagemDTO(cast(c.nivel as String), count(c))
			from Cliente c
			where :idConsultor is null or c.consultor.idConsultor = :idConsultor
			group by c.nivel
			order by c.nivel
			""")
	List<ContagemDTO> contarPorNivel(Integer idConsultor);

	// total de clientes
	@Query("""
			select count(c)
			from Cliente c
			where :idConsultor is null or c.consultor.idConsultor = :idConsultor
			""")
	long contarClientes(Integer idConsultor);

	// faturamento médio (nulo se não houver clientes com faturamento)
	@Query("""
			select avg(c.faturamento)
			from Cliente c
			where :idConsultor is null or c.consultor.idConsultor = :idConsultor
			""")
	Double faturamentoMedio(Integer idConsultor);

}
