import {
  Chart,
  ArcElement,
  BarElement,
  LineElement,
  PointElement,
  CategoryScale,
  LinearScale,
  Filler,
  Tooltip
} from 'chart.js'

// Registramos apenas o que os gráficos do projeto usam
Chart.register(ArcElement, BarElement, LineElement, PointElement, CategoryScale, LinearScale, Filler, Tooltip)

Chart.defaults.font.family = '"DM Sans", sans-serif'
Chart.defaults.color = '#64748b'
Chart.defaults.maintainAspectRatio = false
