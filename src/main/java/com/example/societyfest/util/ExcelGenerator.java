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
        String[] headers = {"Room", "Amount", "Mode", "Date"};
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

            // ===== Title style =====
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);

            // Title
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Ganesh Festival Donations");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, headers.length - 1));

            // ===== Header style =====
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // ===== Building caption style =====
            CellStyle buildingStyle = workbook.createCellStyle();
            Font buildingFont = workbook.createFont();
            buildingFont.setBold(true);
            buildingFont.setFontHeightInPoints((short) 12);
            buildingStyle.setFont(buildingFont);
            buildingStyle.setAlignment(HorizontalAlignment.CENTER);

            // Amount numeric style (right aligned)
            CellStyle numberStyle = workbook.createCellStyle();
            numberStyle.setAlignment(HorizontalAlignment.RIGHT);
            numberStyle.setDataFormat(workbook.createDataFormat().getFormat("0"));

            // Layout settings
            int baseTop = 2;                 // start below title (leave one blank row)
            int gapBetweenRowGroups = 2;     // blank rows between the two big rows of tables
            int buildingsPerRow = (int) Math.ceil(sortedBuildings.size() / 2.0);
            int[] internalColWidths = {8, 8, 10, 12};  // Room, Amount, Mode, Date (in characters)

            // === INTERNAL DONATIONS (building-wise) ===
            for (int bIndex = 0; bIndex < sortedBuildings.size(); bIndex++) {
                String building = sortedBuildings.get(bIndex);

                // Compute a consistent height for the entire row group
                int tableRowGroup = bIndex / buildingsPerRow;                 // 0 or 1
                int groupStartIndex = tableRowGroup * buildingsPerRow;        // start of this row group
                int maxRoomsThisGroup = getMaxRoomsInRowGroup(sortedBuildings, groupStartIndex, buildingsPerRow);

                // Each block has: 1 (building caption) + 1 (header) + maxRoomsThisGroup rows
                int blockHeight = 2 + maxRoomsThisGroup;

                // Row & column offsets for this block
                int rowOffset = baseTop + tableRowGroup * (blockHeight + gapBetweenRowGroups);
                int colOffset = (bIndex % buildingsPerRow) * (headers.length + 1);

                // === Building caption row ===
                Row buildingRow = sheet.getRow(rowOffset);
                if (buildingRow == null) buildingRow = sheet.createRow(rowOffset);
                Cell buildingCell = buildingRow.createCell(colOffset);
                buildingCell.setCellValue(building);
                buildingCell.setCellStyle(buildingStyle);
                sheet.addMergedRegion(new CellRangeAddress(rowOffset, rowOffset, colOffset, colOffset + headers.length - 1));

                // === Header row ===
                Row headerRow = sheet.getRow(rowOffset + 1);
                if (headerRow == null) headerRow = sheet.createRow(rowOffset + 1);
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(colOffset + i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }

                // Column widths for this block
                for (int i = 0; i < headers.length; i++) {
                    sheet.setColumnWidth(colOffset + i, 256 * internalColWidths[i]);
                }

                // Fill rows for expected rooms
                List<String> expectedRooms = getExpectedRoomsForBuilding(building);
                int rowNum = rowOffset + 2;
                for (String room : expectedRooms) {
                    Row row = sheet.getRow(rowNum);
                    if (row == null) row = sheet.createRow(rowNum);

                    int c = colOffset;
                    row.createCell(c++).setCellValue(room);

                    Donation donation = donationsMap.getOrDefault(building, Collections.emptyMap()).get(room);
                    Cell amountCell = row.createCell(c++);
                    if (donation != null) {
                        amountCell.setCellValue(donation.getAmount());
                        amountCell.setCellStyle(numberStyle);
                        row.createCell(c++).setCellValue(donation.getPaymentMode().name());
                        row.createCell(c).setCellValue(donation.getDate().toString());
                    } else {
                        // keep cells empty but create to preserve grid
                        amountCell.setCellValue("");
                        row.createCell(c++).setCellValue("");
                        row.createCell(c).setCellValue("");
                    }
                    rowNum++;
                }
            }

            // === EXTERNAL DONATIONS TABLE (below) ===
            if (!externalDonations.isEmpty()) {
                int startRow = sheet.getLastRowNum() + 3;

                Row externalTitleRow = sheet.createRow(startRow);
                Cell externalTitleCell = externalTitleRow.createCell(0);
                externalTitleCell.setCellValue("External Donations");
                externalTitleCell.setCellStyle(titleStyle);
                sheet.addMergedRegion(new CellRangeAddress(startRow, startRow, 0, externalHeaders.length - 1));

                Row headerRow = sheet.createRow(startRow + 1);
                for (int i = 0; i < externalHeaders.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(externalHeaders[i]);
                    cell.setCellStyle(headerStyle);
                }

                int rowNum = startRow + 2;
                for (Donation d : externalDonations) {
                    Row row = sheet.createRow(rowNum++);
                    int c = 0;
                    row.createCell(c++).setCellValue(d.getName());
                    Cell amountCell = row.createCell(c++);
                    amountCell.setCellValue(d.getAmount());
                    amountCell.setCellStyle(numberStyle);
                    row.createCell(c++).setCellValue(d.getPaymentMode().name());
                    row.createCell(c).setCellValue(d.getDate().toString());
                }

                // Wider Name column for external table
                int[] externalColWidths = {25, 8, 10, 12};
                for (int i = 0; i < externalHeaders.length; i++) {
                    sheet.setColumnWidth(i, 256 * externalColWidths[i]);
                }
            }

            // Fit to single page width when printing
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
                            .map(p -> String.format("₹%.0f on %s%s%s%s",
                                    p.getAmount(),
                                    p.getPaymentDate(),
                                    p.getPaidBy() != null ? " by " + p.getPaidBy() : "",
                                    p.getPaymentMethod() != null ? " via " + p.getPaymentMethod() : "",
                                    p.getNote() != null ? " - " + p.getNote(): ""))
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
