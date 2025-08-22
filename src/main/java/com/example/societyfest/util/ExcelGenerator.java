package com.example.societyfest.util;

import com.example.societyfest.dto.ExpenseResponse;
import com.example.societyfest.entity.Donation;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ExcelGenerator {

    // ============================= DONATIONS EXPORT =============================
    public static InputStream donationsToExcel(List<Donation> donations) {
        String[] headers = {"Building", "Room", "Amount", "Mode", "Date"};
        String[] externalHeaders = {"Name", "Amount", "Mode", "Date"};

        // Separate internal vs external
        List<Donation> internalDonations = donations.stream()
                .filter(d -> Boolean.FALSE.equals(d.getIsExternal()))
                .toList();

        List<Donation> externalDonations = donations.stream()
                .filter(d -> Boolean.TRUE.equals(d.getIsExternal()))
                .toList();

        // Group donations by building for internal
        Map<String, Map<String, Donation>> donationsMap = internalDonations.stream()
                .collect(Collectors.groupingBy(
                        Donation::getBuilding,
                        Collectors.toMap(Donation::getRoomNumber, d -> d, (d1, d2) -> d1)
                ));

        // Sort buildings numerically (D-1, D-2, ...)
        List<String> sortedBuildings = donationsMap.keySet().stream()
                .sorted(Comparator.comparingInt(b -> Integer.parseInt(b.replaceAll("\\D+", ""))))
                .toList();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Donations");

            // ================== TITLE STYLE ==================
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);

            // Add main title at row 0
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Ganesh Festival Donations");
            titleCell.setCellStyle(titleStyle);

            // Merge title across first 5 columns
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, headers.length - 1));

            // ================== HEADER STYLE ==================
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // Numeric style
            CellStyle numberStyle = workbook.createCellStyle();
            numberStyle.setDataFormat(workbook.createDataFormat().getFormat("0"));

            int colOffset = 0;
            int rowOffset = 2; // shift down by 2 rows
            int buildingsPerRow = (int) Math.ceil(sortedBuildings.size() / 2.0);

            // === INTERNAL DONATIONS ===
            for (int bIndex = 0; bIndex < sortedBuildings.size(); bIndex++) {
                String building = sortedBuildings.get(bIndex);

                int tableRowGroup = bIndex / buildingsPerRow;
                rowOffset = 2 + tableRowGroup * (getMaxRoomsInRowGroup(sortedBuildings, bIndex, buildingsPerRow) + 2);
                colOffset = (bIndex % buildingsPerRow) * (headers.length + 1);

                // Header row
                Row headerRow = sheet.getRow(rowOffset);
                if (headerRow == null) headerRow = sheet.createRow(rowOffset);
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(colOffset + i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }

                // Expected rooms
                List<String> expectedRooms = getExpectedRoomsForBuilding(building);

                int rowNum = rowOffset + 1;
                for (String room : expectedRooms) {
                    Row row = sheet.getRow(rowNum);
                    if (row == null) row = sheet.createRow(rowNum);

                    Donation donation = donationsMap.getOrDefault(building, Collections.emptyMap()).get(room);

                    int col = colOffset;
                    row.createCell(col++).setCellValue(building);
                    row.createCell(col++).setCellValue(room);

                    Cell amountCell = row.createCell(col++);
                    if (donation != null) {
                        amountCell.setCellValue(donation.getAmount());
                        amountCell.setCellStyle(numberStyle);
                        row.createCell(col++).setCellValue(donation.getPaymentMode().name());
                        row.createCell(col).setCellValue(donation.getDate().toString());
                    } else {
                        amountCell.setCellValue("");
                        row.createCell(col++).setCellValue("");
                        row.createCell(col).setCellValue("");
                    }

                    rowNum++;
                }
            }

            // === EXTERNAL DONATIONS BELOW ===
            if (!externalDonations.isEmpty()) {
                int startRow = sheet.getLastRowNum() + 3; // leave gap

                // Title
                Row externalTitleRow = sheet.createRow(startRow);
                Cell externalTitleCell = externalTitleRow.createCell(0);
                externalTitleCell.setCellValue("External Donations");
                externalTitleCell.setCellStyle(titleStyle);

                sheet.addMergedRegion(new CellRangeAddress(startRow, startRow, 0, externalHeaders.length - 1));

                // Header
                Row headerRow = sheet.createRow(startRow + 1);
                for (int i = 0; i < externalHeaders.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(externalHeaders[i]);
                    cell.setCellStyle(headerStyle);
                }

                int rowNum = startRow + 2;
                for (Donation d : externalDonations) {
                    Row row = sheet.createRow(rowNum++);
                    int col = 0;
                    row.createCell(col++).setCellValue(d.getName());
                    row.createCell(col++).setCellValue(d.getAmount());
                    row.createCell(col++).setCellValue(d.getPaymentMode().name());
                    row.createCell(col).setCellValue(d.getDate().toString());
                }
            }

            // === Compact column widths ===
            int[] internalcolWidths = {8, 8, 8, 8, 10}; // adjust per column
            for (int i = 0; i < headers.length; i++) {
                sheet.setColumnWidth(i, 256 * internalcolWidths[i]);
            }
            int[] externalcolWidths = {12, 8, 8, 10};
            for (int i = 0; i < externalHeaders.length; i++) {
                sheet.setColumnWidth(i, 256 * externalcolWidths[Math.min(i, externalcolWidths.length - 1)]);
            }

            // === Print setup: fit to single page width ===
            PrintSetup printSetup = sheet.getPrintSetup();
            printSetup.setFitWidth((short) 1);
            printSetup.setFitHeight((short) 0);
            sheet.setAutobreaks(true);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Donations Excel", e);
        }
    }




    private static int getMaxRoomsInRowGroup(List<String> sortedBuildings, int startIndex, int buildingsPerRow) {
        int max = 0;
        int endIndex = Math.min(startIndex + buildingsPerRow, sortedBuildings.size());
        for (int i = startIndex; i < endIndex; i++) {
            max = Math.max(max, getExpectedRoomsForBuilding(sortedBuildings.get(i)).size());
        }
        return max;
    }

    private static List<String> getExpectedRoomsForBuilding(String building) {
        List<String> rooms = new ArrayList<>();
        // Buildings with only G+2 floors (3 levels)
        Set<String> gPlus2 = Set.of("D-1", "D-3", "D-6");
        int floors = gPlus2.contains(building) ? 3 : 4; // G + floors count
        for (int floor = 0; floor < floors; floor++) {
            for (int room = 1; room <= 4; room++) {
                rooms.add(String.format("%d%02d", floor, room)); // e.g., 001, 002
            }
        }
        return rooms;
    }

    // ============================= EXPENSES EXPORT =============================
    public static InputStream expensesToExcel(List<com.example.societyfest.entity.Expense> expenses) {
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
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            for (int i = 0; i < rows.size(); i++) {
                Row row = sheet.createRow(i + 1);
                String[] data = rows.get(i);
                for (int j = 0; j < data.length; j++) {
                    String value = data[j];
                    Cell cell = row.createCell(j);

                    if (value != null && value.matches("^\\d+(\\.\\d+)?$")) {
                        cell.setCellValue(Double.parseDouble(value));
                    } else {
                        cell.setCellValue(value != null ? value : "");
                    }
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

    // ============================= DETAILED EXPENSES EXPORT =============================
    public static InputStream detailedExpensesToExcel(List<ExpenseResponse> expenses) {
        String[] headers = {
                "Expense ID", "Category", "Total Amount", "Paid Amount", "Balance Amount",
                "Date", "Description", "Added By", "Payments"
        };

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Detailed Expenses");

            // Create styles
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle wrapStyle = workbook.createCellStyle();
            wrapStyle.setWrapText(true);

            // Header row
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (ExpenseResponse expense : expenses) {
                Row row = sheet.createRow(rowNum++);

                String paymentsCombined = "";
                if (expense.getPayments() != null && !expense.getPayments().isEmpty()) {
                    paymentsCombined = expense.getPayments().stream()
                            .map(p -> String.format("₹%.0f on %s%s%s",
                                    p.getAmount(),
                                    p.getPaymentDate(),
                                    p.getPaidBy() != null ? " by " + p.getPaidBy() : "",
                                    p.getPaymentMethod() != null ? " via " + p.getPaymentMethod() : ""))
                            .collect(Collectors.joining("\n"));
                }

                int col = 0;
                row.createCell(col++).setCellValue(expense.getId());
                row.createCell(col++).setCellValue(expense.getCategory());
                row.createCell(col++).setCellValue(expense.getAmount());
                row.createCell(col++).setCellValue(expense.getTotalPaid());
                row.createCell(col++).setCellValue(expense.getBalanceAmount());
                row.createCell(col++).setCellValue(expense.getDate() != null ? expense.getDate().toString() : "");
                row.createCell(col++).setCellValue(expense.getDescription() != null ? expense.getDescription() : "");
                row.createCell(col++).setCellValue(expense.getAddedBy());

                Cell paymentCell = row.createCell(col++);
                paymentCell.setCellValue(paymentsCombined);
                paymentCell.setCellStyle(wrapStyle);
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
