export type TipoValor = 'REALIZADO' | 'ESTIMADO';

export interface FaturamentoMensalDTO {
  mes: number;
  ano: number;
  valor: number;
  tipo: TipoValor;
}
