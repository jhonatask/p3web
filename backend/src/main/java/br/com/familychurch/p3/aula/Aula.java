package br.com.familychurch.p3.aula;

import br.com.familychurch.p3.turma.Turma;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

/**
 * A sessao de uma aula numa turma: turma + {@link TipoAula} (catalogo) +
 * data + status. O status e sincronizado pela agenda da turma (ver
 * AulaServiceImpl.sincronizarStatusPorData) - nao ha mais janela de
 * horario nem qr_token por aula, o QR code passa a ser estatico, por turma.
 */
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

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "tipo_aula_id", nullable = false)
	private TipoAula tipoAula;

	@Column(nullable = false)
	private LocalDate data;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private StatusAula status = StatusAula.AGENDADA;

	@Column(name = "encerrada_manualmente", nullable = false)
	private boolean encerradaManualmente = false;

}
