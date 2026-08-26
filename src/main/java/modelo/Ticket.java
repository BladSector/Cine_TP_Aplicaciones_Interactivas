package modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "ticket")
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToOne
    @JoinColumn(name = "espectador_id")
    private Espectador espectador;
    @Column(name = "metodo_pago_resumen")
    private String metodoDePagoResumen;
    @OneToMany(mappedBy = "ticket")
    private List<Entrada> entradas;
    @OneToMany(mappedBy = "ticket")
    private List<ItemConsumo> itemsConsumo;
    @Column(name = "codigo_qr")
    private String codigoQR;

    protected Ticket() {
        this.entradas = new ArrayList<>();
        this.itemsConsumo = new ArrayList<>();
    }

    public Ticket(Espectador espectador) {
        this(espectador, null);
    }

    public Ticket(Espectador espectador, String metodoDePagoResumen) {
        this.id = 0;
        this.espectador = espectador;
        this.metodoDePagoResumen = metodoDePagoResumen;
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
        entrada.asignarTicket(this);
    }

    public void agregarItem(ItemConsumo itemConsumo) {
        if (itemConsumo == null) {
            throw new IllegalArgumentException("El item de consumo no puede ser null.");
        }

        itemsConsumo.add(itemConsumo);
        itemConsumo.asignarTicket(this);
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

    public String getMetodoDePagoResumen() {
        return metodoDePagoResumen;
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
