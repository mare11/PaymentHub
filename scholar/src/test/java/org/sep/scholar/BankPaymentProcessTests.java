package org.sep.scholar;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.springframework.boot.test.context.SpringBootTest;

import static org.sep.scholar.Data.*;

@SpringBootTest
public class BankPaymentProcessTests extends TestBase {

    @Test
    public void bankSuccessfulPaymentProcess() {
        this.executePaymentProcess(bankSuccessfulPayment);
    }

    @Test
    public void bankUnsuccessfulPaymentProcess() {
        this.executePaymentProcess(bankUnsuccessfulPayment);
    }

    private void executePaymentProcess(PaymentItem paymentItem) {
        this.preparePaymentProcess(paymentItem);
        this.completePayment();
        this.backToMerchant();
    }

    private void completePayment() {
        this.driverWait.until(ExpectedConditions.titleIs(BANK_CHECKOUT_TITLE));

        WebElement panElement = this.driver.findElement(By.id("pan"));
        WebElement ccvElement = this.driver.findElement(By.id("ccv"));
        WebElement dateElement = this.driver.findElement(By.id("date"));
        WebElement nameElement = this.driver.findElement(By.id("name"));
        WebElement submitBtn = this.driver.findElement(By.id("submit_btn"));

        this.waitFor(3);
        panElement.sendKeys(BANK_CHECKOUT_PAN);

        this.waitFor(3);
        ccvElement.sendKeys(BANK_CHECKOUT_CCV);

        this.waitFor(3);
        dateElement.sendKeys(BANK_CHECKOUT_DATE);

        this.waitFor(3);
        nameElement.sendKeys(BANK_CHECKOUT_NAME);

        this.waitFor(4);
        submitBtn.click();
    }
}