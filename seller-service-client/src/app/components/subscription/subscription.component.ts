import { SubscriptionService } from './../../services/subscription/subscription.service';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-subscription',
  templateUrl: './subscription.component.html',
  styleUrls: ['./subscription.component.css']
})
export class SubscriptionComponent implements OnInit {

  subscriptionId: string;
  plans: any[];
  selectedPlan = null;
  subscriptionProcessing = false;
  totalCycles: number;

  constructor(private subscriptionService: SubscriptionService, private route: ActivatedRoute) { }

  ngOnInit() {
    this.subscriptionId = this.route.snapshot.paramMap.get('id');
    this.subscriptionService.retrieveSubscriptionPlans(this.subscriptionId).subscribe(
      (plans: []) => {
        this.plans = plans;
      }
    );
  }

  confirm() {
    if (this.selectedPlan && this.totalCycles) {
      this.subscriptionProcessing = true;
      this.selectedPlan.totalCycles = this.totalCycles;
      const subscription = { merchantSubscriptionId: this.subscriptionId, plan: this.selectedPlan };
      this.subscriptionService.createSubscription(subscription).subscribe(
        (subscriptonResponse: any) => {
          if (subscriptonResponse.status === 'SUSPENDED') {
            alert('Error while creating your subscription! Please, try again later.');
          }
          window.location.href = subscriptonResponse.redirectionUrl;
        },
        (response) => {
          this.subscriptionProcessing = false;
          if (response && response.error) {
            alert(response.error.message);
          } else {
            alert('Failure! Please, try again.');
          }
        }
      );
    }
  }
}
