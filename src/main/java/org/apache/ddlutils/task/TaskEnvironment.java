package org.apache.ddlutils.task;

import java.io.File;
import java.util.List;

public interface TaskEnvironment {

  TaskLog getLog();

  List<File> getFiles();

  ClassLoader getClassLoader(ClassLoader loader, boolean parentFirst);
}
