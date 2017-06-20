package com.google.cloud.runtimes.builder.config.domain;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link JdkServerLookup}
 */
public class JdkServerLookupTest {

  private final static String IMAGE_PREFIX = "gcr.io/bucket/";
  private final static String JDK_ONLY_RUNTIME = "jdk-only";

  private final static String OLD_JDK = "oldJdk";
  private final static String CURRENT_JDK = "currentJdk";
  private final static String DEFAULT_JDK = CURRENT_JDK;

  private final static String SERVER_A = "serverA";
  private final static String SERVER_B = "serverB";
  private final static String SERVER_C = "serverC";

  private Map<String, JdkServerLookup.JdkServerMapEntry> jdkMap;
  private JdkServerLookup jdkServerLookup;

  @Before
  public void setup() {
    JdkServerLookup.JdkServerMapEntry oldJdkRuntimes = new JdkServerLookup.JdkServerMapEntry();
    oldJdkRuntimes.setDefaultServer(SERVER_A);
    oldJdkRuntimes.setServerImages(ImmutableMap.of(
        SERVER_A, IMAGE_PREFIX + SERVER_A + ":" + OLD_JDK,
        SERVER_B, IMAGE_PREFIX + SERVER_B + ":" + OLD_JDK,
        SERVER_C, IMAGE_PREFIX + SERVER_C + ":" + OLD_JDK
    ));
    oldJdkRuntimes.setJdkImage(IMAGE_PREFIX + JDK_ONLY_RUNTIME + ":" + OLD_JDK);

    JdkServerLookup.JdkServerMapEntry currentJdkRuntimes = new JdkServerLookup.JdkServerMapEntry();
    currentJdkRuntimes.setDefaultServer(SERVER_B);
    currentJdkRuntimes.setServerImages(ImmutableMap.of(
        SERVER_A, IMAGE_PREFIX + SERVER_A + ":" + CURRENT_JDK,
        SERVER_B, IMAGE_PREFIX + SERVER_B + ":" + CURRENT_JDK,
        SERVER_C, IMAGE_PREFIX + SERVER_C + ":" + CURRENT_JDK
    ));
    currentJdkRuntimes.setJdkImage(IMAGE_PREFIX + JDK_ONLY_RUNTIME + ":" + CURRENT_JDK);
    jdkMap = ImmutableMap.of(
        OLD_JDK,     oldJdkRuntimes,
        CURRENT_JDK, currentJdkRuntimes
    );

    jdkServerLookup = new JdkServerLookup(jdkMap, DEFAULT_JDK);
  }


  @Test
  public void testLookupJdkImageDefault() {
    String expectedRuntime = IMAGE_PREFIX + JDK_ONLY_RUNTIME + ":" + DEFAULT_JDK;
    assertEquals(expectedRuntime, jdkServerLookup.lookupJdkImage(null));
  }

  @Test
  public void testLookupJdkImageNonDefault() {
    String expectedRuntime = IMAGE_PREFIX + JDK_ONLY_RUNTIME + ":" + OLD_JDK;
    // sanity check, to make sure we're testing what we think we're testing
    assertNotEquals(OLD_JDK, DEFAULT_JDK);
    assertEquals(expectedRuntime, jdkServerLookup.lookupJdkImage(OLD_JDK));
  }

  @Test
  public void testLookupServerImageDefaultServerAndJdk() {
    String expectedRuntime = IMAGE_PREFIX + SERVER_B + ":" + DEFAULT_JDK;
    assertEquals(expectedRuntime, jdkServerLookup.lookupServerImage(null, null));
  }

  @Test
  public void testLookupServerImageDefaultServerAndNonDefaultJdk() {
    String expectedRuntime = IMAGE_PREFIX + SERVER_A + ":" + OLD_JDK;
    assertEquals(expectedRuntime, jdkServerLookup.lookupServerImage(OLD_JDK, null));
  }

  @Test
  public void testLookupServerImageNonDefaultServerDefaultJdk() {
    String expectedRuntime = IMAGE_PREFIX + SERVER_C + ":" + DEFAULT_JDK;
    assertEquals(expectedRuntime, jdkServerLookup.lookupServerImage(null, SERVER_C));
  }

  @Test
  public void testLookupServerImageNonDefaultServerAndNonDefaultJdk() {
    String expectedRuntime = IMAGE_PREFIX + SERVER_C + ":" + OLD_JDK;
    assertEquals(expectedRuntime, jdkServerLookup.lookupServerImage(OLD_JDK, SERVER_C));
  }

  @Test
  public void testLookupJdkImageNonexistent() {
    try {
      jdkServerLookup.lookupJdkImage("invalid_jdk");
      fail();
    } catch (IllegalArgumentException e) { }
  }

  @Test
  public void testLookupServerImageInvalidJdk() {
    try {
      jdkServerLookup.lookupServerImage("invalid_jdk", null);
      fail();
    } catch (IllegalArgumentException e) { }
  }

  @Test
  public void testLookupServerImageInvalidServer() {
    try {
      jdkServerLookup.lookupServerImage(null, "invalid_server");
      fail();
    } catch (IllegalArgumentException e) { }
  }

  @Test
  public void testConstructorDefaultJdkNotPresent() {
    try {
      new JdkServerLookup(jdkMap, "invalidJdk");
      fail();
    } catch (IllegalArgumentException e) { }
  }

  @Test
  public void testConstructorDefaultServerNotPresent() {
    JdkServerLookup.JdkServerMapEntry entry1 = new JdkServerLookup.JdkServerMapEntry();
    entry1.setServerImages(ImmutableMap.of(
        SERVER_A, "some_runtime"
    ));
    entry1.setDefaultServer("default_not_in_map");
    entry1.setJdkImage("some_jdk_image");

    Map<String, JdkServerLookup.JdkServerMapEntry> map = ImmutableMap.of(
        CURRENT_JDK, entry1
    );
    try {
      new JdkServerLookup(map, CURRENT_JDK);
      fail();
    } catch (IllegalArgumentException e) { }
  }
}
