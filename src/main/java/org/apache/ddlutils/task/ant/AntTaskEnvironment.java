package org.apache.ddlutils.task.ant;

import org.apache.ddlutils.task.TaskEnvironment;
import org.apache.ddlutils.task.TaskLog;
import org.apache.ddlutils.util.Log;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AntTaskEnvironment implements TaskEnvironment {

  Project project;

  /**
   * The log.
   */
  protected Log _log;

  /**
   * The input files.
   */
  private final ArrayList<FileSet> _fileSets = new ArrayList<>();

  /**
   * Adds a fileset.
   *
   * @param fileset The additional input files
   */
  public void addConfiguredFileset(FileSet fileset) {
    _fileSets.add(fileset);
  }

  @Override
  public TaskLog getLog() {
    return null;
  }

  @Override
  public List<File> getFiles() {

    List<File> files = new ArrayList<>();
    for (FileSet fileSet : _fileSets) {
      File fileSetDir = fileSet.getDir(project);
      DirectoryScanner scanner = fileSet.getDirectoryScanner(project);
      String[] includedFiles = scanner.getIncludedFiles();

      for (String includedFile : includedFiles) {
        files.add(new File(fileSetDir, includedFile));
      }
    }
    return files;
  }

  @Override
  public ClassLoader getClassLoader(ClassLoader loader, boolean parentFirst) {
    return new AntClassLoader(loader, parentFirst);
  }
}
