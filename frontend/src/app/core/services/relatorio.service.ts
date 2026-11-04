import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { PeriodoTipo } from '../models/DTO/periodo-tipo';
import { IndicadoresPeriodoDTO } from '../models/DTO/indicadores-periodo-dto';
import { FaturamentoMensalDTO } from '../models/DTO/faturamento-mensal-dto';
import { RankingProdutoDTO } from '../models/DTO/ranking-produto-dto';
import { RankingClienteDTO } from '../models/DTO/ranking-cliente-dto';

import { API_URL } from '../config';
@Injectable({
  providedIn: 'root'
})
export class RelatorioService {
  private apiUrl = API_URL + '/relatorios';

  constructor(private http: HttpClient) {}

  private montarParams(periodo: PeriodoTipo | null, dataInicio?: string, dataFim?: string): HttpParams {
    let params = new HttpParams();
    if (periodo) {
      params = params.set('periodo', periodo);
    }
    if (dataInicio) {
      params = params.set('dataInicio', dataInicio);
    }
    if (dataFim) {
      params = params.set('dataFim', dataFim);
    }
    return params;
  }

  buscarIndicadores(periodo: PeriodoTipo | null, dataInicio?: string, dataFim?: string): Observable<IndicadoresPeriodoDTO> {
    const params = this.montarParams(periodo, dataInicio, dataFim);
    return this.http.get<IndicadoresPeriodoDTO>(`${this.apiUrl}/indicadores`, { params });
  }

  buscarFaturamentoMensal(ano?: number): Observable<FaturamentoMensalDTO[]> {
    let params = new HttpParams();
    if (ano) {
      params = params.set('ano', ano);
    }
    return this.http.get<FaturamentoMensalDTO[]>(`${this.apiUrl}/faturamento-mensal`, { params });
  }

  buscarRankingProdutos(periodo: PeriodoTipo | null, dataInicio?: string, dataFim?: string): Observable<RankingProdutoDTO[]> {
    const params = this.montarParams(periodo, dataInicio, dataFim);
    return this.http.get<RankingProdutoDTO[]>(`${this.apiUrl}/ranking-produtos`, { params });
  }

  buscarRankingClientes(periodo: PeriodoTipo | null, limite?: number, dataInicio?: string, dataFim?: string): Observable<RankingClienteDTO[]> {
    let params = this.montarParams(periodo, dataInicio, dataFim);
    if (limite) {
      params = params.set('limite', limite);
    }
    return this.http.get<RankingClienteDTO[]>(`${this.apiUrl}/ranking-clientes`, { params });
  }

  gerarPdf(periodo: PeriodoTipo | null, dataInicio?: string, dataFim?: string): Observable<Blob> {
    const params = this.montarParams(periodo, dataInicio, dataFim);
    return this.http.get(`${this.apiUrl}/pdf`, { params, responseType: 'blob' });
  }
}
