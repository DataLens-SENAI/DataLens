export const formatarMoeda = (valor) =>
  valor.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL', maximumFractionDigits: 0 })

export const formatarNumero = (valor) => valor.toLocaleString('pt-BR')

export function formatarTamanho(bytes) {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

export const iniciais = (nome) =>
  nome.split(' ').map((parte) => parte[0]).slice(0, 2).join('').toUpperCase()
