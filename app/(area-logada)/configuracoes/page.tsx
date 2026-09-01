import FormularioTrocarSenha from "@/components/FormularioTrocarSenha";

export default function PaginaConfiguracoes() {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-xl font-semibold text-neutral-900">Configurações</h1>
        <p className="mt-1 text-sm text-neutral-600">Ajustes da sua conta.</p>
      </div>

      <div className="rounded-xl border border-neutral-200 bg-white p-5">
        <h2 className="text-base font-semibold text-neutral-900">Trocar senha</h2>
        <div className="mt-4">
          <FormularioTrocarSenha />
        </div>
      </div>
    </div>
  );
}
