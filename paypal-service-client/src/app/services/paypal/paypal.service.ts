import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class PaypalService {

  constructor(private http: HttpClient) { }

  registerMerchant(registrationDto: any) {
    return this.http.post('/api/register_merchant', registrationDto);
  }

  completePaymentTransaction(completeDto: any) {
    return this.http.post('/api/payment_transaction', completeDto);
  }
  
  completeSubscriptionTransaction(completeDto: any) {
    return this.http.post('/api/subscription_transaction', completeDto);
  }
}
