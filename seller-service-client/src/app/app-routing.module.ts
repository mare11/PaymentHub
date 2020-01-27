import { SubscriptionComponent } from './components/subscription/subscription.component';
import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { SellerPaymentMethodsComponent } from './components/seller-payment-methods/seller-payment-methods.component';
import { PaymentComponent } from './components/payment/payment.component';
import { PaymentMethodsRegistrationFormsComponent } from './components/payment-methods-registration-forms/payment-methods-registration-forms.component';

const routes: Routes = [
  { path: 'seller/:id', component: SellerPaymentMethodsComponent },
  { path: 'payment/:id', component: PaymentComponent },
  { path: 'subscription/:id', component: SubscriptionComponent },
  { path: 'payment_methods/:id', component: PaymentMethodsRegistrationFormsComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }

export const RoutingComponents = [SellerPaymentMethodsComponent, PaymentComponent, SubscriptionComponent, PaymentMethodsRegistrationFormsComponent];
