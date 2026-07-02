type CadastroConcluidoProps = {
  nome: string;
};

export default function CadastroConcluido({ nome }: CadastroConcluidoProps) {
  const primeiroNome = nome.trim().split(/\s+/)[0];

  return (
    <div className="text-center">
      <h1 className="text-xl font-semibold text-neutral-900">Cadastro confirmado, {primeiroNome}!</h1>
      <p className="mt-3 text-neutral-600">
        Seu cadastro foi registrado com sucesso. Assim que o app de chamada estiver disponível, você vai
        poder fazer o check-in direto pelo celular nas próximas aulas — por enquanto, seu professor já
        tem o seu cadastro confirmado.
      </p>
      <p className="mt-4 text-sm text-neutral-500">Pode fechar esta página. Nos vemos na próxima aula!</p>
    </div>
  );
}
