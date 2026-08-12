type ChamadaConcluidaProps = {
  nome: string;
};

export default function ChamadaConcluida({ nome }: ChamadaConcluidaProps) {
  const primeiroNome = nome.trim().split(/\s+/)[0];

  return (
    <div className="text-center">
      <h1 className="text-xl font-semibold text-neutral-900">Presença registrada, {primeiroNome}!</h1>
      <p className="mt-3 text-neutral-600">Obrigado por avaliar a aula. Nos vemos no próximo encontro.</p>
      <p className="mt-4 text-sm text-neutral-500">Pode fechar esta página.</p>
    </div>
  );
}
