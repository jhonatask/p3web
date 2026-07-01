package br.com.familychurch.p3.aluno;

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

	@Column(nullable = false)
	private String nome;

	@Column(nullable = false, unique = true)
	private String cpf;

	@Column(nullable = false)
	private String celular;

	@Column(nullable = false)
	private String campus;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "turma_id", nullable = false)
	private Turma turma;

	@Column(name = "data_cadastro", nullable = false)
	private LocalDateTime dataCadastro;

	@Column(name = "consentimento_lgpd", nullable = false)
	private boolean consentimentoLgpd;

	@Column(name = "consentimento_lgpd_timestamp")
	private LocalDateTime consentimentoLgpdTimestamp;

}
