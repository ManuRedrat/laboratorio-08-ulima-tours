package pe.edu.ulima.is2.viajes.modelo;

import java.time.LocalDate;
import java.util.Objects;

/** Cliente titular de una reserva. */
public class Cliente {

    private final TipoDocumento tipoDocumento;
    private final String numeroDocumento;
    private final String nombres;
    private final String email;
    private final LocalDate fechaNacimiento;
    private CategoriaCliente categoria;

    public Cliente(TipoDocumento tipoDocumento, String numeroDocumento, String nombres,
                   String email, LocalDate fechaNacimiento, CategoriaCliente categoria) {
        this.tipoDocumento = Objects.requireNonNull(tipoDocumento);
        this.numeroDocumento = Objects.requireNonNull(numeroDocumento);
        this.nombres = Objects.requireNonNull(nombres);
        this.email = Objects.requireNonNull(email);
        this.fechaNacimiento = fechaNacimiento;
        this.categoria = Objects.requireNonNull(categoria);
    }

    public TipoDocumento getTipoDocumento() {
        return tipoDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public String getNombres() {
        return nombres;
    }

    public String getEmail() {
        return email;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public CategoriaCliente getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaCliente categoria) {
        this.categoria = Objects.requireNonNull(categoria);
    }

    @Override
    public String toString() {
        return tipoDocumento + " " + numeroDocumento + " - " + nombres + " (" + categoria + ")";
    }
}
