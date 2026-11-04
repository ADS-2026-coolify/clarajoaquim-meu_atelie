import { Component, HostListener, ViewChild } from '@angular/core';
import { FooterComponent } from '../../shared/components/footer/footer.component';
import { HeaderComponent } from '../../shared/components/header/header.component';
import { CadastroClienteComponent } from '../cadastro-cliente/cadastro-cliente.component';
import { RouterModule } from '@angular/router';
import { ClienteResumo } from '../../core/models/cliente-resumo';
import { ClienteService } from '../../core/services/cliente.service';
import { FormsModule } from '@angular/forms';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { ModalComponent } from '../../shared/components/modal/modal.component';
import { Subscription, take } from 'rxjs';

@Component({
  selector: 'app-clientes',
  imports: [CommonModule, FooterComponent, HeaderComponent, CadastroClienteComponent, RouterModule, FormsModule, FooterComponent, CurrencyPipe, ModalComponent],
  templateUrl: './clientes.component.html',
  styleUrl: './clientes.component.css'
})
export class ClientesComponent {
  @ViewChild(ModalComponent) modal!: ModalComponent;
  private desativarSub?: Subscription;
  private reativarSub?: Subscription;
  private excluirSub?: Subscription;

  clientes: ClienteResumo[] = [];
  clientesFiltrados: ClienteResumo[] = [];
  termoBusca: string = '';

  paginaAtual: number = 1;
  pageSize: number = 9;

  totalClientes: number = 0;
  clientesRecorrentes: number = 0;
  novosEsteMes: number = 0;

  filtroStatus: string = 'ativos';
  filtroAtual: string = 'ativos';
  countAtivos: number = 0;
  countRecorrentes: number = 0;
  countNovos: number = 0;
  countInativos: number = 0;
  countTodos: number = 0;

  constructor(private clienteService: ClienteService) { }

  ngOnInit() {
    this.carregarClientes();
    this.ajustarQuantidadePagina(window.innerWidth);
  }

  carregarClientes() {
    this.clienteService.listarResumoClientes().subscribe({
      next: (dados) => {
        let clientesBase = dados.sort((a, b) => {
          const dataA = a.dataCadastro ? new Date(a.dataCadastro).getTime() : 0;
          const dataB = b.dataCadastro ? new Date(b.dataCadastro).getTime() : 0;
          return dataB - dataA;
        });

        const mesAtual = new Date().getMonth() + 1; 
        const anoAtual = new Date().getFullYear();

        this.countTodos = clientesBase.length;
        this.countAtivos = clientesBase.filter(c => c.ativo).length;
        this.countInativos = clientesBase.filter(c => !c.ativo).length;
        this.countRecorrentes = clientesBase.filter(c => c.ativo && c.totalPedidos > 1).length;
        this.countNovos = clientesBase.filter(c => {
          if (!c.dataCadastro || !c.ativo) return false;
          const data = new Date(c.dataCadastro);
          return (data.getMonth() + 1) === mesAtual && data.getFullYear() === anoAtual;
        }).length;

        this.totalClientes = this.countAtivos;
        this.clientesRecorrentes = this.countRecorrentes;
        this.novosEsteMes = this.countNovos;

        if (this.filtroAtual === 'ativos') {
          clientesBase = clientesBase.filter(c => c.ativo);
        } else if (this.filtroAtual === 'inativos') {
          clientesBase = clientesBase.filter(c => !c.ativo);
        } else if (this.filtroAtual === 'recorrentes') {
          clientesBase = clientesBase.filter(c => c.ativo && c.totalPedidos > 1);
        } else if (this.filtroAtual === 'novos') {
          clientesBase = clientesBase.filter(c => {
            if (!c.dataCadastro || !c.ativo) return false;
            const data = new Date(c.dataCadastro);
            return (data.getMonth() + 1) === mesAtual && data.getFullYear() === anoAtual;
          });
        }

        this.clientes = clientesBase;
        this.clientesFiltrados = [...this.clientes];
        
        this.filtrarClientes(); 
      },
      error: (err) => console.error('Erro ao buscar clientes', err)
    });
  }

  calcularMetricas() {
    this.totalClientes = this.clientes.length;
    this.clientesRecorrentes = this.clientes.filter(c => c.totalPedidos > 1).length;

    const mesAtual = new Date().getMonth() + 1;
    const anoAtual = new Date().getFullYear();

    this.novosEsteMes = this.clientes.filter(c => {
      if (!c.dataCadastro) return false;
      const data = new Date(c.dataCadastro);
      return (data.getMonth() + 1) === mesAtual && data.getFullYear() === anoAtual;
    }).length;
  }

  @HostListener('window:resize', ['$event'])
  onResize(event: any) {
    this.ajustarQuantidadePagina(event.target.innerWidth);
  }

  private ajustarQuantidadePagina(width: number) {
    if (width < 768) {
      this.pageSize = 3;
    } else if (width < 1024) {
      this.pageSize = 6;
    } else {
      this.pageSize = 9;
    }
  }

  get clientesExibidos() {
    const inicio = (this.paginaAtual - 1) * this.pageSize;
    return this.clientesFiltrados.slice(inicio, inicio + this.pageSize);
  }

  get totalPaginas(): number {
    return Math.ceil(this.clientesFiltrados.length / this.pageSize);
  }

  filtrarClientes() {
    this.paginaAtual = 1;
    const termo = this.termoBusca?.trim().toLowerCase();
    if (!termo) {
      this.clientesFiltrados = this.clientes;
      return;
    }
    this.clientesFiltrados = this.clientes.filter(c =>
      c.nome?.toLowerCase().includes(termo)
    );
  }

  getIniciais(nome: string): string {
    if (!nome) return '--';
    const partes = nome.trim().split(' ');
    if (partes.length === 1) return partes[0].substring(0, 2).toUpperCase();
    return (partes[0][0] + partes[partes.length - 1][0]).toUpperCase();
  }

  selecionarFiltro(filtro: string) {
    this.filtroAtual = filtro;
    this.paginaAtual = 1;
    this.carregarClientes();
  }

  aoMudarFiltro() {
    this.paginaAtual = 1;
    this.termoBusca = '';
    this.carregarClientes();
  }

  desativar(cliente: ClienteResumo) {
    this.modal.abrirModal('Confirmar desativação', `Deseja realmente desativar o cliente ${cliente.nome}?`, 'confirmacao', 'Desativar');
    this.desativarSub?.unsubscribe();
    this.desativarSub = this.modal.acaoConfirmada.pipe(take(1)).subscribe(() => {
      this.clienteService.desativarCliente(cliente.id).subscribe({
        next: () => this.carregarClientes(),
        error: () => this.modal.abrirModal('Erro', 'Erro ao desativar cliente.', 'erro', 'Fechar')
      });
    });
  }

  reativar(cliente: ClienteResumo) {
    this.modal.abrirModal('Confirmar reativação', `Deseja reativar o cliente ${cliente.nome}?`, 'confirmacao', 'Reativar');
    this.reativarSub?.unsubscribe();
    this.reativarSub = this.modal.acaoConfirmada.pipe(take(1)).subscribe(() => {
      this.clienteService.reativarCliente(cliente.id).subscribe({
        next: () => this.carregarClientes(),
        error: () => this.modal.abrirModal('Erro', 'Erro ao reativar cliente.', 'erro', 'Fechar')
      });
    });
  }

  excluirPermanentemente(cliente: ClienteResumo) {
    this.modal.abrirModal(
      'Excluir cliente',
      `Deseja excluir permanentemente o cliente ${cliente.nome}? Essa ação não pode ser desfeita.`,
      'confirmacao',
      'Excluir'
    );
    this.excluirSub?.unsubscribe();
    this.excluirSub = this.modal.acaoConfirmada.pipe(take(1)).subscribe(() => {
      this.clienteService.deletarPermanentemente(cliente.id).subscribe({
        next: () => this.carregarClientes(),
        error: (err) => {
          const mensagem = err.error?.message || 'Não foi possível excluir o cliente.';
          this.modal.abrirModal('Não foi possível excluir', mensagem, 'erro', 'Fechar');
        }
      });
    });
  }

}
