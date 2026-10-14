# Noche Cine · Frontend React

App React (Vite) para venta de entradas, alineada al diseño **TP CINE — FLIM ZONE** y conectada a la API Java existente.

## Requisitos

- Node.js 18+
- API Spring Boot en `http://localhost:8080`

## Cómo correr

```bash
cd frontend
npm install
npm run dev
```

Abrí `http://localhost:5173`. Vite proxea las rutas de la API al backend (cookies de sesión incluidas).

## Flujos principales

1. **Cartelera** → filtros por día, género, formato e idioma
2. **Ficha de película** → elección de función
3. **Compra** → acceso → butacas → candy bar (opcional) → pago
4. **Mi cuenta** → tickets, perfil y métodos de pago
5. **Personal** (`/personal`) → panel de administración de catálogo/salas/funciones/confitería

No modifica la API Java.
