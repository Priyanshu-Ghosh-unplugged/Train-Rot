package com.ghosh.trainrot.features.tatkal;

import android.content.Context;
import android.util.Log;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class IRCTCAutomationService {
    private static final String TAG = "IRCTCAutomation";
    private static final String IRCTC_URL = "https://www.irctc.co.in/nget/train-search";
    private static final int TIMEOUT_SECONDS = 30;
    
    private final Context context;
    private final CaptchaSolver captchaSolver;
    private WebDriver driver;
    private WebDriverWait wait;

    @Inject
    public IRCTCAutomationService(Context context, CaptchaSolver captchaSolver) {
        this.context = context;
        this.captchaSolver = captchaSolver;
        initializeWebDriver();
    }

    private void initializeWebDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT_SECONDS));
    }

    public void login(String username, String password) throws AutomationException {
        try {
            driver.get(IRCTC_URL);
            
            // Wait for login form and enter credentials
            WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("userId")));
            WebElement passwordField = driver.findElement(By.id("pwd"));
            
            usernameField.sendKeys(username);
            passwordField.sendKeys(password);
            
            // Solve CAPTCHA if present
            if (isCaptchaPresent()) {
                solveCaptcha();
            }
            
            // Click login button
            WebElement loginButton = driver.findElement(By.className("btn-primary"));
            loginButton.click();
            
            // Wait for successful login
            wait.until(ExpectedConditions.presenceOfElementLocated(By.className("user-name")));
            
        } catch (Exception e) {
            Log.e(TAG, "Login failed", e);
            throw new AutomationException("Login failed: " + e.getMessage());
        }
    }

    public void searchTrain(String fromStation, String toStation, String date, String quota) throws AutomationException {
        try {
            // Navigate to train search
            driver.get(IRCTC_URL);
            
            // Enter station details
            WebElement fromField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("origin")));
            WebElement toField = driver.findElement(By.id("destination"));
            
            fromField.sendKeys(fromStation);
            toField.sendKeys(toStation);
            
            // Select date
            WebElement dateField = driver.findElement(By.id("jDate"));
            dateField.clear();
            dateField.sendKeys(date);
            
            // Select quota
            WebElement quotaDropdown = driver.findElement(By.id("quota"));
            quotaDropdown.click();
            WebElement quotaOption = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//span[contains(text(), '" + quota + "')]")));
            quotaOption.click();
            
            // Click search button
            WebElement searchButton = driver.findElement(By.className("search-btn"));
            searchButton.click();
            
            // Wait for results
            wait.until(ExpectedConditions.presenceOfElementLocated(By.className("train-list")));
            
        } catch (Exception e) {
            Log.e(TAG, "Train search failed", e);
            throw new AutomationException("Train search failed: " + e.getMessage());
        }
    }

    public void bookTatkalTicket(String trainNumber, List<PassengerDetails> passengers) throws AutomationException {
        try {
            // Find and click on the train
            WebElement trainElement = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//div[contains(@class, 'train-list')]//div[contains(text(), '" + trainNumber + "')]")));
            trainElement.click();
            
            // Wait for booking form
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("passenger-form")));
            
            // Fill passenger details
            for (int i = 0; i < passengers.size(); i++) {
                PassengerDetails passenger = passengers.get(i);
                fillPassengerDetails(i + 1, passenger);
            }
            
            // Select berth preferences
            selectBerthPreferences(passengers);
            
            // Click book now button
            WebElement bookButton = driver.findElement(By.className("book-now-btn"));
            bookButton.click();
            
            // Wait for payment page
            wait.until(ExpectedConditions.presenceOfElementLocated(By.className("payment-options")));
            
        } catch (Exception e) {
            Log.e(TAG, "Ticket booking failed", e);
            throw new AutomationException("Ticket booking failed: " + e.getMessage());
        }
    }

    public void processPayment(PaymentDetails paymentDetails) throws AutomationException {
        try {
            // Select payment method
            WebElement paymentMethod = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//div[contains(text(), '" + paymentDetails.getMethod() + "')]")));
            paymentMethod.click();
            
            // Fill payment details
            switch (paymentDetails.getMethod()) {
                case "UPI":
                    fillUPIDetails(paymentDetails);
                    break;
                case "CARD":
                    fillCardDetails(paymentDetails);
                    break;
                case "NET_BANKING":
                    fillNetBankingDetails(paymentDetails);
                    break;
            }
            
            // Click pay now button
            WebElement payButton = driver.findElement(By.className("pay-now-btn"));
            payButton.click();
            
            // Wait for payment confirmation
            wait.until(ExpectedConditions.presenceOfElementLocated(By.className("payment-success")));
            
        } catch (Exception e) {
            Log.e(TAG, "Payment processing failed", e);
            throw new AutomationException("Payment processing failed: " + e.getMessage());
        }
    }

    private boolean isCaptchaPresent() {
        try {
            return driver.findElement(By.id("captchaImg")).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    private void solveCaptcha() throws AutomationException {
        try {
            WebElement captchaImage = driver.findElement(By.id("captchaImg"));
            byte[] captchaBytes = captchaImage.getScreenshotAs(org.openqa.selenium.OutputType.BYTES);
            
            String captchaSolution = captchaSolver.solveCaptcha(captchaBytes);
            
            WebElement captchaInput = driver.findElement(By.id("captcha"));
            captchaInput.sendKeys(captchaSolution);
            
        } catch (Exception e) {
            Log.e(TAG, "CAPTCHA solving failed", e);
            throw new AutomationException("CAPTCHA solving failed: " + e.getMessage());
        }
    }

    private void fillPassengerDetails(int index, PassengerDetails passenger) {
        WebElement nameField = driver.findElement(By.id("passenger-name-" + index));
        WebElement ageField = driver.findElement(By.id("passenger-age-" + index));
        WebElement genderField = driver.findElement(By.id("passenger-gender-" + index));
        
        nameField.sendKeys(passenger.getName());
        ageField.sendKeys(String.valueOf(passenger.getAge()));
        genderField.sendKeys(passenger.getGender());
    }

    private void selectBerthPreferences(List<PassengerDetails> passengers) {
        for (int i = 0; i < passengers.size(); i++) {
            PassengerDetails passenger = passengers.get(i);
            WebElement berthDropdown = driver.findElement(By.id("berth-preference-" + (i + 1)));
            berthDropdown.click();
            
            WebElement berthOption = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//span[contains(text(), '" + passenger.getBerthPreference() + "')]")));
            berthOption.click();
        }
    }

    private void fillUPIDetails(PaymentDetails paymentDetails) {
        WebElement upiIdField = driver.findElement(By.id("upi-id"));
        upiIdField.sendKeys(paymentDetails.getUpiId());
    }

    private void fillCardDetails(PaymentDetails paymentDetails) {
        WebElement cardNumberField = driver.findElement(By.id("card-number"));
        WebElement expiryField = driver.findElement(By.id("expiry"));
        WebElement cvvField = driver.findElement(By.id("cvv"));
        
        cardNumberField.sendKeys(paymentDetails.getCardNumber());
        expiryField.sendKeys(paymentDetails.getExpiry());
        cvvField.sendKeys(paymentDetails.getCvv());
    }

    private void fillNetBankingDetails(PaymentDetails paymentDetails) {
        WebElement bankDropdown = driver.findElement(By.id("bank-select"));
        bankDropdown.click();
        
        WebElement bankOption = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//span[contains(text(), '" + paymentDetails.getBankName() + "')]")));
        bankOption.click();
    }

    public void close() {
        if (driver != null) {
            driver.quit();
        }
    }

    public static class AutomationException extends Exception {
        public AutomationException(String message) {
            super(message);
        }
    }

    public static class PassengerDetails {
        private String name;
        private int age;
        private String gender;
        private String berthPreference;
        
        // Getters and setters
    }

    public static class PaymentDetails {
        private String method;
        private String upiId;
        private String cardNumber;
        private String expiry;
        private String cvv;
        private String bankName;
        
        // Getters and setters
    }
} 