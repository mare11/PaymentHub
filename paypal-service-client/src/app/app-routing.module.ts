import { PaymentCompletedComponent } from './components/payment-completed/payment-completed.component';
import { RegistrationComponent } from './components/registration/registration.component';
import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { SubscriptionCompletedComponent } from './components/subscription-completed/subscription-completed.component';


const routes: Routes = [
  { path: 'registration/:merchant_id', component: RegistrationComponent },
  { path: 'success_payment', component: PaymentCompletedComponent },
  { path: 'cancel_payment', component: PaymentCompletedComponent },
  { path: 'success_subscription', component: SubscriptionCompletedComponent },
  { path: 'cancel_subscription', component: SubscriptionCompletedComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }

export const RoutingComponents = [RegistrationComponent, PaymentCompletedComponent, SubscriptionCompletedComponent];
