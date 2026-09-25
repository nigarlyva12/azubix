package com.learnhub.service;

import com.learnhub.entity.Category;
import com.learnhub.entity.User;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Generates a PDF certificate of completion for a user who has
 * finished all topics in a category.
 *
 * Uses OpenPDF (LGPL fork of iText 2.x) for PDF creation.
 * The certificate is A4 landscape with Azubix's purple/teal colour scheme.
 */
@Service
public class CertificateService {

    // Azubix brand colours
    private static final Color COLOR_PRIMARY  = new Color(124, 108, 240); // purple
    private static final Color COLOR_ACCENT   = new Color(45,  212, 191); // teal
    private static final Color COLOR_DARK     = new Color(18,  18,  40);  // near-black
    private static final Color COLOR_GRAY     = new Color(110, 110, 135); // muted
    private static final Color COLOR_LIGHT    = new Color(230, 228, 252); // lavender tint
    private static final Color COLOR_WHITE    = new Color(255, 255, 255);
    private static final Color COLOR_BORDER   = new Color(220, 218, 245); // soft border

    /**
     * Builds a certificate PDF in memory and returns it as a byte array.
     *
     * @param user     the recipient
     * @param category the completed category
     * @return raw PDF bytes ready to stream to the browser
     */
    public byte[] generateCertificate(User user, Category category) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // A4 landscape: 842 x 595 pts  (margins 50 all sides)
            Rectangle pageSize = PageSize.A4.rotate();
            Document doc = new Document(pageSize, 50, 50, 50, 50);
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            doc.open();

            PdfContentByte cb = writer.getDirectContent();
            float W = pageSize.getWidth();   // 842
            float H = pageSize.getHeight();  // 595
            float cx = W / 2f;              // horizontal centre

            // ── 1. Outer decorative border ─────────────────────────────────
            cb.setColorStroke(COLOR_PRIMARY);
            cb.setLineWidth(3f);
            cb.rectangle(24, 24, W - 48, H - 48);
            cb.stroke();

            // ── 2. Inner border (teal, thin) ───────────────────────────────
            cb.setColorStroke(COLOR_ACCENT);
            cb.setLineWidth(1f);
            cb.rectangle(32, 32, W - 64, H - 64);
            cb.stroke();

            // ── 3. Top accent header bar ───────────────────────────────────
            // Sits flush with the outer border top edge
            cb.setColorFill(COLOR_PRIMARY);
            cb.rectangle(24, H - 24 - 58, W - 48, 58);
            cb.fill();

            // ── 4. "Azubix" brand in header ──────────────────────────────
            Font brandFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 17, COLOR_WHITE);
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                    new Phrase("Azubix", brandFont), cx, H - 24 - 36, 0);

            // Monospaced tagline next to brand name
            Font tagFont = FontFactory.getFont(FontFactory.COURIER, 9, COLOR_LIGHT);
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                    new Phrase("// Exam Preparation · Ausbildung", tagFont), cx, H - 24 - 52, 0);

            // ── 5. Main title ──────────────────────────────────────────────
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 30, COLOR_DARK);
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                    new Phrase("Certificate of Completion", titleFont), cx, H - 136, 0);

            // ── 6. Decorative divider under title ──────────────────────────
            drawCentredLine(cb, cx, H - 152, 140, COLOR_ACCENT, 2f);

            // ── 7. "This certifies that" ───────────────────────────────────
            Font subFont = FontFactory.getFont(FontFactory.HELVETICA, 12, COLOR_GRAY);
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                    new Phrase("This certifies that", subFont), cx, H - 188, 0);

            // ── 8. Recipient name ──────────────────────────────────────────
            String displayName = resolveDisplayName(user);
            Font nameFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 26, COLOR_PRIMARY);
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                    new Phrase(displayName, nameFont), cx, H - 230, 0);

            // Underline beneath name
            drawCentredLine(cb, cx, H - 244, 220, COLOR_BORDER, 1f);

            // ── 9. "has successfully completed" ───────────────────────────
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                    new Phrase("has successfully completed", subFont), cx, H - 268, 0);

            // ── 10. Category name ──────────────────────────────────────────
            Font catFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 21, COLOR_DARK);
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                    new Phrase(category.getName(), catFont), cx, H - 306, 0);

            // ── 11. Issue date ─────────────────────────────────────────────
            String date = LocalDate.now()
                    .format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));
            Font dateFont = FontFactory.getFont(FontFactory.HELVETICA, 10, COLOR_GRAY);
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                    new Phrase("Issued on " + date, dateFont), cx, H - 336, 0);

            // ── 12. Three decorative dots ──────────────────────────────────
            float dotY = H - 360;
            fillCircle(cb, cx - 14, dotY, 3.5f, COLOR_ACCENT);
            fillCircle(cb, cx,       dotY, 3.5f, COLOR_PRIMARY);
            fillCircle(cb, cx + 14, dotY, 3.5f, COLOR_ACCENT);

            // ── 13. Bottom accent bar ──────────────────────────────────────
            cb.setColorFill(COLOR_PRIMARY);
            cb.rectangle(24, 24, W - 48, 38);
            cb.fill();

            // "codework.io" watermark-style text in bottom bar
            Font footerFont = FontFactory.getFont(FontFactory.COURIER, 8, COLOR_LIGHT);
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                    new Phrase("Verified completion — Azubix · azubix.de", footerFont),
                    cx, 35, 0);

            doc.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Certificate generation failed", e);
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private String resolveDisplayName(User user) {
        if (user.getName() != null && !user.getName().isBlank()) {
            return user.getName();
        }
        // Fall back to the part before the @ in the email
        String email = user.getEmail();
        int at = email.indexOf('@');
        return at > 0 ? email.substring(0, at) : email;
    }

    private void drawCentredLine(PdfContentByte cb, float cx, float y,
                                  float halfWidth, Color color, float lineWidth) {
        cb.setColorStroke(color);
        cb.setLineWidth(lineWidth);
        cb.moveTo(cx - halfWidth, y);
        cb.lineTo(cx + halfWidth, y);
        cb.stroke();
    }

    private void fillCircle(PdfContentByte cb, float x, float y,
                             float radius, Color color) {
        cb.setColorFill(color);
        cb.circle(x, y, radius);
        cb.fill();
    }
}
