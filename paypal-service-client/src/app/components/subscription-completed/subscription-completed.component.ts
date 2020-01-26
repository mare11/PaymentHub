import { Component, OnInit } from '@angular/core';
import { PaypalService } from 'src/app/services/paypal/paypal.service';
import { Router, ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-subscription-completed',
  templateUrl: './subscription-completed.component.html',
  styleUrls: ['./subscription-completed.component.css']
})
export class SubscriptionCompletedComponent implements OnInit {

  message: string;
  successMessage = 'Your subscription is completed successfully!';
  cancelMessage = 'Your subscription is canceled!';
  returnUrl: string;
  
  constructor(private paypalService: PaypalService,
    private router: Router, 
    private activatedRouter: ActivatedRoute) { }

  ngOnInit() {
    const subscriptionId = this.activatedRouter.snapshot.queryParamMap.get('subscription_id');
    const successFlag = this.router.url.startsWith('/success_subscription') ? true : false;
    const completeDto = {id: subscriptionId, successFlag: successFlag}
    this.paypalService.completeSubscriptionTransaction(completeDto).subscribe(
      (redirectionDto: any) => {
        this.returnUrl = redirectionDto.redirectionUrl;
        console.log(this.router.url);
        if (this.router.url.startsWith('/success_subscription')) {
          this.message = this.successMessage;
        } else {
          this.message = this.cancelMessage;
        }
      },
      (response) => {
        if (response && response.error) {
          alert(response.error.message);
        } else {
          alert('Failure! Please, try again.');
        }
      }
    );
  }
}
