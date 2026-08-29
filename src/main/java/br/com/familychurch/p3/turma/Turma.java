package br.com.familychurch.p3.turma;

import br.com.familychurch.p3.curso.Curso;
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
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "turma")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Turma {

	@Id
	@GeneratedValue
	@UuidGenerator
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "curso_id", nullable = false)
	private Curso curso;

	@Column(nullable = false)
	private String nome;

	@Column(name = "modulo_atual")
	private String moduloAtual;

	@Column(name = "token_cadastro", nullable = false, unique = true)
	private String tokenCadastro;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private StatusTurma status = StatusTurma.ATIVA;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "lider_id")
	private UsuarioAdmin lider;

}
