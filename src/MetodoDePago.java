import java.time.YearMonth;

public class MetodoDePago {
    private String numero;
    private YearMonth fechaVencimiento;
    private String nombre;
    private String cvv;

    public MetodoDePago(String numero, YearMonth fechaVencimiento, String nombre, String cvv) {
        this.numero = numero;
        this.fechaVencimiento = fechaVencimiento;
        this.nombre = nombre;
        this.cvv = cvv;
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
                && cvv != null && cvv.matches("\\d{3}");
    }

}
