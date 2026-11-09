package br.senai.datalens.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.senai.datalens.dto.ClienteDTO;
import br.senai.datalens.dto.ConsultorDTO;
import br.senai.datalens.service.CarteiraService;

// Listagens de clientes e consultores
@RestController
public class CarteiraController {

	private final CarteiraService carteira;

	public CarteiraController(CarteiraService carteira) {
		this.carteira = carteira;
	}

	// GET /clientes?consultor=ID (filtro opcional)
	@GetMapping("/clientes")
	public List<ClienteDTO> clientes(@RequestParam(name = "consultor", required = false) Integer idConsultor) {
		return carteira.listarClientes(idConsultor);
	}

	// GET /consultores
	@GetMapping("/consultores")
	public List<ConsultorDTO> consultores() {
		return carteira.listarConsultores();
	}

}
