![alt](https://saucelabs.com/images/sauce-labs-logo.png)

## Java-TestNg-Appium-iOS

>This code is presented as an example only, since your tests and testing environments may require specialized scripting. This information should be taken only as an
>illustration of how one would set up tests with Sauce Labs, and any modifications will not be supported. For questions regarding Sauce Labs integration, please see 
>our documentation at https://wiki.saucelabs.com/.

### Setting up your Environment and Credentials
```export SAUCE_USERNAME=<your_username>```<br>
```export SAUCE_ACCESS_KEY=<your_access_key>```<br>
```export API_ZIP=<path to your app zip file>```<br>
Optional:
```export BUILD_TAG=<name of your build>```<br>

When using Jenkins the BUILD_TAG is set for you automatically.

### Running the tests
to run: `mvn test`
