import dao.*;
import modelo.*;

import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);

    private static final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private static final PeliculaDAO peliculaDAO = new PeliculaDAO();
    private static final SalaDAO salaDAO = new SalaDAO();
    private static final ButacaDAO butacaDAO = new ButacaDAO();
    private static final FuncionDAO funcionDAO = new FuncionDAO();
    private static final MetodoDePagoDAO metodoDePagoDAO = new MetodoDePagoDAO();
    private static final EspectadorDAO espectadorDAO = new EspectadorDAO();
    private static final EntradaDAO entradaDAO = new EntradaDAO();

    public static void main(String[] args) {
        int opcion;

        do {
            mostrarMenuPrincipal();
            opcion = leerEntero("Elegí una opción: ");

            try {
                switch (opcion) {
                    case 1:
                        probarConexion();
                        break;
                    case 2:
                        menuCategorias();
                        break;
                    case 3:
                        menuPeliculas();
                        break;
                    case 4:
                        menuSalas();
                        break;
                    case 5:
                        menuButacas();
                        break;
                    case 6:
                        menuFunciones();
                        break;
                    case 7:
                        menuMetodosDePago();
                        break;
                    case 8:
                        menuEspectadores();
                        break;
                    case 9:
                        menuEntradas();
                        break;
                    case 0:
                        System.out.println("Saliendo del sistema...");
                        break;
                    default:
                        System.out.println("Opción inválida.");
                        break;
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                e.printStackTrace();
            }

            System.out.println();
        } while (opcion != 0);

        scanner.close();
    }

    private static void mostrarMenuPrincipal() {
        System.out.println("===== MENÚ TP CINE API =====");
        System.out.println("1. Probar conexión con MySQL");
        System.out.println("2. Categorías");
        System.out.println("3. Películas");
        System.out.println("4. Salas");
        System.out.println("5. Butacas");
        System.out.println("6. Funciones");
        System.out.println("7. Métodos de pago");
        System.out.println("8. Espectadores");
        System.out.println("9. Entradas");
        System.out.println("0. Salir");
    }

    private static void mostrarMenuCrud(String titulo) {
        System.out.println("===== " + titulo.toUpperCase() + " =====");
        System.out.println("1. Guardar");
        System.out.println("2. Buscar por ID");
        System.out.println("3. Listar");
        System.out.println("4. Actualizar");
        System.out.println("5. Eliminar");
        System.out.println("0. Volver");
    }

    private static void probarConexion() throws Exception {
        try (Connection conexion = ConexionMySQL.obtenerConexion()) {
            System.out.println("Conexión exitosa con MySQL.");
        }
    }

    private static void menuCategorias() throws Exception {
        int opcion;

        do {
            mostrarMenuCrud("Categorías");
            opcion = leerEntero("Elegí una opción: ");

            switch (opcion) {
                case 1:
                    guardarCategoria();
                    break;
                case 2:
                    buscarCategoria();
                    break;
                case 3:
                    listarCategorias();
                    break;
                case 4:
                    actualizarCategoria();
                    break;
                case 5:
                    eliminarCategoria();
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opción inválida.");
                    break;
            }

            System.out.println();
        } while (opcion != 0);
    }

    private static void guardarCategoria() throws Exception {
        String nombre = leerTexto("Nombre de la categoría: ");
        Categoria categoria = new Categoria(nombre);
        categoriaDAO.guardar(categoria);
        System.out.println("Categoría guardada con ID: " + categoria.getId());
    }

    private static void buscarCategoria() throws Exception {
        Categoria categoria = categoriaDAO.buscarPorId(leerEntero("ID de la categoría: "));
        mostrarCategoria(categoria);
    }

    private static void listarCategorias() throws Exception {
        List<Categoria> categorias = categoriaDAO.listar();

        if (categorias.isEmpty()) {
            System.out.println("No hay categorías guardadas.");
            return;
        }

        for (Categoria categoria : categorias) {
            mostrarCategoria(categoria);
        }
    }

    private static void actualizarCategoria() throws Exception {
        int id = leerEntero("ID de la categoría a actualizar: ");
        String nombre = leerTexto("Nuevo nombre: ");

        Categoria categoria = new Categoria(nombre);
        categoria.asignarId(id);
        categoriaDAO.actualizar(categoria);

        System.out.println("Categoría actualizada correctamente.");
    }

    private static void eliminarCategoria() throws Exception {
        int id = leerEntero("ID de la categoría a eliminar: ");
        categoriaDAO.eliminar(id);
        System.out.println("Categoría eliminada correctamente.");
    }

    private static void menuPeliculas() throws Exception {
        int opcion;

        do {
            mostrarMenuCrud("Películas");
            opcion = leerEntero("Elegí una opción: ");

            switch (opcion) {
                case 1:
                    guardarPelicula();
                    break;
                case 2:
                    buscarPelicula();
                    break;
                case 3:
                    listarPeliculas();
                    break;
                case 4:
                    actualizarPelicula();
                    break;
                case 5:
                    eliminarPelicula();
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opción inválida.");
                    break;
            }

            System.out.println();
        } while (opcion != 0);
    }

    private static void guardarPelicula() throws Exception {
        String titulo = leerTexto("Título: ");
        int duracion = leerEntero("Duración en minutos: ");
        Categoria categoria = pedirCategoria();

        Pelicula pelicula = new Pelicula(titulo, duracion, categoria);
        peliculaDAO.guardar(pelicula);

        System.out.println("Película guardada con ID: " + pelicula.getId());
    }

    private static void buscarPelicula() throws Exception {
        Pelicula pelicula = peliculaDAO.buscarPorId(leerEntero("ID de la película: "));
        mostrarPelicula(pelicula);
    }

    private static void listarPeliculas() throws Exception {
        List<Pelicula> peliculas = peliculaDAO.listar();

        if (peliculas.isEmpty()) {
            System.out.println("No hay películas guardadas.");
            return;
        }

        for (Pelicula pelicula : peliculas) {
            mostrarPelicula(pelicula);
        }
    }

    private static void actualizarPelicula() throws Exception {
        int id = leerEntero("ID de la película a actualizar: ");
        String titulo = leerTexto("Nuevo título: ");
        int duracion = leerEntero("Nueva duración en minutos: ");
        Categoria categoria = pedirCategoria();

        Pelicula pelicula = new Pelicula(titulo, duracion, categoria);
        pelicula.asignarId(id);
        peliculaDAO.actualizar(pelicula);

        System.out.println("Película actualizada correctamente.");
    }

    private static void eliminarPelicula() throws Exception {
        int id = leerEntero("ID de la película a eliminar: ");
        peliculaDAO.eliminar(id);
        System.out.println("Película eliminada correctamente.");
    }

    private static void menuSalas() throws Exception {
        int opcion;

        do {
            mostrarMenuCrud("Salas");
            opcion = leerEntero("Elegí una opción: ");

            switch (opcion) {
                case 1:
                    guardarSala();
                    break;
                case 2:
                    buscarSala();
                    break;
                case 3:
                    listarSalas();
                    break;
                case 4:
                    actualizarSala();
                    break;
                case 5:
                    eliminarSala();
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opción inválida.");
                    break;
            }

            System.out.println();
        } while (opcion != 0);
    }

    private static void guardarSala() throws Exception {
        String nombre = leerTexto("Nombre de la sala: ");
        int capacidad = leerEntero("Capacidad: ");

        Sala sala = new Sala(nombre, capacidad);
        salaDAO.guardar(sala);

        System.out.println("Sala guardada con ID: " + sala.getId());
    }

    private static void buscarSala() throws Exception {
        Sala sala = salaDAO.buscarPorId(leerEntero("ID de la sala: "));
        mostrarSala(sala);
    }

    private static void listarSalas() throws Exception {
        List<Sala> salas = salaDAO.listar();

        if (salas.isEmpty()) {
            System.out.println("No hay salas guardadas.");
            return;
        }

        for (Sala sala : salas) {
            mostrarSala(sala);
        }
    }

    private static void actualizarSala() throws Exception {
        int id = leerEntero("ID de la sala a actualizar: ");
        String nombre = leerTexto("Nuevo nombre: ");
        int capacidad = leerEntero("Nueva capacidad: ");

        Sala sala = new Sala(nombre, capacidad);
        sala.asignarId(id);
        salaDAO.actualizar(sala);

        System.out.println("Sala actualizada correctamente.");
    }

    private static void eliminarSala() throws Exception {
        int id = leerEntero("ID de la sala a eliminar: ");
        salaDAO.eliminar(id);
        System.out.println("Sala eliminada correctamente.");
    }

    private static void menuButacas() throws Exception {
        int opcion;

        do {
            mostrarMenuCrud("Butacas");
            opcion = leerEntero("Elegí una opción: ");

            switch (opcion) {
                case 1:
                    guardarButaca();
                    break;
                case 2:
                    buscarButaca();
                    break;
                case 3:
                    listarButacas();
                    break;
                case 4:
                    actualizarButaca();
                    break;
                case 5:
                    eliminarButaca();
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opción inválida.");
                    break;
            }

            System.out.println();
        } while (opcion != 0);
    }

    private static void guardarButaca() throws Exception {
        String fila = leerTexto("Fila: ");
        int numero = leerEntero("Número: ");
        Sala sala = pedirSala();

        Butaca butaca = new Butaca(fila, numero, sala);
        butacaDAO.guardar(butaca);

        System.out.println("Butaca guardada con ID: " + butaca.getId());
    }

    private static void buscarButaca() throws Exception {
        Butaca butaca = butacaDAO.buscarPorId(leerEntero("ID de la butaca: "));
        mostrarButaca(butaca);
    }

    private static void listarButacas() throws Exception {
        int salaId = leerEntero("ID de sala para filtrar, o 0 para listar todas: ");
        List<Butaca> butacas = salaId == 0 ? butacaDAO.listar() : butacaDAO.listarPorSala(salaId);

        if (butacas.isEmpty()) {
            System.out.println("No hay butacas guardadas.");
            return;
        }

        for (Butaca butaca : butacas) {
            mostrarButaca(butaca);
        }
    }

    private static void actualizarButaca() throws Exception {
        int id = leerEntero("ID de la butaca a actualizar: ");
        String fila = leerTexto("Nueva fila: ");
        int numero = leerEntero("Nuevo número: ");
        Sala sala = pedirSala();
        boolean ocupada = leerBoolean("¿Está ocupada? (s/n): ");

        Butaca butaca = new Butaca(fila, numero, sala);
        butaca.asignarId(id);

        if (ocupada) {
            butaca.ocupar();
        }

        butacaDAO.actualizar(butaca);
        System.out.println("Butaca actualizada correctamente.");
    }

    private static void eliminarButaca() throws Exception {
        int id = leerEntero("ID de la butaca a eliminar: ");
        butacaDAO.eliminar(id);
        System.out.println("Butaca eliminada correctamente.");
    }

    private static void menuFunciones() throws Exception {
        int opcion;

        do {
            mostrarMenuCrud("Funciones");
            opcion = leerEntero("Elegí una opción: ");

            switch (opcion) {
                case 1:
                    guardarFuncion();
                    break;
                case 2:
                    buscarFuncion();
                    break;
                case 3:
                    listarFunciones();
                    break;
                case 4:
                    actualizarFuncion();
                    break;
                case 5:
                    eliminarFuncion();
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opción inválida.");
                    break;
            }

            System.out.println();
        } while (opcion != 0);
    }

    private static void guardarFuncion() throws Exception {
        LocalDate fecha = leerFecha("Fecha (AAAA-MM-DD): ");
        LocalTime horario = leerHora("Horario (HH:MM): ");
        Pelicula pelicula = pedirPelicula();
        Sala sala = pedirSala();

        Funcion funcion = new Funcion(fecha, horario, pelicula, sala);
        funcionDAO.guardar(funcion);

        System.out.println("Función guardada con ID: " + funcion.getId());
    }

    private static void buscarFuncion() throws Exception {
        Funcion funcion = funcionDAO.buscarPorId(leerEntero("ID de la función: "));
        mostrarFuncion(funcion);
    }

    private static void listarFunciones() throws Exception {
        List<Funcion> funciones = funcionDAO.listar();

        if (funciones.isEmpty()) {
            System.out.println("No hay funciones guardadas.");
            return;
        }

        for (Funcion funcion : funciones) {
            mostrarFuncion(funcion);
        }
    }

    private static void actualizarFuncion() throws Exception {
        int id = leerEntero("ID de la función a actualizar: ");
        LocalDate fecha = leerFecha("Nueva fecha (AAAA-MM-DD): ");
        LocalTime horario = leerHora("Nuevo horario (HH:MM): ");
        Pelicula pelicula = pedirPelicula();
        Sala sala = pedirSala();

        Funcion funcion = new Funcion(fecha, horario, pelicula, sala);
        funcion.asignarId(id);
        funcionDAO.actualizar(funcion);

        System.out.println("Función actualizada correctamente.");
    }

    private static void eliminarFuncion() throws Exception {
        int id = leerEntero("ID de la función a eliminar: ");
        funcionDAO.eliminar(id);
        System.out.println("Función eliminada correctamente.");
    }

    private static void menuMetodosDePago() throws Exception {
        int opcion;

        do {
            mostrarMenuCrud("Métodos de pago");
            opcion = leerEntero("Elegí una opción: ");

            switch (opcion) {
                case 1:
                    guardarMetodoDePago();
                    break;
                case 2:
                    buscarMetodoDePago();
                    break;
                case 3:
                    listarMetodosDePago();
                    break;
                case 4:
                    actualizarMetodoDePago();
                    break;
                case 5:
                    eliminarMetodoDePago();
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opción inválida.");
                    break;
            }

            System.out.println();
        } while (opcion != 0);
    }

    private static void guardarMetodoDePago() throws Exception {
        MetodoDePago metodoDePago = pedirDatosMetodoDePago();
        metodoDePagoDAO.guardar(metodoDePago);
        System.out.println("Método de pago guardado con ID: " + metodoDePago.getId());
    }

    private static void buscarMetodoDePago() throws Exception {
        MetodoDePago metodoDePago = metodoDePagoDAO.buscarPorId(leerEntero("ID del método de pago: "));
        mostrarMetodoDePago(metodoDePago);
    }

    private static void listarMetodosDePago() throws Exception {
        List<MetodoDePago> metodos = metodoDePagoDAO.listar();

        if (metodos.isEmpty()) {
            System.out.println("No hay métodos de pago guardados.");
            return;
        }

        for (MetodoDePago metodoDePago : metodos) {
            mostrarMetodoDePago(metodoDePago);
        }
    }

    private static void actualizarMetodoDePago() throws Exception {
        int id = leerEntero("ID del método de pago a actualizar: ");
        MetodoDePago metodoDePago = pedirDatosMetodoDePago();
        metodoDePago.asignarId(id);
        metodoDePagoDAO.actualizar(metodoDePago);
        System.out.println("Método de pago actualizado correctamente.");
    }

    private static void eliminarMetodoDePago() throws Exception {
        int id = leerEntero("ID del método de pago a eliminar: ");
        metodoDePagoDAO.eliminar(id);
        System.out.println("Método de pago eliminado correctamente.");
    }

    private static void menuEspectadores() throws Exception {
        int opcion;

        do {
            mostrarMenuCrud("Espectadores");
            opcion = leerEntero("Elegí una opción: ");

            switch (opcion) {
                case 1:
                    guardarEspectador();
                    break;
                case 2:
                    buscarEspectador();
                    break;
                case 3:
                    listarEspectadores();
                    break;
                case 4:
                    actualizarEspectador();
                    break;
                case 5:
                    eliminarEspectador();
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opción inválida.");
                    break;
            }

            System.out.println();
        } while (opcion != 0);
    }

    private static void guardarEspectador() throws Exception {
        Espectador espectador = pedirDatosEspectador();
        espectadorDAO.guardar(espectador);
        System.out.println("Espectador guardado con ID: " + espectador.getId());
    }

    private static void buscarEspectador() throws Exception {
        Espectador espectador = espectadorDAO.buscarPorId(leerEntero("ID del espectador: "));
        mostrarEspectador(espectador);
    }

    private static void listarEspectadores() throws Exception {
        List<Espectador> espectadores = espectadorDAO.listar();

        if (espectadores.isEmpty()) {
            System.out.println("No hay espectadores guardados.");
            return;
        }

        for (Espectador espectador : espectadores) {
            mostrarEspectador(espectador);
        }
    }

    private static void actualizarEspectador() throws Exception {
        int id = leerEntero("ID del espectador a actualizar: ");
        Espectador espectador = pedirDatosEspectador();
        espectador.asignarId(id);
        espectadorDAO.actualizar(espectador);
        System.out.println("Espectador actualizado correctamente.");
    }

    private static void eliminarEspectador() throws Exception {
        int id = leerEntero("ID del espectador a eliminar: ");
        espectadorDAO.eliminar(id);
        System.out.println("Espectador eliminado correctamente.");
    }

    private static void menuEntradas() throws Exception {
        int opcion;

        do {
            mostrarMenuCrud("Entradas");
            opcion = leerEntero("Elegí una opción: ");

            switch (opcion) {
                case 1:
                    guardarEntrada();
                    break;
                case 2:
                    buscarEntrada();
                    break;
                case 3:
                    listarEntradas();
                    break;
                case 4:
                    actualizarEntrada();
                    break;
                case 5:
                    eliminarEntrada();
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opción inválida.");
                    break;
            }

            System.out.println();
        } while (opcion != 0);
    }

    private static void guardarEntrada() throws Exception {
        Funcion funcion = pedirFuncion();
        Butaca butaca = pedirButaca();
        double precio = leerDouble("Precio: ");
        int espectadorId = leerEntero("ID del espectador, o 0 si no querés asociarlo: ");

        Entrada entrada = funcion.venderEntrada(precio, butaca);
        butacaDAO.actualizar(butaca);
        entradaDAO.guardar(entrada, espectadorId == 0 ? null : espectadorId);

        System.out.println("Entrada guardada con ID: " + entrada.getId());
    }

    private static void buscarEntrada() throws Exception {
        Entrada entrada = entradaDAO.buscarPorId(leerEntero("ID de la entrada: "));
        mostrarEntrada(entrada);
    }

    private static void listarEntradas() throws Exception {
        List<Entrada> entradas = entradaDAO.listar();

        if (entradas.isEmpty()) {
            System.out.println("No hay entradas guardadas.");
            return;
        }

        for (Entrada entrada : entradas) {
            mostrarEntrada(entrada);
        }
    }

    private static void actualizarEntrada() throws Exception {
        int id = leerEntero("ID de la entrada a actualizar: ");
        Funcion funcion = pedirFuncion();
        Butaca butaca = pedirButaca();
        double precio = leerDouble("Nuevo precio: ");
        LocalDateTime horario = leerFechaHora();
        boolean reembolsada = leerBoolean("¿Está reembolsada? (s/n): ");
        int espectadorId = leerEntero("ID del espectador, o 0 si no querés asociarlo: ");

        Entrada entrada = new Entrada(precio, funcion, butaca, horario);
        entrada.asignarId(id);
        entrada.asignarReembolsada(reembolsada);
        entradaDAO.actualizar(entrada, espectadorId == 0 ? null : espectadorId);

        System.out.println("Entrada actualizada correctamente.");
    }

    private static void eliminarEntrada() throws Exception {
        int id = leerEntero("ID de la entrada a eliminar: ");
        entradaDAO.eliminar(id);
        System.out.println("Entrada eliminada correctamente.");
    }

    private static Categoria pedirCategoria() throws Exception {
        int categoriaId = leerEntero("ID de la categoría existente, o 0 para crear una nueva: ");

        if (categoriaId == 0) {
            Categoria categoria = new Categoria(leerTexto("Nombre de la nueva categoría: "));
            categoriaDAO.guardar(categoria);
            return categoria;
        }

        Categoria categoria = categoriaDAO.buscarPorId(categoriaId);

        if (categoria == null) {
            throw new IllegalArgumentException("No existe una categoría con ese ID.");
        }

        return categoria;
    }

    private static Pelicula pedirPelicula() throws Exception {
        Pelicula pelicula = peliculaDAO.buscarPorId(leerEntero("ID de la película: "));

        if (pelicula == null) {
            throw new IllegalArgumentException("No existe una película con ese ID.");
        }

        return pelicula;
    }

    private static Sala pedirSala() throws Exception {
        Sala sala = salaDAO.buscarPorId(leerEntero("ID de la sala: "));

        if (sala == null) {
            throw new IllegalArgumentException("No existe una sala con ese ID.");
        }

        return sala;
    }

    private static Butaca pedirButaca() throws Exception {
        Butaca butaca = butacaDAO.buscarPorId(leerEntero("ID de la butaca: "));

        if (butaca == null) {
            throw new IllegalArgumentException("No existe una butaca con ese ID.");
        }

        return butaca;
    }

    private static Funcion pedirFuncion() throws Exception {
        Funcion funcion = funcionDAO.buscarPorId(leerEntero("ID de la función: "));

        if (funcion == null) {
            throw new IllegalArgumentException("No existe una función con ese ID.");
        }

        return funcion;
    }

    private static MetodoDePago pedirMetodoDePagoOpcional() throws Exception {
        int metodoPagoId = leerEntero("ID del método de pago, o 0 si no querés asociarlo: ");

        if (metodoPagoId == 0) {
            return null;
        }

        MetodoDePago metodoDePago = metodoDePagoDAO.buscarPorId(metodoPagoId);

        if (metodoDePago == null) {
            throw new IllegalArgumentException("No existe un método de pago con ese ID.");
        }

        return metodoDePago;
    }

    private static MetodoDePago pedirDatosMetodoDePago() {
        String numero = leerTexto("Número de tarjeta (16 dígitos): ");
        YearMonth fechaVencimiento = leerYearMonth("Fecha de vencimiento (AAAA-MM): ");
        String nombre = leerTexto("Nombre del titular: ");
        String apellido = leerTexto("Apellido del titular: ");
        String cvv = leerTexto("CVV (3 dígitos): ");

        return new MetodoDePago(numero, fechaVencimiento, nombre, apellido, cvv);
    }

    private static Espectador pedirDatosEspectador() throws Exception {
        String nombre = leerTexto("Nombre: ");
        String apellido = leerTexto("Apellido: ");
        String email = leerTexto("Email: ");
        String contrasenia = leerTexto("Contraseña: ");
        boolean emailVerificado = leerBoolean("¿Email verificado? (s/n): ");
        MetodoDePago metodoDePago = pedirMetodoDePagoOpcional();

        Espectador espectador = new Espectador(nombre, apellido, email, contrasenia);

        if (emailVerificado) {
            espectador.verificarMail();
        }

        if (metodoDePago != null) {
            espectador.agregarMetodoDePago(metodoDePago);
        }

        return espectador;
    }

    private static void mostrarCategoria(Categoria categoria) {
        if (categoria == null) {
            System.out.println("No se encontró la categoría.");
            return;
        }

        System.out.println(categoria.getId() + " - " + categoria.getNombre());
    }

    private static void mostrarPelicula(Pelicula pelicula) {
        if (pelicula == null) {
            System.out.println("No se encontró la película.");
            return;
        }

        System.out.println(pelicula.getId() + " - " + pelicula.getTitulo()
                + " - " + pelicula.getDuracion() + " min"
                + " - categoría: " + pelicula.getCategoria().getNombre());
    }

    private static void mostrarSala(Sala sala) {
        if (sala == null) {
            System.out.println("No se encontró la sala.");
            return;
        }

        System.out.println(sala.getId() + " - " + sala.getNombre()
                + " - capacidad: " + sala.getCapacidad());
    }

    private static void mostrarButaca(Butaca butaca) {
        if (butaca == null) {
            System.out.println("No se encontró la butaca.");
            return;
        }

        System.out.println(butaca.getId() + " - fila " + butaca.getFila()
                + " - número " + butaca.getNumero()
                + " - sala: " + butaca.getSala().getNombre()
                + " - ocupada: " + butaca.isOcupada());
    }

    private static void mostrarFuncion(Funcion funcion) {
        if (funcion == null) {
            System.out.println("No se encontró la función.");
            return;
        }

        System.out.println(funcion.getId() + " - " + funcion.getFecha()
                + " " + funcion.getHorario()
                + " - película: " + funcion.getPelicula().getTitulo()
                + " - sala: " + funcion.getSala().getNombre());
    }

    private static void mostrarMetodoDePago(MetodoDePago metodoDePago) {
        if (metodoDePago == null) {
            System.out.println("No se encontró el método de pago.");
            return;
        }

        System.out.println(metodoDePago.getId() + " - " + metodoDePago.getNombre()
                + " " + metodoDePago.getApellido()
                + " - vencimiento: " + metodoDePago.getFechaVencimiento()
                + " - tarjeta: ****" + metodoDePago.getNumero().substring(12));
    }

    private static void mostrarEspectador(Espectador espectador) {
        if (espectador == null) {
            System.out.println("No se encontró el espectador.");
            return;
        }

        String metodoPago = espectador.getMetodoDePago() == null
                ? "sin método de pago"
                : "método de pago ID " + espectador.getMetodoDePago().getId();

        System.out.println(espectador.getId() + " - " + espectador.getNombre()
                + " " + espectador.getApellido()
                + " - " + espectador.getEmail()
                + " - email verificado: " + espectador.isEmailVerificado()
                + " - " + metodoPago);
    }

    private static void mostrarEntrada(Entrada entrada) {
        if (entrada == null) {
            System.out.println("No se encontró la entrada.");
            return;
        }

        System.out.println(entrada.getId()
                + " - precio: " + entrada.getPrecio()
                + " - función ID: " + entrada.getFuncion().getId()
                + " - butaca ID: " + entrada.getButaca().getId()
                + " - horario: " + entrada.getHorario()
                + " - reembolsada: " + entrada.isReembolsada());
    }

    private static String leerTexto(String mensaje) {
        System.out.print(mensaje);
        return scanner.nextLine();
    }

    private static int leerEntero(String mensaje) {
        System.out.print(mensaje);

        while (!scanner.hasNextInt()) {
            System.out.println("Tenés que ingresar un número entero.");
            scanner.nextLine();
            System.out.print(mensaje);
        }

        int numero = scanner.nextInt();
        scanner.nextLine();
        return numero;
    }

    private static double leerDouble(String mensaje) {
        System.out.print(mensaje);

        while (!scanner.hasNextDouble()) {
            System.out.println("Tenés que ingresar un número.");
            scanner.nextLine();
            System.out.print(mensaje);
        }

        double numero = scanner.nextDouble();
        scanner.nextLine();
        return numero;
    }

    private static boolean leerBoolean(String mensaje) {
        String respuesta = leerTexto(mensaje);
        return respuesta.equalsIgnoreCase("s") || respuesta.equalsIgnoreCase("si");
    }

    private static LocalDate leerFecha(String mensaje) {
        while (true) {
            try {
                return LocalDate.parse(leerTexto(mensaje));
            } catch (Exception e) {
                System.out.println("Formato inválido. Usá AAAA-MM-DD.");
            }
        }
    }

    private static LocalTime leerHora(String mensaje) {
        while (true) {
            try {
                return LocalTime.parse(leerTexto(mensaje));
            } catch (Exception e) {
                System.out.println("Formato inválido. Usá HH:MM.");
            }
        }
    }

    private static YearMonth leerYearMonth(String mensaje) {
        while (true) {
            try {
                return YearMonth.parse(leerTexto(mensaje));
            } catch (Exception e) {
                System.out.println("Formato inválido. Usá AAAA-MM.");
            }
        }
    }

    private static LocalDateTime leerFechaHora() {
        LocalDate fecha = leerFecha("Fecha (AAAA-MM-DD): ");
        LocalTime horario = leerHora("Horario (HH:MM): ");
        return LocalDateTime.of(fecha, horario);
    }
}
