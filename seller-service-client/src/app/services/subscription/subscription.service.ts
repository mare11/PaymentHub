import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class SubscriptionService {

  constructor(private http: HttpClient) { }

  retrieveSubscriptionPlans(id: string) {
    return this.http.get('/api/subscription/' + id);
  }

  createSubscription(customerSubscriptionDto: any) {
    return this.http.post('/api/subscription/', customerSubscriptionDto);
  }
}
