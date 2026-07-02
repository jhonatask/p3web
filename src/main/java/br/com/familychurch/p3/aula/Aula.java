package br.com.familychurch.p3.aula;

import br.com.familychurch.p3.turma.Turma;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "aula")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Aula {

	@Id
	@GeneratedValue
	@UuidGenerator
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "turma_id", nullable = false)
	private Turma turma;

	@Column(nullable = false)
	private Integer numero;

	private String modulo;

	@Column(nullable = false)
	private LocalDate data;

	@Column(name = "janela_abertura", nullable = false)
	private LocalDateTime janelaAbertura;

	@Column(name = "janela_fechamento", nullable = false)
	private LocalDateTime janelaFechamento;

	@Column(name = "qr_token", nullable = false, unique = true)
	private String qrToken;

	public boolean janelaAberta(LocalDateTime referencia) {
		return !referencia.isBefore(janelaAbertura) && !referencia.isAfter(janelaFechamento);
	}

}
