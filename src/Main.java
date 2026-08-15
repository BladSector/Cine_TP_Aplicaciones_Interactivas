import dao.ButacaDAO;
import dao.CategoriaDAO;
import dao.EntradaDAO;
import dao.EspectadorDAO;
import dao.FuncionDAO;
import dao.MetodoDePagoDAO;
import dao.PeliculaDAO;
import modelo.Butaca;
import modelo.Categoria;
import modelo.Entrada;
import modelo.Espectador;
import modelo.Funcion;
import modelo.MetodoDePago;
import modelo.Pelicula;
import modelo.Sala;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;

public class Main {
    public static void main(String[] args) {
        try {
            Categoria categoria = new Categoria("Accion");
            Pelicula pelicula = new Pelicula("Matrix", 136, categoria);
            Sala sala = new Sala("Sala 1", 100);
            Butaca butaca = new Butaca("A", 1, sala);
            Funcion funcion = new Funcion(
                    LocalDate.of(2026, 8, 20),
                    LocalTime.of(20, 30),
                    pelicula,
                    sala
            );

            MetodoDePago metodoDePago = new MetodoDePago(
                    "1234567812345678",
                    YearMonth.of(2028, 12),
                    "Santiago",
                    "Lopez",
                    "123"
            );

            Espectador espectador = new Espectador(
                    "Santiago",
                    "Lopez",
                    "santiago@test.com",
                    "1234"
            );

            espectador.verificarMail();
            espectador.agregarMetodoDePago(metodoDePago);

            CategoriaDAO categoriaDAO = new CategoriaDAO();
            PeliculaDAO peliculaDAO = new PeliculaDAO();
            ButacaDAO butacaDAO = new ButacaDAO();
            FuncionDAO funcionDAO = new FuncionDAO();
            MetodoDePagoDAO metodoDePagoDAO = new MetodoDePagoDAO();
            EspectadorDAO espectadorDAO = new EspectadorDAO();
            EntradaDAO entradaDAO = new EntradaDAO();

            categoriaDAO.guardar(categoria);
            peliculaDAO.guardar(pelicula);
            butacaDAO.guardar(butaca);
            funcionDAO.guardar(funcion);
            metodoDePagoDAO.guardar(metodoDePago);
            espectadorDAO.guardar(espectador);

            Entrada entrada = funcion.venderEntrada(3500, butaca);
            espectador.agregarEntrada(entrada);
            entradaDAO.guardar(entrada, espectador.getId());

            System.out.println("Datos guardados correctamente.");
            System.out.println("Categoria ID: " + categoria.getId());
            System.out.println("Pelicula ID: " + pelicula.getId());
            System.out.println("Sala ID: " + sala.getId());
            System.out.println("Butaca ID: " + butaca.getId());
            System.out.println("Funcion ID: " + funcion.getId());
            System.out.println("Metodo de pago ID: " + metodoDePago.getId());
            System.out.println("Espectador ID: " + espectador.getId());
            System.out.println("Entrada ID: " + entrada.getId());

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}