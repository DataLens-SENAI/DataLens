"""API do serviço de processamento: chamada só pelo backend Java, nunca pelo front."""

from fastapi import FastAPI, UploadFile

from app.processamento import processar

app = FastAPI(title="DataLens - Processamento de planilhas")


@app.post("/processar")
async def processar_planilha(arquivo: UploadFile) -> dict:
    """Recebe a planilha (campo multipart "arquivo") e devolve o resultado da validação/padronização."""
    return processar(await arquivo.read(), arquivo.filename or "")


@app.get("/saude")
def saude() -> dict:
    """Verificação simples de que o serviço está no ar."""
    return {"status": "ok"}
