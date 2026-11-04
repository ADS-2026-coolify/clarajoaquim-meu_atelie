import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { API_URL } from '../config';
@Injectable({
  providedIn: 'root'
})
export class PedidoService {
private apiUrl = API_URL + '/api/encomendas';

  constructor(private http: HttpClient) {}

  salvarEncomenda(dados: any): Observable<any> {
    return this.http.post(`${this.apiUrl}`, dados);
  }

  atualizar(id: number, dados: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}`, dados);
  }

  listarTodas(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl);
  }

  buscarPorId(id: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${id}`);
  }

  listarStatus(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/status`);
  }

  obterDashboard(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/dashboard`);
  }

  gerarRecibo(id: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${id}/recibo`, { responseType: 'blob' });
  }

  deletarPedido(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }
}
