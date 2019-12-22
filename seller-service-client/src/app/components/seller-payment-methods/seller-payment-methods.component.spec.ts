import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SellerPaymentMethodsComponent } from './seller-payment-methods.component';

describe('SellerPaymentMethodsComponent', () => {
  let component: SellerPaymentMethodsComponent;
  let fixture: ComponentFixture<SellerPaymentMethodsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SellerPaymentMethodsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SellerPaymentMethodsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
