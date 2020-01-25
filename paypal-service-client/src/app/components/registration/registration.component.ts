import { PaypalService } from './../../services/paypal/paypal.service';
import { Component, OnInit } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';
import { MatTableDataSource } from '@angular/material';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-registration',
  templateUrl: './registration.component.html',
  styleUrls: ['./registration.component.css']
})
export class RegistrationComponent implements OnInit {

  form: FormGroup;
  displayedColumns: string[];
  columns: any[];
  dataSource = new MatTableDataSource<Element[]>();
  plans: any[];
  subscription = false;
  requestProcessing = false;

  constructor(private paypalService: PaypalService,
    private router: ActivatedRoute) { }

  ngOnInit() {
    this.form = new FormGroup({ 
      clientId: new FormControl('', Validators.required), 
      clientSecret: new FormControl('', Validators.required),
      setupFee: new FormControl('', Validators.min(0)),
    });

    this.plans = [
      {position: 1, plan: 'DAY'},
      {position: 2, plan: 'WEEK'},
      {position: 3, plan: 'MONTH'},
      {position: 4, plan: 'YEAR'}
    ]

    this.plans.forEach(plan => {
      this.form.addControl(plan.plan, new FormControl());
      this.form.addControl(plan.plan + ' enabled', new FormControl());
    });

    this.displayedColumns = ['position','plan', 'price', 'enablePlan'];
    this.dataSource.data = this.plans;
  }

  submit() {
    this.requestProcessing = true;

    const registrationDto = {
      merchantId: this.router.snapshot.paramMap.get('merchant_id'),
      clientId: this.form.value['clientId'],
      clientSecret: this.form.value['clientSecret'],
      subscription: this.subscription,
      setupFee: this.form.value['setupFee'] && this.form.value['setupFee'] != '' ? this.form.value['setupFee'] : 0,
      plans: []
    };

    if (this.subscription) {
      this.plans.forEach(plan => {
        if (this.form.value[plan.plan + ' enabled']) {
          registrationDto.plans.push({
            plan: plan.plan,
            price: this.form.value[plan.plan]
          });
        }
      });
    }

    this.paypalService.registerSeller(registrationDto).subscribe(
      (response: any) => {
        window.location.href = response.redirectionUrl;
      },
      () => {
        alert('Error!');
        this.requestProcessing = false;
      }
    )
  }
}
