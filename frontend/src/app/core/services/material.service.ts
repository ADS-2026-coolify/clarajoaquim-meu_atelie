import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { API_URL } from '../config';
@Injectable({
  providedIn: 'root'
})
export class MaterialService {
  private http = inject(HttpClient);
  private apiUrl = API_URL + '/materiais';

  cadastrar(material: any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/cadastrar`, material);
  }

  listarTodos(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/listar`);
  }

  atualizar(id: number, material: any): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/atualizar/${id}`, material);
  }

  deletar(id: number): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/deletar/${id}`);
  }
}