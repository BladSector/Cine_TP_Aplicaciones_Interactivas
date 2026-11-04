export default function CatalogFilters({
  categorias, categoryId, onCategoryChange,
  formato, onFormatChange, idioma, onLanguageChange,
}) {
  return (
    <div className="filters">
      <p className="filters__intro">Buscar por:</p>
      <label>
        Género
        <select value={categoryId} onChange={(event) => onCategoryChange(event.target.value)}>
          <option value="">Todos</option>
          {categorias.map((category) => (
            <option key={category.id} value={category.id}>{category.nombre}</option>
          ))}
        </select>
      </label>
      <label>
        Formato
        <select value={formato} onChange={(event) => onFormatChange(event.target.value)}>
          <option value="">Todos los formatos</option>
          <option value="DOS_D">2D</option>
          <option value="TRES_D">3D</option>
        </select>
      </label>
      <label>
        Idioma
        <select value={idioma} onChange={(event) => onLanguageChange(event.target.value)}>
          <option value="">Todos los idiomas</option>
          <option value="ESPANIOL">Doblada</option>
          <option value="SUBTITULADA">Subtitulada</option>
        </select>
      </label>
    </div>
  )
}
