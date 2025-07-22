package com.example.societyfest.util;

import com.example.societyfest.entity.Donation;
import com.example.societyfest.entity.Expense;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

public class ExcelGenerator {

    public static InputStream donationsToExcel(List<Donation> donations) {
        String[] headers = {"Room Number", "Amount", "Payment Mode", "Date", "Remarks"};

        return createSheetWithData("Donations", headers, donations.stream().map(d -> new String[]{
                d.getRoomNumber(),
                String.valueOf(d.getAmount()),
                d.getPaymentMode().name(),
                d.getDate().toString(),
                d.getRemarks() != null ? d.getRemarks() : ""
        }).toList());
    }

    public static InputStream expensesToExcel(List<Expense> expenses) {
        String[] headers = {"Category", "Amount", "Date", "Description", "Added By"};

        return createSheetWithData("Expenses", headers, expenses.stream().map(e -> new String[]{
                e.getCategory(),
                String.valueOf(e.getAmount()),
                e.getDate().toString(),
                e.getDescription(),
                e.getAddedBy()
        }).toList());
    }

    private static InputStream createSheetWithData(String sheetName, String[] headers, List<String[]> rows) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(sheetName);
            Row headerRow = sheet.createRow(0);

            // Header styling
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            for (int i = 0; i < rows.size(); i++) {
                Row row = sheet.createRow(i + 1);
                String[] data = rows.get(i);
                for (int j = 0; j < data.length; j++) {
                    row.createCell(j).setCellValue(data[j]);
                }
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Excel", e);
        }
    }
}
