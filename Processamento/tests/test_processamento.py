"""Validação da planilha de ponta a ponta, com planilhas fictícias montadas no próprio teste."""

import io

import pandas as pd
from fastapi.testclient import TestClient

from app.main import app
from app.processamento import processar

CABECALHO = ["Consultor Responsável", "Cliente CTI", "Nome", "Segmento", "Faixa de Faturamento", "Serviços Contratados"]


def xlsx(linhas, cabecalho=CABECALHO) -> bytes:
    """Monta um .xlsx em memória com as linhas informadas."""
    arquivo = io.BytesIO()
    pd.DataFrame(linhas, columns=cabecalho).to_excel(arquivo, index=False)
    return arquivo.getvalue()


def test_planilha_valida_e_padronizada():
    resultado = processar(xlsx([
        ["Ana Souza", 1001, "  Metalúrgica   Andrade ", "IND.", "R$500k–R$2M", "Link dedicado, VOIP"],
        ["Ana Souza", 1002, "Loja Bom Preço", "comercio", "80.000", "Cloud"],
    ]), "carteira.xlsx")

    assert resultado["valido"] is True
    assert resultado["registrosLidos"] == 2
    metalurgica, loja = resultado["registros"]
    assert metalurgica["codigoCliente"] == "1001"
    assert metalurgica["nomeCliente"] == "Metalúrgica Andrade"
    assert metalurgica["segmento"] == "Indústria"
    assert metalurgica["faturamento"] == 1250000.0  # ponto médio da faixa
    assert metalurgica["nivel"] == "A"
    assert [s["nome"] for s in metalurgica["servicos"]] == ["Internet dedicada", "Telefonia IP"]
    assert loja["segmento"] == "Comércio"
    assert loja["nivel"] == "B"


def test_coluna_faltando_invalida_o_arquivo():
    resultado = processar(xlsx([["Ana", 1, "IND", "Cloud"]], ["Consultor", "Cliente", "Segmento", "Serviços"]), "a.xlsx")

    assert resultado["valido"] is False
    assert resultado["registros"] == []
    assert resultado["problemas"] == [{"linha": None, "campo": "faturamento", "mensagem": "Coluna obrigatória ausente.",
                                       "valorOriginal": None, "valorSugerido": None}]


def test_acumula_problemas_de_todas_as_linhas_sem_inventar_dados():
    resultado = processar(xlsx([
        ["Ana", 1, "A", "Indstria", "100000", "Cloud"],   # linha 2: segmento com erro de digitação
        ["", 2, "B", "Serviços", "abc", "Cloud"],          # linha 3: consultor vazio e faturamento inválido
        ["Ana", 3, "C", "Serviços", "90000", None],        # linha 4: serviços vazio
    ]), "a.xlsx")

    assert resultado["valido"] is False
    problemas = {(p["linha"], p["campo"]): p for p in resultado["problemas"]}
    assert set(problemas) == {(2, "segmento"), (3, "consultor"), (3, "faturamento"), (4, "servicos")}
    assert problemas[(2, "segmento")]["valorOriginal"] == "Indstria"
    assert problemas[(2, "segmento")]["valorSugerido"] == "Indústria"


def test_um_servico_por_linha_vira_um_cliente():
    resultado = processar(xlsx([
        ["Ana", 7, "X", "Agro", "200000", "Cloud"],
        ["Ana", 7, "X", "Agro", "200000", "Firewall"],
    ]), "a.xlsx")

    assert resultado["valido"] is True
    assert len(resultado["registros"]) == 1
    assert [s["nome"] for s in resultado["registros"][0]["servicos"]] == ["Cloud", "Segurança de rede"]


def test_mesmo_cliente_com_dados_divergentes_e_problema():
    resultado = processar(xlsx([
        ["Ana", 7, "X", "Agro", "200000", "Cloud"],
        ["Bruno", 7, "X", "Agro", "200000", "VOIP"],
    ]), "a.xlsx")

    assert resultado["valido"] is False
    assert resultado["problemas"][0]["linha"] == 3
    assert resultado["problemas"][0]["campo"] == "consultor"


def test_csv_com_ponto_e_virgula_e_data():
    csv = "Consultor;Cliente;Segmento;Faturamento;Serviços;Data de contratação\nAna;9;Saude;50000;Cloud;15/03/2026\n"

    resultado = processar(csv.encode(), "a.csv")

    assert resultado["valido"] is True
    registro = resultado["registros"][0]
    assert registro["nivel"] == "C"
    assert registro["servicos"] == [{"nome": "Cloud", "dataContratacao": "2026-03-15"}]


def test_formato_nao_suportado():
    resultado = processar(b"qualquer coisa", "a.pdf")

    assert resultado["valido"] is False
    assert resultado["problemas"][0]["campo"] == "arquivo"


def test_arquivo_corrompido_vira_problema():
    resultado = processar(b"nao sou um xlsx", "a.xlsx")

    assert resultado["valido"] is False
    assert "corrompido" in resultado["problemas"][0]["mensagem"]


def test_endpoint_recebe_multipart():
    cliente = TestClient(app)

    resposta = cliente.post("/processar", files={"arquivo": ("a.xlsx", xlsx([["Ana", 1, "A", "TI", "10000", "Cloud"]]))})

    assert resposta.status_code == 200
    assert resposta.json()["registros"][0]["segmento"] == "Tecnologia"
