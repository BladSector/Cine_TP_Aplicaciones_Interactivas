package dao;

import modelo.Espectador;
import modelo.MetodoDePago;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class EspectadorDAO {
    public void guardar(Espectador espectador) throws SQLException {
        String sql = "INSERT INTO espectador (nombre, apellido, email, contrasenia, email_verificado, metodo_pago_id) VALUES (?, ?, ?, ?, ?, ?)";
        MetodoDePago metodoDePago = espectador.getMetodoDePago();

        if (metodoDePago != null && metodoDePago.getId() == 0) {
            MetodoDePagoDAO metodoDePagoDAO = new MetodoDePagoDAO();
            metodoDePagoDAO.guardar(metodoDePago);
        }

        try (Connection conexion = ConexionMySQL.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, espectador.getNombre());
            ps.setString(2, espectador.getApellido());
            ps.setString(3, espectador.getEmail());
            ps.setString(4, espectador.getContrasenia());
            ps.setBoolean(5, espectador.isEmailVerificado());

            if (metodoDePago == null) {
                ps.setNull(6, Types.INTEGER);
            } else {
                ps.setInt(6, metodoDePago.getId());
            }

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();

            if (rs.next()) {
                espectador.asignarId(rs.getInt(1));
            }
        }
    }

    public Espectador buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM espectador WHERE id = ?";

        try (Connection conexion = ConexionMySQL.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return crearEspectadorDesdeResultSet(rs);
            }
        }

        return null;
    }

    public List<Espectador> listar() throws SQLException {
        String sql = "SELECT * FROM espectador";
        List<Espectador> espectadores = new ArrayList<>();

        try (Connection conexion = ConexionMySQL.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                espectadores.add(crearEspectadorDesdeResultSet(rs));
            }
        }

        return espectadores;
    }

    public void actualizar(Espectador espectador) throws SQLException {
        String sql = "UPDATE espectador SET nombre = ?, apellido = ?, email = ?, contrasenia = ?, email_verificado = ?, metodo_pago_id = ? WHERE id = ?";
        MetodoDePago metodoDePago = espectador.getMetodoDePago();

        try (Connection conexion = ConexionMySQL.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, espectador.getNombre());
            ps.setString(2, espectador.getApellido());
            ps.setString(3, espectador.getEmail());
            ps.setString(4, espectador.getContrasenia());
            ps.setBoolean(5, espectador.isEmailVerificado());

            if (metodoDePago == null) {
                ps.setNull(6, Types.INTEGER);
            } else {
                ps.setInt(6, metodoDePago.getId());
            }

            ps.setInt(7, espectador.getId());
            ps.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM espectador WHERE id = ?";

        try (Connection conexion = ConexionMySQL.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Espectador crearEspectadorDesdeResultSet(ResultSet rs) throws SQLException {
        Espectador espectador = new Espectador(
                rs.getString("nombre"),
                rs.getString("apellido"),
                rs.getString("email"),
                rs.getString("contrasenia")
        );
        espectador.asignarId(rs.getInt("id"));

        if (rs.getBoolean("email_verificado")) {
            espectador.verificarMail();
        }

        int metodoPagoId = rs.getInt("metodo_pago_id");

        if (!rs.wasNull()) {
            MetodoDePagoDAO metodoDePagoDAO = new MetodoDePagoDAO();
            MetodoDePago metodoDePago = metodoDePagoDAO.buscarPorId(metodoPagoId);
            espectador.agregarMetodoDePago(metodoDePago);
        }

        return espectador;
    }
}
