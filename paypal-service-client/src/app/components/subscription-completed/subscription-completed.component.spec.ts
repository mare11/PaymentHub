import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SubscriptionCompletedComponent } from './subscription-completed.component';

describe('SubscriptionCompletedComponent', () => {
  let component: SubscriptionCompletedComponent;
  let fixture: ComponentFixture<SubscriptionCompletedComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SubscriptionCompletedComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SubscriptionCompletedComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
