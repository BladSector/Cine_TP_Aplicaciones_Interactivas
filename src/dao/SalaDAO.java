package dao;

import modelo.Sala;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SalaDAO {
    public void guardar(Sala sala) throws SQLException {
        String sql = "INSERT INTO sala (nombre, capacidad) VALUES (?, ?)";

        try (Connection conexion = ConexionMySQL.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, sala.getNombre());
            ps.setInt(2, sala.getCapacidad());
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();

            if (rs.next()) {
                sala.asignarId(rs.getInt(1));
            }
        }
    }

    public Sala buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM sala WHERE id = ?";

        try (Connection conexion = ConexionMySQL.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return crearSalaDesdeResultSet(rs);
            }
        }

        return null;
    }

    public List<Sala> listar() throws SQLException {
        String sql = "SELECT * FROM sala";
        List<Sala> salas = new ArrayList<>();

        try (Connection conexion = ConexionMySQL.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                salas.add(crearSalaDesdeResultSet(rs));
            }
        }

        return salas;
    }

    public void actualizar(Sala sala) throws SQLException {
        String sql = "UPDATE sala SET nombre = ?, capacidad = ? WHERE id = ?";

        try (Connection conexion = ConexionMySQL.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, sala.getNombre());
            ps.setInt(2, sala.getCapacidad());
            ps.setInt(3, sala.getId());
            ps.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM sala WHERE id = ?";

        try (Connection conexion = ConexionMySQL.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Sala crearSalaDesdeResultSet(ResultSet rs) throws SQLException {
        Sala sala = new Sala(rs.getString("nombre"), rs.getInt("capacidad"));
        sala.asignarId(rs.getInt("id"));
        return sala;
    }
}
