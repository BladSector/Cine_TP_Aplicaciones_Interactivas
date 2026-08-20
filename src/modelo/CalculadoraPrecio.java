package modelo;

import java.time.DayOfWeek;

public class CalculadoraPrecio {
    private static final double RECARGO_3D = 0.20;
    private static final double DESCUENTO_FIDELIDAD = 0.10;

    public double calcularPrecioEntrada(Funcion funcion, Espectador espectador, double precioBase) {
        validarDatos(funcion, precioBase, 1);

        double precio = precioBase;

        if (funcion.getFormato() == FormatoFuncion.TRES_D) {
            precio += precioBase * RECARGO_3D;
        }

        if (espectador != null && espectador.esClienteFrecuente()) {
            precio -= precio * DESCUENTO_FIDELIDAD;
        }

        return precio;
    }

    public double calcularTotal(Funcion funcion, Espectador espectador, double precioBase, int cantidadEntradas) {
        validarDatos(funcion, precioBase, cantidadEntradas);

        double precioEntrada = calcularPrecioEntrada(funcion, espectador, precioBase);
        int entradasACobrar = cantidadEntradas;

        if (esMiercoles(funcion)) {
            entradasACobrar = (cantidadEntradas / 2) + (cantidadEntradas % 2);
        }

        return precioEntrada * entradasACobrar;
    }

    private boolean esMiercoles(Funcion funcion) {
        return funcion.getFecha().getDayOfWeek() == DayOfWeek.WEDNESDAY;
    }

    private void validarDatos(Funcion funcion, double precioBase, int cantidadEntradas) {
        if (funcion == null) {
            throw new IllegalArgumentException("La funcion no puede ser null.");
        }

        if (precioBase <= 0) {
            throw new IllegalArgumentException("El precio base debe ser mayor a 0.");
        }

        if (cantidadEntradas <= 0) {
            throw new IllegalArgumentException("La cantidad de entradas debe ser mayor a 0.");
        }
    }
}
