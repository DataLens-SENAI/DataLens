"""Padronização dos valores da planilha: texto, segmento, serviços, faturamento e nível."""

import difflib
import numbers
import re
import unicodedata
from decimal import Decimal, InvalidOperation

# Faturamento anual mínimo de cada nível (abaixo de B = nível C)
FATURAMENTO_NIVEL_A = Decimal("150000")
FATURAMENTO_NIVEL_B = Decimal("70000")

# Equivalências de segmento: nome oficial -> grafias aceitas (comparadas sem acento, caixa e pontuação)
SEGMENTOS = {
    "Indústria": ["IND", "INDUSTRIA", "INDUSTRIAL"],
    "Comércio": ["COM", "COMERCIO", "COMERCIAL"],
    "Serviços": ["SERV", "SERVICO", "SERVICOS"],
    "Agronegócio": ["AGRO", "AGRONEGOCIO", "AGRICULTURA"],
    "Varejo": ["VAR", "VAREJO", "VAREJISTA"],
    "Tecnologia": ["TEC", "TECH", "TI", "TECNOLOGIA"],
    "Saúde": ["SAUDE", "HOSPITALAR"],
    "Logística": ["LOG", "LOGISTICA", "TRANSPORTE"],
}

# Equivalências de serviço: nome oficial -> grafias aceitas. Serviço desconhecido é mantido como veio.
SERVICOS = {
    "Internet dedicada": ["INTERNET DEDICADA", "LINK DEDICADO", "LINK"],
    "Telefonia IP": ["TELEFONIA IP", "TELEFONIA", "VOIP"],
    "Cloud": ["CLOUD", "CLOUD SERVER", "NUVEM"],
    "Segurança de rede": ["SEGURANCA DE REDE", "SEGURANCA", "FIREWALL"],
}

# Multiplicadores aceitos no faturamento: "500k", "150 mil", "2M", "1,5 milhão"
MULTIPLICADORES = {"k": 1_000, "mil": 1_000, "m": 1_000_000, "mi": 1_000_000, "milhao": 1_000_000, "milhoes": 1_000_000}


def chave(texto: str) -> str:
    """Forma usada só para comparar: sem acento, maiúsculas, sem pontuação e com espaços únicos."""
    sem_acento = unicodedata.normalize("NFKD", texto).encode("ascii", "ignore").decode()
    return " ".join(re.sub(r"[^\w\s]", " ", sem_acento).upper().split())


def limpar_texto(valor) -> str | None:
    """Trim e espaços únicos, sem alterar o conteúdo. Vazio vira None."""
    if valor is None:
        return None
    texto = " ".join(str(valor).split())
    return texto or None


def _indice(equivalencias: dict[str, list[str]]) -> dict[str, str]:
    """Monta o mapa chave -> nome oficial (o próprio nome oficial também é aceito)."""
    return {chave(grafia): oficial for oficial, grafias in equivalencias.items() for grafia in [oficial, *grafias]}


_SEGMENTO_POR_CHAVE = _indice(SEGMENTOS)
_SERVICO_POR_CHAVE = _indice(SERVICOS)


def padronizar_segmento(valor: str) -> tuple[str | None, str | None]:
    """Devolve (segmento oficial, None) ou, se desconhecido, (None, sugestão parecida ou None)."""
    encontrado = _SEGMENTO_POR_CHAVE.get(chave(valor))
    if encontrado:
        return encontrado, None
    parecidos = difflib.get_close_matches(chave(valor), _SEGMENTO_POR_CHAVE, n=1, cutoff=0.6)
    return None, _SEGMENTO_POR_CHAVE[parecidos[0]] if parecidos else None


def padronizar_servicos(valor: str) -> list[str]:
    """Separa a célula em serviços (vírgula, ponto e vírgula, barra ou |) e padroniza cada nome, sem repetir."""
    servicos = []
    for parte in re.split(r"[,;/|]", valor):
        nome = limpar_texto(parte)
        if nome:
            servicos.append(_SERVICO_POR_CHAVE.get(chave(nome), nome))
    return list(dict.fromkeys(servicos))


def converter_faturamento(valor) -> Decimal | None:
    """Converte valor ou faixa de faturamento em número. Faixa ("R$500k–R$2M") vira o ponto médio.
    None = não foi possível entender o valor."""
    if isinstance(valor, numbers.Real):
        return Decimal(str(valor)).quantize(Decimal("0.01"))
    partes = re.split(r"\s+(?:a|ate)\s+|-", _simplificar_valor(str(valor)))
    numeros = [_converter_numero(parte) for parte in partes]
    if None in numeros:
        return None
    # ponytail: faixa vira o ponto médio (AGENTS.md §9 item 1); troque aqui se a equipe decidir outro critério
    return (sum(numeros) / len(numeros)).quantize(Decimal("0.01"))


def _simplificar_valor(texto: str) -> str:
    """Minúsculas, sem acento, sem "R$" e com qualquer traço trocado por "-"."""
    texto = texto.replace("–", "-").replace("—", "-")
    sem_acento = unicodedata.normalize("NFKD", texto).encode("ascii", "ignore").decode().lower()
    return sem_acento.replace("r$", " ")


def _converter_numero(texto: str) -> Decimal | None:
    """Converte um número escrito à brasileira, com sufixo opcional: "1.250.000,00", "500k", "1,5 mi"."""
    encontrado = re.fullmatch(r"\s*([\d.,]+)\s*([a-z]*)\s*", texto)
    if not encontrado or encontrado.group(2) not in ("", *MULTIPLICADORES):
        return None
    numero, sufixo = encontrado.groups()
    try:
        return _numero_brasileiro(numero) * MULTIPLICADORES.get(sufixo, 1)
    except InvalidOperation:
        return None


def _numero_brasileiro(numero: str) -> Decimal:
    """Interpreta separadores: "1.250,50" e "1.250.000" usam ponto de milhar; "1,5" e "1.5" são decimais."""
    if "," in numero:
        return Decimal(numero.replace(".", "").replace(",", "."))
    if re.fullmatch(r"\d{1,3}(\.\d{3})+", numero):
        return Decimal(numero.replace(".", ""))
    return Decimal(numero)


def calcular_nivel(faturamento: Decimal) -> str:
    """Nível do cliente pelo faturamento anual: A a partir de 150 mil, B a partir de 70 mil, senão C."""
    if faturamento >= FATURAMENTO_NIVEL_A:
        return "A"
    if faturamento >= FATURAMENTO_NIVEL_B:
        return "B"
    return "C"
