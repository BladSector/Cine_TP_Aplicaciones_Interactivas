package dao;
import modelo.Categoria;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAO {
    public void guardar(Categoria categoria) throws SQLException {
        String sql = "INSERT INTO categoria (nombre) VALUES (?)";

        try (Connection conexion = ConexionMySQL.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, categoria.getNombre());
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();

            if (rs.next()) {
                categoria.asignarId(rs.getInt(1));
            }
        }
    }

    public Categoria buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM categoria WHERE id = ?";

        try (Connection conexion = ConexionMySQL.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Categoria categoria = new Categoria(rs.getString("nombre"));
                categoria.asignarId(rs.getInt("id"));
                return categoria;
            }
        }

        return null;
    }

    public List<Categoria> listar() throws SQLException {
        String sql = "SELECT * FROM categoria";
        List<Categoria> categorias = new ArrayList<>();

        try (Connection conexion = ConexionMySQL.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Categoria categoria = new Categoria(rs.getString("nombre"));
                categoria.asignarId(rs.getInt("id"));
                categorias.add(categoria);
            }
        }

        return categorias;
    }

    public void actualizar(Categoria categoria) throws SQLException {
        String sql = "UPDATE categoria SET nombre = ? WHERE id = ?";

        try (Connection conexion = ConexionMySQL.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, categoria.getNombre());
            ps.setInt(2, categoria.getId());
            ps.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM categoria WHERE id = ?";

        try (Connection conexion = ConexionMySQL.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
