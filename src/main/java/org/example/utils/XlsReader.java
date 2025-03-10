package org.example.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.Debtor;

public class XlsReader {
  private static XlsReader converter;
  XSSFSheet sheet;
  XSSFWorkbook workbook;

  public XSSFWorkbook getWorkbook() {
    return workbook;
  }

  public XlsReader() {
  }

  public static XlsReader getExcelConverterInstance() {
    if (converter == null) {
      converter = new XlsReader();
    }
    return converter;
  }

  public void readWorkbook(String file) throws IOException {
    this.workbook = new XSSFWorkbook(new FileInputStream(file));
  }

  public void readSheets(XSSFWorkbook workbook) {
    this.sheet = workbook.getSheet("Должники");
  }

  public ArrayList<Debtor> readDebtorData() {
    var debtors = new ArrayList<Debtor>();
    Iterator rowIter = sheet.rowIterator();
    XSSFRow row = (XSSFRow) rowIter.next();
    while (rowIter.hasNext()) {
      var debtor = new Debtor();
      row = (XSSFRow) rowIter.next();
      debtor.setNumber(row.getCell(0).getStringCellValue())
          .setName(row.getCell(1).getStringCellValue())
          .setDebt(Double.parseDouble(row.getCell(2).getRawValue()));
      debtors.add(debtor);
    }
    return debtors;
  }
}