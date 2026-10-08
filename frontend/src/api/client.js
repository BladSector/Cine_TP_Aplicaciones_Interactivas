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
  const data = contentType.includes('application/json')
    ? await response.json()
    : await response.text()

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

export const api = {
  getCategorias: () => request('/categorias'),
  getPeliculas: () => request('/peliculas'),
  getPelicula: (id) => request(`/peliculas/${id}`),
  getFunciones: () => request('/funciones'),
  getFuncion: (id) => request(`/funciones/${id}`),
  getSalas: () => request('/salas'),
  getButacas: () => request('/butacas'),
  getProductos: () => request('/productos-confiteria'),
  getEntradas: () => request('/entradas'),
  getTickets: () => request('/tickets'),
  getTicket: (id) => request(`/tickets/${id}`),
  getTicketPdf: async (id) => {
    const response = await fetch(`/tickets/${id}/pdf`, { credentials: 'include' })
    if (!response.ok) throw new Error('No se pudo descargar el PDF')
    return response.blob()
  },
  comprar: (body) => request('/compras', { method: 'POST', body }),
  loginEspectador: (body) => request('/espectadores/login', { method: 'POST', body }),
  registrarEspectador: (body) => request('/espectadores', { method: 'POST', body }),
  getEspectador: (id) => request(`/espectadores/${id}`),
  updateEspectador: (id, body) => request(`/espectadores/${id}`, { method: 'PUT', body }),
  solicitarRecuperacion: (email) =>
    request('/espectadores/solicitar-recuperacion', { method: 'POST', body: { email } }),
  recuperarContrasenia: (body) =>
    request('/espectadores/recuperar-contrasenia', { method: 'POST', body }),
  crearMetodoPago: (body) => request('/metodos-pago', { method: 'POST', body }),
  eliminarMetodoPago: (id) => request(`/metodos-pago/${id}`, { method: 'DELETE' }),
  asociarMetodoPago: (espectadorId, metodoId) =>
    request(`/espectadores/${espectadorId}/metodo-pago/${metodoId}`, { method: 'PUT' }),
  loginPersonal: (body) => request('/sesion/personal', { method: 'POST', body }),
  getPersonal: () => request('/sesion/personal'),
  logout: () => request('/sesion', { method: 'DELETE' }),
  crearCategoria: (body) => request('/categorias', { method: 'POST', body }),
  crearPelicula: (body) => request('/peliculas', { method: 'POST', body }),
  actualizarPelicula: (id, body) => request(`/peliculas/${id}`, { method: 'PUT', body }),
  eliminarPelicula: (id) => request(`/peliculas/${id}`, { method: 'DELETE' }),
  crearSalaConButacas: (body) => request('/salas/con-butacas', { method: 'POST', body }),
  crearFuncion: (body) => request('/funciones', { method: 'POST', body }),
  actualizarFuncion: (id, body) => request(`/funciones/${id}`, { method: 'PUT', body }),
  eliminarFuncion: (id) => request(`/funciones/${id}`, { method: 'DELETE' }),
  crearProducto: (body) => request('/productos-confiteria', { method: 'POST', body }),
  getEmpleados: () => request('/empleados'),
  crearEmpleado: (body) => request('/empleados', { method: 'POST', body }),
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
