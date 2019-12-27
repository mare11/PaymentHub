package org.sep.scholar;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;

import static org.sep.scholar.Data.*;

@SpringBootTest
public class PayPalPaymentProcessTests extends TestBase {

    @Test
    public void payPalSuccessfulPaymentProcess() {
        this.preparePaymentProcess(payPalSuccessfulPayment);
        this.logInPayPalAccount();
        this.completePaymentSuccessfully();
        this.backToMerchant();
    }

    @Test
    public void payPalUnsuccessfulPaymentProcess() {
        this.preparePaymentProcess(payPalUnsuccessfulPayment);
        this.logInPayPalAccount();
        this.completePaymentUnsuccessfully();
        this.backToMerchant();
    }

    private void logInPayPalAccount() {
        this.driverWait.until(ExpectedConditions.titleIs(PAYPAL_SANDBOX_LOGIN_TITLE));

        this.waitFor(4);
        WebElement emailElement = this.driver.findElement(By.xpath("//*[@id=\"email\"]"));
        emailElement.sendKeys(PAYPAL_ACCOUNT_EMAIL);

        this.waitFor(4);
        WebElement nextBtn = this.driver.findElement(By.xpath("//*[@id=\"btnNext\"]"));
        nextBtn.click();

        this.driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

        WebElement passwordElement = this.driver.findElement(By.xpath("//*[@id=\"password\"]"));
        this.driverWait.until(ExpectedConditions.visibilityOf(passwordElement));

        this.waitFor(4);
        passwordElement.sendKeys(PAYPAL_ACCOUNT_PASSWORD);

        this.waitFor(4);
        WebElement loginBtn = this.driver.findElement(By.xpath("//*[@id=\"btnLogin\"]"));
        loginBtn.click();
    }

    private void completePaymentSuccessfully() {
        this.driverWait.until(ExpectedConditions.titleIs(PAYPAL_SANDBOX_PAYMENT_TITLE));

        this.waitFor(7);
        WebElement completeBtn = this.driver.findElement(By.xpath("//*[@id=\"confirmButtonTop\"]"));
        completeBtn.click();
    }

    private void completePaymentUnsuccessfully() {
        this.driverWait.until(ExpectedConditions.titleIs(PAYPAL_SANDBOX_CHOOSE_WAY_TO_PAY));

        this.waitFor(7);
        WebElement cancelLink = this.driver.findElement(By.xpath("//*[@id=\"cancelLink\"]"));
        cancelLink.click();
    }
}