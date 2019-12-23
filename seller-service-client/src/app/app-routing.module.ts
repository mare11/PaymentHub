import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { SellerPaymentMethodsComponent } from './components/seller-payment-methods/seller-payment-methods.component';
import { PaymentComponent } from './components/payment/payment.component';
import { PaymentSuccessComponent } from './components/payment-success/payment-success.component';
import { PaymentCancelComponent } from './components/payment-cancel/payment-cancel.component';

const routes: Routes = [
  { path: 'seller/:id', component: SellerPaymentMethodsComponent },
  { path: 'payment/:id', component: PaymentComponent },
  { path: 'success', component: PaymentSuccessComponent },
  { path: 'cancel', component: PaymentCancelComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }

export const RoutingComponents = [SellerPaymentMethodsComponent, PaymentComponent, PaymentSuccessComponent, PaymentCancelComponent];
