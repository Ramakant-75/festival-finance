package com.example.societyfest.util;

import com.example.societyfest.entity.Expense;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.stream.Stream;

public class PdfGenerator {

    public static ByteArrayInputStream generateFestivalReport(double totalDonations, double totalExpenses, double balance, List<Expense> expenses, int year) {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
            Paragraph title = new Paragraph("Festival Report ™ - " + year, titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Summary
            Font summaryFont = FontFactory.getFont(FontFactory.TIMES_BOLDITALIC, 14);
            Paragraph summary = new Paragraph(String.format(
                    "Total Donations: ₹%.2f\nTotal Expenses: ₹%.2f\nBalance: ₹%.2f",
                    totalDonations, totalExpenses, balance), summaryFont);
            summary.setSpacingAfter(20);
            document.add(summary);

            // Expense Heading
            Font headingFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Paragraph expenseHeading = new Paragraph("Expenses", headingFont);
            expenseHeading.setSpacingAfter(10);
            document.add(expenseHeading);

            // Expense Table
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10);
            table.setSpacingAfter(10);

            Stream.of("Category", "Amount", "Date", "Description")
                    .forEach(header -> {
                        PdfPCell cell = new PdfPCell(new Phrase(header));
                        cell.setBackgroundColor(Color.LIGHT_GRAY);
                        cell.setPadding(5);
                        table.addCell(cell);
                    });

            for (Expense e : expenses) {
                table.addCell(e.getCategory());
                table.addCell(String.format("%.2f", e.getAmount()));
                table.addCell(e.getDate().toString());
                table.addCell(e.getDescription());
            }

            document.add(table);

            // Gratitude
            Paragraph thanks = new Paragraph("We sincerely thank all society members for their generous contributions and active participation in making the festival a grand success.", summaryFont);
            thanks.setSpacingBefore(20);
            thanks.setSpacingAfter(30);
            document.add(thanks);

            // Signatures
            PdfPTable signatureTable = new PdfPTable(2);
            signatureTable.setWidthPercentage(100);
            signatureTable.setSpacingBefore(20);

            PdfPCell cell1 = new PdfPCell(new Phrase("\n\n_______________________\nPresident"));
            cell1.setBorder(Rectangle.NO_BORDER);
            cell1.setHorizontalAlignment(Element.ALIGN_CENTER);

            PdfPCell cell2 = new PdfPCell(new Phrase("\n\n_______________________\nTreasurer"));
            cell2.setBorder(Rectangle.NO_BORDER);
            cell2.setHorizontalAlignment(Element.ALIGN_CENTER);

            signatureTable.addCell(cell1);
            signatureTable.addCell(cell2);

            document.add(signatureTable);
            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }

        return new ByteArrayInputStream(out.toByteArray());
    }
}
