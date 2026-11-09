"""Insights gerados a partir dos clientes já validados de uma planilha.

Regras (PROVISÓRIAS, ver AGENTS.md §9 item 7):
1. Cliente nível A com um único serviço -> oportunidade de cross-sell (ligado ao cliente).
2. Segmento com mais de 40% dos clientes -> risco de concentração (insight da carteira).
3. Clientes nível A sem o serviço mais contratado -> oportunidade de expansão (insight da carteira).
"""

from collections import Counter

LIMITE_CONCENTRACAO = 40  # % de clientes em um único segmento


def gerar_insights(clientes: list[dict]) -> list[dict]:
    """Aplica as três regras e devolve a lista de insights."""
    return [*_cross_sell(clientes), *_concentracao(clientes), *_nivel_a_sem_servico_principal(clientes)]


def _cross_sell(clientes: list[dict]) -> list[dict]:
    """Regra 1: um insight por cliente nível A que contrata só um serviço."""
    return [_insight("oportunidade", "Oportunidade Cross-sell",
                     f"{cliente['nomeCliente']} (nível A) contrata apenas {cliente['servicos'][0]['nome']}.",
                     cliente["faturamento"], cliente["codigoCliente"])
            for cliente in clientes if cliente["nivel"] == "A" and len(cliente["servicos"]) == 1]


def _concentracao(clientes: list[dict]) -> list[dict]:
    """Regra 2: avisa se um segmento concentra mais clientes que o limite."""
    segmento, quantidade = Counter(cliente["segmento"] for cliente in clientes).most_common(1)[0]
    percentual = _percentual(quantidade, len(clientes))
    if percentual <= LIMITE_CONCENTRACAO:
        return []
    return [_insight("risco", "Concentração de Carteira",
                     f"{percentual}% dos clientes estão no segmento {segmento}.", percentual)]


def _nivel_a_sem_servico_principal(clientes: list[dict]) -> list[dict]:
    """Regra 3: % dos clientes nível A que não têm o serviço mais contratado da planilha."""
    nivel_a = [cliente for cliente in clientes if cliente["nivel"] == "A"]
    if not nivel_a:
        return []
    principal = Counter(servico["nome"] for cliente in clientes for servico in cliente["servicos"]).most_common(1)[0][0]
    sem_principal = [cliente for cliente in nivel_a if principal not in {s["nome"] for s in cliente["servicos"]}]
    if not sem_principal:
        return []
    percentual = _percentual(len(sem_principal), len(nivel_a))
    return [_insight("oportunidade", "Oportunidade de Expansão",
                     f"{percentual}% dos clientes nível A não possuem {principal}.", percentual)]


def _percentual(parte: int, total: int) -> int:
    """Percentual arredondado para inteiro."""
    return round(parte * 100 / total)


def _insight(tipo, tag, descricao, valor, codigo_cliente=None) -> dict:
    """Um insight no formato do contrato com o Java (codigoCliente None = insight da carteira toda)."""
    return {"tipo": tipo, "tag": tag, "descricao": descricao, "valor": valor, "codigoCliente": codigo_cliente}
