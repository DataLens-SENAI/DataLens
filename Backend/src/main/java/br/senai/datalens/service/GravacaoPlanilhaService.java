package br.senai.datalens.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.senai.datalens.dto.InsightGeradoDTO;
import br.senai.datalens.dto.RegistroPlanilhaDTO;
import br.senai.datalens.dto.ServicoContratadoDTO;
import br.senai.datalens.model.Cliente;
import br.senai.datalens.model.Consultor;
import br.senai.datalens.model.Contrato;
import br.senai.datalens.model.Insight;
import br.senai.datalens.model.Servico;
import br.senai.datalens.repository.ClienteRepository;
import br.senai.datalens.repository.ContratoRepository;
import br.senai.datalens.repository.InsightRepository;
import br.senai.datalens.repository.ServicoRepository;

// Grava no banco o resultado já validado de uma planilha. Não valida nem calcula nada: isso é do Python.
@Service
public class GravacaoPlanilhaService {

	private final ClienteRepository clientes;
	private final ServicoRepository servicos;
	private final ContratoRepository contratos;
	private final InsightRepository insights;

	public GravacaoPlanilhaService(ClienteRepository clientes, ServicoRepository servicos,
			ContratoRepository contratos, InsightRepository insights) {
		this.clientes = clientes;
		this.servicos = servicos;
		this.contratos = contratos;
		this.insights = insights;
	}

	// grava clientes, contratos e insights numa única transação: se algo falhar, nada fica gravado
	@Transactional
	public void gravar(List<RegistroPlanilhaDTO> registros, List<InsightGeradoDTO> gerados,
			Map<String, Consultor> consultoresDaPlanilha) {
		Map<String, Cliente> clientesPorCodigo = new HashMap<>();
		for (RegistroPlanilhaDTO registro : registros) {
			Cliente cliente = gravarCliente(registro, consultoresDaPlanilha.get(registro.consultor()));
			registro.servicos().forEach(servico -> contratarSeNovo(cliente, servico));
			clientesPorCodigo.put(registro.codigoCliente(), cliente);
		}
		substituirInsights(gerados, clientesPorCodigo);
	}

	// cria o cliente ou atualiza o que já existe com o mesmo código (reenvio não duplica)
	private Cliente gravarCliente(RegistroPlanilhaDTO registro, Consultor consultor) {
		Cliente cliente = clientes.findByCodigoInterno(registro.codigoCliente()).orElseGet(Cliente::new);
		cliente.setCodigoInterno(registro.codigoCliente());
		cliente.setNome(registro.nomeCliente());
		cliente.setSegmento(registro.segmento());
		cliente.setNivel(registro.nivel());
		cliente.setFaturamento(registro.faturamento());
		cliente.setConsultor(consultor);
		return clientes.save(cliente);
	}

	// cria o contrato só se o cliente ainda não tem esse serviço
	private void contratarSeNovo(Cliente cliente, ServicoContratadoDTO contratado) {
		Servico servico = buscarOuCriarServico(contratado.nome());
		boolean jaContratado = cliente.getContratos().stream()
				.anyMatch(contrato -> contrato.getServico().getIdServico().equals(servico.getIdServico()));
		if (jaContratado) {
			return;
		}
		Contrato contrato = new Contrato();
		contrato.setCliente(cliente);
		contrato.setServico(servico);
		contrato.setDataInicio(contratado.dataContratacao());
		cliente.getContratos().add(contratos.save(contrato));
	}

	// serviço novo na planilha é cadastrado automaticamente
	private Servico buscarOuCriarServico(String nome) {
		return servicos.findByNomeIgnoreCase(nome).orElseGet(() -> {
			Servico novo = new Servico();
			novo.setNome(nome);
			return servicos.save(novo);
		});
	}

	// ponytail: os insights do último upload substituem todos os anteriores; acumular por upload se a equipe pedir
	private void substituirInsights(List<InsightGeradoDTO> gerados, Map<String, Cliente> clientesPorCodigo) {
		insights.deleteAllInBatch();
		for (InsightGeradoDTO gerado : gerados) {
			Insight insight = new Insight();
			insight.setTipo(gerado.tipo());
			insight.setTag(gerado.tag());
			insight.setDescricao(gerado.descricao());
			insight.setValor(gerado.valor());
			insight.setCliente(gerado.codigoCliente() == null ? null : clientesPorCodigo.get(gerado.codigoCliente()));
			insights.save(insight);
		}
	}

}
