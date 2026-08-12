package br.com.familychurch.p3.aula;

import br.com.familychurch.p3.curso.Curso;
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

/**
 * Catalogo de aula (Modulo + numero + tema), compartilhado por todas as
 * turmas de um curso. E o que faz a reposicao valer entre turmas: repor e
 * registrar presenca no mesmo TipoAula em outra turma. Sem professor aqui -
 * professor muda por turma/data, entao fica na sessao ({@link Aula}).
 */
@Entity
@Table(name = "tipo_aula")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TipoAula {

	@Id
	@GeneratedValue
	@UuidGenerator
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "curso_id", nullable = false)
	private Curso curso;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ModuloAula modulo;

	@Column(nullable = false)
	private Integer numero;

	@Column(nullable = false)
	private String tema;

	@Column(nullable = false)
	private Integer ordem;

	public boolean contaParaFormatura() {
		return modulo != ModuloAula.INTRODUTORIO;
	}

}
