import Image from "next/image";

type CartaoPaginaProps = {
  children: React.ReactNode;
  largura?: string;
};

export default function CartaoPagina({ children, largura = "max-w-md" }: CartaoPaginaProps) {
  return (
    <main className="flex min-h-screen flex-col items-center justify-center bg-neutral-50 px-4 py-10">
      <div className={`w-full ${largura} rounded-2xl border border-neutral-200 bg-white p-6 shadow-sm sm:p-8`}>
        <div className="mb-4 flex justify-center">
          <Image src="/logo-p3.png" alt="P3" width={64} height={64} priority />
        </div>
        {children}
      </div>
    </main>
  );
}
