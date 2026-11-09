"""Regras de padronização de valores isolados."""

from decimal import Decimal

import pytest

from app.padronizacao import calcular_nivel, converter_faturamento, padronizar_segmento, padronizar_servicos


@pytest.mark.parametrize("grafia", ["IND.", "Industria", "INDUSTRIA", " indústria ", "Ind"])
def test_variacoes_de_segmento(grafia):
    assert padronizar_segmento(grafia) == ("Indústria", None)


def test_segmento_desconhecido_sem_sugestao():
    assert padronizar_segmento("Mineração espacial") == (None, None)


@pytest.mark.parametrize("valor, esperado", [
    (120000, "120000.00"),
    ("150.000", "150000.00"),
    ("R$ 1.250.000,00", "1250000.00"),
    ("500k", "500000.00"),
    ("1,5 mi", "1500000.00"),
    ("R$500k–R$2M", "1250000.00"),
    ("500 mil a 2 milhões", "1250000.00"),
])
def test_faturamento_valor_ou_faixa(valor, esperado):
    assert converter_faturamento(valor) == Decimal(esperado)


@pytest.mark.parametrize("valor", ["abc", "acima de 2M", "", "10 dolares"])
def test_faturamento_invalido(valor):
    assert converter_faturamento(valor) is None


def test_limites_do_nivel():
    assert calcular_nivel(Decimal("150000")) == "A"
    assert calcular_nivel(Decimal("149999.99")) == "B"
    assert calcular_nivel(Decimal("70000")) == "B"
    assert calcular_nivel(Decimal("69999.99")) == "C"


def test_servicos_separados_padronizados_e_sem_repeticao():
    assert padronizar_servicos("link dedicado; Cloud | nuvem / Backup B2B") == ["Internet dedicada", "Cloud", "Backup B2B"]
