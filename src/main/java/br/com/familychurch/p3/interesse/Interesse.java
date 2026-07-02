package br.com.familychurch.p3.interesse;

import br.com.familychurch.p3.aluno.Aluno;
import br.com.familychurch.p3.aula.Aula;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "interesse")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Interesse {

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

	@Column(name = "gc_homens", nullable = false)
	private boolean gcHomens;

	@Column(name = "proximo_awake", nullable = false)
	private boolean proximoAwake;

	@Column(nullable = false)
	private boolean discipulado;

}
