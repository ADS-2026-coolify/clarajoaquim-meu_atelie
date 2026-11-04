import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common'; // Necessário para o @for e pipes
import { HeaderComponent } from '../../shared/components/header/header.component';
import { FooterComponent } from '../../shared/components/footer/footer.component';
import { Router, RouterModule } from '@angular/router';
import { ProdutoService } from '../../core/services/produto.service'; // Ajuste o caminho se necessário
import { ProdutoDTO } from '../../core/models/DTO/produto-dto';
import { FormsModule } from '@angular/forms';
import { ModalComponent } from '../../shared/components/modal/modal.component';
import { Subscription, take } from 'rxjs';

import { API_URL } from '../../core/config';
@Component({
  selector: 'app-produtos',
  standalone: true,
  imports: [HeaderComponent, FooterComponent, RouterModule, CommonModule, FormsModule, ModalComponent],
  templateUrl: './produtos.component.html',
  styleUrl: './produtos.component.css'
})
export class ProdutosComponent implements OnInit {
  readonly apiUrl = API_URL;

  produtos: ProdutoDTO[] = [];
  totalProdutos: number = 0;
  totalDisponiveis: number = 0;
  totalIndisponiveis: number = 0;
  totalSobEncomenda: number = 0;

  produtosFiltrados: ProdutoDTO[] = [];
  termoBusca: string = '';
  statusSelecionado: string = 'disponivel';

  @ViewChild(ModalComponent) modal!: ModalComponent;
  private excluirSub?: Subscription;
  private alternarStatusSub?: Subscription;

  constructor(
    private produtoService: ProdutoService,
    private router: Router
  ) { }

  ngOnInit() {
    this.carregarProdutos();
  }

  carregarProdutos() {
  this.produtoService.listarProdutosDoUsuario().subscribe({
    next: (data) => {
      this.produtos = data;

      this.aplicarFiltros(); 
      this.calcularResumo();
    },
    error: (err) => {
      console.error('Erro ao buscar produtos:', err);
    }
  });
}
  calcularResumo() {
    this.totalProdutos = this.produtos.length;

    this.totalDisponiveis = this.produtos.filter(p => p.status === 'disponivel').length;
    this.totalIndisponiveis = this.produtos.filter(p => p.status === 'indisponivel').length;
  }

  aplicarFiltros() {
  this.produtosFiltrados = this.produtos.filter(p => {
    // 1. Filtro de Busca
    const buscaMatch = this.termoBusca === '' || 
      p.nome.toLowerCase().includes(this.termoBusca.toLowerCase()) ||
      (p.nomeCategoria && p.nomeCategoria.toLowerCase().includes(this.termoBusca.toLowerCase()));

    // 3. Filtro de Status
    const statusMatch = this.statusSelecionado === 'Todos' || p.status === this.statusSelecionado;

    return buscaMatch && statusMatch;
  });
}

  nomesMateriais(produto: ProdutoDTO): string {
    return (produto.materiaisUtilizados ?? []).map(m => m.nomeMaterial).join(', ');
  }

  // Método para navegação de edição
editar(produto: ProdutoDTO) {
  // Você precisará de uma rota /produtos/editar/:id no seu app.routes.ts
  this.router.navigate(['/produtos/editar', produto.id]);
}

confirmarAlternarStatus(produto: ProdutoDTO) {
  const vaiAtivar = produto.status !== 'disponivel';
  this.modal.abrirModal(
    vaiAtivar ? 'Ativar produto' : 'Desativar produto',
    `Deseja realmente ${vaiAtivar ? 'ativar' : 'desativar'} o produto "${produto.nome}"?`,
    'confirmacao',
    vaiAtivar ? 'Ativar' : 'Desativar'
  );
  this.alternarStatusSub?.unsubscribe();
  this.alternarStatusSub = this.modal.acaoConfirmada.pipe(take(1)).subscribe(() => {
    this.alternarStatus(produto);
  });
}

alternarStatus(produto: ProdutoDTO) {
  const novoStatus = produto.status === 'disponivel' ? 'indisponivel' : 'disponivel';

  const payload = {
    nome: produto.nome,
    descricao: produto.descricao,
    preco: produto.preco,
    categoriaId: produto.categoriaId,
    materiais: (produto.materiaisUtilizados ?? []).map(m => ({
      materialId: m.materialId,
      quantidade: m.quantidade,
      unidade: m.unidade
    })),
    precoCusto: produto.precoCusto,
    quantidadeEstoque: produto.quantidadeEstoque,
    status: novoStatus,
    largura: produto.largura,
    altura: produto.altura,
    observacoes: produto.observacoes
  };

  const formData = new FormData();
  formData.append('produto', new Blob([JSON.stringify(payload)], { type: 'application/json' }));

  this.produtoService.atualizarProduto(produto.id!, formData).subscribe({
    next: () => {
      this.carregarProdutos(); // Recarrega a lista
      const mensagem = novoStatus === 'disponivel' ? 'Produto ativado com sucesso!' : 'Produto desativado com sucesso!';
      this.modal.abrirModal('Sucesso', mensagem, 'sucesso', 'OK');
    },
    error: (err) => {
      console.error('Erro ao atualizar status', err);
      this.modal.abrirModal('Erro', 'Não foi possível atualizar o status do produto.', 'erro', 'Fechar');
    }
  });
}


selecionarStatus(status: string) {
  this.statusSelecionado = status;
  this.aplicarFiltros();
}

excluir(produto: ProdutoDTO) {
  this.modal.abrirModal(
    'Excluir produto',
    `Deseja excluir permanentemente o produto "${produto.nome}"? Essa ação não pode ser desfeita.`,
    'confirmacao',
    'Excluir'
  );
  this.excluirSub?.unsubscribe();
  this.excluirSub = this.modal.acaoConfirmada.pipe(take(1)).subscribe(() => {
    this.produtoService.deletarProduto(produto.id!).subscribe({
      next: () => {
        this.carregarProdutos();
        this.modal.abrirModal('Sucesso', `O produto "${produto.nome}" foi excluído permanentemente.`, 'sucesso', 'OK');
      },
      error: (err) => {
        const mensagem = err.error?.message || 'Não foi possível excluir o produto.';
        this.modal.abrirModal('Não foi possível excluir', mensagem, 'erro', 'Fechar');
      }
    });
  });
}

}