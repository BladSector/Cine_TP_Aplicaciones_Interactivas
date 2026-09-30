package modelo.entidades;

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

@Entity
@Table(name = "ticket")
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToOne
    @JoinColumn(name = "espectador_id")
    private Espectador espectador;
    @ManyToOne
    @JoinColumn(name = "metodo_pago_id")
    private MetodoDePago metodoDePago;
    @Column(name = "metodo_pago_resumen")
    private String metodoDePagoResumen;
    @OneToMany(mappedBy = "ticket")
    private List<Entrada> entradas;
    @OneToMany(mappedBy = "ticket")
    private List<ItemConsumo> itemsConsumo;
    @Column(name = "codigo_qr", unique = true)
    private String codigoQR;

    protected Ticket() {
        this.entradas = new ArrayList<>();
        this.itemsConsumo = new ArrayList<>();
    }

    public Ticket(Espectador espectador) {
        this(espectador, null, null, null);
    }

    public Ticket(Espectador espectador, String metodoDePagoResumen) {
        this(espectador, null, metodoDePagoResumen, null);
    }

    public Ticket(Espectador espectador, String metodoDePagoResumen, String codigoQR) {
        this(espectador, null, metodoDePagoResumen, codigoQR);
    }

    public Ticket(Espectador espectador, MetodoDePago metodoDePago,
                  String metodoDePagoResumen, String codigoQR) {
        this.id = 0;
        this.espectador = espectador;
        this.metodoDePago = metodoDePago;
        this.metodoDePagoResumen = metodoDePagoResumen;
        this.entradas = new ArrayList<>();
        this.itemsConsumo = new ArrayList<>();
        this.codigoQR = codigoQR;
    }

    public void agregarEntrada(Entrada entrada) {
        entradas.add(entrada);
        entrada.asignarTicket(this);
    }

    public void agregarItem(ItemConsumo itemConsumo) {
        itemsConsumo.add(itemConsumo);
        itemConsumo.asignarTicket(this);
    }

    public void asignarId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public Espectador getEspectador() {
        return espectador;
    }

    public MetodoDePago getMetodoDePago() {
        return metodoDePago;
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
