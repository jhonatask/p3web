package br.com.familychurch.p3.presenca;

import br.com.familychurch.p3.aluno.Aluno;
import br.com.familychurch.p3.aula.Aula;
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
	@JoinColumn(name = "aluno_id", nullable = false)
	private Aluno aluno;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "aula_id", nullable = false)
	private Aula aula;

	@Column(name = "timestamp_checkin", nullable = false)
	private LocalDateTime timestampCheckin;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private MetodoPresenca metodo;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "validado_por")
	private UsuarioAdmin validadoPor;

}
