package desktop.duenio;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import com.fasterxml.jackson.databind.JsonNode;

import desktop.comun.ApiClient;
import desktop.comun.api.CarteleraApi;
import desktop.comun.api.PersonalApi;
import desktop.comun.api.ProductosApi;
import desktop.comun.api.SalasApi;
import desktop.comun.componentes.VisorDetalle;
import desktop.comun.dialogos.EstadoSalaDialog;
import desktop.duenio.dialogos.CategoriaDialog;
import desktop.duenio.dialogos.FuncionDialog;
import desktop.duenio.dialogos.Opcion;
import desktop.duenio.dialogos.PeliculaDialog;
import desktop.duenio.dialogos.ProductoDialog;
import desktop.duenio.dialogos.SalaDialog;
import desktop.duenio.paneles.CategoriasPanel;
import desktop.duenio.paneles.FuncionesAdminPanel;
import desktop.duenio.paneles.PeliculasPanel;
import desktop.duenio.paneles.ProductosPanel;
import desktop.duenio.paneles.SalasAdminPanel;

import static desktop.comun.util.ConfiguradorTablas.limpiar;
import static desktop.comun.util.ConfiguradorTablas.modelo;
import static desktop.comun.util.ConfiguradorTablas.tablaAdministracion;
import static desktop.comun.util.DetalleHtml.campoHtml;
import static desktop.comun.util.DetalleHtml.inicioHtml;
import static desktop.comun.util.DetalleHtml.itemHtml;
import static desktop.comun.util.DetalleHtml.mensajeHtml;
import static desktop.comun.util.DetalleHtml.mensajeSecundarioHtml;
import static desktop.comun.util.DetalleHtml.seccionHtml;
import static desktop.comun.util.DetalleHtml.tituloHtml;
import static desktop.comun.util.FormatoSwing.nombreEstadoSala;
import static desktop.comun.util.FormatoSwing.nombreFormato;
import static desktop.comun.util.FormatoSwing.nombreIdioma;

public class AdministracionPanel extends JPanel {
    private final CarteleraApi carteleraApi;
    private final SalasApi salasApi;
    private final PersonalApi personalApi;
    private final ProductosApi productosApi;
    private boolean activo = true;
    private final JLabel estado = new JLabel("Cargando administración...");

    private final DefaultTableModel categoriasModel = modelo("ID", "Nombre");
    private final DefaultTableModel peliculasModel = modelo("ID", "Título", "Duración", "Categoría", "Descripción", "Portada");
    private final DefaultTableModel salasModel = modelo("ID", "Nombre", "Capacidad", "Estado", "Detalle");
    private final DefaultTableModel funcionesModel = modelo(
            "ID", "Fecha", "Horario", "Película ID", "Película", "Sala ID", "Sala", "Formato", "Idioma", "Precio"
    );
    private final DefaultTableModel productosModel = modelo(
            "ID", "Nombre", "Precio", "Tipo", "Tamaño", "Descripción", "Imagen"
    );

    private final JTable categoriasTabla = tablaAdministracion(categoriasModel);
    private final JTable peliculasTabla = tablaAdministracion(peliculasModel);
    private final JTable salasTabla = tablaAdministracion(salasModel);
    private final JTable funcionesTabla = tablaAdministracion(funcionesModel);
    private final JTable productosTabla = tablaAdministracion(productosModel);

    private List<Opcion> categorias = new ArrayList<>();
    private List<Opcion> peliculas = new ArrayList<>();
    private List<Opcion> salas = new ArrayList<>();
    private final Map<Integer, String> portadasPeliculas = new HashMap<>();
    private final Map<Integer, String> imagenesProductos = new HashMap<>();
    private Consumer<String> estadoExterno = mensaje -> {
    };

    public AdministracionPanel(ApiClient apiClient) {
        this(apiClient, null, null);
        construirVista();
        cargarTodo();
    }

    public AdministracionPanel(ApiClient apiClient, JTabbedPane pestanias, Consumer<String> estadoExterno) {
        super(new BorderLayout());
        this.carteleraApi = new CarteleraApi(apiClient);
        this.salasApi = new SalasApi(apiClient);
        this.personalApi = new PersonalApi(apiClient);
        this.productosApi = new ProductosApi(apiClient);
        this.estadoExterno = estadoExterno == null ? mensaje -> {
        } : estadoExterno;
        if (pestanias != null) {
            agregarPestanias(pestanias);
        }
    }

    public void recargar() {
        cargarTodo();
    }

    public void detener() {
        activo = false;
    }

    private void construirVista() {
        JTabbedPane pestanias = new JTabbedPane();
        agregarPestanias(pestanias);

        estado.setBorder(BorderFactory.createEmptyBorder(6, 12, 8, 12));
        add(pestanias, BorderLayout.CENTER);
        add(estado, BorderLayout.SOUTH);
    }

    private void agregarPestanias(JTabbedPane pestanias) {
        pestanias.addTab("Funciones", panelFunciones());
        pestanias.addTab("Películas", panelPeliculas());
        pestanias.addTab("Salas", panelSalas());
        pestanias.addTab("Confitería", panelProductos());
        pestanias.addTab("Categorías", panelCategorias());
    }

    private JPanel panelCategorias() {
        Runnable editar = () -> formularioCategoria(true);
        Runnable eliminar = () -> eliminarSeleccionado(categoriasTabla, carteleraApi::eliminarCategoria, "la categoría");
        return new CategoriasPanel(
                categoriasTabla,
                () -> formularioCategoria(false),
                this::cargarTodo,
                () -> mostrarDetalleConAcciones("Categorías", categoriasTabla, editar, eliminar)
        );
    }

    private JPanel panelPeliculas() {
        Runnable editar = () -> formularioPelicula(true);
        Runnable eliminar = () -> eliminarSeleccionado(peliculasTabla, carteleraApi::eliminarPelicula, "la película");
        return new PeliculasPanel(
                peliculasTabla,
                () -> formularioPelicula(false),
                this::cargarTodo,
                () -> mostrarDetalleConAcciones("Películas", peliculasTabla, editar, eliminar)
        );
    }

    private JPanel panelSalas() {
        Runnable editar = () -> formularioMatrizSala();
        Runnable eliminar = () -> eliminarSeleccionado(salasTabla, salasApi::eliminarSala, "la sala");
        return new SalasAdminPanel(
                salasTabla,
                this::formularioSalaNueva,
                this::cargarTodo,
                () -> mostrarDetalleConAcciones("Salas", salasTabla, editar, eliminar)
        );
    }

    private JPanel panelFunciones() {
        Runnable editar = () -> formularioFuncion(true);
        Runnable eliminar = () -> eliminarSeleccionado(funcionesTabla, carteleraApi::eliminarFuncion, "la función");
        return new FuncionesAdminPanel(
                funcionesTabla,
                () -> formularioFuncion(false),
                this::cargarTodo,
                () -> mostrarDetalleConAcciones("Funciones", funcionesTabla, editar, eliminar)
        );
    }

    private JPanel panelProductos() {
        Runnable editar = () -> formularioProducto(true);
        Runnable eliminar = () -> eliminarSeleccionado(productosTabla, productosApi::eliminarProducto, "el producto");
        return new ProductosPanel(
                productosTabla,
                () -> formularioProducto(false),
                this::cargarTodo,
                () -> mostrarDetalleConAcciones(
                        "Productos de confitería", productosTabla, editar, eliminar
                )
        );
    }

    private String descripcionSeleccion(JTable tabla) {
        int filaVista = tabla.getSelectedRow();
        if (filaVista < 0) {
            return mensajeHtml("Seleccioná una fila para ver su información.");
        }
        if (tabla == categoriasTabla) {
            return descripcionCategoriaSeleccionada(filaVista);
        }
        if (tabla == peliculasTabla) {
            return descripcionPeliculaSeleccionada(filaVista);
        }
        if (tabla == salasTabla) {
            return descripcionSalaSeleccionada(filaVista);
        }

        int filaModelo = tabla.convertRowIndexToModel(filaVista);
        StringBuilder descripcion = new StringBuilder(inicioHtml());
        for (int columna = 0; columna < tabla.getModel().getColumnCount(); columna++) {
            Object valor = tabla.getModel().getValueAt(filaModelo, columna);
            descripcion.append(campoHtml(
                    tabla.getModel().getColumnName(columna),
                    valor == null || valor.toString().isBlank() ? "-" : valor
            ));
        }
        return descripcion.append("</body></html>").toString();
    }

    private String descripcionCategoriaSeleccionada(int filaVista) {
        int fila = categoriasTabla.convertRowIndexToModel(filaVista);
        String nombreCategoria = valor(categoriasModel, fila, 1);
        StringBuilder descripcion = new StringBuilder(inicioHtml());
        descripcion.append(tituloHtml("CATEGORÍA"))
                .append(campoHtml("Nombre", nombreCategoria))
                .append(seccionHtml("PELÍCULAS ASOCIADAS"));

        int cantidadPeliculas = 0;
        for (int i = 0; i < peliculasModel.getRowCount(); i++) {
            if (!nombreCategoria.equals(valor(peliculasModel, i, 3))) {
                continue;
            }
            cantidadPeliculas++;
            descripcion.append(itemHtml(valor(peliculasModel, i, 1)))
                    .append(campoHtml("ID", valor(peliculasModel, i, 0)))
                    .append(campoHtml("Duración", valor(peliculasModel, i, 2) + " min"))
                    .append("<hr>");
        }
        if (cantidadPeliculas == 0) {
            descripcion.append(mensajeSecundarioHtml("No hay películas asociadas a esta categoría."));
        }
        return descripcion.append("</body></html>").toString();
    }

    private String descripcionPeliculaSeleccionada(int filaVista) {
        int fila = peliculasTabla.convertRowIndexToModel(filaVista);
        int peliculaId = id(peliculasModel, fila);
        StringBuilder descripcion = new StringBuilder(inicioHtml());
        descripcion.append(tituloHtml("PELÍCULA"))
                .append(campoHtml("Título", valor(peliculasModel, fila, 1)))
                .append(campoHtml("Descripción", valor(peliculasModel, fila, 4)))
                .append(campoHtml("Duración", valor(peliculasModel, fila, 2) + " min"))
                .append(campoHtml("Categoría", valor(peliculasModel, fila, 3)))
                .append(campoHtml("Portada", valor(peliculasModel, fila, 5)))
                .append(seccionHtml("FUNCIONES ASOCIADAS"));

        int cantidadFunciones = 0;
        for (int i = 0; i < funcionesModel.getRowCount(); i++) {
            if (numero(funcionesModel, i, 3) != peliculaId) {
                continue;
            }
            cantidadFunciones++;
            descripcion.append(itemHtml(valor(funcionesModel, i, 1) + " - " + valor(funcionesModel, i, 2)))
                    .append(campoHtml("Sala", valor(funcionesModel, i, 6)))
                    .append(campoHtml("Formato", valor(funcionesModel, i, 7)))
                    .append(campoHtml("Idioma", valor(funcionesModel, i, 8)))
                    .append(campoHtml("Precio", "$" + valor(funcionesModel, i, 9)))
                    .append("<hr>");
        }
        if (cantidadFunciones == 0) {
            descripcion.append(mensajeSecundarioHtml("No hay funciones programadas para esta película."));
        }
        return descripcion.append("</body></html>").toString();
    }

    private String descripcionSalaSeleccionada(int filaVista) {
        int fila = salasTabla.convertRowIndexToModel(filaVista);
        String nombreSala = valor(salasModel, fila, 1);
        StringBuilder descripcion = new StringBuilder(inicioHtml());
        descripcion.append(tituloHtml("SALA"))
                .append(campoHtml("Nombre", nombreSala))
                .append(campoHtml("Capacidad", valor(salasModel, fila, 2)))
                .append(campoHtml("Estado", valor(salasModel, fila, 3)))
                .append(campoHtml("Detalle", valor(salasModel, fila, 4)))
                .append(seccionHtml("FUNCIONES PROGRAMADAS"));

        int cantidadFunciones = 0;
        for (int i = 0; i < funcionesModel.getRowCount(); i++) {
            if (!nombreSala.equals(valor(funcionesModel, i, 6))) {
                continue;
            }
            cantidadFunciones++;
            descripcion.append(itemHtml(valor(funcionesModel, i, 4)))
                    .append(campoHtml("Fecha", valor(funcionesModel, i, 1)))
                    .append(campoHtml("Horario", valor(funcionesModel, i, 2)))
                    .append(campoHtml("Formato", valor(funcionesModel, i, 7)))
                    .append(campoHtml("Idioma", valor(funcionesModel, i, 8)))
                    .append(campoHtml("Precio", "$" + valor(funcionesModel, i, 9)))
                    .append("<hr>");
        }
        if (cantidadFunciones == 0) {
            descripcion.append(mensajeSecundarioHtml("No hay funciones programadas en esta sala."));
        }
        return descripcion.append("</body></html>").toString();
    }

    private void mostrarDetalleConAcciones(String titulo, JTable tabla, Runnable editar, Runnable eliminar) {
        JEditorPane detalle = new VisorDetalle("Seleccioná una fila para ver su información.");
        detalle.setText(descripcionSeleccion(tabla));
        detalle.setCaretPosition(0);
        JScrollPane scroll = new JScrollPane(detalle);
        scroll.setPreferredSize(new java.awt.Dimension(480, 320));

        boolean esSala = tabla == salasTabla;
        Object[] opciones = esSala
                ? new Object[]{"Cerrar", "Editar", "Cambiar estado", "Eliminar"}
                : new Object[]{"Cerrar", "Editar", "Eliminar"};
        int opcion = JOptionPane.showOptionDialog(
                this,
                scroll,
                "Información de " + titulo.toLowerCase(),
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                opciones,
                opciones[0]
        );
        if (opcion == 1) {
            editar.run();
        } else if (esSala && opcion == 2) {
            formularioEstadoSala();
        } else if ((!esSala && opcion == 2) || (esSala && opcion == 3)) {
            eliminar.run();
        }
    }

    private void formularioCategoria(boolean edicion) {
        int fila = edicion ? filaSeleccionada(categoriasTabla) : -1;
        if (edicion && fila < 0) {
            return;
        }
        String nombreActual = edicion ? valor(categoriasModel, fila, 1) : "";
        int categoriaId = edicion ? id(categoriasModel, fila) : 0;
        CategoriaDialog.mostrar(this, nombreActual).ifPresent(datos -> {
            Map<String, Object> cuerpo = Map.of("nombre", datos.nombre());
            if (edicion) {
                ejecutar("Actualizando categoría...",
                        () -> carteleraApi.actualizarCategoria(categoriaId, cuerpo), respuesta -> cargarTodo());
            } else {
                ejecutar("Creando categoría...",
                        () -> carteleraApi.crearCategoria(cuerpo), respuesta -> cargarTodo());
            }
        });
    }

    private void formularioPelicula(boolean edicion) {
        int fila = edicion ? filaSeleccionada(peliculasTabla) : -1;
        if (edicion && fila < 0) {
            return;
        }
        if (categorias.isEmpty()) {
            aviso("Primero creá una Categoría.");
            return;
        }

        int peliculaId = edicion ? id(peliculasModel, fila) : 0;
        PeliculaDialog.Datos inicial = edicion
                ? new PeliculaDialog.Datos(
                valor(peliculasModel, fila, 1),
                numero(peliculasModel, fila, 2),
                valor(peliculasModel, fila, 4),
                portadasPeliculas.getOrDefault(peliculaId, ""),
                0,
                valor(peliculasModel, fila, 3))
                : null;

        PeliculaDialog.mostrar(this, inicial, categorias).ifPresent(datos -> {
            Map<String, Object> cuerpo = Map.of(
                    "titulo", datos.titulo(),
                    "duracion", datos.duracion(),
                    "descripcion", datos.descripcion(),
                    "portadaUrl", datos.portadaUrl(),
                    "categoriaId", datos.categoriaId());
            guardarOActualizar(edicion, "película", () -> edicion
                    ? carteleraApi.actualizarPelicula(peliculaId, cuerpo)
                    : carteleraApi.crearPelicula(cuerpo));
        });
    }

    private void formularioSalaNueva() {
        SalaDialog.mostrarNueva(this).ifPresent(datos -> {
            Map<String, Object> cuerpo = Map.of(
                    "nombre", datos.nombre(),
                    "butacas", datos.butacas());
            ejecutar("Creando sala y butacas...",
                    () -> salasApi.crearSala(cuerpo), respuesta -> cargarTodo());
        });
    }

    private void formularioMatrizSala() {
        int filaSala = filaSeleccionada(salasTabla);
        if (filaSala < 0) {
            return;
        }
        int salaId = id(salasModel, filaSala);
        String nombreSala = valor(salasModel, filaSala, 1);
        ejecutar("Cargando distribución...", salasApi::listarButacas,
                respuesta -> editarDistribucionSala(salaId, nombreSala, respuesta));
    }

    private void editarDistribucionSala(int salaId, String nombreSala, JsonNode respuesta) {
        List<JsonNode> butacasSala = new ArrayList<>();
        respuesta.forEach(butaca -> {
            if (butaca.path("salaId").asInt() == salaId) {
                butacasSala.add(butaca);
            }
        });
        SalaDialog.mostrarEdicion(this, nombreSala, butacasSala).ifPresent(datos -> {
            Map<String, Object> cuerpo = Map.of(
                    "nombre", datos.nombre(),
                    "butacas", datos.butacas());
            ejecutar("Actualizando distribución...",
                    () -> salasApi.actualizarDistribucion(salaId, cuerpo),
                    respuestaApi -> cargarTodo());
        });
    }

    private void formularioEstadoSala() {
        int fila = filaSeleccionada(salasTabla);
        if (fila < 0) {
            return;
        }
        int salaId = id(salasModel, fila);
        String estadoActual = valor(salasModel, fila, 3);
        ejecutar(
                "Cargando empleados...",
                personalApi::listarEmpleadosActivos,
                empleadosActivos -> mostrarFormularioEstadoSala(salaId, estadoActual, empleadosActivos)
        );
    }

    private void mostrarFormularioEstadoSala(int salaId, String estadoActual, JsonNode empleadosActivos) {
        EstadoSalaDialog.mostrar(this, estadoActual, empleadosActivos).ifPresent(datos -> {
            Map<String, Object> cuerpo = new HashMap<>();
            cuerpo.put("estado", datos.estado());
            cuerpo.put("descripcion", datos.descripcion());
            if (datos.destinatarioEmpleadoId() != null) {
                cuerpo.put("destinatarioEmpleadoId", datos.destinatarioEmpleadoId());
            }

            ejecutar("Actualizando estado de la sala...",
                    () -> salasApi.cambiarEstado(salaId, cuerpo), respuesta -> cargarTodo());
        });
    }

    private void formularioFuncion(boolean edicion) {
        if (!edicion) {
            mostrarFormularioFuncion(null, null);
            return;
        }

        int fila = filaSeleccionada(funcionesTabla);
        if (fila < 0) {
            return;
        }
        int funcionId = id(funcionesModel, fila);
        // La tabla puede recargarse mientras se consultan las entradas o se completa el diálogo.
        FuncionDialog.Datos inicial = new FuncionDialog.Datos(
                valor(funcionesModel, fila, 1),
                valor(funcionesModel, fila, 2),
                numero(funcionesModel, fila, 3),
                numero(funcionesModel, fila, 5),
                valor(funcionesModel, fila, 7),
                valor(funcionesModel, fila, 8),
                decimal(funcionesModel, fila, 9));
        ejecutar(
                "Comprobando entradas vendidas...",
                () -> carteleraApi.consultarEntradasActivas(funcionId),
                respuesta -> {
                    long cantidadEntradas = respuesta.path("cantidadEntradas").asLong();
                    if (cantidadEntradas > 0) {
                        int confirmacion = JOptionPane.showConfirmDialog(
                                this,
                                "Esta función tiene " + cantidadEntradas + " entrada(s) vendida(s).\n"
                                        + "Modificar sus datos puede afectar a los espectadores.\n\n"
                                        + "¿Querés continuar?",
                                "Función con entradas vendidas",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE
                        );
                        if (confirmacion != JOptionPane.YES_OPTION) {
                            return;
                        }
                    }
                    mostrarFormularioFuncion(funcionId, inicial);
                }
        );
    }

    private void mostrarFormularioFuncion(Integer funcionId, FuncionDialog.Datos inicial) {
        if (peliculas.isEmpty() || salas.isEmpty()) {
            aviso("Primero deben existir Películas y Salas.");
            return;
        }

        FuncionDialog.mostrar(this, inicial, peliculas, salas).ifPresent(datos -> {
            boolean edicion = funcionId != null;
            Map<String, Object> cuerpo = Map.of(
                    "fecha", datos.fecha(),
                    "horario", datos.horario(),
                    "peliculaId", datos.peliculaId(),
                    "salaId", datos.salaId(),
                    "formato", datos.formato(),
                    "idioma", datos.idioma(),
                    "precioEntrada", datos.precioEntrada());
            guardarOActualizar(edicion, "función", () -> edicion
                    ? carteleraApi.actualizarFuncion(funcionId, cuerpo)
                    : carteleraApi.crearFuncion(cuerpo));
        });
    }

    private void formularioProducto(boolean edicion) {
        int fila = edicion ? filaSeleccionada(productosTabla) : -1;
        if (edicion && fila < 0) {
            return;
        }
        int productoId = edicion ? id(productosModel, fila) : 0;
        ProductoDialog.Datos inicial = edicion
                ? new ProductoDialog.Datos(
                valor(productosModel, fila, 1),
                valor(productosModel, fila, 5),
                decimal(productosModel, fila, 2),
                valor(productosModel, fila, 3),
                valor(productosModel, fila, 4),
                imagenesProductos.getOrDefault(productoId, ""))
                : null;

        ProductoDialog.mostrar(this, inicial).ifPresent(datos -> {
            Map<String, Object> cuerpo = Map.of(
                    "nombre", datos.nombre(),
                    "descripcion", datos.descripcion(),
                    "precio", datos.precio(),
                    "tipo", datos.tipo(),
                    "tamano", datos.tamano(),
                    "imagenUrl", datos.imagenUrl());
            guardarOActualizar(edicion, "producto", () -> edicion
                    ? productosApi.actualizarProducto(productoId, cuerpo)
                    : productosApi.crearProducto(cuerpo));
        });
    }

    private void guardarOActualizar(boolean edicion, String entidad, Operacion<JsonNode> operacion) {
        String mensaje = (edicion ? "Actualizando " : "Creando ") + entidad + "...";
        ejecutar(mensaje, operacion, respuesta -> cargarTodo());
    }

    private void eliminarSeleccionado(JTable tabla, Eliminacion eliminar, String entidad) {
        int fila = filaSeleccionada(tabla);
        if (fila < 0) {
            return;
        }
        int id = ((Number) tabla.getModel().getValueAt(fila, 0)).intValue();
        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Seguro que querés eliminar " + entidad + "?", "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }
        ejecutar("Eliminando...", () -> {
            eliminar.ejecutar(id);
            return null;
        }, ignorado -> cargarTodo());
    }

    private void cargarTodo() {
        ejecutar("Cargando datos administrativos...", () -> new DatosAdministracion(
                carteleraApi.listarCategorias(),
                carteleraApi.listarPeliculas(),
                salasApi.listarSalas(),
                carteleraApi.listarFunciones(),
                productosApi.listarProductos()
        ), this::aplicarDatos);
    }

    private void aplicarDatos(DatosAdministracion datos) {
        limpiar(categoriasModel);
        categorias = new ArrayList<>();
        datos.categorias().forEach(item -> {
            categoriasModel.addRow(new Object[]{item.path("id").asInt(), item.path("nombre").asText()});
            categorias.add(new Opcion(item.path("id").asInt(), item.path("nombre").asText()));
        });

        limpiar(peliculasModel);
        peliculas = new ArrayList<>();
        portadasPeliculas.clear();
        datos.peliculas().forEach(item -> {
            int peliculaId = item.path("id").asInt();
            String portada = item.path("portadaUrl").asText("");
            portadasPeliculas.put(peliculaId, portada);
            peliculasModel.addRow(new Object[]{
                    peliculaId, item.path("titulo").asText(), item.path("duracion").asInt(),
                    item.path("categoriaNombre").asText(), item.path("descripcion").asText(),
                    portada.isBlank() ? "Sin portada" : "Cargada"
            });
            peliculas.add(new Opcion(peliculaId, item.path("titulo").asText()));
        });

        limpiar(salasModel);
        salas = new ArrayList<>();
        datos.salas().forEach(item -> {
            salasModel.addRow(new Object[]{
                    item.path("id").asInt(), item.path("nombre").asText(),
                    item.path("capacidad").asInt(),
                    nombreEstadoSala(item.path("estado").asText("DISPONIBLE")),
                    item.path("detalleEstado").isNull() || item.path("detalleEstado").asText().isBlank()
                            ? "-"
                            : item.path("detalleEstado").asText()
            });
            salas.add(new Opcion(item.path("id").asInt(), item.path("nombre").asText()));
        });

        limpiar(funcionesModel);
        datos.funciones().forEach(item -> funcionesModel.addRow(new Object[]{
                item.path("id").asInt(), item.path("fecha").asText(), item.path("horario").asText(),
                item.path("peliculaId").asInt(), item.path("peliculaTitulo").asText(),
                item.path("salaId").asInt(), item.path("salaNombre").asText(),
                nombreFormato(item.path("formato").asText()), nombreIdioma(item.path("idioma").asText()),
                item.path("precioEntrada").asDouble()
        }));

        limpiar(productosModel);
        imagenesProductos.clear();
        datos.productos().forEach(item -> {
            int productoId = item.path("id").asInt();
            String imagen = item.path("imagenUrl").asText("");
            imagenesProductos.put(productoId, imagen);
            productosModel.addRow(new Object[]{
                    productoId, item.path("nombre").asText(), item.path("precio").asDouble(),
                    item.path("tipo").asText(), item.path("tamano").asText(),
                    item.path("descripcion").asText(""),
                    imagen.isBlank() ? "Sin imagen" : "Cargada"
            });
        });
    }

    private int filaSeleccionada(JTable tabla) {
        int filaVista = tabla.getSelectedRow();
        if (filaVista < 0) {
            aviso("Seleccioná primero un registro.");
            return -1;
        }
        return tabla.convertRowIndexToModel(filaVista);
    }

    private void aviso(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Atención", JOptionPane.WARNING_MESSAGE);
    }

    private <T> void ejecutar(String mensaje, Operacion<T> operacion, Consumer<T> alCompletar) {
        if (!activo) {
            return;
        }
        actualizarEstado(mensaje);
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return operacion.ejecutar();
            }

            @Override
            protected void done() {
                if (!activo) {
                    return;
                }
                try {
                    alCompletar.accept(get());
                    actualizarEstado("Operación completada");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    actualizarEstado("Operación interrumpida");
                } catch (ExecutionException e) {
                    actualizarEstado("La operación falló");
                    JOptionPane.showMessageDialog(AdministracionPanel.this,
                            e.getCause().getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void actualizarEstado(String mensaje) {
        estado.setText(mensaje);
        estadoExterno.accept(mensaje);
    }

    private static int id(DefaultTableModel model, int fila) {
        return ((Number) model.getValueAt(fila, 0)).intValue();
    }

    private static int numero(DefaultTableModel model, int fila, int columna) {
        return ((Number) model.getValueAt(fila, columna)).intValue();
    }

    private static double decimal(DefaultTableModel model, int fila, int columna) {
        return ((Number) model.getValueAt(fila, columna)).doubleValue();
    }

    private static String valor(DefaultTableModel model, int fila, int columna) {
        Object valor = model.getValueAt(fila, columna);
        return valor == null ? "" : valor.toString();
    }

    private record DatosAdministracion(JsonNode categorias, JsonNode peliculas, JsonNode salas,
                                       JsonNode funciones, JsonNode productos) {
    }

    @FunctionalInterface
    private interface Eliminacion {
        void ejecutar(int id) throws Exception;
    }

    @FunctionalInterface
    private interface Operacion<T> {
        T ejecutar() throws Exception;
    }
}
