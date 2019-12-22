import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { SellerPaymentMethodsComponent } from './components/seller-payment-methods/seller-payment-methods.component';
import { PaymentComponent } from './components/payment/payment.component';

const routes: Routes = [
  { path: 'seller/:id', component: SellerPaymentMethodsComponent },
  { path: 'payment/:id', component: PaymentComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }

export const RoutingComponents = [SellerPaymentMethodsComponent, PaymentComponent];
