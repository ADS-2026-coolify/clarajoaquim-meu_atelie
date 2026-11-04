// URL da API lida em tempo de execução (public/config.js, gerado a partir da variável API_URL no container).
// Sem essa configuração, usa o back-end local.
export const API_URL: string =
  ((window as any).APP_CONFIG?.apiUrl as string | undefined)?.replace(/\/$/, '') || 'http://localhost:8080';
