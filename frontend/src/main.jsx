import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import App from './App'
import { AuthProvider } from './context/AuthContext'
import { CatalogProvider } from './context/CatalogContext'
import { PurchaseProvider } from './context/PurchaseContext'
import './styles/global.css'

const savedTheme = localStorage.getItem('film-zone-theme')
const initialTheme = savedTheme === 'dark' || savedTheme === 'light'
  ? savedTheme
  : window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'

document.documentElement.dataset.theme = initialTheme

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <BrowserRouter>
      <AuthProvider>
        <CatalogProvider>
          <PurchaseProvider>
            <App />
          </PurchaseProvider>
        </CatalogProvider>
      </AuthProvider>
    </BrowserRouter>
  </StrictMode>,
)
