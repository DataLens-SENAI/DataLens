"""Leitura e validação da planilha: transforma o arquivo enviado no resultado que o Java grava.

Formato esperado (PROVISÓRIO até a planilha de exemplo, ver AGENTS.md §9):
- uma linha por cliente com vários serviços na mesma célula, ou uma linha por serviço repetindo o cliente;
- cabeçalho na primeira linha; nomes comparados sem acento e sem caixa (aceita as variações de COLUNAS).
"""

import io
from datetime import date, datetime

import pandas as pd

from app.insights import gerar_insights
from app.padronizacao import (calcular_nivel, chave, converter_faturamento, limpar_texto, padronizar_segmento,
                              padronizar_servicos)

EXTENSOES = ("xlsx", "xls", "csv")

# campo -> nomes de coluna aceitos no cabeçalho
COLUNAS = {
    "consultor": ["CONSULTOR", "CONSULTOR RESPONSAVEL"],
    "cliente": ["CLIENTE", "CLIENTE CTI", "CODIGO", "CODIGO INTERNO", "CODIGO CLIENTE"],
    "nome": ["NOME", "NOME CLIENTE", "NOME DO CLIENTE", "RAZAO SOCIAL"],
    "segmento": ["SEGMENTO", "SEGMENTO DE ATUACAO"],
    "faturamento": ["FATURAMENTO", "FATURAMENTO ANUAL", "FAIXA DE FATURAMENTO", "FAIXA DE FATURAMENTO ANUAL"],
    "servicos": ["SERVICOS", "SERVICOS CONTRATADOS", "SERVICO"],
    "data_contratacao": ["DATA CONTRATACAO", "DATA DE CONTRATACAO", "DATA"],
}
OBRIGATORIAS = ["consultor", "cliente", "segmento", "faturamento", "servicos"]

# o cabeçalho é a linha 1 da planilha; os dados começam na linha 2
PRIMEIRA_LINHA_DE_DADOS = 2


def processar(conteudo: bytes, nome_arquivo: str) -> dict:
    """Ponto de entrada: lê, valida, padroniza e gera insights. Nunca lança erro de dado: tudo vira problema."""
    extensao = nome_arquivo.rsplit(".", 1)[-1].lower() if "." in nome_arquivo else ""
    if extensao not in EXTENSOES:
        return _invalido(0, [_problema(None, "arquivo", "Formato não suportado. Envie .xlsx, .xls ou .csv.", nome_arquivo)])

    try:
        tabela = _ler(conteudo, extensao)
    except Exception:
        return _invalido(0, [_problema(None, "arquivo", "Não foi possível ler o arquivo. Verifique se ele não está corrompido.")])

    colunas = _mapear_colunas(tabela.columns)
    faltando = [campo for campo in OBRIGATORIAS if campo not in colunas]
    if faltando:
        return _invalido(len(tabela), [_problema(None, campo, "Coluna obrigatória ausente.") for campo in faltando])
    if tabela.empty:
        return _invalido(0, [_problema(None, "arquivo", "A planilha não tem registros.")])

    registros, problemas = [], []
    for indice, linha in enumerate(tabela.to_dict("records")):
        valores = {campo: linha[coluna] for campo, coluna in colunas.items()}
        registro, problemas_da_linha = _validar_linha(indice + PRIMEIRA_LINHA_DE_DADOS, valores)
        problemas += problemas_da_linha
        if registro:
            registros.append(registro)

    clientes, problemas_de_juncao = _juntar_por_cliente(registros)
    problemas += problemas_de_juncao
    if problemas:
        return _invalido(len(tabela), problemas)
    return {"valido": True, "registrosLidos": len(tabela), "registros": clientes,
            "problemas": [], "insights": gerar_insights(clientes)}


def _ler(conteudo: bytes, extensao: str) -> pd.DataFrame:
    """Lê o arquivo como texto/valor bruto; células vazias viram None."""
    if extensao == "csv":
        tabela = _ler_csv(conteudo)
    else:
        motor = "openpyxl" if extensao == "xlsx" else "xlrd"
        tabela = pd.read_excel(io.BytesIO(conteudo), dtype=object, engine=motor)
    tabela = tabela.dropna(how="all")
    return tabela.astype(object).where(tabela.notna(), None)


def _ler_csv(conteudo: bytes) -> pd.DataFrame:
    """CSV com separador detectado automaticamente (vírgula ou ponto e vírgula); tenta UTF-8 e depois Latin-1."""
    try:
        return pd.read_csv(io.BytesIO(conteudo), dtype=object, sep=None, engine="python", encoding="utf-8-sig")
    except UnicodeDecodeError:
        return pd.read_csv(io.BytesIO(conteudo), dtype=object, sep=None, engine="python", encoding="latin-1")


def _mapear_colunas(cabecalho) -> dict[str, str]:
    """campo -> nome real da coluna na planilha, para as colunas reconhecidas."""
    aceitos = {nome: campo for campo, nomes in COLUNAS.items() for nome in nomes}
    return {aceitos[chave(str(coluna))]: coluna for coluna in cabecalho if chave(str(coluna)) in aceitos}


def _validar_linha(numero: int, valores: dict) -> tuple[dict | None, list[dict]]:
    """Valida e padroniza uma linha. Devolve (registro ou None se houver problema, problemas da linha)."""
    texto = {campo: limpar_texto(valor) for campo, valor in valores.items()}
    problemas = [_problema(numero, campo, "Campo obrigatório em branco.") for campo in OBRIGATORIAS if not texto[campo]]

    segmento = None
    if texto["segmento"]:
        segmento, sugestao = padronizar_segmento(texto["segmento"])
        if not segmento:
            problemas.append(_problema(numero, "segmento", "Segmento não reconhecido.", texto["segmento"], sugestao))

    faturamento = None
    if texto["faturamento"]:
        faturamento = converter_faturamento(valores["faturamento"])
        if faturamento is None:
            problemas.append(_problema(numero, "faturamento", "Valor de faturamento inválido.", texto["faturamento"]))

    data_contratacao = None
    if texto.get("data_contratacao"):
        data_contratacao = _converter_data(valores["data_contratacao"])
        if data_contratacao is None:
            problemas.append(_problema(numero, "data_contratacao", "Data inválida. Use dd/mm/aaaa.", texto["data_contratacao"]))

    if problemas:
        return None, problemas

    codigo = _codigo(valores["cliente"])
    return {
        "linha": numero,
        "consultor": texto["consultor"],
        "codigoCliente": codigo,
        # sem coluna de nome, o cliente é identificado pelo próprio código (não inventa nome)
        "nomeCliente": texto.get("nome") or codigo,
        "segmento": segmento,
        "nivel": calcular_nivel(faturamento),
        "faturamento": float(faturamento),
        "servicos": [{"nome": nome, "dataContratacao": _iso(data_contratacao)}
                     for nome in padronizar_servicos(texto["servicos"])],
    }, []


def _juntar_por_cliente(registros: list[dict]) -> tuple[list[dict], list[dict]]:
    """Junta linhas do mesmo código de cliente (formato "um serviço por linha").
    Dados divergentes entre essas linhas viram problema."""
    clientes, problemas = {}, []
    for registro in registros:
        existente = clientes.get(registro["codigoCliente"])
        if not existente:
            clientes[registro["codigoCliente"]] = registro
            continue
        for campo in ("consultor", "segmento", "faturamento"):
            if registro[campo] != existente[campo]:
                problemas.append(_problema(registro["linha"], campo,
                                           f"Diverge da linha {existente['linha']} para o mesmo cliente.",
                                           str(registro[campo]), str(existente[campo])))
        ja_contratados = {servico["nome"] for servico in existente["servicos"]}
        existente["servicos"] += [servico for servico in registro["servicos"] if servico["nome"] not in ja_contratados]
    return list(clientes.values()), problemas


def _codigo(valor) -> str:
    """Código do cliente como texto; número do Excel como 1023.0 vira "1023"."""
    if isinstance(valor, float) and valor.is_integer():
        return str(int(valor))
    return limpar_texto(valor)


def _converter_data(valor) -> date | None:
    """Aceita data do Excel ou texto dd/mm/aaaa, aaaa-mm-dd ou dd-mm-aaaa. None = inválida."""
    if isinstance(valor, datetime):
        return valor.date()
    if isinstance(valor, date):
        return valor
    for formato in ("%d/%m/%Y", "%Y-%m-%d", "%d-%m-%Y"):
        try:
            return datetime.strptime(str(valor).strip(), formato).date()
        except ValueError:
            pass
    return None


def _iso(data: date | None) -> str | None:
    """Data no formato que o Java lê (aaaa-mm-dd)."""
    return data.isoformat() if data else None


def _problema(linha, campo, mensagem, valor_original=None, valor_sugerido=None) -> dict:
    """Um problema encontrado: linha None = problema do arquivo inteiro."""
    return {"linha": linha, "campo": campo, "mensagem": mensagem,
            "valorOriginal": valor_original, "valorSugerido": valor_sugerido}


def _invalido(registros_lidos: int, problemas: list[dict]) -> dict:
    """Resultado de planilha inválida: nada para gravar."""
    return {"valido": False, "registrosLidos": registros_lidos, "registros": [], "problemas": problemas, "insights": []}
