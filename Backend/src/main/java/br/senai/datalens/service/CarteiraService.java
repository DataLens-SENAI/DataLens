package br.senai.datalens.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.senai.datalens.dto.ClienteDTO;
import br.senai.datalens.dto.ConsultorDTO;
import br.senai.datalens.repository.ClienteRepository;
import br.senai.datalens.repository.ConsultorRepository;

// Listagens da carteira: clientes e consultores.
// Transação só de leitura porque os DTOs leem contratos e insights das entidades.
@Service
@Transactional(readOnly = true)
public class CarteiraService {

	private final ClienteRepository clientes;
	private final ConsultorRepository consultores;

	public CarteiraService(ClienteRepository clientes, ConsultorRepository consultores) {
		this.clientes = clientes;
		this.consultores = consultores;
	}

	// clientes em ordem alfabética (idConsultor nulo = todos)
	// ponytail: lê contratos/insights cliente a cliente (N+1); trocar por consulta com fetch join se a carteira crescer
	public List<ClienteDTO> listarClientes(Integer idConsultor) {
		return clientes.listar(idConsultor).stream().map(ClienteDTO::de).toList();
	}

	// todos os consultores com seus indicadores de carteira
	public List<ConsultorDTO> listarConsultores() {
		return consultores.findAll().stream().map(ConsultorDTO::de).toList();
	}

}
