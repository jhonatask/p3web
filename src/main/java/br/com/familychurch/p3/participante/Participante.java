package br.com.familychurch.p3.participante;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "participante")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Participante {

	@Id
	@GeneratedValue
	@UuidGenerator
	private UUID id;

	@Column(nullable = false)
	private String nome;

	@Column(nullable = false, unique = true)
	private String cpf;

	@Column(name = "celular")
	private String celular;

	@Column(name = "email")
	private String email;

	@Column(name = "campus")
	private String campus;

	@Column(name = "data_nascimento")
	private LocalDate dataNascimento;

	@Enumerated(EnumType.STRING)
	@Column(name = "estado_civil")
	private EstadoCivil estadoCivil;

	@Column(name = "tem_filhos")
	private Boolean temFilhos;

	@Column(name = "quantidade_filhos")
	private Integer quantidadeFilhos;

	@Embedded
	private Endereco endereco = new Endereco();

	@Enumerated(EnumType.STRING)
	@Column(name = "tempo_na_family")
	private TempoNaFamily tempoNaFamily;

	@Column(name = "batizado")
	private Boolean batizado;

	@Column(name = "serve_ministerio")
	private Boolean serveMinisterio;

	@Column(name = "qual_ministerio_serve")
	private String qualMinisterioServe;

	@Column(name = "instagram")
	private String instagram;

	@Column(name = "facebook")
	private String facebook;

	@Column(name = "deseja_servir")
	private String desejaServir;

	@Column(name = "habilidades")
	private String habilidades;

	@Column(name = "participa_gc")
	private Boolean participaGc;

	@ElementCollection
	@CollectionTable(name = "participante_gc", joinColumns = @JoinColumn(name = "participante_id"))
	@Column(name = "gc")
	private Set<String> quaisGcs = new HashSet<>();

	@Column(name = "consentimento_lgpd")
	private Boolean consentimentoLgpd;

	@Column(name = "consentimento_lgpd_timestamp")
	private LocalDateTime consentimentoLgpdTimestamp;

	@Column(name = "token_matricula")
	private String tokenMatricula;

	@Column(name = "token_matricula_expira_em")
	private LocalDateTime tokenMatriculaExpiraEm;

	@Column(name = "token_atualizacao")
	private String tokenAtualizacao;

	@Column(name = "token_atualizacao_expira_em")
	private LocalDateTime tokenAtualizacaoExpiraEm;

}
