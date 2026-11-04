import { Component, OnInit } from '@angular/core';
import { FooterComponent } from '../../shared/components/footer/footer.component';
import { HeaderComponent } from '../../shared/components/header/header.component';
import { RouterModule } from '@angular/router';
import { PedidoService } from '../../core/services/pedido.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-home',
  imports: [FooterComponent, HeaderComponent, RouterModule, CommonModule],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit {
  dashboard: any = null;
  saudacao: string = '';

  constructor(private pedidoService: PedidoService) {}

  ngOnInit(): void {
    this.definirSaudacao();
    this.pedidoService.obterDashboard().subscribe({
      next: (dados) => {
        const ordenados = dados.pedidosRecentes.sort((a: any, b: any) => (a.id ?? 0) - (b.id ?? 0));
        
        const comIdVisual = ordenados.map((pedido: any, index: number) => ({
          ...pedido,
          idVisual: index + 1
        }));
        
        dados.pedidosRecentes = comIdVisual.slice(-5);
        
        this.dashboard = dados;
      },
      error: (erro) => console.error('Erro ao carregar dashboard', erro)
    });
  }

  obterStatusAjustado(statusObj: any): string {
    if (typeof statusObj === 'string') return statusObj.toUpperCase();
    if (!statusObj || !statusObj.descricao) return '';
    return statusObj.descricao
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .toUpperCase()
      .replace(' ', '_');
  }

getClasseStatus(statusObj: any): string {
    const statusFormatado = this.obterStatusAjustado(statusObj);
    switch (statusFormatado) {
      case 'EM_ANDAMENTO': return 'status--progress';
      case 'FINALIZADO': return 'status--done';
      case 'ORCAMENTO': return 'status--orcamento';
      case 'CANCELADO': 
      case 'ATRASADO': return 'status--late';
      default: return 'status--progress';
    }
  }

  getLabelStatus(statusObj: any): string {
    if (statusObj && statusObj.descricao) {
      return statusObj.descricao;
    }
    
    const statusFormatado = this.obterStatusAjustado(statusObj);
    const mapa: any = {
      'EM_ANDAMENTO': 'Em andamento',
      'FINALIZADO': 'Finalizado',
      'ATRASADO': 'Atrasado',
      'CANCELADO': 'Cancelado',
      'ORCAMENTO': 'Orçamento'
    };
    return mapa[statusFormatado] || statusFormatado;
  }

  definirSaudacao() {
  const hora = new Date().getHours();
  if (hora >= 6 && hora < 12) {
    this.saudacao = 'Bom dia';
  } else if (hora >= 12 && hora < 19) {
    this.saudacao = 'Boa tarde';
  } else {
    this.saudacao = 'Boa noite';
  }
}
}
