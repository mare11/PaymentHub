package org.sep.scholar;

import lombok.SneakyThrows;
import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.concurrent.TimeUnit;

import static org.sep.scholar.Data.*;

class TestBase {

    WebDriver driver;
    WebDriverWait driverWait;

    @Before
    public void prepareDriver() {
        System.setProperty("webdriver.chrome.driver", "src/test/resources/chromedriver.exe");

        this.driver = new ChromeDriver();
        this.driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        this.driver.manage().deleteAllCookies();
        this.driver.manage().window().maximize();

        this.driverWait = new WebDriverWait(this.driver, 25);
    }

    void preparePaymentProcess(PaymentItem paymentItem) {
        this.beginExecution(paymentItem);
        this.choosePaymentMethod(paymentItem.getMethod().getName());
    }

    private void beginExecution(PaymentItem paymentItem) {
        this.driver.get(HTTP_PREFIX + HOSTNAME + ":" + SCIENTIFIC_CENTER_SERVER_PORT + SCIENTIFIC_CENTER_PAYMENT_PATH);

        WebElement itemElement = this.driver.findElement(By.id("item"));
        WebElement descriptionElement = this.driver.findElement(By.id("description"));
        WebElement priceElement = this.driver.findElement(By.id("price"));
        WebElement submitBtn = this.driver.findElement(By.id("submit_payment_btn"));

        this.waitFor(3);
        itemElement.sendKeys(paymentItem.getItem());
        this.waitFor(3);
        descriptionElement.sendKeys(paymentItem.getDescription());
        this.waitFor(3);
        priceElement.sendKeys(paymentItem.getPrice().toString());
        this.waitFor(3);
        submitBtn.click();
    }

    private void choosePaymentMethod(String name) {
        this.driverWait.until(ExpectedConditions.titleIs(SELLER_TITLE));

        WebElement paymentMethodInput = this.driver.findElement(By.xpath("//*[text() = '" + name + "']/preceding-sibling::input"));
        paymentMethodInput.click();

        this.waitFor(4);
        WebElement submitBtn = this.driver.findElement(By.id("submit_btn"));
        submitBtn.click();
    }

    void backToMerchant() {
        this.driverWait.until(ExpectedConditions.titleIs(PAYMENT_COMPLETED));

        this.waitFor(5);
        WebElement backBtn = this.driver.findElement(By.xpath("//*[@id=\"back\"]"));
        backBtn.click();

        this.driverWait.until(ExpectedConditions.titleIs(SCIENTIFIC_CENTER_HOMEPAGE));
    }

    @After
    public void closeDriver() {
        this.waitFor(5);
        this.driver.close();
    }

    @SneakyThrows
    void waitFor(long seconds) {
        Thread.sleep(seconds * 1000);
    }
}
