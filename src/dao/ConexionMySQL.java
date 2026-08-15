package dao;
//Importa Connection, que representa una conexión activa entre Java y la base de datos.
import java.sql.Connection;
//Importa DriverManager. Esta clase se encarga de crear la conexión usando la URL, usuario y contraseña.
import java.sql.DriverManager;
//SQLException, que es la excepción
import java.sql.SQLException;

//Conexión con el MySQl
//static: pertenecen a la clase ConexionMySQL, no a un objeto particular.
//final: una vez asignado el valor, no puede cambiar.
public class ConexionMySQL {
    private static final String URL = "jdbc:mysql://localhost:3307/tp_cine_api";
    private static final String USUARIO = "root";
    private static final String PASSWORD = "CineAPI";

    public static Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, PASSWORD);
    }
}
//Para llamar:
//try {
//    Connection conexion = ConexionMySQL.obtenerConexion();
//} catch (SQLException e) {
//    System.out.println("Error al conectar");
//}