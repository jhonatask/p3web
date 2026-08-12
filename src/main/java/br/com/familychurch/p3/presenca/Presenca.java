package br.com.familychurch.p3.presenca;

import br.com.familychurch.p3.aula.Aula;
import br.com.familychurch.p3.participante.Participante;
import br.com.familychurch.p3.turma.Turma;
import br.com.familychurch.p3.usuarioadmin.UsuarioAdmin;
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
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

/**
 * Presenca e identificada por {@link Participante} (nao mais por matricula
 * numa turma especifica), pois o mesmo tipo de aula pode ser cumprido em
 * qualquer turma (reposicao). "turma" e a turma da sessao onde a presenca
 * aconteceu - pode ser diferente da turma de origem do participante, quando
 * origem=REPOSICAO.
 */
@Entity
@Table(name = "presenca")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Presenca {

	@Id
	@GeneratedValue
	@UuidGenerator
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "participante_id", nullable = false)
	private Participante participante;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "turma_id", nullable = false)
	private Turma turma;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "aula_id", nullable = false)
	private Aula aula;

	@Column(name = "timestamp_checkin", nullable = false)
	private LocalDateTime timestampCheckin;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private MetodoPresenca metodo;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OrigemPresenca origem;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "validado_por")
	private UsuarioAdmin validadoPor;

}
