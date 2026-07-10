package com.financetracker.service;

import com.financetracker.entity.Transaction;
import com.financetracker.entity.User;
import com.financetracker.exception.ResourceNotFoundException;
import com.financetracker.repository.TransactionRepository;
import com.financetracker.repository.UserRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExportService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    public byte[] exportCsv(String username, int month, int year) {
        User user = getUser(username);
        List<Transaction> transactions = getMonthTransactions(user, month, year);

        StringWriter stringWriter = new StringWriter();
        try (CSVWriter csvWriter = new CSVWriter(stringWriter)) {
            csvWriter.writeNext(new String[]{"Date", "Type", "Category", "Amount", "Description"});
            for (Transaction t : transactions) {
                csvWriter.writeNext(new String[]{
                        t.getDate().toString(),
                        t.getType().toString(),
                        t.getCategory(),
                        t.getAmount().toString(),
                        t.getDescription() == null ? "" : t.getDescription()
                });
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate CSV export", e);
        }

        return stringWriter.toString().getBytes();
    }

    public byte[] exportPdf(String username, int month, int year) {
        User user = getUser(username);
        List<Transaction> transactions = getMonthTransactions(user, month, year);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Paragraph title = new Paragraph(
                    "Expense Report - " + YearMonth.of(year, month).format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                    titleFont);
            title.setSpacingAfter(15);
            document.add(title);

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2, 2, 2.5f, 2, 4});

            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BaseColor.WHITE);
            String[] headers = {"Date", "Type", "Category", "Amount", "Description"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                cell.setBackgroundColor(new BaseColor(52, 73, 94));
                cell.setPadding(6);
                table.addCell(cell);
            }

            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            for (Transaction t : transactions) {
                table.addCell(new PdfPCell(new Phrase(t.getDate().toString(), cellFont)));
                table.addCell(new PdfPCell(new Phrase(t.getType().toString(), cellFont)));
                table.addCell(new PdfPCell(new Phrase(t.getCategory(), cellFont)));
                table.addCell(new PdfPCell(new Phrase(t.getAmount().toString(), cellFont)));
                table.addCell(new PdfPCell(new Phrase(t.getDescription() == null ? "" : t.getDescription(), cellFont)));
            }

            document.add(table);
            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Failed to generate PDF export", e);
        }

        return out.toByteArray();
    }

    private List<Transaction> getMonthTransactions(User user, int month, int year) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        return transactionRepository.findByUserAndDateBetweenOrderByDateDesc(user, start, end);
    }
}
