import { ComponentFixture, TestBed } from "@angular/core/testing";

import { RelatorioClientes } from "./relatorio-clientes";

describe("RelatorioClientes", () => {
  let component: RelatorioClientes;
  let fixture: ComponentFixture<RelatorioClientes>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RelatorioClientes],
    }).compileComponents();

    fixture = TestBed.createComponent(RelatorioClientes);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });
});
