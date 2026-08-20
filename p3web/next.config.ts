import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  // O quick tunnel do Cloudflare (trycloudflare.com) gera um subdominio
  // aleatorio a cada execucao; sem isso, o dev server do Next bloqueia como
  // cross-origin (protecao contra DNS rebinding) e o HMR via WebSocket quebra
  // com "Unauthorized".
  allowedDevOrigins: ["*.trycloudflare.com"],
};

export default nextConfig;
