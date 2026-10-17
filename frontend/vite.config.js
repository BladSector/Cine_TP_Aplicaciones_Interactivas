import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/categorias': 'http://localhost:8080',
      '/peliculas': 'http://localhost:8080',
      '/salas': 'http://localhost:8080',
      '/butacas': 'http://localhost:8080',
      '/funciones': 'http://localhost:8080',
      '/metodos-pago': 'http://localhost:8080',
      '/espectadores': 'http://localhost:8080',
      '/entradas': 'http://localhost:8080',
      '/tickets': 'http://localhost:8080',
      '/compras': 'http://localhost:8080',
      '/productos-confiteria': 'http://localhost:8080',
      '/sesion': 'http://localhost:8080',
      '/precios': 'http://localhost:8080',
    },
  },
})
