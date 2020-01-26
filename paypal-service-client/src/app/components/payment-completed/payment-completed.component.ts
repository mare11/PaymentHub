import { PaypalService } from './../../services/paypal/paypal.service';
import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-payment-completed',
  templateUrl: './payment-completed.component.html',
  styleUrls: ['./payment-completed.component.css']
})
export class PaymentCompletedComponent implements OnInit {

  message: string;
  successMessage = 'Your payment is completed successfully!';
  cancelMessage = 'Your payment is canceled!';
  returnUrl: string;
  
  constructor(private paypalService: PaypalService,
    private router: Router, 
    private activatedRouter: ActivatedRoute) { }

  ngOnInit() {
    const orderId = this.activatedRouter.snapshot.queryParamMap.get('token');
    const successFlag = this.router.url.startsWith('/success_payment') ? true : false;
    const completeDto = {id: orderId, successFlag: successFlag}
    this.paypalService.completePaymentTransaction(completeDto).subscribe(
      (redirectionDto: any) => {
        this.returnUrl = redirectionDto.redirectionUrl;
        console.log(this.router.url);
        if (this.router.url.startsWith('/success_payment')) {
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
