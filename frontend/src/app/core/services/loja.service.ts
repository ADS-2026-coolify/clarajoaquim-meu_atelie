import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { LojaDTO } from '../models/DTO/loja-dto';

import { API_URL } from '../config';
@Injectable({
  providedIn: 'root'
})
export class LojaService {
  private apiUrl = API_URL + '/api/perfil';

  constructor(private http: HttpClient) { }

  obterPerfil(idUsuario: number): Observable<LojaDTO> {
    return this.http.get<LojaDTO>(`${this.apiUrl}/${idUsuario}`);
  }
  
  uploadFoto(idUsuario: number, arquivo: File): Observable<any> {
  const formData = new FormData();
  formData.append('arquivo', arquivo);
  return this.http.post(`${this.apiUrl}/${idUsuario}/foto`, formData);
}

  atualizarPerfil(idUsuario: number, dados: LojaDTO): Observable<LojaDTO> {
    return this.http.put<LojaDTO>(`${this.apiUrl}/${idUsuario}`, dados);
  }

  buscarCep(cep: string): Observable<any> {
    return this.http.get(`https://viacep.com.br/ws/${cep}/json/`);
  }
}
