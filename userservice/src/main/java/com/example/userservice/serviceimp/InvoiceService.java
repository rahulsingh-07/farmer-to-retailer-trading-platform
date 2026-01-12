package com.example.userservice.serviceimp;

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

            // ===== Header =====
            PdfPTable header = new PdfPTable(2);
            header.setWidthPercentage(100);
            header.setWidths(new int[]{70, 30});
            header.addCell(createLogoCell("FarmFresh Trading", 18, Color.black));
            PdfPCell invoiceCell = createCell("INVOICE\n#INV-" + orderId, Font.BOLD, Element.ALIGN_CENTER, 14, Color.BLUE);
            invoiceCell.setBackgroundColor(Color.GREEN);
            invoiceCell.setPadding(10);
            header.addCell(invoiceCell);
            document.add(header);
            document.add(new Paragraph(" "));

            // ===== Billing Details =====
            PdfPTable billTable = new PdfPTable(2);
            billTable.setWidthPercentage(100);
            billTable.setSpacingBefore(10);
            billTable.setSpacingAfter(10);
            billTable.setWidths(new int[]{50, 50});

            billTable.addCell(createBoldCell("Bill To:", Color.LIGHT_GRAY));
            billTable.addCell(createBoldCell("From Farmer:", Color.LIGHT_GRAY));
            billTable.addCell(createCell(order.getRetailer().getFullName() + "\n" +
                    order.getRetailer().getRetailerDetails().getBusinessAddress()));
            billTable.addCell(createCell(order.getFarmer().getFarmerDetails().getAddress() + "\n" +
                    "Village: " + order.getCrop().getLocation()));
            document.add(billTable);

            // ===== Order Details Table =====
            document.add(new Paragraph("Order Details:", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.BLACK)));
            PdfPTable itemsTable = new PdfPTable(new float[]{30, 15, 15, 15, 20});
            itemsTable.setWidthPercentage(100);
            itemsTable.setSpacingBefore(10);
            itemsTable.setSpacingAfter(10);

            // Table headers
            itemsTable.addCell(createHeaderCell("Description", Color.GREEN, Color.BLACK));
            itemsTable.addCell(createHeaderCell("Qty", Color.GREEN, Color.BLACK));
            itemsTable.addCell(createHeaderCell("Rate", Color.GREEN, Color.BLACK));
            itemsTable.addCell(createHeaderCell("Tax", Color.GREEN, Color.BLACK));
            itemsTable.addCell(createHeaderCell("Amount", Color.GREEN, Color.BLACK));

            // Table data
            String cropName = order.getCrop().getCropName();
            BigDecimal qty = BigDecimal.valueOf(order.getCrop().getQuantity());
            BigDecimal rate = order.getFinalPrice();
            BigDecimal tax = rate.multiply(qty).multiply(BigDecimal.valueOf(0.05));
            BigDecimal total = rate.multiply(qty).add(tax);

            itemsTable.addCell(createCell(cropName));
            itemsTable.addCell(createCell(qty + " Qt"));
            itemsTable.addCell(createCell("₹" + rate));
            itemsTable.addCell(createCell("₹" + tax));
            itemsTable.addCell(createCell("₹" + total));

            document.add(itemsTable);

            // ===== Totals =====
            PdfPTable footer = new PdfPTable(2);
            footer.setWidthPercentage(40);
            footer.setHorizontalAlignment(Element.ALIGN_RIGHT);

            footer.addCell(createBoldCell("Subtotal:"));
            footer.addCell(createCell("₹" + rate.multiply(qty)));
            footer.addCell(createBoldCell("CGST (2.5%):"));
            footer.addCell(createCell("₹" + tax.divide(BigDecimal.valueOf(2))));
            footer.addCell(createBoldCell("SGST (2.5%):"));
            footer.addCell(createCell("₹" + tax.divide(BigDecimal.valueOf(2))));
            footer.addCell(createBoldCell("TOTAL:", Color.GREEN, 12));
            PdfPCell totalCell = createCell("₹" + total);
            totalCell.setBackgroundColor(new Color(230, 255, 230));
            footer.addCell(totalCell);

            document.add(footer);

            // ===== Payment Confirmation Note =====
            Paragraph note = new Paragraph();
            note.add(new Chunk(
                    "\nPayment received successfully!\n",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLUE)));
            note.add(new Chunk(
                    "Thank you for your purchase. Please keep this invoice for your records.\nWe appreciate your business with FarmFresh.",
                    FontFactory.getFont(FontFactory.HELVETICA, 11, Color.DARK_GRAY)));
            document.add(note);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Failed to generate invoice PDF for orderId: {}", orderId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "PDF generation failed");
        }
    }

    // ===== Helper Methods =====
    // For backward compatibility
    private PdfPCell createCell(String text) {
        return createCell(text, Font.NORMAL, Element.ALIGN_LEFT, 12, Color.BLACK);
    }

    private PdfPCell createCell(String text, int fontStyle, int alignment, int fontSize, Color color) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA, fontSize, fontStyle, color);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.BOX);
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(6);
        return cell;
    }

    private PdfPCell createBoldCell(String text) {
        return createBoldCell(text, Color.LIGHT_GRAY);
    }

    private PdfPCell createBoldCell(String text, Color bgColor) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bgColor);
        cell.setPadding(6);
        return cell;
    }

    private PdfPCell createBoldCell(String text, Color bgColor, int fontSize) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, fontSize, Color.BLACK);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bgColor);
        cell.setPadding(6);
        return cell;
    }

    private PdfPCell createLogoCell(String text, int fontSize, Color color) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, fontSize, color);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(8);
        return cell;
    }

    private PdfPCell createHeaderCell(String text, Color bgColor, Color fontColor) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, fontColor);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bgColor);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(8);
        return cell;
    }
}
