import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { SellerPaymentMethodsService } from 'src/app/services/seller-payment-methods/seller-payment-methods.service';

@Component({
  selector: 'app-seller-payment-methods',
  templateUrl: './seller-payment-methods.component.html',
  styleUrls: ['./seller-payment-methods.component.css']
})
export class SellerPaymentMethodsComponent implements OnInit {

  sellerId: number;
  paymentMethods: any[];
  selectionProcessing = false;

  constructor(private sellerService: SellerPaymentMethodsService, private route: ActivatedRoute) { }

  ngOnInit() {
    this.sellerId = parseInt(this.route.snapshot.paramMap.get('id'), 10);
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
    const sellerPaymentMethods = { sellerId: this.sellerId, paymentMethods: []};
    sellerPaymentMethods.paymentMethods = this.paymentMethods.filter(method => method.checked)
                                                            .map(method => {
                                                              delete method.checked;
                                                              return method;
                                                            });
    this.selectionProcessing = true;
    this.sellerService.sellerPaymentMethods(sellerPaymentMethods).subscribe(
      (response: any) => {
        window.location.href = response.redirectionUrl;
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
