package com.google.cloud.runtimes.builder.build;

import com.google.cloud.runtimes.builder.exception.BuildToolInvokerException;
import com.google.cloud.runtimes.builder.workspace.Workspace;

public interface BuildToolInvoker {

  void invoke(Workspace workspace) throws BuildToolInvokerException;

}
