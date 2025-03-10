package org.example;

public class Debtor {
  private String name;
  private String number;
  private double debt;
  public String getName() {
    return name;
  }

  public Debtor setName(String name) {
    this.name = name;
    return this;
  }

  public String getNumber() {
    return number;
  }

  public Debtor setNumber(String number) {
    this.number = number;
    return this;
  }

  public double getDebt() {
    return debt;
  }

  public Debtor setDebt(double debt) {
    this.debt = debt / 100;
    return this;
  }
}
