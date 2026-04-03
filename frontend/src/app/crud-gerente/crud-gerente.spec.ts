import { ComponentFixture, TestBed } from "@angular/core/testing";

import { CrudGerente } from "./crud-gerente";

describe("CrudGerente", () => {
  let component: CrudGerente;
  let fixture: ComponentFixture<CrudGerente>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CrudGerente],
    }).compileComponents();

    fixture = TestBed.createComponent(CrudGerente);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });
});
