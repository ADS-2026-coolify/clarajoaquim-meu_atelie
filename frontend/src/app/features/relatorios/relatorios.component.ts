import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FooterComponent } from '../../shared/components/footer/footer.component';
import { HeaderComponent } from '../../shared/components/header/header.component';
import { RelatorioService } from '../../core/services/relatorio.service';
import { PeriodoTipo } from '../../core/models/DTO/periodo-tipo';
import { IndicadoresPeriodoDTO } from '../../core/models/DTO/indicadores-periodo-dto';
import { FaturamentoMensalDTO } from '../../core/models/DTO/faturamento-mensal-dto';
import { RankingProdutoDTO } from '../../core/models/DTO/ranking-produto-dto';
import { RankingClienteDTO } from '../../core/models/DTO/ranking-cliente-dto';
import { ModalComponent } from '../../shared/components/modal/modal.component';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';

type PeriodoSelecao = PeriodoTipo | 'PERSONALIZADO';

interface MesBase extends FaturamentoMensalDTO {
  label: string;
  isMesAtual: boolean;
  isProjetado: boolean;
}

interface MesExibicao extends MesBase {
  alturaPercentual: number;
}

const NOMES_MESES = ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez'];

const ROTULOS_PERIODO: Record<PeriodoTipo, string> = {
  SEMANA: 'da semana',
  MES: 'do mês',
  TRIMESTRE: 'do trimestre',
  SEMESTRE: 'do semestre',
  ANO: 'do ano',
};

@Component({
  selector: 'app-relatorios',
  imports: [CommonModule, FooterComponent, HeaderComponent, ModalComponent, FormsModule],
  templateUrl: './relatorios.component.html',
  styleUrl: './relatorios.component.css'
})
export class RelatoriosComponent implements OnInit {
  @ViewChild(ModalComponent) modal!: ModalComponent;

  periodos: { valor: PeriodoSelecao; label: string }[] = [
    { valor: 'SEMANA', label: 'Semana' },
    { valor: 'MES', label: 'Mês' },
    { valor: 'TRIMESTRE', label: 'Trimestre' },
    { valor: 'SEMESTRE', label: 'Semestre' },
    { valor: 'ANO', label: 'Ano' },
    { valor: 'PERSONALIZADO', label: 'Personalizado' },
  ];
  periodoSelecionado: PeriodoSelecao = 'MES';

  dataMaxima: string = new Date().toISOString().substring(0, 10);
  dataInicioCustom: string = '';
  dataFimCustom: string = '';

  gerandoPdf = false;

  indicadores: IndicadoresPeriodoDTO | null = null;
  mesesExibidos: MesBase[] = [];
  rankingProdutos: RankingProdutoDTO[] = [];
  rankingClientes: RankingClienteDTO[] = [];

  carregandoIndicadores = false;
  carregandoRankingProdutos = false;
  carregandoRankingClientes = false;

  anoAtual = new Date().getFullYear();

  constructor(private relatorioService: RelatorioService) {}

  ngOnInit(): void {
    this.carregarIndicadoresERankings();
    this.carregarFaturamentoMensal();
  }

  selecionarPeriodo(periodo: PeriodoSelecao): void {
    if (periodo === this.periodoSelecionado) {
      return;
    }
    this.periodoSelecionado = periodo;
    if (periodo === 'PERSONALIZADO') {
      return;
    }
    this.carregarIndicadoresERankings();
  }

  aplicarPeriodoPersonalizado(): void {
    if (!this.dataInicioCustom || !this.dataFimCustom || this.dataFimCustom < this.dataInicioCustom) {
      return;
    }
    this.carregarIndicadoresERankings();
  }

  rotuloPeriodo(): string {
    if (this.periodoSelecionado === 'PERSONALIZADO') {
      if (this.dataInicioCustom && this.dataFimCustom) {
        return `de ${this.formatarDataCurta(this.dataInicioCustom)} a ${this.formatarDataCurta(this.dataFimCustom)}`;
      }
      return 'personalizado';
    }
    return ROTULOS_PERIODO[this.periodoSelecionado];
  }

  get mostrarGraficoFaturamento(): boolean {
    return this.periodoSelecionado === 'TRIMESTRE' || this.periodoSelecionado === 'SEMESTRE' || this.periodoSelecionado === 'ANO';
  }

  get mesesGrafico(): MesExibicao[] {
    let visiveis: MesBase[];

    if (this.periodoSelecionado === 'TRIMESTRE') {
      visiveis = this.filtrarUltimosMeses(3);
    } else if (this.periodoSelecionado === 'SEMESTRE') {
      visiveis = this.filtrarUltimosMeses(6);
    } else if (this.periodoSelecionado === 'ANO') {
      const hoje = new Date();
      const anoAtualValue = hoje.getFullYear();
      const limite = Math.min(hoje.getMonth() + 2, 12);
      visiveis = this.mesesExibidos.filter((m) => m.ano === anoAtualValue && m.mes <= limite);
    } else {
      visiveis = [];
    }

    const maiorValor = Math.max(...visiveis.map((item) => item.valor), 1);

    return visiveis.map((item) => ({
      ...item,
      alturaPercentual: Math.max(Math.round((item.valor / maiorValor) * 100), 2)
    }));
  }

  private filtrarUltimosMeses(quantidade: number): MesBase[] {
    const hoje = new Date();
    const janela = Array.from({ length: quantidade }, (_, i) => {
      const referencia = new Date(hoje.getFullYear(), hoje.getMonth() - (quantidade - 1 - i), 1);
      return { ano: referencia.getFullYear(), mes: referencia.getMonth() + 1 };
    });

    return janela
      .map((alvo) => this.mesesExibidos.find((m) => m.ano === alvo.ano && m.mes === alvo.mes))
      .filter((m): m is MesBase => !!m);
  }

  private formatarDataCurta(iso: string): string {
    const [, mes, dia] = iso.split('-');
    return `${dia}/${mes}`;
  }

  gerarPdf(): void {
    if (this.periodoSelecionado === 'PERSONALIZADO' && (!this.dataInicioCustom || !this.dataFimCustom)) {
      this.modal.abrirModal('Atenção', 'Selecione o período personalizado antes de gerar o PDF.', 'erro', 'Fechar');
      return;
    }

    const periodo = this.periodoSelecionado === 'PERSONALIZADO' ? null : this.periodoSelecionado;
    const dataInicio = this.periodoSelecionado === 'PERSONALIZADO' ? this.dataInicioCustom : undefined;
    const dataFim = this.periodoSelecionado === 'PERSONALIZADO' ? this.dataFimCustom : undefined;

    this.gerandoPdf = true;
    this.relatorioService.gerarPdf(periodo, dataInicio, dataFim).subscribe({
      next: (blob) => {
        this.gerandoPdf = false;
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `relatorio-${this.periodoSelecionado.toLowerCase()}.pdf`;
        link.click();
        window.URL.revokeObjectURL(url);
      },
      error: () => {
        this.gerandoPdf = false;
        this.modal.abrirModal('Erro', 'Não foi possível gerar o PDF do relatório.', 'erro', 'Fechar');
      }
    });
  }

  trendClass(variacao: number | null): string {
    if (variacao === null || variacao === undefined) {
      return '';
    }
    return variacao >= 0 ? 'metric-trend--up' : 'metric-trend--down';
  }

  private carregarIndicadoresERankings(): void {
    const periodo = this.periodoSelecionado === 'PERSONALIZADO' ? null : this.periodoSelecionado;
    const dataInicio = this.periodoSelecionado === 'PERSONALIZADO' ? this.dataInicioCustom : undefined;
    const dataFim = this.periodoSelecionado === 'PERSONALIZADO' ? this.dataFimCustom : undefined;

    this.carregandoIndicadores = true;
    this.relatorioService.buscarIndicadores(periodo, dataInicio, dataFim).subscribe({
      next: (dados) => {
        this.indicadores = dados;
        this.carregandoIndicadores = false;
      },
      error: () => {
        this.indicadores = null;
        this.carregandoIndicadores = false;
      }
    });

    this.carregandoRankingProdutos = true;
    this.relatorioService.buscarRankingProdutos(periodo, dataInicio, dataFim).subscribe({
      next: (dados) => {
        this.rankingProdutos = dados;
        this.carregandoRankingProdutos = false;
      },
      error: () => {
        this.rankingProdutos = [];
        this.carregandoRankingProdutos = false;
      }
    });

    this.carregandoRankingClientes = true;
    this.relatorioService.buscarRankingClientes(periodo, 10, dataInicio, dataFim).subscribe({
      next: (dados) => {
        this.rankingClientes = dados;
        this.carregandoRankingClientes = false;
      },
      error: () => {
        this.rankingClientes = [];
        this.carregandoRankingClientes = false;
      }
    });
  }

  private carregarFaturamentoMensal(): void {
    const anoAtualValue = new Date().getFullYear();
    forkJoin({
      anterior: this.relatorioService.buscarFaturamentoMensal(anoAtualValue - 1),
      atual: this.relatorioService.buscarFaturamentoMensal(anoAtualValue)
    }).subscribe({
      next: ({ anterior, atual }) => {
        this.mesesExibidos = this.montarMesesExibidos([...anterior, ...atual]);
      },
      error: () => {
        this.mesesExibidos = [];
      }
    });
  }

  private montarMesesExibidos(dados: FaturamentoMensalDTO[]): MesBase[] {
    const mesAtual = new Date().getMonth() + 1;
    const anoAtualValue = new Date().getFullYear();

    const visiveis = [...dados].sort((a, b) => (a.ano - b.ano) || (a.mes - b.mes));

    return visiveis.map((item) => ({
      ...item,
      label: NOMES_MESES[item.mes - 1],
      isMesAtual: item.mes === mesAtual && item.ano === anoAtualValue && item.tipo === 'REALIZADO',
      isProjetado: item.tipo === 'ESTIMADO'
    }));
  }
}
