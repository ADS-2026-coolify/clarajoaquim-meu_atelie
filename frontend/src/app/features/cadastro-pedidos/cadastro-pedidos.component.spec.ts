import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CadastroPedidosComponent } from './cadastro-pedidos.component';

describe('CadastroPedidosComponent', () => {
  let component: CadastroPedidosComponent;
  let fixture: ComponentFixture<CadastroPedidosComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CadastroPedidosComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CadastroPedidosComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
