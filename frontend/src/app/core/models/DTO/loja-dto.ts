export interface LojaDTO {
  idLoja?: number;
  nome: string;
  email: string;
  descricao: string;
  cnpj: string;
  whatsapp: string;
  facebook: string;
  instagram: string;
  idUsuario: number;
  nomeUsuario?: string;
  fotoUrl: string;
  endereco?: {
    cep: string;
    rua: string;
    numero: number | null;
    complemento: string;
    bairro: string;
    cidade: string;
    estado: string;
  };
}