package com.example.userservice.invoice;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {
    private final InvoiceService invoiceService;

    @GetMapping("/{orderId}/pdf")
    public void generateInvoicePdf(
            @PathVariable UUID orderId,
            HttpServletResponse response) throws Exception {

        byte[] pdfBytes = invoiceService.generateInvoicePdf(orderId);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition",
                "inline; filename=invoice-" + orderId + ".pdf");
        response.setContentLength(pdfBytes.length);
        response.getOutputStream().write(pdfBytes);
    }
}
