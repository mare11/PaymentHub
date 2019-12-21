import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class SellerPaymentMethodsService {

  constructor(private http: HttpClient) { }

  getPaymentMethods() {
    return this.http.get('/api/payment_method');
  }

  sellerPaymentMethods(sellerPaymentMethods: any) {
    return this.http.post('/api/methods_chosen', sellerPaymentMethods);
  }
}
