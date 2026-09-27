// Gera e baixa um arquivo CSV a partir de uma lista de objetos
export function exportarCSV(nomeArquivo, linhas) {
  if (!linhas.length) return

  const colunas = Object.keys(linhas[0])
  const escapar = (valor) => `"${String(valor ?? '').replace(/"/g, '""')}"`
  const conteudo = [
    colunas.map(escapar).join(';'),
    ...linhas.map((linha) => colunas.map((coluna) => escapar(linha[coluna])).join(';'))
  ].join('\n')

  // BOM para o Excel reconhecer acentuação
  const blob = new Blob(['\ufeff' + conteudo], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = nomeArquivo
  link.click()
  URL.revokeObjectURL(url)
}
