import { TestBed } from '@angular/core/testing';

import { AuthenticationService } from './authentication.service';
import {provideHttpClient} from "@angular/common/http";

describe('AuthenticationService', () => {
  let service: AuthenticationService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient()]
    });
    service = TestBed.inject(AuthenticationService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
