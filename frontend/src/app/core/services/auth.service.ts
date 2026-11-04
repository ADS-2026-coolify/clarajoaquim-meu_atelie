import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { tap } from 'rxjs';

import { API_URL } from '../config';
@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private apiUrl = API_URL + '/usuarios';

  constructor(private http: HttpClient) {}

  login(email: string, senha: string) {
  return this.http.post<any>(API_URL + '/usuarios/login', { email, senha })
    .pipe(
      tap(response => {
        localStorage.setItem('token', response.token);
      })
    );
}


  getToken(): string | null {
    return localStorage.getItem('token');
  }

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('usuario');
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }
}
