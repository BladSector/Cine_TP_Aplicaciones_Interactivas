package dao; // Indica que esta clase pertenece al paquete dao.

import modelo.Categoria; // Importa la clase Categoria del paquete modelo.

import java.sql.Connection; // Permite crear una conexion con la base de datos.
import java.sql.PreparedStatement; // Permite preparar consultas SQL con parametros.
import java.sql.SQLException; // Representa errores relacionados con SQL o la conexion.
import java.sql.Statement; // Se usa para pedir que MySQL devuelva el id generado.
import java.sql.ResultSet; // Guarda resultados de consultas SELECT o ids generados.
import java.util.ArrayList; // Lista modificable para guardar categorias.
import java.util.List; // Interfaz para manejar listas.

public class CategoriaDAO { // Clase encargada de acceder a la tabla categoria.

    public void guardar(Categoria categoria) throws SQLException { // Guarda una categoria en MySQL.
        String sql = "INSERT INTO categoria (nombre) VALUES (?)"; // Consulta SQL. El ? se reemplaza despues.

        try (Connection conexion = ConexionMySQL.obtenerConexion(); // Abre la conexion con MySQL.
             PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) { // Prepara el INSERT y pide el id generado.

            ps.setString(1, categoria.getNombre()); // Reemplaza el primer ? por el nombre de la categoria.
            ps.executeUpdate(); // Ejecuta el INSERT.

            ResultSet rs = ps.getGeneratedKeys(); // Obtiene el id AUTO_INCREMENT que genero MySQL.

            if (rs.next()) { // Si MySQL devolvio un id generado...
                categoria.asignarId(rs.getInt(1)); // Se lo asigna al objeto Categoria.
            }
        } // Se cierran automaticamente conexion y PreparedStatement.
    }

    public Categoria buscarPorId(int id) throws SQLException { // Busca una categoria por su id.
        String sql = "SELECT * FROM categoria WHERE id = ?"; // Consulta SQL con filtro por id.

        try (Connection conexion = ConexionMySQL.obtenerConexion(); // Abre la conexion.
             PreparedStatement ps = conexion.prepareStatement(sql)) { // Prepara el SELECT.

            ps.setInt(1, id); // Reemplaza el ? por el id recibido.
            ResultSet rs = ps.executeQuery(); // Ejecuta el SELECT y guarda el resultado.

            if (rs.next()) { // Si encontro una fila...
                Categoria categoria = new Categoria(rs.getString("nombre")); // Crea una Categoria con el nombre de MySQL.
                categoria.asignarId(rs.getInt("id")); // Le asigna el id que vino de MySQL.
                return categoria; // Devuelve la categoria encontrada.
            }
        }

        return null; // Si no encontro nada, devuelve null.
    }

    public List<Categoria> listar() throws SQLException { // Devuelve todas las categorias.
        String sql = "SELECT * FROM categoria"; // Consulta SQL sin filtro.
        List<Categoria> categorias = new ArrayList<>(); // Lista donde se van a guardar los objetos Categoria.

        try (Connection conexion = ConexionMySQL.obtenerConexion(); // Abre la conexion.
             PreparedStatement ps = conexion.prepareStatement(sql); // Prepara el SELECT.
             ResultSet rs = ps.executeQuery()) { // Ejecuta el SELECT.

            while (rs.next()) { // Recorre cada fila encontrada.
                Categoria categoria = new Categoria(rs.getString("nombre")); // Crea una Categoria con el nombre.
                categoria.asignarId(rs.getInt("id")); // Le asigna el id de MySQL.
                categorias.add(categoria); // Agrega la categoria a la lista.
            }
        }

        return categorias; // Devuelve la lista completa.
    }

    public void actualizar(Categoria categoria) throws SQLException { // Modifica una categoria existente.
        String sql = "UPDATE categoria SET nombre = ? WHERE id = ?"; // Cambia el nombre de la categoria con ese id.

        try (Connection conexion = ConexionMySQL.obtenerConexion(); // Abre la conexion.
             PreparedStatement ps = conexion.prepareStatement(sql)) { // Prepara el UPDATE.

            ps.setString(1, categoria.getNombre()); // Reemplaza el primer ? por el nuevo nombre.
            ps.setInt(2, categoria.getId()); // Reemplaza el segundo ? por el id de la categoria.
            ps.executeUpdate(); // Ejecuta el UPDATE.
        }
    }

    public void eliminar(int id) throws SQLException { // Elimina una categoria por id.
        String sql = "DELETE FROM categoria WHERE id = ?"; // Consulta SQL para borrar una fila.

        try (Connection conexion = ConexionMySQL.obtenerConexion(); // Abre la conexion.
             PreparedStatement ps = conexion.prepareStatement(sql)) { // Prepara el DELETE.

            ps.setInt(1, id); // Reemplaza el ? por el id recibido.
            ps.executeUpdate(); // Ejecuta el DELETE.
        }
    }
}