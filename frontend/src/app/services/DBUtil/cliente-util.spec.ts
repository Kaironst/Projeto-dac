import { TestBed } from "@angular/core/testing";

import { ClienteUtil } from "./cliente-util";

describe("ClienteUtil", () => {
  let service: ClienteUtil;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ClienteUtil);
  });

  it("should be created", () => {
    expect(service).toBeTruthy();
  });
});
