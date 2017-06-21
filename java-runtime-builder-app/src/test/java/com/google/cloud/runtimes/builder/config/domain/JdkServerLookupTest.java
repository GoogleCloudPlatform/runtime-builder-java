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

  private Map<String, String> jdkMap;
  private Map<String, String> serverMap;

  private JdkServerLookup jdkServerLookup;

  @Before
  public void setup() {
    jdkMap = ImmutableMap.of(
        "oldjdk", "jdk:old",
        "currentjdk", "jdk:current",
        "_", "defaultjdk"
    );

    serverMap = ImmutableMap.of(
       "newjdk#server1", "server1:new",
       "newjdk#_", "newjdk:defaultserver",
       "oldjdk#server1", "server1:old",
       "_#server1", "defaultjdk:server1",
       "_#_", "bothdefaults"
    );

    jdkServerLookup = new JdkServerLookup(jdkMap, serverMap);
  }

  @Test
  public void testLookupJdkImageDefault() {
    assertEquals("defaultjdk", jdkServerLookup.lookupJdkImage(null));
  }

  @Test
  public void testLookupJdkImageNonDefault() {
    assertEquals("jdk:old", jdkServerLookup.lookupJdkImage("oldjdk"));
  }

  @Test
  public void testLookupServerImageDefaultServerAndJdk() {
    assertEquals("bothdefaults", jdkServerLookup.lookupServerImage(null, null));
  }

  @Test
  public void testLookupServerImageDefaultServerAndNonDefaultJdk() {
    assertEquals("newjdk:defaultserver", jdkServerLookup.lookupServerImage("newjdk", null));
  }

  @Test
  public void testLookupServerImageNonDefaultServerDefaultJdk() {
    assertEquals("defaultjdk:server1", jdkServerLookup.lookupServerImage(null, "server1"));
  }

  @Test
  public void testLookupServerImageNonDefaultServerAndNonDefaultJdk() {
    assertEquals("server1:old", jdkServerLookup.lookupServerImage("oldjdk", "server1"));
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
  public void testConstructorNoJdkDefaultPresent() {
    Map<String, String> jdkMap = ImmutableMap.of(
        "jdk", "value"
    );
    Map<String, String> serverMap = ImmutableMap.of(
        "jdk#server", "value",
        "_#_", "value"
    );
    try {
      new JdkServerLookup(jdkMap, serverMap);
      fail();
    } catch (IllegalArgumentException e) { }
  }

  @Test
  public void testConstructorNoServerDefaultPresent() {
    Map<String, String> jdkMap = ImmutableMap.of(
        "jdk", "value",
        "_", "value"
    );
    Map<String, String> serverMap = ImmutableMap.of(
        "jdk#server", "value"
    );
    try {
      new JdkServerLookup(jdkMap, serverMap);
      fail();
    } catch (IllegalArgumentException e) { }
  }
}
