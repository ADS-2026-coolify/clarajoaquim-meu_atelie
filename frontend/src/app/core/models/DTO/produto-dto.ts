export interface ProdutoMaterialDTO {
  materialId: number;
  nomeMaterial?: string;
  quantidade?: number;
  unidade?: string;
  custoCalculado?: number;
}

export interface ProdutoDTO {
  id?: number;
  nomeCategoria?: string;

  nome: string;
  descricao: string;
  preco: number;
  categoriaId: number;
  materiaisUtilizados?: ProdutoMaterialDTO[];
  custoMateriaisCalculado?: number;

  precoCusto?: number;
  quantidadeEstoque?: number;
  status?: string;
  largura?: number;
  altura?: number;
  observacoes?: string;
  imagem?: string;
}
