package modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.YearMonth;

@Entity
@Table(name = "metodo_pago")
public class MetodoDePago {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String numero;
    @Convert(converter = YearMonthAttributeConverter.class)
    @Column(name = "fecha_vencimiento", length = 7)
    private YearMonth fechaVencimiento;
    private String nombre;
    private String apellido;
    private String cvv;
    private Boolean activa = true;
    @ManyToOne
    @JoinColumn(name = "espectador_id")
    private Espectador espectador;

    protected MetodoDePago() {
    }

    public MetodoDePago(String numero, YearMonth fechaVencimiento, String nombre, String apellido, String cvv) {
        this.id = 0;
        this.numero = numero;
        this.fechaVencimiento = fechaVencimiento;
        this.nombre = nombre;
        this.apellido= apellido;
        this.cvv = cvv;
        this.activa = true;
        this.espectador = null;
    }

    public void actualizarDatos(String numero, YearMonth fechaVencimiento, String nombre, String apellido, String cvv) {
        this.numero = numero;
        this.fechaVencimiento = fechaVencimiento;
        this.nombre = nombre;
        this.apellido = apellido;
        this.cvv = cvv;
    }

    public void asignarId(int id) {
        this.id = id;
    }

    public void asignarEspectador(Espectador espectador) {
        this.espectador = espectador;
    }

    public void desactivar() {
        this.activa = false;
    }

    public boolean isActiva() {
        return activa == null || activa;
    }

    public int getId() {
        return id;
    }

    public String getNumero() {
        return numero;
    }

    public YearMonth getFechaVencimiento() {
        return fechaVencimiento;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getTitular() {
        if (apellido == null || apellido.isBlank()) {
            return nombre;
        }

        return nombre + " " + apellido;
    }

    public String getCvv() {
        return cvv;
    }

    public Espectador getEspectador() {
        return espectador;
    }

    public String getUltimosNumeros() {
        if (numero == null || numero.length() < 4) {
            return "";
        }

        return numero.substring(numero.length() - 4);
    }
}
