import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class TokenService {

  
  public jwtPayload: any = '';
  private servidor: string;

  constructor(private http: HttpClient,
    private JwtHelper: JwtHelperService,
    private servidorService: ServidorService,
    private router: Router) {
    this.servidor = this.servidorService.getServidor() + '/oauth/token';
    this.carregarToken();
  }

  /**
   * 
   * @param token 
   * 
   * @description
   * Armazena o token retornado do backend na memória do browser do usuário.
   * @returns void;
   */
  public armazenarToken(token: string): void {
    this.jwtPayload = this.JwtHelper.decodeToken(token);
    localStorage.removeItem('token');
    localStorage.setItem('token', token);
  }

  /**
   * @description
   * Carrega o token na memoria do browser do usuario.
   * 
   * @see armazenarToken
   */
  public carregarToken(): void {
    const token = localStorage.getItem('token');

    if (token != null) {
      if (token.length > 1) {
        this.armazenarToken(token);
      }
    }

  }

  /**
   * @description Verifica se token é válido ou não
   */
  public isAccessTokenInvalido(): boolean {
    const token = localStorage.getItem('token');

    return !token || this.JwtHelper.isTokenExpired(token);
  }

  /**
   * @description Verifica se token expirou
   */
  public verificarTokenExpirado() {
    const token = localStorage.getItem('token');
    return this.JwtHelper.isTokenExpired(token);
  }

  /**
   * @description 
   * Verifica o access token está definido ou não.
   */
  public verificarTokenDefinido() {
    const token = localStorage.getItem('token');
    if (token != null) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * 
   * @param permissao 
   * 
   * @description
   * Verifica se o token do usuário tem a permissão passada por parametro
   * 
   * @returns token && boolean
   */
  public temPermissao(permissao: string): any {
    return this.jwtPayload && this.jwtPayload.authorities.includes(permissao);
  }

  /**
   * @description
   * Requisita um novo access token para o backend
   * 
   * @returns void;
   */
  public requisitarNovoAccessToken(): Promise<any> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/x-www-form-urlencoded',
      'Authorization': 'Basic YWRtaW46QWRNaU4='
    });
    const body = 'grant_type=refresh_token';

    return this.http.post<any>(this.servidor, body, { headers, withCredentials: true })
      .toPromise()
      .then(response => {
        this.armazenarToken(response.access_token);
        return response;
      })
      .catch(response => {
        return response;
      });
  }

  public limparAccessToken() {
    localStorage.removeItem('token');
    this.jwtPayload = null;
  }

  public logout() {
    let headers = new HttpHeaders().set('Content-Type', 'application/json');

    const token = localStorage.getItem('token');
    if (token) {
      headers = headers.set('Authorization', 'Bearer ' + token);
    }

    this.http.delete(this.servidorService.getServidor() + '/tokens/revoke', { headers }).toPromise()
      .finally(() => {
        this.limparAccessToken();
        localStorage.clear();

        this.router.navigate(['/login']);
      });
  }
}
