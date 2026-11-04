import { Component } from 'react'

export default class RouteErrorBoundary extends Component {
  state = { failed: false }

  static getDerivedStateFromError() {
    return { failed: true }
  }

  render() {
    if (this.state.failed) {
      return (
        <section className="info-box info-box--error" role="alert">
          <p><strong>No pudimos abrir esta página</strong></p>
          <p>Revisá tu conexión y volvé a cargar la página.</p>
          <button type="button" className="primary-button" onClick={() => window.location.reload()}>
            Volver a cargar
          </button>
        </section>
      )
    }

    return this.props.children
  }
}
