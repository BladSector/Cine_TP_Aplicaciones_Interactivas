package dao;

import modelo.Butaca;
import modelo.Sala;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ButacaDAO {
    public void guardar(Butaca butaca) throws SQLException {
        if (butaca.getSala().getId() == 0) {
            SalaDAO salaDAO = new SalaDAO();
            salaDAO.guardar(butaca.getSala());
        }

        String sql = "INSERT INTO butaca (fila, numero, ocupada, sala_id) VALUES (?, ?, ?, ?)";

        try (Connection conexion = ConexionMySQL.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, butaca.getFila());
            ps.setInt(2, butaca.getNumero());
            ps.setBoolean(3, butaca.isOcupada());
            ps.setInt(4, butaca.getSala().getId());
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();

            if (rs.next()) {
                butaca.asignarId(rs.getInt(1));
            }
        }
    }

    public Butaca buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM butaca WHERE id = ?";

        try (Connection conexion = ConexionMySQL.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return crearButacaDesdeResultSet(rs);
            }
        }

        return null;
    }

    public List<Butaca> listar() throws SQLException {
        String sql = "SELECT * FROM butaca";
        List<Butaca> butacas = new ArrayList<>();

        try (Connection conexion = ConexionMySQL.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                butacas.add(crearButacaDesdeResultSet(rs));
            }
        }

        return butacas;
    }

    public List<Butaca> listarPorSala(int salaId) throws SQLException {
        String sql = "SELECT * FROM butaca WHERE sala_id = ?";
        List<Butaca> butacas = new ArrayList<>();

        try (Connection conexion = ConexionMySQL.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, salaId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                butacas.add(crearButacaDesdeResultSet(rs));
            }
        }

        return butacas;
    }

    public void actualizar(Butaca butaca) throws SQLException {
        String sql = "UPDATE butaca SET fila = ?, numero = ?, ocupada = ?, sala_id = ? WHERE id = ?";

        try (Connection conexion = ConexionMySQL.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, butaca.getFila());
            ps.setInt(2, butaca.getNumero());
            ps.setBoolean(3, butaca.isOcupada());
            ps.setInt(4, butaca.getSala().getId());
            ps.setInt(5, butaca.getId());
            ps.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM butaca WHERE id = ?";

        try (Connection conexion = ConexionMySQL.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Butaca crearButacaDesdeResultSet(ResultSet rs) throws SQLException {
        SalaDAO salaDAO = new SalaDAO();
        Sala sala = salaDAO.buscarPorId(rs.getInt("sala_id"));

        Butaca butaca = new Butaca(rs.getString("fila"), rs.getInt("numero"), sala);

        if (rs.getBoolean("ocupada")) {
            butaca.ocupar();
        }

        butaca.asignarId(rs.getInt("id"));
        return butaca;
    }
}
