package com.epay.auth.service;

import java.io.IOException;
import java.util.List;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.example.auth_user_service.dtos.BankStatement;
import com.example.auth_user_service.interfaces.INotificationServiceClient;
import com.example.auth_user_service.interfaces.IPDFService;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.extgstate.PdfExtGState;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;
import com.epay.auth.interfaces.IStatementService;
import com.epay.domain.auth.dto.BankStatement;

@Service
public class StatementService implements IStatementService{

    private static final Color LIGHT_GRAY = new DeviceRgb(220, 220, 220);
    private static final Color DARK_GRAY = new DeviceRgb(80, 80, 80);
    private static final Color HEADER_COLOR = new DeviceRgb(240, 240, 240);
    private static final Color WATERMARK_COLOR = new DeviceRgb(100, 100, 100);
    // private static final Color BORDER_COLOR = new DeviceRgb(180, 180, 180);


        @Autowired
    private INotificationServiceClient notificationServiceClient;

    // Inner class for footer event handler
    private static class FooterEventHandler implements IEventHandler {
        @SuppressWarnings("unused")
        private int transactionCount;
        // private int totalPages;
        private int currentPage = 0;
        
        public FooterEventHandler(int transactionCount) {
            this.transactionCount = transactionCount;
        }
        
        @Override
        public void handleEvent(Event event) {
            PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
            PdfDocument pdf = docEvent.getDocument();
            PdfPage page = docEvent.getPage();
            Rectangle pageSize = page.getPageSize();
            
            currentPage++;
            if (currentPage == pdf.getNumberOfPages()) {
                float footerY = pageSize.getBottom() + 20;
                
                try {
                    PdfCanvas pdfCanvas = new PdfCanvas(page.newContentStreamBefore(), page.getResources(), pdf);
                    try (Canvas canvas = new Canvas(pdfCanvas, pageSize)) {
                        addFooterContent(canvas, footerY, pageSize);
                    }
                } catch (Exception e) {}
            }
        }
        
        private void addFooterContent(com.itextpdf.layout.Canvas canvas, float y, Rectangle pageSize) {
            float pageWidth = pageSize.getWidth();
            
            Paragraph footer = new Paragraph()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(8)
                    .setFixedPosition(0, y, pageWidth);

            footer.add("This is an automated transaction alert service. For enquiries on PESCO BANK's products and services, " +
                    "please call the PESCO BANK Contact Centre on +234 0201-2802500, +234 0201-2712500-7 or send an email to contactcenter@pescobank.com");
            
            canvas.add(footer);
        }
    }

    private static class CurrencyMetrics {
        double openingBalance;
        double totalCredits;
        double totalDebits;
        double closingBalance;
        
        CurrencyMetrics(double openingBalance, double totalCredits, double totalDebits, double closingBalance) {
            this.openingBalance = openingBalance;
            this.totalCredits = totalCredits;
            this.totalDebits = totalDebits;
            this.closingBalance = closingBalance;
        }
    }

    @Override
    public byte[] generateBankStatementPDF(List<BankStatement> statements) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        try (Document document = new Document(pdf)) {
            document.setMargins(20, 20, 50, 20);
            FooterEventHandler footerHandler = new FooterEventHandler(statements.size());
            pdf.addEventHandler(PdfDocumentEvent.END_PAGE, footerHandler);
            
            Paragraph logoParagraph = new Paragraph();
            logoParagraph.setTextAlignment(TextAlignment.CENTER);
            logoParagraph.setMarginBottom(5);
            
            Image logo = loadLogoFromResources();
            logo.setWidth(60);
            logo.setMaxHeight(30);
            logoParagraph.add(logo);
            document.add(logoParagraph);
            
            Paragraph header = new Paragraph("ACCOUNT STATEMENT")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold()
                    .setFontSize(14)
                    .setMarginBottom(2);
            document.add(header);
            
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy hh:mm:ss a", Locale.ENGLISH);
            String formattedDate = now.format(formatter);
            
            Paragraph generatedOn = new Paragraph("Generated on " + formattedDate)
                    .setFontColor(DARK_GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(8)
                    .setMarginBottom(15);
            document.add(generatedOn);
            
            // Check if we have multiple currencies
            boolean hasMultipleCurrencies = hasMultipleCurrencies(statements);
            String primaryCurrencyType = getPrimaryCurrency(statements);
            String currencySymbol = getCurrencySymbol(primaryCurrencyType);
            
            Table gridTable = new Table(new float[]{1, 1});
            gridTable.setWidth(UnitValue.createPercentValue(100));
            gridTable.setMarginBottom(15);
            
            Cell leftColumn = new Cell();
            leftColumn.setPadding(0)
                    .setBorder(Border.NO_BORDER);
            
            Paragraph accountTitle = new Paragraph("Account Information")
                    .setFontColor(ColorConstants.BLACK)
                    .setBold()
                    .setFontSize(10)
                    .setMarginBottom(8);
            leftColumn.add(accountTitle);
            
            Table infoTable = new Table(new float[]{1, 1});
            infoTable.setWidth(UnitValue.createPercentValue(100));
            
            final String CUSTOMER_ADDRESS_TEXT = "Nil";
            
            addAccountInfoRow(infoTable, "Account Number:", "1234567890");
            addAccountInfoRow(infoTable, "Account Holder:", "John Doe");
            addAccountInfoRow(infoTable, "Account Type:", "Premium Savings");
            addAccountInfoRow(infoTable, "Branch:", "Main Downtown");
            addAccountInfoRow(infoTable, "Statement Date:", LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MMM-yy")).toUpperCase());
            addAccountInfoRow(infoTable, "Customer Address:", CUSTOMER_ADDRESS_TEXT);
            
            leftColumn.add(infoTable);
            gridTable.addCell(leftColumn);
            
            Cell rightColumn = new Cell();
            rightColumn.setPadding(0)
                    .setBorder(Border.NO_BORDER);
            
            Paragraph summaryTitle = new Paragraph("Financial Summary")
                    .setFontColor(ColorConstants.BLACK)
                    .setBold()
                    .setFontSize(10)
                    .setMarginBottom(8);
            rightColumn.add(summaryTitle);
            
            Table summaryTable = new Table(new float[]{1, 1});
            summaryTable.setWidth(UnitValue.createPercentValue(100));
            
            if (hasMultipleCurrencies) {
                // For multiple currencies, Financial Summary only shows minimal info
                addSummaryTextRow(summaryTable, "Summary:", "See Currency Breakdown Below");
                addSummaryTextRow(summaryTable, "Details:", "Refer to currency table");
            } else {
                // For single currency, calculate and show normal financial summary
                double totalCredits = statements.stream()
                        .filter(s -> "DEPOSIT".equalsIgnoreCase(s.getType()) ||
                                "TRANSFER".equalsIgnoreCase(s.getType()) || 
                                "CREDIT".equalsIgnoreCase(s.getType()))
                        .mapToDouble(BankStatement::getNetAmount)
                        .sum();
                
                double totalDebits = statements.stream()
                        .filter(s -> !("DEPOSIT".equalsIgnoreCase(s.getType()) ||
                                "TRANSFER".equalsIgnoreCase(s.getType()) || 
                                "CREDIT".equalsIgnoreCase(s.getType())))
                        .mapToDouble(BankStatement::getNetAmount)
                        .sum();
                
                double openingBalance = statements.isEmpty() ? 0 : statements.get(statements.size() - 1).getBalance() + totalDebits - totalCredits;
                double closingBalance = statements.isEmpty() ? 0 : statements.get(0).getBalance();
                
                addSummaryRow(summaryTable, "Opening Balance", openingBalance, currencySymbol);
                addSummaryRow(summaryTable, "Total Credits", totalCredits, currencySymbol);
                addSummaryRow(summaryTable, "Total Debits", totalDebits, currencySymbol);
                addSummaryRow(summaryTable, "Closing Balance", closingBalance, currencySymbol);
            }
            
            addSummaryTextRow(summaryTable, "Period Covered:", getPeriodCovered(statements));
            addSummaryTextRow(summaryTable, "Currency:", hasMultipleCurrencies ? "MULTIPLE" : primaryCurrencyType);
            
            rightColumn.add(summaryTable);
            gridTable.addCell(rightColumn);
            
            document.add(gridTable);
            
            // ADD SEPARATE CURRENCY BREAKDOWN TABLE (ONLY WHEN MULTIPLE CURRENCIES)
            if (hasMultipleCurrencies) {
                addCurrencyBreakdownTable(document, statements);
            }
            
            Paragraph transactionsTitle = new Paragraph("Transaction Details")
                    .setFontColor(ColorConstants.BLACK)
                    .setBold()
                    .setFontSize(12)
                    .setMarginBottom(8);
            document.add(transactionsTitle);
            
            // Create transaction table with proper structure
            float[] transactionWidths = {1.8f, 3, 1.2f, 1.2f, 1.2f, 1f};
            Table transactionTable = new Table(transactionWidths);
            transactionTable.setWidth(UnitValue.createPercentValue(100));
            transactionTable.setKeepTogether(true);
            transactionTable.setMarginBottom(15);
            
            // Add headers
            addTransactionHeader(transactionTable, "Date");
            addTransactionHeader(transactionTable, "Description");
            addTransactionHeader(transactionTable, "Credit");
            addTransactionHeader(transactionTable, "Debit");
            addTransactionHeader(transactionTable, "Balance");
            addTransactionHeader(transactionTable, "Currency");
            
            boolean alternate = false;
            for (BankStatement statement : statements) {
                addTransactionRow(transactionTable, statement, alternate, hasMultipleCurrencies);
                alternate = !alternate;
            }
            
            document.add(transactionTable);
            addWatermark(pdf);
        }
        return baos.toByteArray();
    }

    // Method to add currency breakdown table
    private void addCurrencyBreakdownTable(Document document, List<BankStatement> statements) {
        // Calculate currency metrics
        Map<String, CurrencyMetrics> currencyMetrics = calculateCurrencyMetrics(statements);
        
        // Add section header - same style as Transaction Details
        Paragraph currencyHeader = new Paragraph("Currency Breakdown")
                .setFontColor(ColorConstants.BLACK)
                .setBold()
                .setFontSize(12)
                .setMarginTop(10)
                .setMarginBottom(8);
        document.add(currencyHeader);
        
        // Create the currency breakdown table - same styling as transaction table
        float[] currencyWidths = {1.5f, 2f, 2f, 2f, 2f};
        Table currencyTable = new Table(currencyWidths);
        currencyTable.setWidth(UnitValue.createPercentValue(100));
        currencyTable.setKeepTogether(true);
        currencyTable.setMarginBottom(15);
        
        // Add table headers - same style as transaction headers
        addCurrencyTableHeader(currencyTable, "Currency");
        addCurrencyTableHeader(currencyTable, "Opening Balance");
        addCurrencyTableHeader(currencyTable, "Total Credits");
        addCurrencyTableHeader(currencyTable, "Total Debits");
        addCurrencyTableHeader(currencyTable, "Closing Balance");
        
        // Add data rows
        boolean alternateRow = false;
        for (Map.Entry<String, CurrencyMetrics> entry : currencyMetrics.entrySet()) {
            String currency = entry.getKey();
            CurrencyMetrics metrics = entry.getValue();
            
            addCurrencyTableRow(currencyTable, currency, metrics, alternateRow);
            alternateRow = !alternateRow;
        }
        
        document.add(currencyTable);
    }
    
    // Calculate metrics for each currency
    private Map<String, CurrencyMetrics> calculateCurrencyMetrics(List<BankStatement> statements) {
        Map<String, CurrencyMetrics> metrics = new LinkedHashMap<>();
        Map<String, List<BankStatement>> statementsByCurrency = new LinkedHashMap<>();
        
        // Group statements by currency
        for (BankStatement statement : statements) {
            String currency = statement.getCurrencyType();
            if (currency != null) {
                statementsByCurrency.computeIfAbsent(currency, k -> new ArrayList<>()).add(statement);
            }
        }
        
        // Calculate metrics for each currency
        for (Map.Entry<String, List<BankStatement>> entry : statementsByCurrency.entrySet()) {
            String currency = entry.getKey();
            List<BankStatement> currencyStatements = entry.getValue();
            
            // Sort by date (oldest first)
            currencyStatements.sort(Comparator.comparing(BankStatement::getDate));
            
            double totalCredits = statements.stream()
                .filter(s -> s.getType() != null && (
                        "DEPOSIT".equalsIgnoreCase(s.getType()) ||
                        "TRANSFER".equalsIgnoreCase(s.getType()) ||
                        "CREDIT".equalsIgnoreCase(s.getType())))
                .mapToDouble(s -> s.getNetAmount() != null ? s.getNetAmount() : 0.0)
                .sum();

        double totalDebits = statements.stream()
                .filter(s -> s.getType() == null || !(
                        "DEPOSIT".equalsIgnoreCase(s.getType()) ||
                        "TRANSFER".equalsIgnoreCase(s.getType()) ||
                        "CREDIT".equalsIgnoreCase(s.getType())))
                .mapToDouble(s -> s.getNetAmount() != null ? s.getNetAmount() : 0.0)
                .sum();
                    
            // Get oldest and newest statements
            BankStatement oldest = currencyStatements.get(0);
            BankStatement newest = currencyStatements.get(currencyStatements.size() - 1);
            
            double openingBalance = oldest.getBalance() + totalDebits - totalCredits;
            double closingBalance = newest.getBalance();
            
            metrics.put(currency, new CurrencyMetrics(openingBalance, totalCredits, totalDebits, closingBalance));
        }
        
        return metrics;
    }
    
    // Add currency table header - same style as transaction header
    private void addCurrencyTableHeader(Table table, String text) {
        Cell headerCell = new Cell()
                .add(new Paragraph(text).setBold().setFontSize(9))
                .setBackgroundColor(LIGHT_GRAY)
                .setFontColor(ColorConstants.BLACK)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(4)  // Same as transaction header
                .setBorder(new SolidBorder(ColorConstants.WHITE, 1));
        
        table.addHeaderCell(headerCell);
    }
    
    // Add currency table row - same style as transaction row
    private void addCurrencyTableRow(Table table, String currency, CurrencyMetrics metrics, boolean alternate) {
        Color rowColor = alternate ? new DeviceRgb(248, 248, 248) : ColorConstants.WHITE;
        
        // Currency column
        table.addCell(new Cell()
                .add(new Paragraph(currency).setFontSize(8))
                .setBackgroundColor(rowColor)
                .setPadding(5)  // Same as transaction row
                .setBorder(new SolidBorder(new DeviceRgb(100, 100, 100), 0.2f))
                .setTextAlignment(TextAlignment.CENTER));
        
        // Opening Balance
        table.addCell(new Cell()
                .add(new Paragraph(String.format("%,.2f", metrics.openingBalance)).setFontSize(8))
                .setBackgroundColor(rowColor)
                .setPadding(5)  // Same as transaction row
                .setBorder(new SolidBorder(new DeviceRgb(100, 100, 100), 0.2f))
                .setTextAlignment(TextAlignment.RIGHT));
        
        // Total Credits
        table.addCell(new Cell()
                .add(new Paragraph(String.format("%,.2f", metrics.totalCredits)).setFontSize(8))
                .setBackgroundColor(rowColor)
                .setPadding(5)  // Same as transaction row
                .setBorder(new SolidBorder(new DeviceRgb(100, 100, 100), 0.2f))
                .setTextAlignment(TextAlignment.RIGHT));
        
        // Total Debits
        table.addCell(new Cell()
                .add(new Paragraph(String.format("%,.2f", metrics.totalDebits)).setFontSize(8))
                .setBackgroundColor(rowColor)
                .setPadding(5)  // Same as transaction row
                .setBorder(new SolidBorder(new DeviceRgb(100, 100, 100), 0.2f))
                .setTextAlignment(TextAlignment.RIGHT));
        
        // Closing Balance
        table.addCell(new Cell()
                .add(new Paragraph(String.format("%,.2f", metrics.closingBalance)).setFontSize(8))
                .setBackgroundColor(rowColor)
                .setPadding(5)  // Same as transaction row
                .setBorder(new SolidBorder(new DeviceRgb(100, 100, 100), 0.2f))
                .setTextAlignment(TextAlignment.RIGHT));
    }

    // Helper method to check if there are multiple currencies
    private boolean hasMultipleCurrencies(List<BankStatement> statements) {
        if (statements == null || statements.size() <= 1) {
            return false;
        }
        
        Set<String> currencies = new HashSet<>();
        for (BankStatement statement : statements) {
            if (statement.getCurrencyType() != null) {
                currencies.add(statement.getCurrencyType().toUpperCase());
            }
        }
        
        return currencies.size() > 1;
    }

    // Helper method to get primary currency (most frequent)
    private String getPrimaryCurrency(List<BankStatement> statements) {
        if (statements == null || statements.isEmpty()) {
            return "USD";
        }
        
        Map<String, Integer> currencyCount = new HashMap<>();
        for (BankStatement statement : statements) {
            String currency = statement.getCurrencyType();
            if (currency != null) {
                currencyCount.put(currency, currencyCount.getOrDefault(currency, 0) + 1);
            }
        }
        
        // Return the most frequent currency
        return currencyCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("USD");
    }

    private String breakLongText(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        
        int breakPoint = text.lastIndexOf(' ', maxLength);
        if (breakPoint == -1) {
            breakPoint = maxLength;
        }
        
        return text.substring(0, breakPoint) + "\n" + text.substring(breakPoint).trim();
    }

    private void addAccountInfoRow(Table table, String label, String value) {
        String displayValue = value;
        if (label.equals("Customer Address:") && value.length() > 22) {
            displayValue = breakLongText(value, 22);
        }

        Cell labelCell = new Cell();
        Paragraph labelPara = new Paragraph(label).setFontSize(9).setBold();
        labelCell.add(labelPara)
                .setPadding(6)
                .setBorderBottom(new SolidBorder(LIGHT_GRAY, 0.5f))
                .setBorderLeft(Border.NO_BORDER)
                .setBorderRight(Border.NO_BORDER)
                .setBorderTop(Border.NO_BORDER)
                .setBackgroundColor(HEADER_COLOR);

        Cell valueCell = new Cell();
        
        // Set smaller font size and custom font for Customer Address
        int fontSize = 9; // Default size
        Paragraph valuePara = new Paragraph(displayValue).setFontSize(fontSize);
        
        if ("Customer Address:".equals(label)) {
            fontSize = 7; // Smaller size for address
            valuePara.setFontSize(fontSize);
            
            try {
                PdfFont customAddressFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
                valuePara.setFont(customAddressFont);
            } catch (IOException e) {
                System.err.println("Failed to load custom font: " + e.getMessage());
            }
        }

        valueCell.add(valuePara)
                .setPadding(6)
                .setBorderBottom(new SolidBorder(LIGHT_GRAY, 0.5f))
                .setBorderLeft(Border.NO_BORDER)
                .setBorderRight(Border.NO_BORDER)
                .setBorderTop(Border.NO_BORDER);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    // Method for double values (financial amounts) with currency symbol
    private void addSummaryRow(Table table, String label, double value, String currencySymbol) {
        Cell labelCell = new Cell();
        Paragraph labelPara = new Paragraph(label).setFontSize(9).setBold();
        labelCell.add(labelPara)
                .setPadding(6)
                .setBorderBottom(new SolidBorder(LIGHT_GRAY, 0.5f))
                .setBorderLeft(Border.NO_BORDER)
                .setBorderRight(Border.NO_BORDER)
                .setBorderTop(Border.NO_BORDER)
                .setBackgroundColor(HEADER_COLOR)
                .setTextAlignment(TextAlignment.LEFT);

        Cell valueCell = new Cell();
        Paragraph valuePara = new Paragraph(String.format("%s%,.2f", currencySymbol, value)).setFontSize(9);
        valueCell.add(valuePara)
                .setPadding(6)
                .setBorderBottom(new SolidBorder(LIGHT_GRAY, 0.5f))
                .setBorderLeft(Border.NO_BORDER)
                .setBorderRight(Border.NO_BORDER)
                .setBorderTop(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.RIGHT)
                .setFontColor(ColorConstants.BLACK);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    // Method for string values (text fields)
    private void addSummaryTextRow(Table table, String label, String value) {
        Cell labelCell = new Cell();
        Paragraph labelPara = new Paragraph(label).setFontSize(9).setBold();
        labelCell.add(labelPara)
                .setPadding(6)
                .setBorderBottom(new SolidBorder(LIGHT_GRAY, 0.5f))
                .setBorderLeft(Border.NO_BORDER)
                .setBorderRight(Border.NO_BORDER)
                .setBorderTop(Border.NO_BORDER)
                .setBackgroundColor(HEADER_COLOR)
                .setTextAlignment(TextAlignment.LEFT);

        Cell valueCell = new Cell();
        Paragraph valuePara = new Paragraph(value).setFontSize(9);
        valueCell.add(valuePara)
                .setPadding(6)
                .setBorderBottom(new SolidBorder(LIGHT_GRAY, 0.5f))
                .setBorderLeft(Border.NO_BORDER)
                .setBorderRight(Border.NO_BORDER)
                .setBorderTop(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.RIGHT)
                .setFontColor(ColorConstants.BLACK);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    // Simplified logo creation method
    private Image loadLogoFromResources() throws IOException {
        ClassPathResource resource = new ClassPathResource("static/image/logo.png");
        try (InputStream is = resource.getInputStream()) {
            byte[] imageBytes = is.readAllBytes();
            return new Image(ImageDataFactory.create(imageBytes));
        }
    }

    private void addWatermark(PdfDocument pdf) throws IOException {
        for (int i = 1; i <= pdf.getNumberOfPages(); i++) {
            PdfCanvas canvas = new PdfCanvas(pdf.getPage(i));
            Rectangle pageSize = pdf.getPage(i).getPageSize();

            try (Canvas canvasModel = new Canvas(canvas, pageSize)) {
                canvas.saveState();
                PdfExtGState gs = new PdfExtGState();
                gs.setFillOpacity(0.2f);
                canvas.setExtGState(gs);
                
                canvasModel.setFont(com.itextpdf.kernel.font.PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD));
                canvasModel.setFontSize(80);
                canvasModel.setFontColor(WATERMARK_COLOR);
                
                canvasModel.showTextAligned(
                        "CONFIDENTIAL",
                        pageSize.getWidth() / 2,
                        pageSize.getHeight() / 2,
                        TextAlignment.CENTER,
                        com.itextpdf.layout.properties.VerticalAlignment.MIDDLE,
                        (float) Math.toRadians(45)
                );
                
                canvas.restoreState();
            }
        }
    }

    private void addTransactionHeader(Table table, String text) {
        Cell headerCell = new Cell()
                .add(new Paragraph(text).setBold().setFontSize(9))
                .setBackgroundColor(LIGHT_GRAY)
                .setFontColor(ColorConstants.BLACK)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(4)
                .setBorder(new SolidBorder(ColorConstants.WHITE, 1));
        
        table.addHeaderCell(headerCell);
    }

    // Transaction row method
    private void addTransactionRow(Table table, BankStatement statement, boolean alternate, boolean hasMultipleCurrencies) {
        Color rowColor = alternate ? new DeviceRgb(248, 248, 248) : ColorConstants.WHITE;
        String formattedDate = formatBankDate(statement.getDate());

        // Get currency symbol for this specific transaction
        String transactionCurrencySymbol = getCurrencySymbol(statement.getCurrencyType());

        // 1. Date cell
        table.addCell(createTransactionCell(formattedDate, rowColor).setTextAlignment(TextAlignment.LEFT));
        
        // 2. Description cell
        table.addCell(createTransactionCell(statement.getDescription(), rowColor));
        
        // 3. & 4. Credit and Debit cells
        String creditAmount = "-";
        String debitAmount = "-";
        
        String transactionType = statement.getType() != null ? statement.getType().toUpperCase() : "";
        // CREDIT transactions: DEPOSIT, TRANSFER (incoming), CREDIT
        if ("DEPOSIT".equals(transactionType) || "TRANSFER".equals(transactionType) || "CREDIT".equals(transactionType)) {
            creditAmount = String.format("%s%,.2f", transactionCurrencySymbol, statement.getNetAmount());
            debitAmount = "-";
        }
        // DEBIT transactions: WITHDRAW, DEBIT, and any other types
        else {
            creditAmount = "-";
            debitAmount = String.format("%s%,.2f", transactionCurrencySymbol, statement.getNetAmount());
        }
        
        // 3. Credit cell
        table.addCell(createTransactionCell(creditAmount, rowColor)
                .setTextAlignment(TextAlignment.RIGHT)
                .setFontColor(ColorConstants.BLACK));
        
        // 4. Debit cell
        table.addCell(createTransactionCell(debitAmount, rowColor)
                .setTextAlignment(TextAlignment.RIGHT)
                .setFontColor(ColorConstants.BLACK));
        
        // 5. Balance cell
        table.addCell(createTransactionCell(String.format("%s%,.2f", transactionCurrencySymbol, statement.getBalance()), rowColor)
                .setTextAlignment(TextAlignment.RIGHT));
        
        // 6. Currency cell - only show if multiple currencies exist
        String currencyDisplay = hasMultipleCurrencies ? statement.getCurrencyType() : "";
        table.addCell(createTransactionCell(currencyDisplay, rowColor)
                .setTextAlignment(TextAlignment.CENTER));
    }

    private String getCurrencySymbol(String currencyType) {
        if (currencyType == null || currencyType.trim().isEmpty()) {
            return "$"; // Default to USD symbol
        }
        
        return switch (currencyType.toUpperCase()) {
            case "USD" -> "$";
            case "EUR" -> "€";
            case "NGN" -> "₦";
            case "GBP" -> "£";
            case "JPY" -> "¥";
            case "AUD" -> "A$";
            case "CAD" -> "C$";
            case "CHF" -> "CHF ";
            case "CNY" -> "¥";
            case "INR" -> "₹";
            default -> currencyType + " ";
        };
    }

    private Cell createTransactionCell(String text, Color backgroundColor) {
        Paragraph paragraph = new Paragraph(text).setFontSize(8);
        
        return new Cell()
                .add(paragraph)
                .setBackgroundColor(backgroundColor)
                .setPadding(5)
                .setBorder(new SolidBorder(new DeviceRgb(100, 100, 100), 0.2f))
                .setTextAlignment(TextAlignment.LEFT);
    }

    private String formatBankDate(String date) {
        try {
            OffsetDateTime dateTime = OffsetDateTime.parse(date);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm:ss");
            return dateTime.format(formatter);
        } catch (Exception e1) {
            try {
                LocalDate localDate = LocalDate.parse(date);
                return localDate.format(DateTimeFormatter.ofPattern("dd-MMM-yy")).toUpperCase();
            } catch (Exception e2) {
                return date;
            }
        }
    }

    private String getPeriodCovered(List<BankStatement> statements) {
        if (statements.isEmpty()) {
            return "N/A";
        }
        String startDate = formatBankDate(statements.get(statements.size() - 1).getDate());
        String endDate = formatBankDate(statements.get(0).getDate());
        return startDate + " to " + endDate;
    }

    @Override
    public void generateAndSendBankStatement(String email, String username, List<BankStatement> statements) {
        try {
            byte[] pdfBytes = generateBankStatementPDF(statements);
            String period = getPeriodCovered(statements).replace(" ", "-").replace(":", "");
            
            ByteArrayResource pdfResource = new ByteArrayResource(pdfBytes) {
                @Override
                public String getFilename() {
                    return "bank-statement.pdf";
                }
            };
            
            notificationServiceClient.sendBankStatementEmail(email, username, pdfResource, period);
            
        } catch (Exception e) { // catch ALL exceptions, not just IOException
            throw new RuntimeException("Failed to generate and send bank statement: " + e.getMessage(), e);
        }
    }
}

