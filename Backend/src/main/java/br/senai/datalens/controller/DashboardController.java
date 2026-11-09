package br.senai.datalens.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.senai.datalens.dto.DashboardDTO;
import br.senai.datalens.service.DashboardService;

// Dados da tela de Dashboard
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

	private final DashboardService dashboard;

	public DashboardController(DashboardService dashboard) {
		this.dashboard = dashboard;
	}

	// GET /dashboard?consultor=ID (filtro opcional)
	@GetMapping
	public DashboardDTO montar(@RequestParam(name = "consultor", required = false) Integer idConsultor) {
		return dashboard.montar(idConsultor);
	}

}
