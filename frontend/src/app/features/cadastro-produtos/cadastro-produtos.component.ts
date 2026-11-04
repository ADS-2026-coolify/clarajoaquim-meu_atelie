import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { FooterComponent } from '../../shared/components/footer/footer.component';
import { HeaderComponent } from '../../shared/components/header/header.component';
import { ProdutoService } from '../../core/services/produto.service';
import { MaterialService } from '../../core/services/material.service';
import { ProdutoDTO } from '../../core/models/DTO/produto-dto';
import { ModalComponent } from '../../shared/components/modal/modal.component';
import { take } from 'rxjs';
import { API_URL } from '../../core/config';
declare var bootstrap: any;

@Component({
  selector: 'app-cadastro-produtos',
  standalone: true,
  imports: [HeaderComponent, FooterComponent, ReactiveFormsModule, CommonModule, FormsModule, ModalComponent],
  templateUrl: './cadastro-produtos.component.html',
  styleUrl: './cadastro-produtos.component.css'
})
export class CadastroProdutosComponent implements OnInit {

  @ViewChild('modalCategoria') modalCategoria!: ElementRef<HTMLDialogElement>;
  @ViewChild('toastElement') toastElement!: ElementRef;
  @ViewChild(ModalComponent) modal!: ModalComponent;

  isEdicao = false;
  produtoId: number | null = null;
  produtoForm!: FormGroup;

  arquivoImagem: File | null = null;
  imagemPreview: string | ArrayBuffer | null = null;

  categorias: any[] = [];
  novaCategoriaNome: string = '';
  novaCategoriaDescricao: string = '';
  materiaisCadastrados: any[] = [];
  materiaisSelecionados: any[] = [];

  constructor(
    private fb: FormBuilder,
    private produtoService: ProdutoService,
    private materialService: MaterialService,
    private router: Router,
    private route: ActivatedRoute,
  ) { }

  ngOnInit() {
    this.produtoForm = this.fb.group({
      nome: ['', Validators.required],
      descricao: ['', Validators.required],
      preco: ['', Validators.required],
      categoriaId: ['', Validators.required],
      precoCusto: [''],
      modoPreco: ['manual'],
      percentualMargem: [''],
      valorAdicionalVenda: [''],
      status: ['disponivel', Validators.required],
      largura: [''],
      altura: [''],
      peso: [''],
      observacoes: ['']
    });

    this.produtoService.listarCategorias().subscribe({
      next: (dados) => this.categorias = dados,
      error: (err) => console.error('Erro ao buscar categorias', err)
    });

    this.materialService.listarTodos().subscribe({
      next: (dados) => {
        this.materiaisCadastrados = dados;

        const id = this.route.snapshot.paramMap.get('id');
        if (id) {
          this.isEdicao = true;
          this.produtoId = Number(id);
          this.carregarProduto(this.produtoId);
        }
      },
      error: (err) => console.error('Erro ao buscar materiais', err)
    });
  }

  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      this.arquivoImagem = file;

      // Lê o arquivo para gerar o preview
      const reader = new FileReader();
      reader.onload = e => this.imagemPreview = reader.result;
      reader.readAsDataURL(file);
    }
  }

  mostrarToast() {
    const toast = new bootstrap.Toast(this.toastElement.nativeElement);
    toast.show();
  }

  removerImagem(event?: Event) {
    if (event) event.preventDefault();
    this.arquivoImagem = null;
    this.imagemPreview = null;
  }

  adicionarMaterial(event: Event) {
    const select = event.target as HTMLSelectElement;
    const id = Number(select.value);

    const material = this.materiaisCadastrados.find(m => m.id === id);
    if (material && !this.materiaisSelecionados.some(m => m.id === id)) {
      this.materiaisSelecionados.push({
        ...material,
        quantidade: null,
        unidade: material.unidadeMedida === 'Metros' ? 'cm' : null
      });
    }
    select.value = '';
    this.atualizarCustoCalculado();
  }

  removerMaterial(id: number) {
    this.materiaisSelecionados = this.materiaisSelecionados.filter(m => m.id !== id);
    this.atualizarCustoCalculado();
  }

  calcularCustoLinha(mat: any): number {
    if (!mat.quantidade || !mat.precoUnitario) {
      return 0;
    }
    let quantidadeNaUnidadeDoMaterial = mat.quantidade;
    if (mat.unidadeMedida === 'Metros' && mat.unidade === 'cm') {
      quantidadeNaUnidadeDoMaterial = mat.quantidade / 100;
    }
    return quantidadeNaUnidadeDoMaterial * mat.precoUnitario;
  }

  get custoTotalMateriais(): number {
    return this.materiaisSelecionados.reduce((total, mat) => total + this.calcularCustoLinha(mat), 0);
  }

  atualizarCustoCalculado() {
    const total = this.custoTotalMateriais;
    this.produtoForm.get('precoCusto')?.setValue(total > 0 ? this.formatarValorParaInput(total) : '', { emitEvent: false });
    this.atualizarPrecoVenda();
  }

  selecionarModoPreco(modo: 'manual' | 'percentual' | 'valor') {
    this.produtoForm.get('modoPreco')?.setValue(modo);
    this.atualizarPrecoVenda();
  }

  atualizarPrecoVenda() {
    const modo = this.produtoForm.get('modoPreco')?.value;
    if (modo === 'manual') {
      return;
    }

    const custo = this.limparMoeda(this.produtoForm.get('precoCusto')?.value);
    let precoCalculado = 0;

    if (modo === 'percentual') {
      const percentual = Number(this.produtoForm.get('percentualMargem')?.value) || 0;
      precoCalculado = custo * (1 + percentual / 100);
    } else if (modo === 'valor') {
      const valorAdicional = this.limparMoeda(this.produtoForm.get('valorAdicionalVenda')?.value);
      precoCalculado = custo + valorAdicional;
    }

    this.produtoForm.get('preco')?.setValue(precoCalculado > 0 ? this.formatarValorParaInput(precoCalculado) : '', { emitEvent: false });
  }

  private limparMoeda(valor: any): number {
    if (!valor) return 0;
    return Number(valor.toString().replace(/\./g, '').replace(',', '.'));
  }

  private formatarValorParaInput(valor: number): string {
    const partes = valor.toFixed(2).split('.');
    const inteiro = partes[0].replace(/\B(?=(\d{3})+(?!\d))/g, '.');
    return `${inteiro},${partes[1]}`;
  }

  formatarMoeda(event: any, controlName: string) {
    let valor = event.target.value.replace(/\D/g, '');
    if (valor.length > 10) {
      valor = valor.substring(0, 10);
    }

    if (!valor) {
      this.produtoForm.get(controlName)?.setValue('');
      return;
    }

    valor = (Number(valor) / 100).toFixed(2).replace('.', ',');
    valor = valor.replace(/(\d)(?=(\d{3})+(?!\d))/g, '$1.'); // Ponto de milhar

    this.produtoForm.get(controlName)?.setValue(valor, { emitEvent: false });
    event.target.value = valor;
  }

salvar() {
    if (this.produtoForm.invalid) {
      this.produtoForm.markAllAsTouched();
      return;
    }
    
    const formValues = this.produtoForm.value;

    // Função interna para limpar a máscara de moeda antes de enviar
    const limparMoeda = (valor: any) => {
        if (!valor) return 0;
        return Number(valor.toString().replace(/\./g, '').replace(',', '.'));
    };

    const requestDTO: any = {
      nome: formValues.nome,
      descricao: formValues.descricao,
      preco: limparMoeda(formValues.preco),
      categoriaId: Number(formValues.categoriaId),
      materiais: this.materiaisSelecionados.map(m => ({
        materialId: m.id,
        quantidade: m.quantidade ?? undefined,
        unidade: m.unidade ?? undefined
      })),
      precoCusto: formValues.precoCusto ? limparMoeda(formValues.precoCusto) : undefined,
      status: formValues.status,
      largura: formValues.largura ? Number(formValues.largura) : undefined,
      altura: formValues.altura ? Number(formValues.altura) : undefined,
      observacoes: formValues.observacoes
    };

    const formData = new FormData();
    formData.append('produto', new Blob([JSON.stringify(requestDTO)], { type: 'application/json' }));

    if (this.arquivoImagem) {
      formData.append('imagem', this.arquivoImagem);
    }

    // Lógica para decidir entre Cadastrar ou Atualizar
    if (this.isEdicao && this.produtoId) {
      this.produtoService.atualizarProduto(this.produtoId, formData).subscribe({
        next: () => {
          this.modal.abrirModal('Sucesso!', 'Produto atualizado com sucesso!', 'sucesso', 'Continuar');
          this.modal.acaoConfirmada.pipe(take(1)).subscribe(() => this.router.navigate(['/produtos']));
        },
        error: (err) => { console.error(err); this.modal.abrirModal('Erro', 'Erro ao atualizar.', 'erro', 'Fechar'); }
      });
    } else {
      this.produtoService.cadastrarProduto(formData).subscribe({
        next: () => {
          this.modal.abrirModal('Sucesso!', 'Produto cadastrado com sucesso!', 'sucesso', 'Continuar');
          this.modal.acaoConfirmada.pipe(take(1)).subscribe(() => this.router.navigate(['/produtos']));
        },
        error: (err) => { console.error(err); this.modal.abrirModal('Erro', 'Erro ao cadastrar.', 'erro', 'Fechar'); }
      });
    }
  }

  abrirModalCategoria() {
    this.novaCategoriaNome = '';
    this.novaCategoriaDescricao = '';
    this.modalCategoria.nativeElement.showModal();
  }

  fecharModalCategoria() {
    this.modalCategoria.nativeElement.close();
  }

  salvarCategoria() {
    if (!this.novaCategoriaNome || this.novaCategoriaNome.trim() === '') {
      this.modal.abrirModal('Atenção', 'O nome da categoria é obrigatório.', 'erro', 'Fechar');
      return;
    }

    const dto = {
      nome: this.novaCategoriaNome,
      descricao: this.novaCategoriaDescricao
    };

    this.produtoService.cadastrarCategoria(dto).subscribe({
      next: (novaCategoria) => {
        this.categorias = [...this.categorias, novaCategoria];
        this.produtoForm.get('categoriaId')?.setValue(novaCategoria.id);
        this.fecharModalCategoria();
        this.mostrarToast();
      },
      error: (err) => console.error('Erro ao cadastrar categoria', err)
    });
  }

  // Adicione isso junto aos outros métodos da classe
get materiaisDisponiveis() {
  return this.materiaisCadastrados.filter(
    mat => !this.materiaisSelecionados.some(selecionado => selecionado.id === mat.id)
  );
}

carregarProduto(id: number) {
  this.produtoService.buscarPorId(id).subscribe({
    next: (data) => {

      // 1. Preenche os campos do formulário (Inputs simples)
      this.produtoForm.patchValue({
        nome: data.nome,
        descricao: data.descricao,
        // Formatamos o preço para o padrão (ponto por vírgula) para exibir no input
        preco: data.preco ? data.preco.toString().replace('.', ',') : '',
        precoCusto: data.precoCusto ? data.precoCusto.toString().replace('.', ',') : '',
        categoriaId: data.categoriaId ?? '',
        status: data.status,
        largura: data.largura,
        altura: data.altura,
        observacoes: data.observacoes
      });

      // 2. Preenche a pré-visualização da imagem
      if (data.imagem) {
        this.imagemPreview = API_URL + '/uploads/' + data.imagem;
      }

      // 3. Preenche a lista de materiais selecionados com as quantidades/unidades salvas
      if (data.materiaisUtilizados && Array.isArray(data.materiaisUtilizados) && this.materiaisCadastrados.length > 0) {
        this.materiaisSelecionados = data.materiaisUtilizados
          .map((mu: any) => {
            const material = this.materiaisCadastrados.find(mat => mat.id === mu.materialId);
            return material ? { ...material, quantidade: mu.quantidade ?? null, unidade: mu.unidade ?? null } : undefined;
          })
          .filter((mat: any) => mat !== undefined);
      } else {
        this.materiaisSelecionados = [];
      }
    },
    error: (err) => {
      console.error('Erro ao buscar produto:', err);
      this.modal.abrirModal('Erro', 'Não foi possível carregar os dados do produto.', 'erro', 'Fechar');
    }
  });
}


}