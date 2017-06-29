package com.google.cloud.runtimes.builder;

import static org.junit.Assert.assertEquals;

import com.google.cloud.runtimes.builder.injection.RootModule;
import java.util.Map;
import org.junit.Test;

public class TestApplication {

  @Test(expected = IllegalArgumentException.class)
  public void testConstructPipelineNoMapArgs() {
    String[] args = {"foo"};
    Application.configureApplicationModule(args);
  }
  @Test(expected = IllegalArgumentException.class)
  public void testConstructPipelineMapArgsWithNoMappings() {
    String[] args = {"--jdk-runtimes-map", "--server-runtimes-map"};
    Application.configureApplicationModule(args);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructPipelineOnlyOneMapArg() {
    String[] args = {"--jdk-runtimes-map", "foo=bar"};
    Application.configureApplicationModule(args);
  }

  @Test
  public void testConstructPipelineWithValidArgs() {
    String[] args = {
        "--server-runtimes-map",
        "foo=bar",
        "keya*|keyb*=complexValue",
        "--jdk-runtimes-map",
        "foo=bar2"
    };
    RootModule module = Application.configureApplicationModule(args);

    Map<String, String> serverMap = module.getServerMap();
    assertEquals(2, serverMap.keySet().size());
    assertEquals(serverMap.get("foo"), "bar");
    assertEquals(serverMap.get("keya*|keyb*"), "complexValue");

    Map<String, String> jdkMap = module.getJdkMap();
    assertEquals(1, jdkMap.keySet().size());
    assertEquals(jdkMap.get("foo"), "bar2");
  }

}
