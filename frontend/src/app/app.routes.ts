import { Routes } from '@angular/router';
import { LoginComponent } from './features/auth/login/login.component';
import { HomeComponent } from './features/home/home.component';
import { CadastroComponent } from './features/cadastro/cadastro.component';
import { MateriaisComponent } from './features/materiais/materiais.component';
import { ClientesComponent } from './features/clientes/clientes.component';
import { PedidosComponent } from './features/pedidos/pedidos.component';
import { RelatoriosComponent } from './features/relatorios/relatorios.component';

import { CadastroClienteComponent } from './features/cadastro-cliente/cadastro-cliente.component';
import { CadastroMateriaisComponent } from './features/cadastro-materiais/cadastro-materiais.component';
import { ProdutosComponent } from './features/produtos/produtos.component';
import { CadastroProdutosComponent } from './features/cadastro-produtos/cadastro-produtos.component';
import { PerfilComponent } from './features/perfil/perfil.component';
import { CadastroPedidosComponent } from './features/cadastro-pedidos/cadastro-pedidos.component';
import { authGuard } from './features/auth/auth.guard';

export const routes: Routes = [
    { path: 'login', component: LoginComponent},
    { path: 'cadastro', component: CadastroComponent},
    { path: 'home', component: HomeComponent, canActivate: [authGuard]},
    { path: 'materiais', component: MateriaisComponent, canActivate: [authGuard]},
    { path: 'clientes', component: ClientesComponent, canActivate: [authGuard]},
    { path: 'pedidos', component: PedidosComponent, canActivate: [authGuard]},
    { path: 'produtos', component: ProdutosComponent, canActivate: [authGuard]},
    { path: 'relatorios', component: RelatoriosComponent, canActivate: [authGuard]},
    { path: 'pedidos/cadastro', component: CadastroPedidosComponent, canActivate: [authGuard]},
    { path: 'pedidos/editar/:id', component: CadastroPedidosComponent, canActivate: [authGuard]},
    { path: 'clientes/cadastro', component: CadastroClienteComponent, canActivate: [authGuard]},
    { path: 'clientes/editar/:id', component: CadastroClienteComponent, canActivate: [authGuard] },
    { path: 'materiais/cadastro', component: CadastroMateriaisComponent, canActivate: [authGuard]},
    { path: 'produtos/cadastro', component: CadastroProdutosComponent, canActivate: [authGuard]},
    { path: 'produtos/editar/:id', component: CadastroProdutosComponent, canActivate: [authGuard] },
    { path: 'meu-perfil', component: PerfilComponent, canActivate: [authGuard]},
    { path: '', redirectTo: 'login', pathMatch: 'full'},
];
