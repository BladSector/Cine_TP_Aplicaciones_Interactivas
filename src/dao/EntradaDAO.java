package dao;

import modelo.Butaca;
import modelo.Entrada;
import modelo.Funcion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class EntradaDAO {
    public void guardar(Entrada entrada) throws SQLException {
        guardar(entrada, null);
    }

    public void guardar(Entrada entrada, Integer espectadorId) throws SQLException {
        String sql = "INSERT INTO entrada (precio, funcion_id, butaca_id, horario, reembolsada, espectador_id) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conexion = ConexionMySQL.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setDouble(1, entrada.getPrecio());
            ps.setInt(2, entrada.getFuncion().getId());
            ps.setInt(3, entrada.getButaca().getId());
            ps.setTimestamp(4, Timestamp.valueOf(entrada.getHorario()));
            ps.setBoolean(5, entrada.isReembolsada());

            if (espectadorId == null) {
                ps.setNull(6, Types.INTEGER);
            } else {
                ps.setInt(6, espectadorId);
            }

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();

            if (rs.next()) {
                entrada.asignarId(rs.getInt(1));
            }
        }
    }

    public Entrada buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM entrada WHERE id = ?";

        try (Connection conexion = ConexionMySQL.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return crearEntradaDesdeResultSet(rs);
            }
        }

        return null;
    }

    public List<Entrada> listar() throws SQLException {
        String sql = "SELECT * FROM entrada";
        List<Entrada> entradas = new ArrayList<>();

        try (Connection conexion = ConexionMySQL.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                entradas.add(crearEntradaDesdeResultSet(rs));
            }
        }

        return entradas;
    }

    public void actualizar(Entrada entrada) throws SQLException {
        actualizar(entrada, null);
    }

    public void actualizar(Entrada entrada, Integer espectadorId) throws SQLException {
        String sql = "UPDATE entrada SET precio = ?, funcion_id = ?, butaca_id = ?, horario = ?, reembolsada = ?, espectador_id = ? WHERE id = ?";

        try (Connection conexion = ConexionMySQL.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setDouble(1, entrada.getPrecio());
            ps.setInt(2, entrada.getFuncion().getId());
            ps.setInt(3, entrada.getButaca().getId());
            ps.setTimestamp(4, Timestamp.valueOf(entrada.getHorario()));
            ps.setBoolean(5, entrada.isReembolsada());

            if (espectadorId == null) {
                ps.setNull(6, Types.INTEGER);
            } else {
                ps.setInt(6, espectadorId);
            }

            ps.setInt(7, entrada.getId());
            ps.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM entrada WHERE id = ?";

        try (Connection conexion = ConexionMySQL.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Entrada crearEntradaDesdeResultSet(ResultSet rs) throws SQLException {
        FuncionDAO funcionDAO = new FuncionDAO();
        ButacaDAO butacaDAO = new ButacaDAO();

        Funcion funcion = funcionDAO.buscarPorId(rs.getInt("funcion_id"));
        Butaca butaca = butacaDAO.buscarPorId(rs.getInt("butaca_id"));

        Entrada entrada = new Entrada(
                rs.getDouble("precio"),
                funcion,
                butaca,
                rs.getTimestamp("horario").toLocalDateTime()
        );
        entrada.asignarId(rs.getInt("id"));
        entrada.asignarReembolsada(rs.getBoolean("reembolsada"));
        return entrada;
    }
}
