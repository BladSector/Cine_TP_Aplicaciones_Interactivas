async function request(url, options = {}) {
  const config = {
    method: options.method || 'GET',
    credentials: 'include',
    headers: {},
  }

  if (options.body !== undefined) {
    config.headers['Content-Type'] = 'application/json'
    config.body = JSON.stringify(options.body)
  }

  const response = await fetch(url, config)
  const contentType = response.headers.get('content-type') || ''
  const raw = await response.text()
  const data = raw
    ? contentType.includes('application/json')
      ? JSON.parse(raw)
      : raw
    : null

  if (!response.ok) {
    const message =
      typeof data === 'object' && data !== null
        ? data.message || data.error || JSON.stringify(data)
        : String(data || 'Error de servidor')
    const error = new Error(message)
    error.status = response.status
    error.data = data
    throw error
  }

  return data === '' ? null : data
}

async function download(url) {
  const response = await fetch(url, { credentials: 'include' })
  if (!response.ok) {
    const contentType = response.headers.get('content-type') || ''
    const data = contentType.includes('application/json')
      ? await response.json()
      : await response.text()
    const message =
      typeof data === 'object' && data !== null
        ? data.message || data.error || JSON.stringify(data)
        : String(data || 'No se pudo descargar el archivo')
    throw new Error(message)
  }
  return response.blob()
}

export const api = {
  getCategorias: () => request('/categorias'),
  getPeliculas: () => request('/peliculas'),
  getPelicula: (id) => request(`/peliculas/${id}`),
  getFunciones: () => request('/funciones'),
  getFuncion: (id) => request(`/funciones/${id}`),
  getSalas: () => request('/salas'),
  getButacas: () => request('/butacas'),
  getReservasButacas: (funcionId) => request(`/reservas-butacas/funciones/${funcionId}`),
  reservarButaca: (funcionId, butacaId) =>
    request('/reservas-butacas', { method: 'POST', body: { funcionId, butacaId } }),
  liberarReservaButaca: (funcionId, butacaId) =>
    request(`/reservas-butacas/funciones/${funcionId}/butacas/${butacaId}`, {
      method: 'DELETE',
    }),
  getProductos: () => request('/productos-confiteria'),
  getMembresias: () => request('/membresias'),
  getMembresiaActual: () => request('/membresias/actual'),
  cotizarMembresia: (id) => request(`/membresias/${id}/cotizacion`),
  darDeBajaMembresia: () => request('/membresias/actual', { method: 'DELETE' }),
  comprarMembresia: (body) => request('/compras/membresia', { method: 'POST', body }),
  getEntradas: () => request('/entradas'),
  getTickets: () => request('/tickets'),
  getTicket: (id) => request(`/tickets/${id}`),
  getTicketPdf: (id) => download(`/tickets/${id}/pdf`),
  comprar: (body) => request('/compras', { method: 'POST', body }),
  comprarCandy: (body) => request('/compras/candy', { method: 'POST', body }),
  loginEspectador: (body) => request('/espectadores/login', { method: 'POST', body }),
  registrarEspectador: (body) => request('/espectadores', { method: 'POST', body }),
  verificarMail: (id) => request(`/espectadores/${id}/verificar-mail`, { method: 'PUT' }),
  getEspectador: (id) => request(`/espectadores/${id}`),
  updateEspectador: (id, body) => request(`/espectadores/${id}`, { method: 'PUT', body }),
  solicitarRecuperacion: (email) =>
    request('/espectadores/solicitar-recuperacion', { method: 'POST', body: { email } }),
  recuperarContrasenia: (body) =>
    request('/espectadores/recuperar-contrasenia', { method: 'POST', body }),
  crearMetodoPago: (body) => request('/metodos-pago', { method: 'POST', body }),
  eliminarMetodoPago: (id) => request(`/metodos-pago/${id}`, { method: 'DELETE' }),
  logout: () => request('/sesion', { method: 'DELETE' }),
  calcularPrecio: (funcionId, espectadorId, precioBase, cantidad) => {
    const params = new URLSearchParams({
      funcionId,
      precioBase,
      cantidadEntradas: cantidad,
    })
    if (espectadorId) params.set('espectadorId', espectadorId)
    return request(`/precios/total?${params}`)
  },
}

export function errorMessage(error) {
  if (!error) return 'Error desconocido'
  if (typeof error.data === 'object' && error.data?.message) return error.data.message
  return error.message || 'Ocurrió un error'
}
