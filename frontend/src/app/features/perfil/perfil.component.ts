import { Component, OnInit, ViewChild } from '@angular/core';
import { FooterComponent } from '../../shared/components/footer/footer.component';
import { HeaderComponent } from '../../shared/components/header/header.component';
import { LojaDTO } from '../../core/models/DTO/loja-dto';
import { LojaService } from '../../core/services/loja.service';
import { FormsModule } from '@angular/forms';
import { ModalComponent } from '../../shared/components/modal/modal.component';

import { API_URL } from '../../core/config';
@Component({
  selector: 'app-perfil',
  imports: [FooterComponent, HeaderComponent, FormsModule, ModalComponent],
  templateUrl: './perfil.component.html',
  styleUrl: './perfil.component.css'
})
export class PerfilComponent implements OnInit {
  loja: Partial<LojaDTO> = {
    endereco: { cep: '', rua: '', numero: null, complemento: '', bairro: '', cidade: '', estado: '' }
  };
  idUsuarioAtual = this.obterIdUsuarioLogado();
  editando = false;
  urlServidorImagem = API_URL + '/uploads/';
  @ViewChild(ModalComponent) modal!: ModalComponent;

  constructor(private lojaService: LojaService) { }

  private obterIdUsuarioLogado(): number {
    const token = localStorage.getItem('token');
    if (!token) {
      return 0;
    }
    try {
      const payloadBase64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
      const payload = JSON.parse(atob(payloadBase64));
      return payload.idUsuario ?? 0;
    } catch {
      return 0;
    }
  }

  ngOnInit() {
    this.lojaService.obterPerfil(this.idUsuarioAtual).subscribe({
      next: (dados) => {
        this.loja = dados;
        if (!this.loja.endereco) {
          this.loja.endereco = { cep: '', rua: '', numero: null, complemento: '', bairro: '', cidade: '', estado: '' };
        }
      },
      error: (err) => console.error('Erro ao buscar perfil', err)
    });
  }

  alternarEdicao() {
    this.editando = !this.editando;
  }

  aplicarMascaraCep(event: any) {
    let valor = event.target.value.replace(/\D/g, '');
    if (valor.length > 5) {
      valor = valor.replace(/^(\d{5})(\d)/, '$1-$2');
    }
    if (this.loja.endereco) {
      this.loja.endereco.cep = valor;
    }
  }

  buscarCep() {
    const cep = this.loja.endereco?.cep?.replace(/\D/g, '');

    if (cep && cep.length === 8) {
      this.lojaService.buscarCep(cep).subscribe({
        next: (dados) => {
          if (!dados.erro && this.loja.endereco) {
            this.loja.endereco.rua = dados.logradouro;
            this.loja.endereco.bairro = dados.bairro;
            this.loja.endereco.cidade = dados.localidade;
            this.loja.endereco.estado = dados.uf;
          } else {
            console.error('CEP não encontrado.');
          }
        },
        error: (err) => console.error('Erro ao buscar o CEP.', err)
      });
    }
  }

  salvarAlteracoes() {
    this.lojaService.atualizarPerfil(this.idUsuarioAtual, this.loja as LojaDTO).subscribe({
      next: (dados) => {
        this.loja = dados;
        this.editando = false; // <-- Volta para o modo leitura após salvar
        this.modal.abrirModal('Sucesso!', 'Perfil atualizado com sucesso!', 'sucesso', 'OK');
      },
      error: (err) => {
        console.error('Erro ao atualizar', err);
        this.modal.abrirModal('Erro', 'Erro ao atualizar o perfil.', 'erro', 'Fechar');
      }
    });
  }

  onArquivoSelecionado(event: Event) {
  const input = event.target as HTMLInputElement;
  const TAMANHO_MAXIMO_BYTES = 5 * 1024 * 1024;

  if (input.files && input.files.length > 0) {
    const arquivo = input.files[0];

    if (arquivo.size > TAMANHO_MAXIMO_BYTES) {
      input.value = '';
      this.modal.abrirModal('Arquivo muito grande', 'O sistema só aceita arquivos menores que 5MB. Escolha uma foto menor e tente novamente.', 'erro', 'Fechar');
      return;
    }

    this.lojaService.uploadFoto(this.idUsuarioAtual, arquivo).subscribe({
      next: (resposta: any) => {
        this.loja.fotoUrl = resposta.fotoUrl;
        this.modal.abrirModal('Sucesso!', 'Foto atualizada com sucesso!', 'sucesso', 'OK');
      },
      error: (err) => {
        console.error('Erro no upload', err);
        if (err.status === 413) {
          this.modal.abrirModal('Arquivo muito grande', 'O sistema só aceita arquivos menores que 5MB. Escolha uma foto menor e tente novamente.', 'erro', 'Fechar');
        } else {
          this.modal.abrirModal('Erro', 'Falha ao enviar a foto.', 'erro', 'Fechar');
        }
      }
    });
  }
}
}
