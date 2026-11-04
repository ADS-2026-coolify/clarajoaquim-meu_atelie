export interface EncomendaDTO {
    id: number;
    idVisual?: number;
    nomeCliente: string;
    dataPedido: string;
    dataEntrega: string | null;
    status: { descricao: string };
    valorTotal: number;
    itens?: any;
}
