import { Component, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { FormsModule } from '@angular/forms';
import { ModalComponent } from '../../../shared/components/modal/modal.component';

@Component({
  selector: 'app-login',
  imports: [FormsModule, ModalComponent],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {

  email: string = '';
  senha: string = '';
  @ViewChild(ModalComponent) modal!: ModalComponent;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  fazerLogin() {
    this.authService.login(this.email, this.senha)
      .subscribe({
        next: () => {
          this.modal.abrirModal(
          'Sucesso!',
          'Login efetuado com sucesso!',
          'sucesso',
          'Continuar'
        );

        this.modal.acaoConfirmada.subscribe(() => {
          this.router.navigate(['/home']);
        });
        },
        error: (err) => {
          if (this.modal) {
          this.modal.abrirModal(
            'Erro',
            'E-mail ou senha incorreto',
            'erro',
            'Fechar'
          );
        } else {
          console.error('Modal não inicializado', err);
        }
        }
      });
  }

}
