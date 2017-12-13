package com.google.cloud.runtimes.builder.config.domain;

import com.google.cloud.runtimes.builder.Application;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link JdkServerLookup}
 */
public class JdkServerLookupTest {

  private final static String[] jdkMapStrings = {
      "oldjdk =  jdk:old  ",
      "currentjdk =  jdk:current",
      "* =   defaultjdk "};
  private final static String[] serverMapStrings = {
      "   newjdk| server1 = server1:new",
      "newjdk |*    = newjdk:defaultserver ",
      " oldjdk | server1    =     server1:old",
      "*| server1 =    defaultjdk:server1",
      " * | *    =    bothdefaults"};

  private JdkServerLookup jdkServerLookup;
  private JdkServerLookup jdkServerLookupMergedDefaultSettings;

  @Before
  public void before() {
    jdkServerLookup = new JdkServerLookup(
        jdkMapStrings, serverMapStrings);
    jdkServerLookupMergedDefaultSettings = Application
        .mergeSettingsWithDefaults(jdkMapStrings, serverMapStrings);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValidateDefaultJdk() {
    String[] jdkMapStringsNoDefault = {
        "oldjdk =  jdk:old  ",
        "currentjdk =  jdk:current"};

    new JdkServerLookup(jdkMapStringsNoDefault, serverMapStrings);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValidateDefaultServer() {
    String[] serverMapStringsNoDefault = {"   newjdk| server1 = server1:new",
        "newjdk |*    = newjdk:defaultserver ",
        " oldjdk | server1    =     server1:old",
        "*| server1 =    defaultjdk:server1"};

    new JdkServerLookup(jdkMapStrings, serverMapStringsNoDefault);
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
    assertNull(jdkServerLookup.lookupJdkImage("invalid_jdk"));
    jdkServerLookupMergedDefaultSettings.lookupJdkImage("invalid_jdk");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testLookupServerImageInvalidJdk() {
    assertNull(jdkServerLookup.lookupServerImage("invalid_jdk", null));
    jdkServerLookupMergedDefaultSettings.lookupServerImage("invalid_jdk", null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testLookupServerImageInvalidServer() {
    assertNull(jdkServerLookup.lookupServerImage(null, "invalid_server"));
    jdkServerLookupMergedDefaultSettings.lookupServerImage(null, "invalid_server");
  }


  @Test(expected = IllegalArgumentException.class)
  public void testConstructorNoJdkDefaultPresent() {
    String[] jdkMap = {
        "jdk=value"
    };
    String[] serverMap = {
        "jdk#server=value",
        "_#_=value"
    };
    new JdkServerLookup(jdkMap, serverMap);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorNoServerDefaultPresent() {
    String[] jdkMap = {
        "jdk=value",
        "_=value"};
    String[] serverMap = {
        "jdk#server=value"
    };
    new JdkServerLookup(jdkMap, serverMap);
  }
}
