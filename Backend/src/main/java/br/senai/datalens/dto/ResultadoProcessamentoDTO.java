package br.senai.datalens.dto;

import java.util.List;

// Contrato Java <-> Python: resposta do serviço Python ao processar uma planilha.
// valido = true  -> registros prontos para gravar, problemas vazio
// valido = false -> nada é gravado; problemas lista cada erro encontrado
public record ResultadoProcessamentoDTO(
		boolean valido,
		List<RegistroPlanilhaDTO> registros,
		List<ProblemaDTO> problemas) {
}
