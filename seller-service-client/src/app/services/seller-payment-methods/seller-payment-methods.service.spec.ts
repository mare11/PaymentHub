import { TestBed } from '@angular/core/testing';

import { SellerPaymentMethodsService } from './seller-payment-methods.service';

describe('SellerPaymentMethodsService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: SellerPaymentMethodsService = TestBed.get(SellerPaymentMethodsService);
    expect(service).toBeTruthy();
  });
});
