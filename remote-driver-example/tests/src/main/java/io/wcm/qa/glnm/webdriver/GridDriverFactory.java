/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2020 wcm.io
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.wcm.qa.glnm.webdriver;

import static io.wcm.qa.glnm.configuration.GaleniumConfiguration.getGridHost;
import static io.wcm.qa.glnm.configuration.GaleniumConfiguration.isHeadless;
import static io.wcm.qa.glnm.reporting.GaleniumReportUtil.getLogger;
import static io.wcm.qa.glnm.util.GaleniumContext.getDriver;
import static java.text.MessageFormat.format;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.SkipException;

import io.wcm.qa.glnm.configuration.GaleniumConfiguration;
import io.wcm.qa.glnm.device.TestDevice;
import io.wcm.qa.glnm.selenium.RunMode;
import io.wcm.qa.glnm.util.GaleniumContext;

/**
 * Workaround to inject full grid URL for {@link RemoteWebDriver}.
 */
public final class GridDriverFactory {

  private GridDriverFactory() {
    // do not instantiate
  }

  private static OptionsProvider getDesiredCapabilitiesProvider(TestDevice device) {
    switch (device.getBrowserType()) {
      case CHROME:
        OptionsProvider chromeOptionProvider;
        String chromeEmulator = device.getChromeEmulator();
        boolean withEmulator = StringUtils.isNotBlank(chromeEmulator);
        ChromeEmulatorOptionsProvider emulatorProvider = new ChromeEmulatorOptionsProvider(chromeEmulator);
        if (isHeadless()) {
          HeadlessChromeCapabilityProvider headlessProvider = new HeadlessChromeCapabilityProvider(device);
          getLogger().trace("chrome headless: " + ReflectionToStringBuilder.toString(headlessProvider, ToStringStyle.MULTI_LINE_STYLE));
          if (withEmulator) {
            getLogger().trace("with emulator: " + ReflectionToStringBuilder.toString(emulatorProvider, ToStringStyle.MULTI_LINE_STYLE));
            chromeOptionProvider = new CombinedOptionsProvider(headlessProvider, emulatorProvider);
          }
          else {
            chromeOptionProvider = headlessProvider;
          }
        }
        else if (withEmulator) {
          getLogger().trace("with emulator: " + ReflectionToStringBuilder.toString(emulatorProvider, ToStringStyle.MULTI_LINE_STYLE));
          chromeOptionProvider = emulatorProvider;
        }
        else {
          chromeOptionProvider = new ChromeOptionsProvider();
        }
        getLogger().debug("chrome provider: " + ReflectionToStringBuilder.toString(chromeOptionProvider, ToStringStyle.MULTI_LINE_STYLE));
        return chromeOptionProvider;
      case FIREFOX:
        return new FirefoxOptionsProvider();
      case IE:
        return new InternetExplorerOptionsProvider();
      default:
        break;
    }
    return null;
  }

  /**
   * Create webdriver based on test device
   * @param newTestDevice info on browser and size
   * @return ready to use driver
   */
  public static WebDriver newDriver(TestDevice newTestDevice) {

    RunMode runMode = GaleniumConfiguration.getRunMode();
    getLogger()
        .info(format("Creating new {0} {1} WebDriver for thread {2}",
            runMode,
            newTestDevice.getBrowserType(),
            Thread.currentThread().getName()));

    try {
      OptionsProvider capabilities = getDesiredCapabilitiesProvider(newTestDevice);
      setDriver(getRemoteDriver(capabilities));
    }
    catch (WebDriverException ex) {
      throw new SkipException("Could not connect to browser.", ex);
    }

    int timeout = GaleniumConfiguration.getDefaultWebdriverTimeout();
    getDriver().manage().timeouts().implicitlyWait(timeout, TimeUnit.SECONDS);
    return getDriver();
  }

  private static void setDriver(WebDriver driver) {
    GaleniumContext.getContext().setDriver(driver);
  }

  private static WebDriver getRemoteDriver(OptionsProvider capabilitiesProvider) {
    String gridHost = getGridHost();
    getLogger().info("Connecting to grid at '" + gridHost + "'");
    try {
      MutableCapabilities options = capabilitiesProvider.getOptions();
      if (getLogger().isDebugEnabled()) {
        options.setCapability("browserstack.debug", true);
      }

      return new RemoteWebDriver(new URL(gridHost), options);
    }
    catch (MalformedURLException ex) {
      throw new RuntimeException(
          format("Couldn''t construct valid URL using selenium.host={0}",
              gridHost));
    }
  }
}
