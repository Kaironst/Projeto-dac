import { ComponentFixture, TestBed } from "@angular/core/testing";

import { ListarGerente } from "./listar-gerente";

describe("ListarGerente", () => {
  let component: ListarGerente;
  let fixture: ComponentFixture<ListarGerente>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ListarGerente],
    }).compileComponents();

    fixture = TestBed.createComponent(ListarGerente);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });
});
