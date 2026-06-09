import { TestBed } from "@angular/core/testing";

import { GerenteUtil } from "./gerente-util";

describe("GerenteUtil", () => {
  let service: GerenteUtil;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(GerenteUtil);
  });

  it("should be created", () => {
    expect(service).toBeTruthy();
  });
});
