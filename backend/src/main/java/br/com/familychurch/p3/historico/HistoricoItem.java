package br.com.familychurch.p3.historico;

import br.com.familychurch.p3.participante.Participante;
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

@Entity
@Table(name = "historico_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HistoricoItem {

	@Id
	@GeneratedValue
	@UuidGenerator
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "participante_id", nullable = false)
	private Participante participante;

	@Column(nullable = false)
	private String item;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TipoHistorico tipo;

	private String turma;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private StatusHistorico status;

	private LocalDate data;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OrigemHistorico origem;

}
