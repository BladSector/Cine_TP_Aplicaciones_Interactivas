package modelo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Ticket {
    private int id;
    private Espectador espectador;
    private List<Entrada> entradas;
    private List<ItemConsumo> itemsConsumo;
    private String codigoQR;

    public Ticket(Espectador espectador) {
        this.id = 0;
        this.espectador = espectador;
        this.entradas = new ArrayList<>();
        this.itemsConsumo = new ArrayList<>();
        this.codigoQR = generarCodigoQR();

        if (!validarDatos()) {
            throw new IllegalArgumentException("Los datos del ticket no son validos.");
        }
    }

    private boolean validarDatos() {
        return espectador != null
                && codigoQR != null
                && !codigoQR.isBlank();
    }

    public void agregarEntrada(Entrada entrada) {
        if (entrada == null) {
            throw new IllegalArgumentException("La entrada no puede ser null.");
        }

        entradas.add(entrada);
    }

    public void agregarItem(ItemConsumo itemConsumo) {
        if (itemConsumo == null) {
            throw new IllegalArgumentException("El item de consumo no puede ser null.");
        }

        itemsConsumo.add(itemConsumo);
    }

    public double calcularTotal() {
        double total = 0;

        for (Entrada entrada : entradas) {
            total += entrada.getPrecio();
        }

        for (ItemConsumo itemConsumo : itemsConsumo) {
            total += itemConsumo.calcularSubtotal();
        }

        return total;
    }

    private String generarCodigoQR() {
        return "TCK-" + UUID.randomUUID();
    }

    public void asignarId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("El id debe ser mayor a 0.");
        }

        this.id = id;
    }

    public int getId() {
        return id;
    }

    public Espectador getEspectador() {
        return espectador;
    }

    public List<Entrada> getEntradas() {
        return entradas;
    }

    public List<ItemConsumo> getItemsConsumo() {
        return itemsConsumo;
    }

    public String getCodigoQR() {
        return codigoQR;
    }
}
