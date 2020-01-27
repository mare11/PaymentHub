import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { SellerPaymentMethodsService } from 'src/app/services/seller-payment-methods/seller-payment-methods.service';

@Component({
  selector: 'app-seller-payment-methods',
  templateUrl: './seller-payment-methods.component.html',
  styleUrls: ['./seller-payment-methods.component.css']
})
export class SellerPaymentMethodsComponent implements OnInit {

  sellerId: string;
  paymentMethods: any[];
  selectionProcessing = false;

  constructor(private sellerService: SellerPaymentMethodsService, 
    private route: ActivatedRoute,
    private router: Router) { }

  ngOnInit() {
    this.sellerId = this.route.snapshot.paramMap.get('id');
    this.sellerService.getPaymentMethods().subscribe(
      (methods: []) => {
        this.paymentMethods = methods.map((method: any) => {
          method.checked = false;
          return method;
        });
      }
    );
  }

  confirm() {
    const sellerPaymentMethods = { merchantId: this.sellerId, paymentMethods: []};
    sellerPaymentMethods.paymentMethods = this.paymentMethods.filter(method => method.checked)
                                                            .map(method => {
                                                              delete method.checked;
                                                              return method;
                                                            });
    if (sellerPaymentMethods.paymentMethods.length == 0) {
      alert('You have to choose atleast one payment method!');
      return;
    }
    this.selectionProcessing = true;
    this.sellerService.chooseMethods(sellerPaymentMethods).subscribe(
      (response: any) => {
        if (response && response.successFlag) {
          this.router.navigateByUrl('payment_methods/'.concat(this.sellerId))
        } else {
          this.selectionProcessing = false;
          alert(response.message);
        }
      },
      (response) => {
        this.selectionProcessing = false;
        if (response && response.error) {
          alert(response.error.message);
        } else {
          alert('Failure! Please, try again.');
        }
      }
    );
  }
}
