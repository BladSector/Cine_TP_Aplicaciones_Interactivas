package dao;

import modelo.Categoria;
import modelo.Pelicula;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PeliculaDAO {
    public void guardar(Pelicula pelicula) throws SQLException {
        String sql = "INSERT INTO pelicula (titulo, duracion, categoria_id) VALUES (?, ?, ?)";

        if (pelicula.getCategoria().getId() == 0) {
            CategoriaDAO categoriaDAO = new CategoriaDAO();
            categoriaDAO.guardar(pelicula.getCategoria());
        }

        try (Connection conexion = ConexionMySQL.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, pelicula.getTitulo());
            ps.setInt(2, pelicula.getDuracion());
            ps.setInt(3, pelicula.getCategoria().getId());
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();

            if (rs.next()) {
                pelicula.asignarId(rs.getInt(1));
            }
        }
    }

    public Pelicula buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM pelicula WHERE id = ?";

        try (Connection conexion = ConexionMySQL.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return crearPeliculaDesdeResultSet(rs);
            }
        }

        return null;
    }

    public List<Pelicula> listar() throws SQLException {
        String sql = "SELECT * FROM pelicula";
        List<Pelicula> peliculas = new ArrayList<>();

        try (Connection conexion = ConexionMySQL.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                peliculas.add(crearPeliculaDesdeResultSet(rs));
            }
        }

        return peliculas;
    }

    public void actualizar(Pelicula pelicula) throws SQLException {
        String sql = "UPDATE pelicula SET titulo = ?, duracion = ?, categoria_id = ? WHERE id = ?";

        try (Connection conexion = ConexionMySQL.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, pelicula.getTitulo());
            ps.setInt(2, pelicula.getDuracion());
            ps.setInt(3, pelicula.getCategoria().getId());
            ps.setInt(4, pelicula.getId());
            ps.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM pelicula WHERE id = ?";

        try (Connection conexion = ConexionMySQL.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Pelicula crearPeliculaDesdeResultSet(ResultSet rs) throws SQLException {
        CategoriaDAO categoriaDAO = new CategoriaDAO();
        Categoria categoria = categoriaDAO.buscarPorId(rs.getInt("categoria_id"));

        Pelicula pelicula = new Pelicula(rs.getString("titulo"), rs.getInt("duracion"), categoria);
        pelicula.asignarId(rs.getInt("id"));
        return pelicula;
    }
}
