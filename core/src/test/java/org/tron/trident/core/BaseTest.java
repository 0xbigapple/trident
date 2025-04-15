package org.tron.trident.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.tron.trident.core.key.KeyPair;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BaseTest {

  protected static final String CONFIG_FILE = "application-test.properties";
  protected static ApiWrapper client;
  protected static Properties properties;
  protected static String testAddress;
  protected static String tokenId;

  @BeforeAll
  static void setUp() {
    try {
      // load config
      properties = loadConfig();
      String privateKey = properties.getProperty("tron.private-key");
      KeyPair keyPair;
      try {
        keyPair = new KeyPair(privateKey);
      } catch (Exception e) {
        throw new RuntimeException("invalid private-key, "
            + "please check tron.private-key value in application-test.properties");
      }

      tokenId = properties.getProperty("tron.tokenId");

      client = ApiWrapper.ofNile(privateKey);
      testAddress = keyPair.toBase58CheckAddress();

    } catch (IOException e) {
      throw new RuntimeException("load config failed", e);
    }
  }

  @AfterAll
  static void tearDown() {
    if (client != null) {
      client.close();
    }
  }

  private static Properties loadConfig() throws IOException {
    Properties props = new Properties();
    try (InputStream input = BaseTest.class.getClassLoader()
        .getResourceAsStream(CONFIG_FILE)) {
      if (input == null) {
        throw new IOException("can't find config file : " + CONFIG_FILE);
      }
      props.load(input);
    }
    return props;
  }
}
