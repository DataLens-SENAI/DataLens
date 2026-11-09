"""Regras de insights com carteira fictícia e resultado calculado à mão."""

from app.insights import gerar_insights


def cliente(codigo, segmento, nivel, *servicos):
    return {"codigoCliente": codigo, "nomeCliente": f"Cliente {codigo}", "segmento": segmento, "nivel": nivel,
            "faturamento": 200000.0, "servicos": [{"nome": nome, "dataContratacao": None} for nome in servicos]}


def test_gera_as_tres_regras():
    # 3 de 4 clientes na Indústria = 75% (> 40%); Cloud é o mais contratado (3x);
    # nível A: 1, 2 e 4 -> só o 4 não tem Cloud = 33%; o 2 tem um único serviço
    insights = gerar_insights([
        cliente("1", "Indústria", "A", "Cloud", "VOIP"),
        cliente("2", "Indústria", "A", "Cloud"),
        cliente("3", "Indústria", "C", "Cloud"),
        cliente("4", "Comércio", "A", "VOIP", "Firewall"),
    ])

    assert [(i["tipo"], i["codigoCliente"], i["valor"]) for i in insights] == [
        ("oportunidade", "2", 200000.0),
        ("risco", None, 75),
        ("oportunidade", None, 33),
    ]
    assert insights[2]["descricao"] == "33% dos clientes nível A não possuem Cloud."


def test_carteira_equilibrada_sem_nivel_a_nao_gera_insight():
    assert gerar_insights([cliente("1", "Indústria", "B", "Cloud"), cliente("2", "Comércio", "C", "VOIP"),
                           cliente("3", "Serviços", "C", "Cloud")]) == []
