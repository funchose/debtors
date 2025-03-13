package org.example;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import org.example.utils.XlsReader;

public class Sender extends Thread {
  private boolean isInterrupted = false;
  private boolean hasSent = false;
  private ArrayList<Integer> statuses = new ArrayList<>();
  private final String inputDirPath = "src/main/java/org/example/in/";
  private final String outputDirPath = "src/main/java/org/example/out/";
  private final String logPath = "src/main/java/org/example/log/log.txt";
  private FileWriter logWriter;

  public void setInterrupted(boolean interrupted) {
    isInterrupted = interrupted;
  }

  @Override
  public void run() {
    while (!isInterrupted) {
      if (LocalDateTime.now().getHour() == 12 && !hasSent) {
        var filenames = scanFilesNames(inputDirPath);
        var debtors = getDebtorsListFromXlsx(filenames);
        try {
          logWriter = new FileWriter(logPath);
          for (Debtor debtor : debtors) {
            sendSms(debtor);
          }
          logWriter.close();
          if (statuses.stream().allMatch(element -> element.equals(200))) {
            moveFiles(filenames);
            statuses = new ArrayList<>();
            hasSent = true;
          }
        } catch (IOException e) {
          System.out.println("Ошибка перемещения файла или записи в лог");
        }
      } else {
        try {
          sleep(3600000);
          hasSent = false;
        } catch (InterruptedException e) {
          interrupt();
        }
      }
    }
  }

  public Set<String> scanFilesNames(String directory) {
    try (Stream<Path> stream = Files.list(Paths.get(directory))) {
      return stream
          .filter(file -> !Files.isDirectory(file))
          .map(Path::getFileName)
          .map(Path::toString)
          .filter(name -> name.endsWith("xlsx"))
          .collect(Collectors.toSet());
    } catch (IOException e) {
      System.out.println("Ошибка чтения названий файлов");
    }
    return new HashSet<>();
  }

  private ArrayList<Debtor> getDebtorsListFromXlsx(Set<String> fileNames) {
    var debtors = new ArrayList<Debtor>();
    try {
      for (String name : fileNames) {
        XlsReader.getExcelConverterInstance()
            .readWorkbook(inputDirPath + name);
        XlsReader.getExcelConverterInstance()
            .readSheets(XlsReader.getExcelConverterInstance().getWorkbook());
        debtors.addAll(XlsReader.getExcelConverterInstance().readDebtorData());
      }
    } catch (IOException e) {
      System.out.println("Ошибка чтения файла");
    }
    return debtors;
  }

  private void sendSms(Debtor debtor) {
    HttpURLConnection con;
    int status;
    URL url;
    try {
      url = new URL(
          String.format("http://mf.ru/sms?num=%s&text=Дорогой_%s!_Ваш_долг_&secretkey=%f",
              debtor.getNumber(), debtor.getName(), debtor.getDebt()));
      con = (HttpURLConnection) url.openConnection();
      status = con.getResponseCode();
      logWriter.append(String.format("Статус отправки СМС на номер %s: %s\n",
          debtor.getNumber(), status)).flush();
      statuses.add(status);
      con.disconnect();
    } catch (IOException e) {
      System.out.println("Ошибка отправки запроса");
    }
  }

  private void moveFiles(Set<String> filenames) throws IOException {
    for (String name : filenames) {
      FileUtils.moveFile(new File(inputDirPath + name),
          new File(outputDirPath + name));
    }
  }
}
