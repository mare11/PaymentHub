import { ActivatedRoute } from '@angular/router';
import { SellerPaymentMethodsService } from 'src/app/services/seller-payment-methods/seller-payment-methods.service';
import { Component, OnInit } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';

@Component({
  selector: 'app-payment-methods-registration-forms',
  templateUrl: './payment-methods-registration-forms.component.html',
  styleUrls: ['./payment-methods-registration-forms.component.css']
})
export class PaymentMethodsRegistrationFormsComponent implements OnInit {

  merchantId: string;
  paymentMethods: any[];
  confirmProcessing = false;

  constructor(private sellerPaymentMethodsService: SellerPaymentMethodsService,
    private route: ActivatedRoute,
    private sanitizer: DomSanitizer) { }

  ngOnInit() {
    this.merchantId = this.route.snapshot.paramMap.get('id');
    this.sellerPaymentMethodsService.getMerchantPaymentMethodsRegistrationUrls(this.merchantId).subscribe(
      (paymentMethods: any[]) => {
        this.paymentMethods = paymentMethods;
        this.paymentMethods.forEach(method => {
          method.safeUrl = this.sanitizer.bypassSecurityTrustResourceUrl(method.registrationUrl);
        });
      },
      (response) => {
        if (response && response.error) {
          alert(response.error.message);
        } else {
          alert('Could not retrieve payment methods registration forms! Please, try again later.');
        }
      }
    );
  }

  confirm() {
    this.confirmProcessing = true;

    this.sellerPaymentMethodsService.confirmPaymentMethodsRegistration(this.merchantId).subscribe(
      (response: any) => {
        if (response && response.successFlag) {
          window.location.href = response.returnUrl;
        } else {
          this.confirmProcessing = false;
          alert(response.message);
        }
      },
      (response) => {
        this.confirmProcessing = false;
        if (response && response.error) {
          alert(response.error.message);
        } else {
          alert('Confirmation failed! Please, try again.');
        }
      }
    );
  }
}