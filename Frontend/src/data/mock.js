// Dados simulados usados nas telas enquanto não há integração com a API

export const kpisDashboard = [
  { titulo: 'Total de Clientes', valor: '1.248', variacao: '+8,4%', tipoVariacao: 'positivo', icone: 'Users' },
  { titulo: 'Clientes Nível A', valor: '287', descricao: '23% do total', icone: 'Star' },
  { titulo: 'Faturamento Pot.', valor: 'R$ 42.5M', icone: 'DollarSign' },
  { titulo: 'Serviços Contrat.', valor: '3.120', descricao: '~2.5/cliente', icone: 'Layers' },
  { titulo: 'Segmentos Ativos', valor: '12', icone: 'Globe' }
]

export const insights = [
  {
    tipo: 'oportunidade',
    texto: '42% dos clientes nível A não possuem serviço de Backup.',
    tag: 'Oportunidade Cross-sell'
  },
  {
    tipo: 'risco',
    texto: 'Queda de 15% na renovação de contratos de Link Dedicado no segmento de Varejo.',
    tag: 'Risco de Churn'
  },
  {
    tipo: 'performance',
    texto: 'Consultor Carlos Silva atingiu 120% da meta trimestral em serviços Cloud.',
    tag: 'Performance Alta'
  }
]

// Soma = 1.248 clientes (A 23% · B 35% · C 42%)
export const distribuicaoNivel = [
  { nivel: 'Nível A', total: 287, cor: '#1d4ed8' },
  { nivel: 'Nível B', total: 437, cor: '#6ee7b7' },
  { nivel: 'Nível C', total: 524, cor: '#e2e8f0' }
]

// Soma = 1.248 clientes
export const clientesPorSegmento = [
  { segmento: 'Indústria', total: 310 },
  { segmento: 'Serviços', total: 265 },
  { segmento: 'Varejo', total: 220 },
  { segmento: 'Tecnol.', total: 180 },
  { segmento: 'Saúde', total: 120 },
  { segmento: 'Logíst.', total: 88 },
  { segmento: 'Outros', total: 65 }
]

export const consultores = [
  { nome: 'Carlos Silva', clientesAtivos: 142, ticketMedio: 4250, status: 'acima' },
  { nome: 'Ana Paula', clientesAtivos: 118, ticketMedio: 3890, status: 'na-meta' },
  { nome: 'Roberto Oliveira', clientesAtivos: 95, ticketMedio: 5100, status: 'acima' },
  { nome: 'Mariana Costa', clientesAtivos: 64, ticketMedio: 2900, status: 'atencao' }
]

export const topServicos = [
  { nome: 'Link Dedicado', contratos: 1450 },
  { nome: 'Cloud Server', contratos: 890 },
  { nome: 'Backup B2B', contratos: 520 },
  { nome: 'PABX Virtual', contratos: 260 }
]

export const importacoesRecentes = [
  { arquivo: 'base_clientes_q3_2023.xlsx', etapa: 'Padronização', quando: 'Há 2 dias' }
]

// Home pública — prévia do dashboard de qualidade
export const evolucaoQualidade = {
  meses: ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul'],
  valores: [77, 78, 83, 84, 88, 92, 94]
}

// Clientes usados na tela de Análises
const empresas = [
  'Metalúrgica Andrade', 'Grupo Vitalis', 'Supermercados Bom Preço', 'TechNova Sistemas', 'Clínica São Lucas',
  'TransLog Cargas', 'Construtora Horizonte', 'Farmácias Viva', 'Agro Campos Verdes', 'Nexus Consultoria',
  'Plásticos Paraná', 'Hospital Santa Clara', 'Rede Mais Varejo', 'DataCore Cloud', 'Expresso Sul',
  'Têxtil Brasil', 'Escola Integrada', 'Moda Center', 'InovaSoft', 'Laboratório BioVida',
  'Frigorífico Serrano', 'Atacadão Central', 'Pixel Studio', 'Rápido Entregas'
]
const segmentos = ['Indústria', 'Serviços', 'Varejo', 'Tecnologia', 'Saúde', 'Logística']
const segmentoPorEmpresa = [0, 1, 2, 3, 4, 5, 0, 2, 0, 1, 0, 4, 2, 3, 5, 0, 1, 2, 3, 4, 0, 2, 3, 5]
const statusCliente = ['ativo', 'oportunidade', 'risco']

// Nível definido pelo faturamento anual
const nivelPorFaturamento = (faturamento) => (faturamento >= 150_000 ? 'A' : faturamento >= 70_000 ? 'B' : 'C')

export const clientes = empresas.map((nome, indice) => {
  const faturamento = 18000 + ((indice * 37_919) % 220_000)

  return {
    id: indice + 1,
    nome,
    segmento: segmentos[segmentoPorEmpresa[indice]],
    nivel: nivelPorFaturamento(faturamento),
    consultor: consultores[indice % consultores.length].nome,
    servicos: 1 + ((indice * 3) % 5),
    faturamento,
    status: statusCliente[(indice * 4) % 7 === 0 ? 2 : (indice % 3 === 1 ? 1 : 0)]
  }
})

export const opcoesSegmento = segmentos
