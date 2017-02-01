package com.google.cloud.runtimes.builder.config;

import java.io.IOException;
import java.nio.file.Path;

public interface YamlParser<T> {

  T parse(Path yamlFilePath) throws IOException;

}
