import { Component, inject, OnInit } from '@angular/core';
import { FooterComponent } from '../../shared/components/footer/footer.component';
import { HeaderComponent } from '../../shared/components/header/header.component';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { MaterialService } from '../../core/services/material.service';
import { FornecedorService } from '../../core/services/fornecedor.service';

@Component({
  selector: 'app-materiais',
  standalone: true, 
  imports: [FooterComponent, HeaderComponent, RouterModule, FormsModule, CommonModule],
  templateUrl: './materiais.component.html',
  styleUrl: './materiais.component.css'
})
export class MateriaisComponent implements OnInit {

  constructor(
    private fornecedorService: FornecedorService,
    private materialService: MaterialService
  ) { }

  listaMateriais: any[] = [];
  fornecedores: any[] = [];

  termoPesquisa: string = '';
listaMateriaisFiltrada: any[] = [];
filtroStatus: string = 'todos';

  material: any = {};
  novoFornecedor: any = {};

  modoEdicao: boolean = false;
  idMaterialParaDeletar: number | null = null;
  mensagemSucesso: string = '';
  mensagemErro: string = '';
  listaEstoqueBaixo: any[] = [];

  ngOnInit() {
    this.carregarMateriais();
    this.carregarFornecedores();
  }

  carregarMateriais() {
  this.materialService.listarTodos().subscribe(dados => {
    this.listaMateriais = dados.sort((a: any, b: any) => a.id - b.id);
    this.filtrarMateriais();
    this.atualizarListaEstoqueBaixo();
  });
}

filtrarMateriais() {
  let resultado = [...this.listaMateriais];

  if (this.filtroStatus === 'disponivel') {
    resultado = resultado.filter(item => item.status !== 'indisponivel');
  } else if (this.filtroStatus === 'indisponivel') {
    resultado = resultado.filter(item => item.status === 'indisponivel');
  }

  const termo = this.termoPesquisa?.trim().toLowerCase();
  if (termo) {
    resultado = resultado.filter(item =>
      item.nome?.toLowerCase().includes(termo) ||
      item.categoria?.toLowerCase().includes(termo) ||
      item.codigoReferencia?.toLowerCase().includes(termo)
    );
  }

  this.listaMateriaisFiltrada = resultado;
}

selecionarFiltroStatus(status: string) {
  this.filtroStatus = status;
  this.filtrarMateriais();
}

get countTodosMateriais(): number {
  return this.listaMateriais.length;
}

get countMateriaisDisponiveis(): number {
  return this.listaMateriais.filter(item => item.status !== 'indisponivel').length;
}

get countMateriaisIndisponiveis(): number {
  return this.listaMateriais.filter(item => item.status === 'indisponivel').length;
}

  carregarFornecedores() {
    this.fornecedorService.listarTodos().subscribe(dados => {
      this.fornecedores = dados;
    });
  }

  abrirModalMaterial() {
    this.modoEdicao = false;
    this.material = { status: 'disponivel' };
    (document.getElementById('modal-material') as HTMLDialogElement).showModal();
  }

  fecharModalMaterial() {
    (document.getElementById('modal-material') as HTMLDialogElement).close();
  }

  editarMaterial(item: any) {
    this.modoEdicao = true;
    this.material = { ...item };
    (document.getElementById('modal-material') as HTMLDialogElement).showModal();
  }

  confirmarSalvar() {
    (document.getElementById('modal-confirmacao-salvar') as HTMLDialogElement).showModal();
  }

  fecharConfirmacaoSalvar() {
    (document.getElementById('modal-confirmacao-salvar') as HTMLDialogElement).close();
  }

  executarSalvar() {
    if (this.modoEdicao) {
      this.materialService.atualizar(this.material.id, this.material).subscribe({
        next: () => {
          this.fecharConfirmacaoSalvar();
          this.fecharModalMaterial();
          this.carregarMateriais();
          this.mostrarSucesso('Material atualizado com sucesso!');
        },
        error: (err) => {
          this.fecharConfirmacaoSalvar();
          this.mostrarErro(err.error?.message || 'Não foi possível atualizar o material.');
        }
      });
    } else {
      this.materialService.cadastrar(this.material).subscribe({
        next: () => {
          this.fecharConfirmacaoSalvar();
          this.fecharModalMaterial();
          this.carregarMateriais();
          this.mostrarSucesso('Material cadastrado com sucesso!');
        },
        error: (err) => {
          this.fecharConfirmacaoSalvar();
          this.mostrarErro(err.error?.message || 'Não foi possível cadastrar o material.');
        }
      });
    }
  }

  mostrarSucesso(mensagem: string) {
    this.mensagemSucesso = mensagem;
    (document.getElementById('modal-sucesso') as HTMLDialogElement).showModal();
  }

  fecharSucesso() {
    (document.getElementById('modal-sucesso') as HTMLDialogElement).close();
  }

  mostrarErro(mensagem: string) {
    this.mensagemErro = mensagem;
    (document.getElementById('modal-erro') as HTMLDialogElement).showModal();
  }

  fecharErro() {
    (document.getElementById('modal-erro') as HTMLDialogElement).close();
  }

  abrirConfirmacaoDelecao(id: number) {
    this.idMaterialParaDeletar = id;
    (document.getElementById('modal-confirmacao') as HTMLDialogElement).showModal();
  }

  fecharConfirmacaoDelecao() {
    this.idMaterialParaDeletar = null;
    (document.getElementById('modal-confirmacao') as HTMLDialogElement).close();
  }

  executarDelecao() {
    if (this.idMaterialParaDeletar !== null) {
      this.materialService.deletar(this.idMaterialParaDeletar).subscribe({
        next: () => {
          this.fecharConfirmacaoDelecao();
          this.carregarMateriais();
          this.mostrarSucesso('Material removido do estoque!');
        },
        error: (err) => {
          this.fecharConfirmacaoDelecao();
          this.mostrarErro(err.error?.message || 'Não foi possível excluir o material.');
        }
      });
    }
  }

  alternarStatus(item: any) {
    const novoStatus = item.status === 'indisponivel' ? 'disponivel' : 'indisponivel';
    const payload = { ...item, status: novoStatus };
    this.materialService.atualizar(item.id, payload).subscribe({
      next: () => this.carregarMateriais(),
      error: (err) => this.mostrarErro(err.error?.message || 'Não foi possível atualizar o status do material.')
    });
  }

  abrirModalFornecedor() {
    this.novoFornecedor = {};
    (document.getElementById('modal-fornecedor') as HTMLDialogElement).showModal();
  }

  fecharModalFornecedor() {
    (document.getElementById('modal-fornecedor') as HTMLDialogElement).close();
  }

  salvarFornecedor() {
    this.fornecedorService.salvar(this.novoFornecedor).subscribe(fornecedorSalvo => {
      this.carregarFornecedores(); 
      this.material.fornecedorId = (fornecedorSalvo as any).id; 
      this.fecharModalFornecedor();
    });
  }

  normalizarDecimal(event: any, campo: 'quantidadeEstoque' | 'estoqueMinimo') {
    let valor = event.target.value.replace(',', '.').replace(/[^0-9.]/g, '');
    const partes = valor.split('.');
    if (partes.length > 2) {
      valor = partes[0] + '.' + partes.slice(1).join('');
    }
    this.material[campo] = valor;
    event.target.value = valor;
  }

  formatarDocumento(event: any) {
    let valor = event.target.value.replace(/\D/g, '');

    if (valor.length <= 11) { 
      valor = valor.replace(/(\d{3})(\d)/, '$1.$2');
      valor = valor.replace(/(\d{3})(\d)/, '$1.$2');
      valor = valor.replace(/(\d{3})(\d{1,2})$/, '$1-$2');
    } else { 
      valor = valor.replace(/^(\d{2})(\d)/, '$1.$2');
      valor = valor.replace(/^(\d{2})\.(\d{3})(\d)/, '$1.$2.$3');
      valor = valor.replace(/\.(\d{3})(\d)/, '.$1/$2');
      valor = valor.replace(/(\d{4})(\d)/, '$1-$2');
    }

    event.target.value = valor;
    this.novoFornecedor.cnpjCpf = valor;
  }

  formatarTelefone(event: any) {
    let valor = event.target.value.replace(/\D/g, '');

    if (valor.length <= 10) { 
      valor = valor.replace(/^(\d{2})(\d)/g, '($1) $2');
      valor = valor.replace(/(\d{4})(\d)/, '$1-$2');
    } else { 
      valor = valor.replace(/^(\d{2})(\d)/g, '($1) $2');
      valor = valor.replace(/(\d{5})(\d)/, '$1-$2');
    }

    event.target.value = valor;
    this.novoFornecedor.telefone = valor;
  }

  atualizarListaEstoqueBaixo() {
    this.listaEstoqueBaixo = this.listaMateriais.filter(item => {
      if (item.estoqueMinimo === null || item.estoqueMinimo === undefined || item.estoqueMinimo === '') {
        return false;
      }
      return Number(item.quantidadeEstoque) <= Number(item.estoqueMinimo);
    });
  }

  get materiaisComEstoqueBaixo(): any[] {
    return this.listaMateriais.filter(item => {
      if (item.estoqueMinimo === null || item.estoqueMinimo === undefined || item.estoqueMinimo === '') {
        return false;
      }

      const qtd = Number(item.quantidadeEstoque);
      const min = Number(item.estoqueMinimo);

      return qtd <= min;
    });
  }

  // Substitua os getters antigos por estes mais simples
  get qtdEstoqueBaixo(): number {
    return this.listaEstoqueBaixo.length;
  }

  get qtdEstoqueNormal(): number {
    return this.listaMateriais.length - this.listaEstoqueBaixo.length;
  }
}