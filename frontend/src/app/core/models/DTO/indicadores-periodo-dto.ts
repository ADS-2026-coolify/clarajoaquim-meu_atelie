export interface IndicadoresPeriodoDTO {
  faturamento: number;
  faturamentoVariacaoPercentual: number | null;
  totalPedidos: number;
  totalPedidosVariacaoPercentual: number | null;
  novosClientes: number;
  novosClientesVariacaoPercentual: number | null;
  ticketMedio: number;
  ticketMedioVariacaoPercentual: number | null;
}
