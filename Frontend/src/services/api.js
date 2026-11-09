// Comunicação com a API Java. Em desenvolvimento o Vite redireciona /api para http://localhost:8080 (ver vite.config.js).
const BASE = '/api'
const CHAVE_SESSAO = 'datalens:sessao'

// Sessão salva no navegador: { token, idConsultor, nome } ou null
export function lerSessao() {
  try {
    return JSON.parse(localStorage.getItem(CHAVE_SESSAO))
  } catch {
    return null
  }
}

// Apaga a sessão (logout ou token expirado)
export function encerrarSessao() {
  try {
    localStorage.removeItem(CHAVE_SESSAO)
  } catch {
    // sem acesso ao localStorage: não há o que apagar
  }
}

// Login: guarda o token devolvido pela API para as próximas requisições
export async function entrar(email, senha) {
  const sessao = await requisitar('/auth/login', { method: 'POST', body: JSON.stringify({ email, senha }) })
  try {
    localStorage.setItem(CHAVE_SESSAO, JSON.stringify(sessao))
  } catch {
    // sem localStorage a sessão não persiste, mas o login funcionou
  }
  return sessao
}

// Requisição JSON com o token; erro da API vira Error com a mensagem em português
export async function requisitar(caminho, opcoes = {}) {
  let resposta
  try {
    resposta = await fetch(BASE + caminho, {
      ...opcoes,
      headers: { 'Content-Type': 'application/json', ...cabecalhoToken(), ...opcoes.headers }
    })
  } catch {
    throw new Error('Não foi possível conectar ao servidor.')
  }
  return tratarResposta(resposta.status, await resposta.text(), caminho)
}

// Envio de arquivo (multipart, campo "arquivo") com progresso; usa XMLHttpRequest porque o fetch não informa progresso
export function enviarArquivo(caminho, arquivo, aoProgredir) {
  return new Promise((resolver, rejeitar) => {
    const dados = new FormData()
    dados.append('arquivo', arquivo)

    const xhr = new XMLHttpRequest()
    xhr.open('POST', BASE + caminho)
    Object.entries(cabecalhoToken()).forEach(([nome, valor]) => xhr.setRequestHeader(nome, valor))
    xhr.upload.onprogress = (evento) => {
      if (evento.lengthComputable) aoProgredir(Math.round((evento.loaded / evento.total) * 100))
    }
    xhr.onload = () => {
      try {
        resolver(tratarResposta(xhr.status, xhr.responseText, caminho))
      } catch (erro) {
        rejeitar(erro)
      }
    }
    xhr.onerror = () => rejeitar(new Error('Não foi possível conectar ao servidor.'))
    xhr.send(dados)
  })
}

// Header Authorization com o token da sessão, se houver
function cabecalhoToken() {
  const token = lerSessao()?.token
  return token ? { Authorization: `Bearer ${token}` } : {}
}

// Converte a resposta em dados ou em erro; 401 fora do login = sessão expirada, volta para o login
function tratarResposta(status, texto, caminho) {
  const corpo = lerJson(texto)
  if (status === 401 && caminho !== '/auth/login') {
    encerrarSessao()
    window.location.assign('/login')
    throw new Error('Sessão expirada. Entre novamente.')
  }
  if (status >= 400) throw new Error(corpo?.mensagem || 'Erro ao comunicar com o servidor.')
  return corpo
}

// JSON da resposta, ou null se vier vazia ou não for JSON
function lerJson(texto) {
  try {
    return texto ? JSON.parse(texto) : null
  } catch {
    return null
  }
}
