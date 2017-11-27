package com.google.cloud.runtimes.builder.config.domain;

import com.google.cloud.runtimes.builder.Application;
import com.google.common.collect.ImmutableMap;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link JdkServerLookupImpl}
 */
public class JdkServerLookupTest {

  private JdkServerLookup jdkServerLookup, jdkServerLookupFromStrings, jdkServerLookupStringsMissingDefaultJdk, jdkServerLookupStringsMissingDefaultServer, jdkServerLookupMergedDefaultSettings;

  @Before
  public void before() {
    Map<String, String> jdkMap = ImmutableMap.of(
        "oldjdk ", "  jdk:old  ",
        "currentjdk", "jdk:current",
        "* ", "defaultjdk "
    );
    String[] jdkMapStrings = {"oldjdk =  jdk:old  ",
        "currentjdk =  jdk:current",
        "* =   defaultjdk "};
    String[] jdkMapStringsNoDefault = {"oldjdk =  jdk:old  ",
        "currentjdk =  jdk:current"};

    Map<String, String> serverMap = ImmutableMap.of(
        "   newjdk| server1", " server1:new",
        "newjdk |*", " newjdk:defaultserver ",
        " oldjdk | server1 ", "server1:old",
        "*| server1", "defaultjdk:server1",
        " * | * ", "bothdefaults"
    );
    String[] serverMapStrings = {"   newjdk| server1 = server1:new",
        "newjdk |*    = newjdk:defaultserver ",
        " oldjdk | server1    =     server1:old",
        "*| server1 =    defaultjdk:server1",
        " * | *    =    bothdefaults"};
    String[] serverMapStringsNoDefault = {"   newjdk| server1 = server1:new",
        "newjdk |*    = newjdk:defaultserver ",
        " oldjdk | server1    =     server1:old",
        "*| server1 =    defaultjdk:server1"};

    jdkServerLookup = new JdkServerLookupImpl(jdkMap, serverMap);
    jdkServerLookupFromStrings = new JdkServerLookupImpl(jdkMapStrings, serverMapStrings);
    jdkServerLookupStringsMissingDefaultJdk = new JdkServerLookupImpl(jdkMapStringsNoDefault,
        serverMapStrings);
    jdkServerLookupStringsMissingDefaultServer = new JdkServerLookupImpl(jdkMapStrings,
        serverMapStringsNoDefault);
    jdkServerLookupMergedDefaultSettings = Application
        .mergeSettingsWithDefaults(serverMapStrings, jdkMapStrings);
  }

  @Test
  public void testConstructorsForStringsMaps() {
    assertEquals(jdkServerLookup.lookupJdkImage("*"),
        jdkServerLookupFromStrings.lookupJdkImage("*"));
    assertEquals(jdkServerLookup.lookupJdkImage("currentjdk"),
        jdkServerLookupFromStrings.lookupJdkImage("currentjdk"));
    assertEquals(jdkServerLookup.lookupServerImage("newjdk", "server1"),
        jdkServerLookupFromStrings.lookupServerImage("newjdk", "server1"));
    assertEquals(jdkServerLookup.lookupServerImage("oldjdk", "server1"),
        jdkServerLookupFromStrings.lookupServerImage("oldjdk", "server1"));
    assertEquals(jdkServerLookup.lookupServerImage("*", "server1"),
        jdkServerLookupFromStrings.lookupServerImage("*", "server1"));
    assertEquals(jdkServerLookup.lookupServerImage("*", "*"),
        jdkServerLookupFromStrings.lookupServerImage("*", "*"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValidateDefaultJdk() {
    new JdkServerLookup(true) {

      @Override
      public String lookupJdkImage(String jdk) {
        return jdkServerLookupStringsMissingDefaultJdk.lookupJdkImage(jdk);
      }

      @Override
      public Set<String> getAvailableJdks() {
        return jdkServerLookupStringsMissingDefaultJdk.getAvailableJdks();
      }

      @Override
      public String lookupServerImage(String jdk, String serverType) {
        return jdkServerLookupStringsMissingDefaultJdk.lookupServerImage(jdk, serverType);
      }

      @Override
      public Set<String> getAvailableJdkServerPairs() {
        return jdkServerLookupStringsMissingDefaultJdk.getAvailableJdkServerPairs();
      }
    };
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValidateDefaultServer() {
    new JdkServerLookup(true) {

      @Override
      public String lookupJdkImage(String jdk) {
        return jdkServerLookupStringsMissingDefaultServer.lookupJdkImage(jdk);
      }

      @Override
      public Set<String> getAvailableJdks() {
        return jdkServerLookupStringsMissingDefaultServer.getAvailableJdks();
      }

      @Override
      public String lookupServerImage(String jdk, String serverType) {
        return jdkServerLookupStringsMissingDefaultServer.lookupServerImage(jdk, serverType);
      }

      @Override
      public Set<String> getAvailableJdkServerPairs() {
        return jdkServerLookupStringsMissingDefaultServer.getAvailableJdkServerPairs();
      }
    };
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
    Map<String, String> jdkMap = ImmutableMap.of(
        "jdk", "value"
    );
    Map<String, String> serverMap = ImmutableMap.of(
        "jdk#server", "value",
        "_#_", "value"
    );
    new JdkServerLookupImpl(jdkMap, serverMap);
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
    new JdkServerLookupImpl(jdkMap, serverMap);
  }
}
