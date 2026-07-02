type CartaoPaginaProps = {
  children: React.ReactNode;
};

export default function CartaoPagina({ children }: CartaoPaginaProps) {
  return (
    <main className="flex min-h-screen flex-col items-center justify-center bg-neutral-50 px-4 py-10">
      <div className="w-full max-w-md rounded-2xl border border-neutral-200 bg-white p-6 shadow-sm sm:p-8">
        {children}
      </div>
    </main>
  );
}
