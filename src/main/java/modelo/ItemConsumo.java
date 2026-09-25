package modelo;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "item_consumo")
public class ItemConsumo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToOne
    @JoinColumn(name = "producto_id")
    private ProductoConfiteria producto;
    @ManyToOne
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;
    private int cantidad;
    @Enumerated(EnumType.STRING)
    private EstadoConsumo estado;

    protected ItemConsumo() {
        this.estado = EstadoConsumo.PENDIENTE;
    }

    public ItemConsumo(ProductoConfiteria producto, int cantidad) {
        this.id = 0;
        this.producto = producto;
        this.cantidad = cantidad;
        this.estado = EstadoConsumo.PENDIENTE;
    }

    public void actualizarDatos(ProductoConfiteria producto, int cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;
    }

    public void asignarId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void asignarTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public void actualizarEstado(EstadoConsumo estado) {
        this.estado = estado;
    }

    public ProductoConfiteria getProducto() {
        return producto;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public int getCantidad() {
        return cantidad;
    }

    public EstadoConsumo getEstado() {
        return estado == null ? EstadoConsumo.PENDIENTE : estado;
    }
}
