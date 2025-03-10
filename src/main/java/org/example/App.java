package org.example;

import java.util.Scanner;

public class App {
  public static void main(String[] args) {
    var sender = new Sender();
    sender.start();
    Scanner scanner = new Scanner(System.in);
    if (scanner.next().equals("1")) {
      sender.setInterrupted(true);
    }
  }
}