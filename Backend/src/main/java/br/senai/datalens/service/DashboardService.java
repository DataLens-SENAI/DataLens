package br.senai.datalens.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.senai.datalens.dto.DashboardDTO;
import br.senai.datalens.dto.InsightDTO;
import br.senai.datalens.dto.KpisDTO;
import br.senai.datalens.repository.ClienteRepository;
import br.senai.datalens.repository.ConsultorRepository;
import br.senai.datalens.repository.ContratoRepository;
import br.senai.datalens.repository.InsightRepository;
import br.senai.datalens.repository.ServicoRepository;

// Monta o dashboard; as contas são feitas no banco (GROUP BY). idConsultor nulo = carteira inteira.
@Service
public class DashboardService {

	private final ClienteRepository clientes;
	private final ConsultorRepository consultores;
	private final ContratoRepository contratos;
	private final ServicoRepository servicos;
	private final InsightRepository insights;

	public DashboardService(ClienteRepository clientes, ConsultorRepository consultores, ContratoRepository contratos,
			ServicoRepository servicos, InsightRepository insights) {
		this.clientes = clientes;
		this.consultores = consultores;
		this.contratos = contratos;
		this.servicos = servicos;
		this.insights = insights;
	}

	// junta KPIs, gráficos e insights
	@Transactional(readOnly = true)
	public DashboardDTO montar(Integer idConsultor) {
		return new DashboardDTO(
				kpis(idConsultor),
				clientes.contarPorSegmento(idConsultor),
				clientes.contarPorNivel(idConsultor),
				contratos.contarPorMes(idConsultor),
				servicos.contarContratosPorServico(idConsultor),
				insights.listar(idConsultor).stream().map(InsightDTO::de).toList());
	}

	// indicadores do topo; com filtro, o total de consultores é 1 (ou 0 se o id não existir)
	private KpisDTO kpis(Integer idConsultor) {
		long totalConsultores = idConsultor == null ? consultores.count() : (consultores.existsById(idConsultor) ? 1 : 0);
		return new KpisDTO(
				clientes.contarClientes(idConsultor),
				totalConsultores,
				clientes.faturamentoMedio(idConsultor),
				contratos.contarContratos(idConsultor),
				contratos.contarSemData(idConsultor));
	}

}
