import { ComponentFixture, TestBed } from "@angular/core/testing";

import { AlterarGerente } from "./alterar-gerente";

describe("AlterarGerente", () => {
  let component: AlterarGerente;
  let fixture: ComponentFixture<AlterarGerente>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AlterarGerente],
    }).compileComponents();

    fixture = TestBed.createComponent(AlterarGerente);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });
});
