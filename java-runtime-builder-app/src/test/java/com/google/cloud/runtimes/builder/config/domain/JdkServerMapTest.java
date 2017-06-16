package com.google.cloud.runtimes.builder.config.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class JdkServerMapTest {

  private final static String IMAGE_PREFIX = "gcr.io/bucket/";
  private final static String JDK_ONLY_RUNTIME = "jdk-only";

  private final static String OLD_JDK = "oldJdk";
  private final static String CURRENT_JDK = "currentJdk";
  private final static String BETA_JDK = "betaJdk";
  private final static String DEFAULT_JDK = CURRENT_JDK;

  private final static String SERVER_A = "serverA";
  private final static String SERVER_B = "serverB";
  private final static String SERVER_C = "serverC";
  private final static String NO_SERVER = "no-server";
  private final static String DEFAULT_SERVER_TYPE = SERVER_A;

  private Map<String, Map<String, String>> jdkMap;
  private JdkServerMap jdkServerMap;

  @Before
  public void setup() {
    Map<String, String> oldJdkServerImagesRuntimes = ImmutableMap.of(
        SERVER_A, IMAGE_PREFIX + SERVER_A + ":" + OLD_JDK,
        SERVER_B, IMAGE_PREFIX + SERVER_B + ":" + OLD_JDK,
        SERVER_C, IMAGE_PREFIX + SERVER_C + ":" + OLD_JDK,
        NO_SERVER, IMAGE_PREFIX + JDK_ONLY_RUNTIME + ":" + OLD_JDK
    );

    Map<String, String> currentJdkServerImagesRuntimes = ImmutableMap.of(
        SERVER_A, IMAGE_PREFIX + SERVER_A + ":" + CURRENT_JDK,
        SERVER_B, IMAGE_PREFIX + SERVER_B + ":" + CURRENT_JDK,
        SERVER_C, IMAGE_PREFIX + SERVER_C + ":" + CURRENT_JDK,
        NO_SERVER, IMAGE_PREFIX + JDK_ONLY_RUNTIME + ":" + CURRENT_JDK
    );

    Map<String, String> betaJdkServerImagesRuntimes = ImmutableMap.of(
        SERVER_A, IMAGE_PREFIX + SERVER_A + ":" + BETA_JDK,
        SERVER_B, IMAGE_PREFIX + SERVER_B + ":" + BETA_JDK,
        SERVER_C, IMAGE_PREFIX + SERVER_C + ":" + BETA_JDK,
        NO_SERVER, IMAGE_PREFIX + JDK_ONLY_RUNTIME + ":" + BETA_JDK
    );

    jdkMap = ImmutableMap.of(
        OLD_JDK,     oldJdkServerImagesRuntimes,
        CURRENT_JDK, currentJdkServerImagesRuntimes,
        BETA_JDK,    betaJdkServerImagesRuntimes
    );

    jdkServerMap = new JdkServerMap(jdkMap, DEFAULT_JDK, DEFAULT_SERVER_TYPE);
  }

  @Test
  public void testLookupJdkImageDefault() {
    String expectedRuntime = IMAGE_PREFIX + JDK_ONLY_RUNTIME + ":" + DEFAULT_JDK;
    assertEquals(expectedRuntime, jdkServerMap.lookupJdkImage(null));
  }

  @Test
  public void testLookupJdkImageNonDefault() {
    String expectedRuntime = IMAGE_PREFIX + JDK_ONLY_RUNTIME + ":" + OLD_JDK;
    assertEquals(expectedRuntime, jdkServerMap.lookupJdkImage(OLD_JDK));
  }

  @Test
  public void testLookupServerImageDefaultServerAndJdk() {
    String expectedRuntime = IMAGE_PREFIX + DEFAULT_SERVER_TYPE + ":" + DEFAULT_JDK;
    assertEquals(expectedRuntime, jdkServerMap.lookupServerImage(null, null));
  }

  @Test
  public void testLookupServerImageDefaultServerAndNonDefaultJdk() {
    String expectedRuntime = IMAGE_PREFIX + DEFAULT_SERVER_TYPE + ":" + BETA_JDK;
    assertEquals(expectedRuntime, jdkServerMap.lookupServerImage(BETA_JDK, null));
  }

  @Test
  public void testLookupServerImageNonDefaultServerDefaultJdk() {
    String expectedRuntime = IMAGE_PREFIX + SERVER_C + ":" + DEFAULT_JDK;
    assertEquals(expectedRuntime, jdkServerMap.lookupServerImage(null, SERVER_C));
  }

  @Test
  public void testLookupServerImageNonDefaultServerAndNonDefaultJdk() {
    String expectedRuntime = IMAGE_PREFIX + SERVER_C + ":" + OLD_JDK;
    assertEquals(expectedRuntime, jdkServerMap.lookupServerImage(OLD_JDK, SERVER_C));
  }

  @Test
  public void testLookupJdkImageNonexistent() {
    try {
      jdkServerMap.lookupJdkImage("invalid_jdk");
      fail();
    } catch (IllegalArgumentException e) { }
  }

  @Test
  public void testLookupServerImageInvalidJdk() {
    try {
      jdkServerMap.lookupServerImage("invalid_jdk", null);
      fail();
    } catch (IllegalArgumentException e) { }
  }

  @Test
  public void testLookupServerImageInvalidServer() {
    try {
      jdkServerMap.lookupServerImage(null, "invalid_server");
      fail();
    } catch (IllegalArgumentException e) { }
  }

  @Test
  public void testConstructorDefaultJdkNotPresent() {
    try {
      new JdkServerMap(jdkMap, "invalidJdk", DEFAULT_SERVER_TYPE);
      fail();
    } catch (IllegalArgumentException e) { }
  }

  @Test
  public void testConstructorDefaultServerNotPresent() {
    String defaultServer = "different_defaultServer";
    Map<String, Map<String, String>> map = ImmutableMap.of(
      DEFAULT_JDK, ImmutableMap.of(
        SERVER_A, "some_runtime",
        defaultServer, "some_runtime"
      ),
      "other_jdk", ImmutableMap.of(
         SERVER_A, "some_runtime"
      )
    );

    try {
      new JdkServerMap(map, DEFAULT_JDK, defaultServer);
      fail();
    } catch (IllegalArgumentException e) { }
  }
}
