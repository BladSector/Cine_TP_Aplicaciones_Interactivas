package dao;

import modelo.MetodoDePago;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class MetodoDePagoDAO {
    public void guardar(MetodoDePago metodoDePago) throws SQLException {
        String sql = "INSERT INTO metodo_pago (numero, fecha_vencimiento, nombre, apellido, cvv) VALUES (?, ?, ?, ?, ?)";

        try (Connection conexion = ConexionMySQL.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, metodoDePago.getNumero());
            ps.setString(2, metodoDePago.getFechaVencimiento().toString());
            ps.setString(3, metodoDePago.getNombre());
            ps.setString(4, metodoDePago.getApellido());
            ps.setString(5, metodoDePago.getCvv());
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();

            if (rs.next()) {
                metodoDePago.asignarId(rs.getInt(1));
            }
        }
    }

    public MetodoDePago buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM metodo_pago WHERE id = ?";

        try (Connection conexion = ConexionMySQL.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return crearMetodoDePagoDesdeResultSet(rs);
            }
        }

        return null;
    }

    public List<MetodoDePago> listar() throws SQLException {
        String sql = "SELECT * FROM metodo_pago";
        List<MetodoDePago> metodosDePago = new ArrayList<>();

        try (Connection conexion = ConexionMySQL.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                metodosDePago.add(crearMetodoDePagoDesdeResultSet(rs));
            }
        }

        return metodosDePago;
    }

    public void actualizar(MetodoDePago metodoDePago) throws SQLException {
        String sql = "UPDATE metodo_pago SET numero = ?, fecha_vencimiento = ?, nombre = ?, apellido = ?, cvv = ? WHERE id = ?";

        try (Connection conexion = ConexionMySQL.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, metodoDePago.getNumero());
            ps.setString(2, metodoDePago.getFechaVencimiento().toString());
            ps.setString(3, metodoDePago.getNombre());
            ps.setString(4, metodoDePago.getApellido());
            ps.setString(5, metodoDePago.getCvv());
            ps.setInt(6, metodoDePago.getId());
            ps.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM metodo_pago WHERE id = ?";

        try (Connection conexion = ConexionMySQL.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private MetodoDePago crearMetodoDePagoDesdeResultSet(ResultSet rs) throws SQLException {
        MetodoDePago metodoDePago = new MetodoDePago(
                rs.getString("numero"),
                YearMonth.parse(rs.getString("fecha_vencimiento")),
                rs.getString("nombre"),
                rs.getString("apellido"),
                rs.getString("cvv")
        );
        metodoDePago.asignarId(rs.getInt("id"));
        return metodoDePago;
    }
}
