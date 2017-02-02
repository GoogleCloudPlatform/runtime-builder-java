package com.google.cloud.runtimes.builder.template;

import java.util.Map;

public class TemplateRenderer {

  private final char VARIABLE_START = '$';

  public String render(String template, Map<String, String> variables) {
    StringBuilder document = new StringBuilder();

    int position = 0;
    while (position < template.length()) {
      if (Character.isWhitespace(template.charAt(position))) {
        document.append(template.charAt(position));
        position++;
      } else {
        StringBuilder wordBuilder = new StringBuilder();

        char current;
        while (position < template.length()
            && !Character.isWhitespace(current = template.charAt(position))) {
          wordBuilder.append(current);
          position++;
        }

        String word = wordBuilder.toString();
        if (word.charAt(0) == VARIABLE_START) {
          document.append(replace(variables, word));
        } else {
          document.append(word);
        }
      }
    }

    return document.toString();
  }

  private String replace(Map<String,String> variables, String key) {
    if (!variables.containsKey(key)) {
      throw new IllegalArgumentException(String.format("Template variable %s not found.", key));
    }
    return variables.get(key);
  }

}
