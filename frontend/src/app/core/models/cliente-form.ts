export interface ClienteForm {
  nome: string;
  email: string;
  telefone: string;
  cpf?: string,
  cnpj?: string;
  dataNascimento?: string;
  observacoes?: string;
  canalId?: number;

  endereco: {
    rua: string;
    numero: string;
    bairro: string;
    cidade: string;
    estado: string;
    cep: string;
  };
}