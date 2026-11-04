import { Component, OnInit } from '@angular/core';
import { HeaderComponent } from '../../shared/components/header/header.component';
import { FooterComponent } from '../../shared/components/footer/footer.component';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { FornecedorService } from '../../core/services/fornecedor.service';
import { MaterialService } from '../../core/services/material.service';
import { forkJoin } from 'rxjs';

// Importação das interfaces de contrato
import { FornecedorForm } from '../../core/models/Form/fornecedor-form';
import { FornecedorDTO } from '../../core/models/DTO/fornecedor-dto';

@Component({
  selector: 'app-cadastro-materiais',
  standalone: true,
  imports: [HeaderComponent, FooterComponent, RouterModule, FormsModule, CommonModule],
  templateUrl: './cadastro-materiais.component.html',
  styleUrl: './cadastro-materiais.component.css'
})
export class CadastroMateriaisComponent implements OnInit {

  constructor(
    private fornecedorService: FornecedorService,
    private materialService: MaterialService,
    private router: Router
  ) { }

  // Listas e estados
  fornecedores: FornecedorDTO[] = [];
  materiaisRascunho: any[] = [];
  
  // Objetos de manipulação
  material: any = {};
  novoFornecedor: FornecedorForm = this.resetarFornecedorForm();
  fornecedorSelecionadoId: number | null = null; 
  
  modoEdicao: boolean = false;
  indexEdicao: number = -1;

  // Feedback ao usuário
  mensagemValidacao: string = '';
  mensagemSucesso: string = '';
  navegarAposSucesso: boolean = false;

  ngOnInit() {
    this.carregarFornecedores();
  }

  // --- LÓGICA DE FORNECEDOR ---

  carregarFornecedores() {
    this.fornecedorService.listarTodos().subscribe({
      next: (dados) => this.fornecedores = dados,
      error: () => console.error('Erro ao carregar fornecedores')
    });
  }

  private resetarFornecedorForm(): FornecedorForm {
    return { nome: '', cnpjCpf: '', telefone: '', email: '' };
  }

  abrirModalFornecedor() {
    this.novoFornecedor = this.resetarFornecedorForm();
    (document.getElementById('modal-fornecedor') as HTMLDialogElement).showModal();
  }

  fecharModalFornecedor() {
    (document.getElementById('modal-fornecedor') as HTMLDialogElement).close();
  }

  salvarFornecedor() {
    if (!this.novoFornecedor.nome) {
      this.mostrarValidacao('O nome do fornecedor é obrigatório.');
      return;
    }

    this.fornecedorService.salvar(this.novoFornecedor).subscribe({
      next: (f: FornecedorDTO) => {
        this.carregarFornecedores();
        this.fornecedorSelecionadoId = f.id; // Seleciona automaticamente o fornecedor criado
        this.fecharModalFornecedor();
        this.novoFornecedor = this.resetarFornecedorForm();
      },
      error: (err) => {
        console.error(err);
        this.mostrarValidacao('Erro ao salvar fornecedor no servidor.');
      }
    });
  }

  // --- LÓGICA DE MATERIAIS (RASCUNHO) ---

  abrirModalMaterial() {
    this.modoEdicao = false;
    this.indexEdicao = -1;
    this.material = { categoria: '', unidadeMedida: '', anotacoes: '' }; 
    (document.getElementById('modal-material') as HTMLDialogElement).showModal();
  }

  editarDoRascunho(index: number) {
    this.modoEdicao = true;
    this.indexEdicao = index;
    this.material = { ...this.materiaisRascunho[index] }; 
    (document.getElementById('modal-material') as HTMLDialogElement).showModal();
  }

  fecharModalMaterial() {
    (document.getElementById('modal-material') as HTMLDialogElement).close();
  }

  executarSalvar() {
    if (this.modoEdicao) {
      this.materiaisRascunho[this.indexEdicao] = { ...this.material };
    } else {
      this.materiaisRascunho.push({ ...this.material });
    }
    this.fecharConfirmacaoSalvar();
    this.fecharModalMaterial();
  }

  removerDoRascunho(index: number) {
    this.materiaisRascunho.splice(index, 1);
  }

  salvarLoteNoBanco() {
    if (this.materiaisRascunho.length === 0) return;
    if (!this.fornecedorSelecionadoId) {
      this.mostrarValidacao('Selecione o Fornecedor antes de salvar o lote.');
      return;
    }

    const materiaisParaSalvar = this.materiaisRascunho.map(mat => {
      return {
        ...mat,
        fornecedorId: this.fornecedorSelecionadoId,
        precoUnitario: mat.precoUnitario.toString().replace(/\./g, '').replace(',', '.')
      };
    });
    
    const requests = materiaisParaSalvar.map(mat => this.materialService.cadastrar(mat));
    
    forkJoin(requests).subscribe({
      next: () => this.mostrarSucesso('Todos os materiais foram cadastrados com sucesso!', true),
      error: () => this.mostrarValidacao('Erro ao salvar lote no servidor. Verifique os dados.')
    });
  }

  // --- MODAIS DE FEEDBACK E VALIDAÇÃO ---

  validarFormulario(): boolean {
    const m = this.material;
    if (!m.nome || !m.categoria || !m.cor || !m.marca || m.quantidadeEstoque == null || !m.unidadeMedida || !m.precoUnitario) {
      this.mostrarValidacao('Preencha todos os campos obrigatórios (*).');
      return false;
    }
    return true;
  }

  mostrarValidacao(mensagem: string) {
    this.mensagemValidacao = mensagem;
    (document.getElementById('modal-validacao') as HTMLDialogElement).showModal();
  }

  fecharValidacao() {
    (document.getElementById('modal-validacao') as HTMLDialogElement).close();
  }

  mostrarSucesso(mensagem: string, navegar: boolean = false) {
    this.mensagemSucesso = mensagem;
    this.navegarAposSucesso = navegar;
    (document.getElementById('modal-sucesso') as HTMLDialogElement).showModal();
  }

  fecharSucesso() {
    (document.getElementById('modal-sucesso') as HTMLDialogElement).close();
    if (this.navegarAposSucesso) this.router.navigate(['/materiais']);
  }

  confirmarSalvar() {
    if (!this.validarFormulario()) return;
    (document.getElementById('modal-confirmacao-salvar') as HTMLDialogElement).showModal();
  }

  fecharConfirmacaoSalvar() {
    (document.getElementById('modal-confirmacao-salvar') as HTMLDialogElement).close();
  }

  // --- MÁSCARAS E FORMATAÇÕES ---

  formatarDocumento(event: any) {
    let valor = event.target.value.replace(/\D/g, '');
    if (valor.length <= 11) { 
      valor = valor.replace(/(\d{3})(\d)/, '$1.$2').replace(/(\d{3})(\d)/, '$1.$2').replace(/(\d{3})(\d{1,2})$/, '$1-$2');
    } else { 
      valor = valor.replace(/^(\d{2})(\d)/, '$1.$2').replace(/^(\d{2})\.(\d{3})(\d)/, '$1.$2.$3').replace(/\.(\d{3})(\d)/, '.$1/$2').replace(/(\d{4})(\d)/, '$1-$2');
    }
    this.novoFornecedor.cnpjCpf = valor;
    event.target.value = valor;
  }

  formatarTelefone(event: any) {
    let valor = event.target.value.replace(/\D/g, '');
    valor = valor.replace(/^(\d{2})(\d)/g, '($1) $2');
    valor = valor.length <= 14 ? valor.replace(/(\d{4})(\d)/, '$1-$2') : valor.replace(/(\d{5})(\d)/, '$1-$2');
    this.novoFornecedor.telefone = valor;
    event.target.value = valor;
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

  formatarMoeda(event: any) {
    let valor = event.target.value.replace(/\D/g, '');
    if (!valor) { this.material.precoUnitario = ''; return; }
    valor = (Number(valor) / 100).toFixed(2).replace('.', ',');
    valor = valor.replace(/(\d)(?=(\d{3})+(?!\d))/g, '$1.');
    this.material.precoUnitario = valor;
    event.target.value = valor;
  }
}