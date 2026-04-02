import { ComponentFixture, TestBed } from "@angular/core/testing";

import { RemoverGerente } from "./remover-gerente";

describe("RemoverGerente", () => {
  let component: RemoverGerente;
  let fixture: ComponentFixture<RemoverGerente>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RemoverGerente],
    }).compileComponents();

    fixture = TestBed.createComponent(RemoverGerente);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });
});
