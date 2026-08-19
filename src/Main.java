import dao.CategoriaDAO;
import dao.ConexionMySQL;
import modelo.Categoria;

import java.sql.Connection;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final CategoriaDAO categoriaDAO = new CategoriaDAO();

    public static void main(String[] args) {
        int opcion;

        do {
            mostrarMenu();
            opcion = leerEntero("Elegí una opción: ");

            try {
                switch (opcion) {
                    case 1:
                        probarConexion();
                        break;
                    case 2:
                        guardarCategoria();
                        break;
                    case 3:
                        buscarCategoria();
                        break;
                    case 4:
                        listarCategorias();
                        break;
                    case 5:
                        eliminarCategoria();
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

    private static void mostrarMenu() {
        System.out.println("===== MENÚ TP CINE API =====");
        System.out.println("1. Probar conexión con MySQL");
        System.out.println("2. Guardar categoría");
        System.out.println("3. Buscar categoría por ID");
        System.out.println("4. Listar categorías");
        System.out.println("5. Eliminar categoría");
        System.out.println("0. Salir");
    }

    private static void probarConexion() throws Exception {
        try (Connection conexion = ConexionMySQL.obtenerConexion()) {
            System.out.println("Conexión exitosa con MySQL.");
        }
    }

    private static void guardarCategoria() throws Exception {
        System.out.print("Nombre de la categoría: ");
        String nombre = scanner.nextLine();

        Categoria categoria = new Categoria(nombre);
        categoriaDAO.guardar(categoria);

        System.out.println("Categoría guardada con ID: " + categoria.getId());
    }

    private static void buscarCategoria() throws Exception {
        int id = leerEntero("ID de la categoría: ");
        Categoria categoria = categoriaDAO.buscarPorId(id);

        if (categoria == null) {
            System.out.println("No se encontró una categoría con ese ID.");
        } else {
            System.out.println("Categoría encontrada:");
            System.out.println(categoria.getId() + " - " + categoria.getNombre());
        }
    }

    private static void listarCategorias() throws Exception {
        List<Categoria> categorias = categoriaDAO.listar();

        if (categorias.isEmpty()) {
            System.out.println("No hay categorías guardadas.");
            return;
        }

        System.out.println("Categorías guardadas:");

        for (Categoria categoria : categorias) {
            System.out.println(categoria.getId() + " - " + categoria.getNombre());
        }
    }

    private static void eliminarCategoria() throws Exception {
        int id = leerEntero("ID de la categoría a eliminar: ");
        categoriaDAO.eliminar(id);

        System.out.println("Categoría eliminada correctamente.");
    }

    private static int leerEntero(String mensaje) {
        System.out.print(mensaje);

        while (!scanner.hasNextInt()) {
            System.out.println("Tenés que ingresar un número.");
            scanner.nextLine();
            System.out.print(mensaje);
        }

        int numero = scanner.nextInt();
        scanner.nextLine();
        return numero;
    }
}
