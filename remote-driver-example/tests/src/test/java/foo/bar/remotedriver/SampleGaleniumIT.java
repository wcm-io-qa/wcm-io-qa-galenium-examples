package foo.bar.remotedriver;

import static io.wcm.qa.glnm.reporting.GaleniumReportUtil.takeScreenshot;

import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import io.wcm.qa.glnm.device.TestDevice;
import io.wcm.qa.glnm.interaction.Browser;
import io.wcm.qa.glnm.interaction.Wait;
import io.wcm.qa.glnm.providers.TestDeviceProvider;
import io.wcm.qa.glnm.verification.driver.CurrentUrlVerification;

/**
 * Sample Test for Galenium.
 */
public class SampleGaleniumIT extends AbstractExampleBase {

  private static final String URL = "https://qa.wcm.io/";

  @Factory(dataProviderClass = TestDeviceProvider.class, dataProvider = TestDeviceProvider.GALENIUM_TEST_DEVICES_ALL)
  public SampleGaleniumIT(TestDevice testDevice) {
    super(testDevice);
  }

  @Test
  public void testIt() {
    Browser.load(URL);
    Wait.forCondition(new CurrentUrlVerification("WCM.io QA", URL));
    takeScreenshot();
  }

}
