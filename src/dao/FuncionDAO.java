package dao;

import modelo.Funcion;
import modelo.Pelicula;
import modelo.Sala;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

public class FuncionDAO {
    public void guardar(Funcion funcion) throws SQLException {
        String sql = "INSERT INTO funcion (fecha, horario, pelicula_id, sala_id) VALUES (?, ?, ?, ?)";

        if (funcion.getPelicula().getId() == 0) {
            PeliculaDAO peliculaDAO = new PeliculaDAO();
            peliculaDAO.guardar(funcion.getPelicula());
        }

        if (funcion.getSala().getId() == 0) {
            SalaDAO salaDAO = new SalaDAO();
            salaDAO.guardar(funcion.getSala());
        }

        try (Connection conexion = ConexionMySQL.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setDate(1, Date.valueOf(funcion.getFecha()));
            ps.setTime(2, Time.valueOf(funcion.getHorario()));
            ps.setInt(3, funcion.getPelicula().getId());
            ps.setInt(4, funcion.getSala().getId());
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();

            if (rs.next()) {
                funcion.asignarId(rs.getInt(1));
            }
        }
    }

    public Funcion buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM funcion WHERE id = ?";

        try (Connection conexion = ConexionMySQL.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return crearFuncionDesdeResultSet(rs);
            }
        }

        return null;
    }

    public List<Funcion> listar() throws SQLException {
        String sql = "SELECT * FROM funcion";
        List<Funcion> funciones = new ArrayList<>();

        try (Connection conexion = ConexionMySQL.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                funciones.add(crearFuncionDesdeResultSet(rs));
            }
        }

        return funciones;
    }

    public void actualizar(Funcion funcion) throws SQLException {
        String sql = "UPDATE funcion SET fecha = ?, horario = ?, pelicula_id = ?, sala_id = ? WHERE id = ?";

        try (Connection conexion = ConexionMySQL.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(funcion.getFecha()));
            ps.setTime(2, Time.valueOf(funcion.getHorario()));
            ps.setInt(3, funcion.getPelicula().getId());
            ps.setInt(4, funcion.getSala().getId());
            ps.setInt(5, funcion.getId());
            ps.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM funcion WHERE id = ?";

        try (Connection conexion = ConexionMySQL.obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Funcion crearFuncionDesdeResultSet(ResultSet rs) throws SQLException {
        PeliculaDAO peliculaDAO = new PeliculaDAO();
        SalaDAO salaDAO = new SalaDAO();

        Pelicula pelicula = peliculaDAO.buscarPorId(rs.getInt("pelicula_id"));
        Sala sala = salaDAO.buscarPorId(rs.getInt("sala_id"));

        Funcion funcion = new Funcion(
                rs.getDate("fecha").toLocalDate(),
                rs.getTime("horario").toLocalTime(),
                pelicula,
                sala
        );
        funcion.asignarId(rs.getInt("id"));
        return funcion;
    }
}
