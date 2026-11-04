import { Component, ViewChild, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AbstractControl, FormBuilder, FormGroup, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { Router } from '@angular/router';

// Imports de Serviços e Componentes
import { UsuarioService } from '../../core/services/usuario.service';
import { LojaService } from '../../core/services/loja.service'; // Importe o LojaService que criamos antes
import { ModalComponent } from '../../shared/components/modal/modal.component';

@Component({
  selector: 'app-cadastro',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, ModalComponent],
  templateUrl: './cadastro.component.html',
  styleUrl: './cadastro.component.css'
})
export class CadastroComponent implements OnInit {

  @ViewChild(ModalComponent) modal!: ModalComponent;

  cadastroForm!: FormGroup;
  emailJaCadastrado: boolean = false;
  
  // Variáveis para upload de imagem
  fotoSelecionada: File | null = null;
  fotoPreview: string | ArrayBuffer | null = null;

  constructor(
    private formBuilder: FormBuilder,
    private usuarioService: UsuarioService,
    private lojaService: LojaService, // Injetando o serviço para upload da foto
    private router: Router
  ) { }

  ngOnInit() {
    this.cadastroForm = this.formBuilder.group({
      nome: ['', Validators.required],
      email: ['', [Validators.email]],
      cpf: ['', [validarCpfBackend]], 
      telefone: ['', Validators.required],
      senha: ['', [Validators.required, Validators.minLength(6)]],
      confirmarSenha: ['', Validators.required],

      nomeLoja: ['', Validators.required],
      descricaoLoja: [''],
      cnpj: [''], 
      whatsapp: ['', Validators.required],
      facebook: [''],
      instagram: [''],

      cep: [''],
      endereco: [''],
      numero: [''],
      complemento: [''],
      bairro: [''],
      cidade: [''],
      estado: ['']
    }, {
      validators: this.validarSenha
    });
  }

  // Captura o arquivo de imagem selecionado pelo usuário
  onFotoSelecionada(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.fotoSelecionada = input.files[0];
      
      // Gera o preview para mostrar na tela
      const reader = new FileReader();
      reader.onload = () => this.fotoPreview = reader.result;
      reader.readAsDataURL(this.fotoSelecionada);
    }
  }

  cadastrar() {
    if (this.cadastroForm.invalid) {
      this.cadastroForm.markAllAsTouched();
      return;
    }

    this.emailJaCadastrado = false;

    const cpfFormatado = this.cadastroForm.get('cpf')?.value || '';
    const cpfLimpo = cpfFormatado.replace(/\D/g, '');

    const cnpjLimpo = this.cadastroForm.get('cnpj')?.value.replace(/\D/g, '');
    const whatsLimpo = this.cadastroForm.get('whatsapp')?.value.replace(/\D/g, '');

    const telefoneFormatado = this.cadastroForm.get('telefone')?.value || '';
    const telefoneLimpo = telefoneFormatado.replace(/\D/g, '');

    const novoUsuario: any = {
      nome: this.cadastroForm.get('nome')?.value,
      email: this.cadastroForm.get('email')?.value,
      senha: this.cadastroForm.get('senha')?.value,
      cpf: cpfLimpo,
      telefone: telefoneLimpo,
      nomeLoja: this.cadastroForm.get('nomeLoja')?.value,
      descricaoLoja: this.cadastroForm.get('descricaoLoja')?.value,
      cnpj: cnpjLimpo,
      whatsapp: whatsLimpo,
      facebook: this.cadastroForm.get('facebook')?.value,
      instagram: this.cadastroForm.get('instagram')?.value,
      endereco: {
        cep: this.cadastroForm.get('cep')?.value?.replace(/\D/g, '') || '',
        rua: this.cadastroForm.get('endereco')?.value,
        numero: this.cadastroForm.get('numero')?.value ? Number(this.cadastroForm.get('numero')?.value) : null,
        complemento: this.cadastroForm.get('complemento')?.value,
        bairro: this.cadastroForm.get('bairro')?.value,
        cidade: this.cadastroForm.get('cidade')?.value,
        estado: this.cadastroForm.get('estado')?.value
      }
    };

    this.usuarioService.criarUsuario(novoUsuario).subscribe({
      next: (res: any) => {
        // Pega o ID gerado (ajuste 'idUsuario' para o nome exato que seu backend retorna)
        const idGerado = res?.idUsuario || res?.id; 

        // Se o usuário selecionou uma foto e temos o ID gerado, fazemos o upload
        if (this.fotoSelecionada && idGerado) {
          this.lojaService.uploadFoto(idGerado, this.fotoSelecionada).subscribe({
            next: () => this.finalizarCadastroSucesso(),
            error: (err) => {
              console.error('Erro ao salvar foto da loja', err);
              // Mesmo falhando a foto, a conta foi criada, então liberamos o login
              this.finalizarCadastroSucesso(); 
            }
          });
        } else {
          // Se não selecionou foto, finaliza o cadastro direto
          this.finalizarCadastroSucesso();
        }
      },
      error: (err) => {
        if (err.status === 409) {
          this.modal.abrirModal(
            'Atenção',
            'Este E-mail e/ou CPF já possui cadastro!',
            'erro',
            'Ir para Login'
          );
          this.modal.acaoConfirmada.subscribe(() => {
            this.router.navigate(['/login']);
          });
        } else {
          this.modal.abrirModal(
            'Erro',
            'Ocorreu um erro inesperado. Tente novamente.',
            'erro',
            'Fechar'
          );
        }
      }
    });
  }

  // Método centralizado para lidar com a mensagem de sucesso
  finalizarCadastroSucesso() {
    this.modal.abrirModal(
      'Sucesso!',
      'Usuário e Loja cadastrados com sucesso.',
      'sucesso',
      'Ir para Login'
    );

    this.modal.acaoConfirmada.subscribe(() => {
      this.router.navigate(['/login']);
    });
  }

  irParaLogin() {
    this.router.navigate(['/login']);
  }

  // Validador de Senhas
  validarSenha(form: AbstractControl): ValidationErrors | null {
    const senha = form.get('senha')?.value;
    const confirmarSenha = form.get('confirmarSenha')?.value;

    if (senha && confirmarSenha && senha !== confirmarSenha) {
      form.get('confirmarSenha')?.setErrors({ senhaDiferente: true });
      return { senhaDiferente: true };
    }
    return null;
  }

  // Máscaras
  formatarTelefone(event: any) {
    let valor = event.target.value.replace(/\D/g, '');
    if (valor.length > 11) valor = valor.slice(0, 11);
    if (valor.length > 2) valor = valor.replace(/^(\d{2})(\d)/g, '($1) $2');
    if (valor.length > 7) valor = valor.replace(/(\d{5})(\d{4})$/, '$1-$2');
    event.target.value = valor;
    this.cadastroForm.get('telefone')?.setValue(valor, { emitEvent: false });
  }

  formatarCPF(event: any) {
    let valor = event.target.value.replace(/\D/g, '');
    if (valor.length > 11) valor = valor.slice(0, 11);
    valor = valor.replace(/(\d{3})(\d)/, '$1.$2');
    valor = valor.replace(/(\d{3})(\d)/, '$1.$2');
    valor = valor.replace(/(\d{3})(\d{1,2})$/, '$1-$2');
    event.target.value = valor;
    this.cadastroForm.get('cpf')?.setValue(valor, { emitEvent: false });
  }

  formatarWhatsApp(event: any) {
    let valor = event.target.value.replace(/\D/g, '');
    if (valor.length > 11) valor = valor.slice(0, 11);
    if (valor.length > 2) valor = valor.replace(/^(\d{2})(\d)/g, '($1) $2');
    if (valor.length > 7) valor = valor.replace(/(\d{5})(\d{4})$/, '$1-$2');
    event.target.value = valor;
    this.cadastroForm.get('whatsapp')?.setValue(valor, { emitEvent: false });
  }

  aplicarMascaraCep(event: any) {
    let valor = event.target.value.replace(/\D/g, '');
    if (valor.length > 5) {
      valor = valor.replace(/^(\d{5})(\d)/, '$1-$2');
    }
    event.target.value = valor;
    this.cadastroForm.get('cep')?.setValue(valor, { emitEvent: false });
  }

  buscarCep() {
    const cep = this.cadastroForm.get('cep')?.value?.replace(/\D/g, '');

    if (cep && cep.length === 8) {
      this.lojaService.buscarCep(cep).subscribe({
        next: (dados) => {
          if (!dados.erro) {
            this.cadastroForm.patchValue({
              endereco: dados.logradouro,
              bairro: dados.bairro,
              cidade: dados.localidade,
              estado: dados.uf
            });
          } else {
            console.error('CEP não encontrado.');
          }
        },
        error: (err) => console.error('Erro ao buscar o CEP.', err)
      });
    }
  }

  formatarCNPJ(event: any) {
    let valor = event.target.value.replace(/\D/g, '');
    if (valor.length > 14) valor = valor.slice(0, 14);
    valor = valor.replace(/^(\d{2})(\d)/, '$1.$2');
    valor = valor.replace(/^(\d{2})\.(\d{3})(\d)/, '$1.$2.$3');
    valor = valor.replace(/\.(\d{3})(\d)/, '.$1/$2');
    valor = valor.replace(/(\d{4})(\d)/, '$1-$2');
    event.target.value = valor;
    this.cadastroForm.get('cnpj')?.setValue(valor, { emitEvent: false });
  }

  // Getters
  get nome() { return this.cadastroForm.get('nome'); }
  get email() { return this.cadastroForm.get('email'); }
  get telefone() { return this.cadastroForm.get('telefone'); }
  get cpf() { return this.cadastroForm.get('cpf'); }
  get senha() { return this.cadastroForm.get('senha'); }
  get confirmarSenha() { return this.cadastroForm.get('confirmarSenha'); }
}

function validarCpfBackend(control: AbstractControl): ValidationErrors | null {
  if (!control.value) return null;

  const cpf = control.value.toString().replace(/\D/g, '');

  if (cpf.length !== 11 || /^(\d)\1{10}$/.test(cpf)) {
    return { cpfInvalido: true };
  }

  let soma = 0;
  let resto;

  for (let i = 1; i <= 9; i++)
    soma = soma + parseInt(cpf.substring(i - 1, i)) * (11 - i);

  resto = (soma * 10) % 11;
  if ((resto === 10) || (resto === 11)) resto = 0;
  if (resto !== parseInt(cpf.substring(9, 10))) return { cpfInvalido: true };

  soma = 0;
  for (let i = 1; i <= 10; i++)
    soma = soma + parseInt(cpf.substring(i - 1, i)) * (12 - i);

  resto = (soma * 10) % 11;
  if ((resto === 10) || (resto === 11)) resto = 0;
  if (resto !== parseInt(cpf.substring(10, 11))) return { cpfInvalido: true };

  return null;
}