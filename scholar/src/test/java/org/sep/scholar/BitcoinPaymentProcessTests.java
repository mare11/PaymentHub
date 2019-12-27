package org.sep.scholar;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.springframework.boot.test.context.SpringBootTest;

import static org.sep.scholar.Data.bitcoinSuccessfulPayment;
import static org.sep.scholar.Data.bitcoinUnsuccessfulPayment;

@SpringBootTest
public class BitcoinPaymentProcessTests extends TestBase {

    @Test
    public void bitcoinSuccessfulPaymentProcess() {
        this.preparePaymentProcess(bitcoinSuccessfulPayment);
        this.selectPaymentCurrency();
        this.completePayment(true);
        this.backToMerchant();
    }

    @Test
    public void bitcoinUnsuccessfulPaymentProcess() {
        this.preparePaymentProcess(bitcoinUnsuccessfulPayment);
        this.selectPaymentCurrency();
        this.completePayment(false);
        this.backToMerchant();
    }

    private void selectPaymentCurrency() {
        WebElement bitcoinDiv = this.driver.findElement(By.xpath("//div[contains(@class, 'currency-card')][1]"));
        this.driverWait.until(ExpectedConditions.visibilityOf(bitcoinDiv));

        this.waitFor(4);
        bitcoinDiv.click();

        WebElement payBtn = this.driver.findElement(By.xpath("//*[@id=\"invoice-checkout-button\"]"));
        this.waitFor(4);
        payBtn.click();
    }

    private void completePayment(boolean success) {
        int index = success ? 1 : 2;
        WebElement completeBtn = this.driver.findElement(By.xpath("//div[contains(@class, 'ant-col-12')][" + index + "]"));
        this.driverWait.until(ExpectedConditions.visibilityOf(completeBtn));
        this.waitFor(5);
        completeBtn.click();

        WebElement backToMerchantBtn = this.driver.findElement(By.xpath("//span[contains(@class, 'back-link')]"));
        this.driverWait.until(ExpectedConditions.visibilityOf(backToMerchantBtn));
        this.waitFor(5);
        backToMerchantBtn.click();
    }
}