package modelo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OrdenCompra {
    private int id;
    private Entrada entrada;
    private List<ItemConsumo> itemsConsumo;
    private String codigoQR;

    public OrdenCompra(Entrada entrada) {
        this.id = 0;
        this.entrada = entrada;
        this.itemsConsumo = new ArrayList<>();
        this.codigoQR = generarCodigoQR();

        if (!validarDatos()) {
            throw new IllegalArgumentException("Los datos de la orden de compra no son validos.");
        }
    }

    private boolean validarDatos() {
        return entrada != null
                && codigoQR != null
                && !codigoQR.isBlank();
    }

    public void agregarItem(ItemConsumo itemConsumo) {
        if (itemConsumo == null) {
            throw new IllegalArgumentException("El item de consumo no puede ser null.");
        }

        itemsConsumo.add(itemConsumo);
    }

    public double calcularTotal() {
        double total = entrada.getPrecio();

        for (ItemConsumo itemConsumo : itemsConsumo) {
            total += itemConsumo.calcularSubtotal();
        }

        return total;
    }

    private String generarCodigoQR() {
        return "ORD-" + UUID.randomUUID();
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

    public Entrada getEntrada() {
        return entrada;
    }

    public List<ItemConsumo> getItemsConsumo() {
        return itemsConsumo;
    }

    public String getCodigoQR() {
        return codigoQR;
    }
}
