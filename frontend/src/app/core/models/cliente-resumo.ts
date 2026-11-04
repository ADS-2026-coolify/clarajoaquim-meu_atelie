export interface ClienteResumo {
  id: number;
  nome: string;
  telefone: string;
  email: string;
  totalPedidos: number;
  totalGasto: number;
  dataCadastro: string;
  ativo: boolean;
}