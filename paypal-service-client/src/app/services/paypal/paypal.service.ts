import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class PaypalService {

  constructor(private http: HttpClient) { }

  registerSeller(registrationDto: any) {
    return this.http.post('/api/register_seller', registrationDto);
  }

  getPaymentTransaction(orderId: string) {
    return this.http.get('/api/payment_transaction/'.concat(orderId));
  }
  
  getSubscriptionTransaction(subscriptionId: string) {
    return this.http.get('/api/subscription_transaction/'.concat(subscriptionId));
  }
}
