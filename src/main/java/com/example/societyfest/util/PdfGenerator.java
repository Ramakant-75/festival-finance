package com.example.societyfest.util;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;
import com.example.societyfest.entity.Donation;
import com.example.societyfest.entity.Expense;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.stream.Stream;

public class PdfGenerator {

    public static ByteArrayInputStream generateFestivalSummary(
            List<Donation> donations,
            List<Expense> expenses,
            double previousYearCarryForward
    ) {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Title
            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("Society Festival Summary", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));

            // Summary values
            double currentYearDonations = donations.stream().mapToDouble(Donation::getAmount).sum();
            double totalAvailable = currentYearDonations + previousYearCarryForward;
            double totalExpenses = expenses.stream().mapToDouble(Expense::getAmount).sum();
            double balance = totalAvailable - totalExpenses;

            Font normalFont = new Font(Font.HELVETICA, 12);
            document.add(new Paragraph("Current Year Donations: ₹" + currentYearDonations, normalFont));
            document.add(new Paragraph("Previous Year Carry Forward: ₹" + previousYearCarryForward, normalFont));
            document.add(new Paragraph("Total Available: ₹" + totalAvailable, normalFont));
            document.add(new Paragraph("Total Expenses: ₹" + totalExpenses, normalFont));
            document.add(new Paragraph("Remaining Balance: ₹" + balance, normalFont));
            document.add(new Paragraph(" "));

            // Expense Details Table
            Font sectionFont = new Font(Font.HELVETICA, 14, Font.BOLD);
            document.add(new Paragraph("Expense Breakdown", sectionFont));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            Stream.of("Date", "Category", "Amount", "Description", "Added By").forEach(header -> {
                PdfPCell cell = new PdfPCell(new Phrase(header));
                cell.setBackgroundColor(Color.LIGHT_GRAY);
                table.addCell(cell);
            });

            for (Expense e : expenses) {
                table.addCell(e.getDate().toString());
                table.addCell(e.getCategory());
                table.addCell(String.valueOf(e.getAmount()));
                table.addCell(e.getDescription());
                table.addCell(e.getAddedBy());
            }

            document.add(table);
            document.close();
        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed", e);
        }

        return new ByteArrayInputStream(out.toByteArray());
    }
}
