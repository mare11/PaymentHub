import { Component, OnInit } from '@angular/core';
import { PaymentService } from 'src/app/services/payment/payment.service';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-payment',
  templateUrl: './payment.component.html',
  styleUrls: ['./payment.component.css']
})
export class PaymentComponent implements OnInit {

  paymentId: number;
  paymentMethods: any[];
  selectedMethod = null;
  paymentProcessing = false;

  constructor(private paymentService: PaymentService, private route: ActivatedRoute) { }

  ngOnInit() {
    this.paymentId = parseInt(this.route.snapshot.paramMap.get('id'), 10);
    this.paymentService.getSellerPaymentMethods(this.paymentId).subscribe(
      (methods: []) => {
        this.paymentMethods = methods;
      }
    );
  }

  confirm() {
    if (this.selectedMethod) {
      const payment = { paymentId: this.paymentId, paymentMethod: this.selectedMethod };
      this.paymentProcessing = true;
      this.paymentService.payment(payment).subscribe(
        (paymentResponse: any) => {
          if (paymentResponse.status === 'ERROR') {
            alert('Error while creating your payment! Please, try again later.');
          }

          window.location.href = paymentResponse.paymentUrl;
        },
        (response) => {
          this.paymentProcessing = false;
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
