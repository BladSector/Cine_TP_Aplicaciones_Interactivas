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
        this.espectador = null;
        //valida los datos y si no pasa validarDatos() no se crea.
        if (!validarDatos()) {
            throw new IllegalArgumentException("Los datos del metodo de pago no son validos.");
        }
    }

    //Verifica que la fecha de vencimiento sea válida (!vacío y fecha posterior a la actual).
    public boolean estaVencido() {
        return fechaVencimiento == null || fechaVencimiento.isBefore(YearMonth.now());
    }

    // isBlank() verifica que el nombre no este vacío ni tenga solo espacios.
    // matches("\\d{16}") y matches("\\d{3}") verifican que el String tenga solo números (\\d) y la cantidad exacta de dígitos({3} o {16}).
    private boolean validarDatos() {
        return numero != null && numero.matches("\\d{16}")
                && fechaVencimiento != null && !estaVencido()
                && nombre != null && !nombre.isBlank()
                && apellido != null && !apellido.isBlank()
                && cvv != null && cvv.matches("\\d{3}");
    }

    public void actualizarDatos(String numero, YearMonth fechaVencimiento, String nombre, String apellido, String cvv) {
        this.numero = numero;
        this.fechaVencimiento = fechaVencimiento;
        this.nombre = nombre;
        this.apellido = apellido;
        this.cvv = cvv;

        if (!validarDatos()) {
            throw new IllegalArgumentException("Los datos del metodo de pago no son validos.");
        }
    }

    public void asignarId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("El id debe ser mayor a 0.");
        }

        this.id = id;
    }

    public void asignarEspectador(Espectador espectador) {
        if (espectador == null) {
            throw new IllegalArgumentException("El espectador no puede ser null.");
        }

        this.espectador = espectador;
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
