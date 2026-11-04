import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Usuario } from '../models/usuario';

import { API_URL } from '../config';
@Injectable({
  providedIn: 'root'
})
export class UsuarioService {

  private apiUrl = API_URL + '/usuarios';

  constructor(private http: HttpClient) { }

  criarUsuario(usuario: Usuario): Observable<Usuario> {
    return this.http.post<Usuario>(this.apiUrl, usuario);
  }

  listarUsuarios(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(this.apiUrl);
  }
}
