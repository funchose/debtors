package org.example;


import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import org.example.utils.XlsReader;
import org.example.utils.XlsWriter;

public class Sender extends Thread {
  private boolean isInterrupted = false;
  private boolean hasSent = false;
  private ArrayList<Integer> statuses = new ArrayList<>();
  private final XlsWriter writer = new XlsWriter();
  public void setInterrupted(boolean interrupted) {
    isInterrupted = interrupted;
  }

  @Override
  public void run() {
    while (!isInterrupted) {
      if (LocalDateTime.now().getHour() == 15 && !hasSent) {
        var debtors = getDebtorsListFromXlsx();
        for (Debtor debtor : debtors) {
          sendSms(debtor);
        }
        if (statuses.stream().allMatch(element -> element.equals(200))) {
          writeFileToOut();
          statuses = new ArrayList<>();
          hasSent = true;
        }
      } else {
        try {
          sleep(3600000);
          hasSent = false;
        } catch (InterruptedException e) {
          System.out.println(getName() + " был прерван");
          System.out.println(isInterrupted());
          interrupt();
        }
      }
    }
  }

  private static ArrayList<Debtor> getDebtorsListFromXlsx() {
    try {
      XlsReader.getExcelConverterInstance()
          .readWorkbook("src/main/java/org/example/in/input.xlsx");
    } catch (IOException e) {
      System.out.println("Ошибка чтения файла");
    }
    XlsReader.getExcelConverterInstance()
        .readSheets(XlsReader.getExcelConverterInstance().getWorkbook());
    return new ArrayList<>(XlsReader.getExcelConverterInstance().readDebtorData());
  }

  private void writeFileToOut() {
    try {
      writer.writeFile("src/main/java/org/example/in/input.xlsx",
          "src/main/java/org/example/out/output.xlsx");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
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
      logSending(debtor, status);
      statuses.add(status);
      con.disconnect();
    } catch (IOException e) {
      System.out.println("Ошибка отправки запроса");
    }
  }

  private static void logSending(Debtor debtor, int status) throws IOException {
    try (FileWriter logWriter
             = new FileWriter("src/main/java/org/example/log/log.txt")) {
      logWriter.append(String.format("Status of sms sending to number %s: %s\n",
          debtor.getNumber(), status)).flush();
    }
  }
}
