package br.com.familychurch.p3.aluno;

import br.com.familychurch.p3.participante.Participante;
import br.com.familychurch.p3.turma.Turma;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * Matricula: liga um {@link Participante} a sua turma de origem na Escola de
 * Hombridade. Nao e um cadastro visivel por formulario - e criada
 * automaticamente na primeira chamada normal (nao-reposicao) do participante,
 * sem pedir nada de novo (campus/LGPD ja vem do cadastro base, em
 * Participante). Um participante so pode ter uma matricula para sempre
 * (participante_id unico) - reposicao em outra turma nao cria uma nova
 * matricula, so um registro de Presenca com origem=REPOSICAO.
 */
@Entity
@Table(name = "aluno")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Aluno {

	@Id
	@GeneratedValue
	@UuidGenerator
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "participante_id", nullable = false, unique = true)
	private Participante participante;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "turma_id", nullable = false)
	private Turma turma;

	@Column(name = "data_matricula", nullable = false)
	private LocalDateTime dataMatricula;

}
