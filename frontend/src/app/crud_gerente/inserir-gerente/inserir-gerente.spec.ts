import { ComponentFixture, TestBed } from "@angular/core/testing";

import { InserirGerente } from "./inserir-gerente";

describe("InserirGerente", () => {
  let component: InserirGerente;
  let fixture: ComponentFixture<InserirGerente>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InserirGerente],
    }).compileComponents();

    fixture = TestBed.createComponent(InserirGerente);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });
});
