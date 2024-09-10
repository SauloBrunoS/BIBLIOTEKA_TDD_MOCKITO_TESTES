package ufc.vv.biblioteka.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.JoinColumn;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.voodoodyne.jackson.jsog.JSOGGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Data
@NoArgsConstructor
@JsonIdentityInfo(generator = JSOGGenerator.class)
public class Reserva {

    public static final int PRAZO_RESERVA_ATIVA_EM_DIAS = 2;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "livro_id")
    @NotNull(message = "Livro não pode ser nulo")
    private Livro livro;

    @ManyToOne
    @JoinColumn(name = "leitor_id")
    @NotNull(message = "Leitor não pode ser nulo")
    private Leitor leitor;

    @DateTimeFormat(pattern = "dd/MM/yyyy")
    @Setter(AccessLevel.NONE)
    private LocalDateTime dataCadastro;

    @DateTimeFormat(pattern = "dd/MM/yyyy")
    @Setter(AccessLevel.NONE)
    private LocalDate dataLimite;

    @Enumerated(EnumType.STRING)
    @Setter(AccessLevel.NONE)
    @NotNull(message = "Status da Reserva não pode ser nulo")
    private StatusReserva status;

    @OneToOne
    @JoinColumn(name = "emprestimo_id")
    private Emprestimo emprestimo;

    public void marcarComoEmAndamento() {
        this.status = StatusReserva.EM_ANDAMENTO;
        this.dataLimite = LocalDate.now().plusDays(PRAZO_RESERVA_ATIVA_EM_DIAS);
    }

    public void marcarComoEmEspera() {
        this.status = StatusReserva.EM_ESPERA;
    }

    public void marcarComoCancelada() {
        this.status = StatusReserva.CANCELADA;
    }

    public void marcarComoExpirada() {
        this.status = StatusReserva.EXPIRADA;
    }

    public void marcarComoAtendida() {
        this.status = StatusReserva.ATENDIDA;
    }

    public Reserva(Livro livro, Leitor leitor) {
        this.livro = livro;
        this.leitor = leitor;
        this.dataCadastro = LocalDateTime.now();
    }

}
