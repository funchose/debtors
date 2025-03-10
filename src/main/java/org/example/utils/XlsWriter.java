package org.example.utils;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;

public class XlsWriter {
  int count = 1;
  public void writeFile(String fromPath, String toPath) throws IOException {
    FileUtils.copyFile(new File(fromPath),
        new File(toPath.substring(0, toPath.length() - 5) + count + ".xlsx"));
    count++;
  }
}
