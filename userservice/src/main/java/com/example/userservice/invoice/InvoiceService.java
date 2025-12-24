package com.example.userservice.invoice;

import com.example.userservice.entity.Order;
import com.example.userservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openpdf.text.*;
import org.openpdf.text.Font;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {

    private final OrderRepository orderRepository;

    public byte[] generateInvoicePdf(UUID orderId) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 50, 50, 80, 50);
            PdfWriter.getInstance(document, baos);
            document.open();

            // Header with Logo Space
            PdfPTable header = new PdfPTable(3);
            header.setWidthPercentage(100);
            header.addCell(createImageCell());
            header.addCell(createCell("GSTIN: 08ABCDE1234F1Z5", Font.NORMAL, Element.ALIGN_RIGHT));
            header.addCell(createCell("Invoice #INV-" + orderId, Font.BOLD, Element.ALIGN_RIGHT));
            document.add(header);

            // Billing Details
            document.add(new Paragraph(" "));
            PdfPTable billTable = new PdfPTable(2);
            billTable.setWidthPercentage(100);
            billTable.addCell(createCell("Bill To:", Font.BOLD));
            billTable.addCell(createCell("From Farmer:", Font.BOLD));
            billTable.addCell(createCell(order.getRetailer().getFullName() + "\n" +
                    order.getRetailer().getRetailerDetails().getBusinessAddress(), Font.NORMAL));
            billTable.addCell(createCell(order.getFarmer().getFarmerDetails().getAddress() + "\n" +
                    "Village: " + order.getAuction().getCrop().getLocation(), Font.NORMAL));
            document.add(billTable);

            // Items Table with proper borders
            document.add(new Paragraph("Order Details:", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
            PdfPTable itemsTable = new PdfPTable(new float[]{30, 20, 15, 15, 20});
            itemsTable.setWidthPercentage(100);
            itemsTable.setSpacingBefore(10);

            // Headers
            itemsTable.addCell(createHeaderCell("Description"));
            itemsTable.addCell(createHeaderCell("Qty"));
            itemsTable.addCell(createHeaderCell("Rate"));
            itemsTable.addCell(createHeaderCell("Tax"));
            itemsTable.addCell(createHeaderCell("Amount"));

            // Item Row
            String cropName = order.getAuction().getCrop().getCropName();
            BigDecimal qty = BigDecimal.valueOf(order.getAuction().getCrop().getQuantity());
            BigDecimal rate = order.getFinalPrice().divide(qty, 2, RoundingMode.HALF_UP);
            BigDecimal tax = rate.multiply(qty).multiply(BigDecimal.valueOf(0.05));
            BigDecimal total = rate.multiply(qty).add(tax);

            itemsTable.addCell(createCell(cropName));
            itemsTable.addCell(createCell(qty + " Qt"));
            itemsTable.addCell(createCell("₹" + rate));
            itemsTable.addCell(createCell("₹" + tax));
            itemsTable.addCell(createCell("₹" + total));

            document.add(itemsTable);

            // Totals Footer
            PdfPTable footer = new PdfPTable(2);
            footer.setWidthPercentage(60);
            footer.setHorizontalAlignment(Element.ALIGN_RIGHT);
            footer.addCell(createCell("Subtotal:", Font.BOLD));
            footer.addCell(createCell("₹" + rate.multiply(qty)));
            footer.addCell(createCell("CGST (2.5%):", Font.BOLD));
            footer.addCell(createCell("₹" + tax.divide(BigDecimal.valueOf(2))));
            footer.addCell(createCell("SGST (2.5%):", Font.BOLD));
            footer.addCell(createCell("₹" + tax.divide(BigDecimal.valueOf(2))));
            footer.addCell(createCell("TOTAL:", Font.BOLD));
            footer.addCell(createCell("₹" + total, Font.BOLD));
            document.add(footer);

            // Payment Terms
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Payment Terms: Net 7 days. Bank: SBI, A/c: 1234567890, IFSC: SBIN0001234",
                    FontFactory.getFont(FontFactory.HELVETICA, 10)));

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Failed to generate invoice PDF for orderId: {}", orderId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "PDF generation failed");
        }
    }


    private PdfPCell createCell(String text) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.NORMAL);
        Phrase phrase = new Phrase(text, font);
        PdfPCell cell = new PdfPCell(phrase);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(5);
        return cell;
    }

    private PdfPCell createCell(String text, int fontStyle) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA, 12, fontStyle);
        Phrase phrase = new Phrase(text, font);
        PdfPCell cell = new PdfPCell(phrase);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(5);
        return cell;
    }

    private PdfPCell createCell(String text, int fontStyle, int alignment) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA, 12, fontStyle);
        Phrase phrase = new Phrase(text, font);
        PdfPCell cell = new PdfPCell(phrase);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(5);
        return cell;
    }

    private PdfPCell createImageCell() {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Font.BOLD);
        Phrase phrase = new Phrase("FarmFresh Trading", font);
        PdfPCell cell = new PdfPCell(phrase);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPadding(10);
        return cell;
    }
    private PdfPCell createHeaderCell(String text) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
        Phrase phrase = new Phrase(text, font);
        PdfPCell cell = new PdfPCell(phrase);
        cell.setBackgroundColor(new Color(240, 240, 240));  // Light gray RGB
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(8);
        return cell;
    }


}
