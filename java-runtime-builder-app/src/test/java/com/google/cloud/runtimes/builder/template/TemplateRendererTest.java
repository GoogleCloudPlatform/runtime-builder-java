package com.google.cloud.runtimes.builder.template;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Unit tests for {@link TemplateRenderer}.
 */
public class TemplateRendererTest {

  private TemplateRenderer templateRenderer;

  @Before
  public void setup() {
    templateRenderer = new TemplateRenderer();
  }

  @Test
  public void testRender_noOp() {
    String template = "string that doesn't contain any template variables";
    String result = templateRenderer.render(template, new HashMap<>());
    assertEquals(template, result);
  }

  @Test
  public void testRender_vars() {
    String template = "this contains variables: $var1 $var2";
    Map<String, String> vars = new HashMap<>();
    vars.put("$var1", "var one");
    vars.put("$var2", "var 2");

    String expected = "this contains variables: var one var 2";
    String result = templateRenderer.render(template, vars);
    assertEquals(expected, result);
  }

  @Test
  public void testRender_missing_substitutions() {
    String template = "$var1";

    try {
      templateRenderer.render(template, new HashMap<>());
    } catch (IllegalArgumentException exception) {
      assertEquals("Template variable $var1 not found.", exception.getMessage());
      return;
    }
    fail();
  }
}
