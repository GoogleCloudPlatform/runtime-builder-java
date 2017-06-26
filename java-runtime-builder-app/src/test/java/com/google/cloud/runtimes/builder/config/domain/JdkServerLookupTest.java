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

  private JdkServerLookup jdkServerLookup;

  @Before
  public void before() {
    Map<String, String> jdkMap = ImmutableMap.of(
        "oldjdk", "jdk:old",
        "currentjdk", "jdk:current",
        "*", "defaultjdk"
    );

    Map<String, String> serverMap = ImmutableMap.of(
       "newjdk|server1", "server1:new",
       "newjdk|*", "newjdk:defaultserver",
       "oldjdk|server1", "server1:old",
       "*|server1", "defaultjdk:server1",
       "*|*", "bothdefaults"
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

  @Test(expected = IllegalArgumentException.class)
  public void testLookupJdkImageNonexistent() {
    jdkServerLookup.lookupJdkImage("invalid_jdk");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testLookupServerImageInvalidJdk() {
    jdkServerLookup.lookupServerImage("invalid_jdk", null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testLookupServerImageInvalidServer() {
    jdkServerLookup.lookupServerImage(null, "invalid_server");
  }


  @Test(expected = IllegalArgumentException.class)
  public void testConstructorNoJdkDefaultPresent() {
    Map<String, String> jdkMap = ImmutableMap.of(
        "jdk", "value"
    );
    Map<String, String> serverMap = ImmutableMap.of(
        "jdk#server", "value",
        "_#_", "value"
    );
    new JdkServerLookup(jdkMap, serverMap);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorNoServerDefaultPresent() {
    Map<String, String> jdkMap = ImmutableMap.of(
        "jdk", "value",
        "_", "value"
    );
    Map<String, String> serverMap = ImmutableMap.of(
        "jdk#server", "value"
    );
    new JdkServerLookup(jdkMap, serverMap);
  }
}
