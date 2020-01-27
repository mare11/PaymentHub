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
  registrationSuccess = false;

  constructor(private paypalService: PaypalService,
    private router: ActivatedRoute) { }

  ngOnInit() {
    this.form = new FormGroup({ 
      clientId: new FormControl('', Validators.required), 
      clientSecret: new FormControl('', Validators.required),
      setupFee: new FormControl('', Validators.min(0)),
    });

    this.plans = [
      {position: 1, plan: '1 MONTH', intervalUnit:'MONTH', intervalCount: 1},
      {position: 2, plan: '3 MONTHS', intervalUnit:'MONTH', intervalCount: 3},
      {position: 3, plan: '6 MONTHS', intervalUnit:'MONTH', intervalCount: 6},
      {position: 4, plan: '1 YEAR', intervalUnit:'YEAR', intervalCount: 1}
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
            intervalUnit: plan.intervalUnit,
            intervalCount: plan.intervalCount,
            price: this.form.value[plan.plan]
          });
        }
      });
    }

    this.paypalService.registerMerchant(registrationDto).subscribe(
      (response: any) => {
        if (response && response.successFlag) {
          this.registrationSuccess = true;
        } else {
          this.requestProcessing = false;
        }
      },
      (response: any) => {
        if (response && response.error) {
          alert(response.error.message);
        } else {
          alert('Unexpected error! Please, try again.')
        }
        this.requestProcessing = false;
      }
    )
  }
}
