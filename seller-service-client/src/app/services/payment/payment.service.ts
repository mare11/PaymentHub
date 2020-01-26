import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class PaymentService {

  constructor(private http: HttpClient) { }

  getSellerPaymentMethods(id: string) {
    return this.http.get('/api/methods/' + id);
  }

  payment(payment: any) {
    return this.http.post('/api/payment', payment);
  }
}
