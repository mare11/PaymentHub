import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PaymentMethodsRegistrationFormsComponent } from './payment-methods-registration-forms.component';

describe('PaymentMethodsRegistrationFormsComponent', () => {
  let component: PaymentMethodsRegistrationFormsComponent;
  let fixture: ComponentFixture<PaymentMethodsRegistrationFormsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PaymentMethodsRegistrationFormsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PaymentMethodsRegistrationFormsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
