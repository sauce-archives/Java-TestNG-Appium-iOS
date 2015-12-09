package com.yourcompany;

import com.saucelabs.common.SauceOnDemandAuthentication;
import com.saucelabs.common.SauceOnDemandSessionIdProvider;
import com.saucelabs.saucerest.SauceREST;
import com.saucelabs.testng.SauceOnDemandAuthenticationProvider;
import com.saucelabs.testng.SauceOnDemandTestListener;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;

import static org.testng.Assert.assertEquals;

import io.appium.java_client.MobileElement;
import io.appium.java_client.ios.IOSDriver;

import java.io.*;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Simple TestNG test which demonstrates being instantiated via a DataProvider in order to supply multiple browser
 * combinations.
 * @author Neil Manvar
 */

@Listeners({SauceOnDemandTestListener.class})
public class SampleSauceTest implements SauceOnDemandSessionIdProvider, SauceOnDemandAuthenticationProvider {

    protected String appFile = System.getenv("APP_ZIP");

    private String appURI = null;
    private String buildName = null;
    /**
     * Constructs a {@link com.saucelabs.common.SauceOnDemandAuthentication} instance using the supplied user
     * name/access key. To use the authentication supplied by environment variables or from an external file, use the
     * no-arg {@link com.saucelabs.common.SauceOnDemandAuthentication} constructor.
     */
    public SauceOnDemandAuthentication authentication = new SauceOnDemandAuthentication(System.getenv("SAUCE_USERNAME"),
            System.getenv("SAUCE_ACCESS_KEY"));

    /**
     * ThreadLocal variable which contains the  {@link WebDriver} instance which is used to perform browser interactions with.
     */
    private ThreadLocal<IOSDriver<WebElement>> webDriver = new ThreadLocal<IOSDriver<WebElement>>();

    /**
     * ThreadLocal variable which contains the Sauce Job Id.
     */
    private ThreadLocal<String> sessionId = new ThreadLocal<String>();

    /**
     * DataProvider that explicitly sets the browser combinations to be used.
     *
     * @param testMethod
     * @return
     */
    @DataProvider(name = "hardCodedBrowsers", parallel = true)
    public static Object[][] sauceBrowserDataProvider(Method testMethod) {
        return new Object[][]{
            new Object[]{"iOS", "iPhone 6", "8.4", "", "portrait", "1.4.11", testMethod.getName()},
            new Object[]{"iOS", "iPhone 6 Plus", "8.4", "", "portrait", "1.4.12", testMethod.getName()}
        };
    }

    /**
     * /**
     * Constructs a new {@link RemoteWebDriver} instance which is configured to use the capabilities defined by the platformName,
     * deviceName, platformVersion, and app and which is configured to run against ondemand.saucelabs.com, using
     * the username and access key populated by the {@link #authentication} instance.
     *
     * @param platformName Represents the platform to be run.
     * @param deviceName Represents the device to be tested on
     * @param platformVersion Version Represents version of the platform.
     * @param browserName browser name
     * @param deviceOrientation device orientation during test.
     * @param appiumVersion appium version to be used
     * @return IOSDriver<WebElement> driver for the project
     * @throws MalformedURLException if an error occurs parsing the url
     */
    private IOSDriver<WebElement> createDriver(String platformName, String deviceName, String platformVersion,
                                               String browserName, String deviceOrientation, String appiumVersion, String testName) throws MalformedURLException {

    	DesiredCapabilities capabilities = new DesiredCapabilities();

        capabilities.setCapability("platformName", platformName);
        capabilities.setCapability("deviceName", deviceName);
        capabilities.setCapability("platformVersion", platformVersion);
        capabilities.setCapability("app", this.appURI);
        capabilities.setCapability("browserName", browserName);
        capabilities.setCapability("deviceOrientation", deviceOrientation);
        capabilities.setCapability("appiumVersion", appiumVersion);

        capabilities.setCapability("name", testName);

        if (this.buildName != null) {
            capabilities.setCapability("build", this.buildName);
        }

        String sauceURI = getSauceURI();
        webDriver.set(new IOSDriver<WebElement>(
                new URL("http://" + authentication.getUsername() + ":" + authentication.getAccessKey() +
                        sauceURI), capabilities));
        String id = ((RemoteWebDriver) getWebDriver()).getSessionId().toString();
        sessionId.set(id);
        return webDriver.get();
    }

    /**
     * Runs a simple test verifying we were able to launch and close the browser
     *
     * @param platformName Represents the platform to be run.
     * @param deviceName Represents the device to be tested on
     * @param browserName browser to be used
     * @param deviceOrientation orientation of the device in use.
     * @throws Exception if an error occurs during the running of the test
     */
    @Test(dataProvider = "hardCodedBrowsers")
    public void launchTest(String platformName, String deviceName, String platformVersion, String browserName,
                           String deviceOrientation, String appiumVersion, String testName) throws Exception {
    	IOSDriver<WebElement> driver = createDriver(platformName, deviceName, platformVersion, browserName,
                deviceOrientation, appiumVersion, testName);
        driver.quit();
    }

    @Test(dataProvider = "hardCodedBrowsers")
    public void addSumTest(String platformName, String deviceName, String platformVersion, String browserName,
                           String deviceOrientation, String appiumVersion, String testName) throws Exception {
    	IOSDriver<WebElement> driver = createDriver(platformName, deviceName, platformVersion, browserName,
                deviceOrientation, appiumVersion, testName);

        MobileElement fieldOne = (MobileElement) driver.findElementByAccessibilityId("TextField1");
        fieldOne.sendKeys("12");

        MobileElement fieldTwo = (MobileElement) driver.findElementsByClassName("UIATextField").get(1);
        fieldTwo.sendKeys("8");

        // trigger computation by using the button
        driver.findElementByAccessibilityId("ComputeSumButton").click();

        // is sum equal?
        String sum = driver.findElementsByClassName("UIAStaticText").get(0).getText();
        assertEquals(Integer.parseInt(sum), 20);

        driver.quit();
    }

    /**
     * @return the {@link WebDriver} for the current thread
     */
    public IOSDriver<WebElement> getWebDriver() {
        System.out.println("WebDriver" + webDriver.get());
        return webDriver.get();
    }

    /**
     *
     * @return the Sauce Job id for the current thread
     */
    public String getSessionId() {
        return sessionId.get();
    }

    /*
     * @return the {@link SauceOnDemandAuthentication} instance containing the Sauce username/access key
     */
    @Override public SauceOnDemandAuthentication getAuthentication() {
        return authentication;
    }

    @BeforeClass
    public void executeTestPreRun() throws Exception{
        this.buildName = System.getenv("BUILD_TAG");
        this.uploadAppToSauceStorage();

    }

    private static String getFileMD5(String filePath) throws IOException{
        if (filePath == null || !Files.exists(Paths.get(filePath))){
            throw new FileNotFoundException(
                    "Please set your APP Zip file location in APP_ZIP environment variable and try again!\n"+
                            "$ export APP_ZIP=<full path to app zip file>");
        }
        FileInputStream fis = new FileInputStream(new File(filePath));
        String md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(fis);
        fis.close();
        return md5;
    }

    private void uploadAppToSauceStorage() throws Exception{
        String fileURI = "sauce-storage:%s";
        //upload the app file to sauce storage.
        String srcMD5 = getFileMD5(this.appFile);
        String fileName = Paths.get(this.appFile).getFileName().toString();
        SauceREST sauceREST = new SauceREST(this.authentication.getUsername(), this.authentication.getAccessKey());
        String dstMD5 = sauceREST.uploadFile(new File(this.appFile));
        if (!srcMD5.contentEquals(dstMD5)) {
            throw new Exception("File upload failed! MD5 signatures do not match!");
        } else {
            this.appURI = String.format(fileURI, fileName);
            System.out.printf("File: %s uploaded successfully!\n", this.appFile);
        }
    }

    private static String getSauceURI(){
        //If SC plugin is running in our CI environment this env var will be set.
        String sePort = System.getenv("SELENIUM_PORT");
        String sauceURI = "@%s:%s/wd/hub";
        if (sePort != null){
            sauceURI = String.format(sauceURI, "localhost", sePort);
        } else {
            //direct connection to Sauce
            sauceURI = String.format(sauceURI, "ondemand.saucelabs.com", "80");
        }
        return sauceURI;
    }

}

