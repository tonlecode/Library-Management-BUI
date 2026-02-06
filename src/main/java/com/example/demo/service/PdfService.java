package com.example.demo.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class PdfService {

    // =========================
    // Compact Brand Styles
    // =========================
    private static final Color PRIMARY = new Color(0, 51, 153);
    private static final Color HEADER_BLUE = new Color(51, 102, 204);
    private static final Color ALT_ROW = new Color(245, 248, 255);
    private static final Color TEXT_GRAY = new Color(64, 64, 64);
    private static final Color BORDER_LIGHT = new Color(230, 230, 230);

    // One-page tuning
    private static final int TOP_BOOKS_LIMIT = 10;

    // A4 margins (slightly larger top, compact body)
    private static final float M_LEFT = 26f;
    private static final float M_RIGHT = 26f;
    private static final float M_TOP = 18f;
    private static final float M_BOTTOM = 18f;

    // Fonts (AWT)
    private final java.awt.Font khmerRegular;
    private final java.awt.Font khmerBold;
    private final java.awt.Font khmerTitle;

    // Stable FRC
    private final FontRenderContext frc;

    public PdfService() {
        java.awt.Font reg = new java.awt.Font("Serif", java.awt.Font.PLAIN, 10);
        java.awt.Font bold = new java.awt.Font("Serif", java.awt.Font.BOLD, 11);
        java.awt.Font title = new java.awt.Font("Serif", java.awt.Font.BOLD, 14);

        try (InputStream isReg = new ClassPathResource("fonts/KhmerOSBattambang.ttf").getInputStream();
             InputStream isBold = new ClassPathResource("fonts/KhmerOSMuolLight.ttf").getInputStream()) {

            reg = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, isReg).deriveFont(10.5f);
            bold = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, isBold).deriveFont(11.5f);
            title = bold.deriveFont(14.5f);

        } catch (Exception e) {
            e.printStackTrace();
        }

        this.khmerRegular = reg;
        this.khmerBold = bold;
        this.khmerTitle = title;

        BufferedImage tmp = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = tmp.createGraphics();
        this.frc = g.getFontRenderContext();
        g.dispose();
    }

    public byte[] generateReport(
            long totalBooks,
            long monthlyLoans,
            long overdueLoans,
            long newMembers,
            List<Map<String, Object>> topBooks
    ) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Document document = new Document(PageSize.A4, M_LEFT, M_RIGHT, M_TOP, M_BOTTOM);
            PdfWriter writer = PdfWriter.getInstance(document, out);
            document.open();

            addHeader(document, writer);
            addDivider(document);

            addTitleAndDate(document, writer);

            // compact metrics (2x2) to avoid huge blocks
            addMetrics2x2(document, writer, totalBooks, monthlyLoans, overdueLoans, newMembers);

            addTopBooksCompactTable(document, writer, topBooks);

            addSignatureCompact(document, writer);

            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // =========================================
    // Header
    // =========================================
    private void addHeader(Document document, PdfWriter writer) throws Exception {
        PdfPTable header = new PdfPTable(2);
        header.setWidthPercentage(100);
        header.setWidths(new float[]{7, 3});

        // Left
        PdfPCell left = noBorderCell();
        Image kh = khmerImg(writer, "សាកលវិទ្យាល័យ ប៊ែលធី អន្តរជាតិ", khmerBold, PRIMARY);
        if (kh != null) { kh.scaleToFit(360, 16); left.addElement(kh); }

        Image en = khmerImg(writer, "BELTEI International University", new java.awt.Font("Helvetica", java.awt.Font.BOLD, 10), PRIMARY);
        if (en != null) { en.scaleToFit(360, 14); left.addElement(en); }

        Image sys = khmerImg(writer, "ប្រព័ន្ធគ្រប់គ្រងបណ្ណាល័យ - ក្រុមទី ៣", khmerRegular, TEXT_GRAY);
        if (sys != null) { sys.scaleToFit(360, 14); left.addElement(sys); }

        header.addCell(left);

        // Right (compact)
        PdfPCell right = noBorderCell();
        right.setHorizontalAlignment(Element.ALIGN_CENTER);

        Image motto1 = khmerImg(writer, "ព្រះរាជាណាចក្រកម្ពុជា", khmerBold.deriveFont(11f), Color.BLACK);
        if (motto1 != null) { motto1.scaleToFit(160, 14); motto1.setAlignment(Element.ALIGN_CENTER); right.addElement(motto1); }

        Image motto2 = khmerImg(writer, "ជាតិ សាសនា ព្រះមហាក្សត្រ", khmerBold.deriveFont(11f), Color.BLACK);
        if (motto2 != null) { motto2.scaleToFit(160, 14); motto2.setAlignment(Element.ALIGN_CENTER); right.addElement(motto2); }

        header.addCell(right);

        document.add(header);
    }

    private void addDivider(Document document) throws Exception {
        PdfPTable line = new PdfPTable(1);
        line.setWidthPercentage(100);
        line.setSpacingBefore(6);
        line.setSpacingAfter(10);

        PdfPCell cell = new PdfPCell(new Phrase(" "));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setBackgroundColor(PRIMARY);
        cell.setFixedHeight(2f);
        line.addCell(cell);

        document.add(line);
    }

    private void addTitleAndDate(Document document, PdfWriter writer) throws Exception {
        Image title = khmerImg(writer, "របាយការណ៍សង្ខេបប្រចាំខែ", khmerTitle, Color.BLACK);
        if (title != null) {
            title.scaleToFit(400, 20);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(2);
            document.add(title);
        }

        Image date = khmerImg(writer, "កាលបរិច្ឆេទ: " + getKhmerDateString(), khmerRegular, TEXT_GRAY);
        if (date != null) {
            date.scaleToFit(400, 14);
            date.setAlignment(Element.ALIGN_CENTER);
            date.setSpacingAfter(10);
            document.add(date);
        }
    }

    // =========================================
    // Metrics (2x2 compact)
    // =========================================
    private void addMetrics2x2(
            Document document,
            PdfWriter writer,
            long totalBooks,
            long monthlyLoans,
            long overdueLoans,
            long newMembers
    ) throws Exception {

        PdfPTable grid = new PdfPTable(2);
        grid.setWidthPercentage(100);
        grid.setWidths(new float[]{1, 1});
        grid.setSpacingAfter(10);
        grid.setKeepTogether(true);

        grid.addCell(metricCard(writer, "សៀវភៅសរុប", toKhmerNum(totalBooks),
                new Color(225, 245, 254), new Color(1, 87, 155)));

        grid.addCell(metricCard(writer, "ការខ្ចីប្រចាំខែ", toKhmerNum(monthlyLoans),
                new Color(232, 245, 233), new Color(27, 94, 32)));

        grid.addCell(metricCard(writer, "ហួសកំណត់", toKhmerNum(overdueLoans),
                new Color(255, 235, 238), new Color(183, 28, 28)));

        grid.addCell(metricCard(writer, "សមាជិកថ្មី", toKhmerNum(newMembers),
                new Color(255, 243, 224), new Color(230, 81, 0)));

        document.add(grid);
    }

    private PdfPCell metricCard(PdfWriter writer, String label, String value, Color bg, Color valueColor) {
        PdfPCell card = new PdfPCell();
        card.setBackgroundColor(bg);
        card.setBorderColor(Color.WHITE);
        card.setBorderWidth(2);
        card.setPadding(8);
        card.setFixedHeight(88f); // IMPORTANT: keeps it compact
        card.setVerticalAlignment(Element.ALIGN_MIDDLE);

        // Use small title/value to avoid huge blocks
        java.awt.Font vFont = khmerBold.deriveFont(16f);
        java.awt.Font lFont = khmerRegular.deriveFont(10.5f);

        Image v = khmerImg(writer, value, vFont, valueColor);
        if (v != null) {
            v.scaleToFit(220, 22);
            v.setAlignment(Element.ALIGN_CENTER);
            card.addElement(v);
        }

        Image l = khmerImg(writer, label, lFont, Color.BLACK);
        if (l != null) {
            l.scaleToFit(240, 14);
            l.setAlignment(Element.ALIGN_CENTER);
            card.addElement(l);
        }

        return card;
    }

    // =========================================
    // Top Books Table (compact)
    // =========================================
    private void addTopBooksCompactTable(Document document, PdfWriter writer, List<Map<String, Object>> topBooks) throws Exception {
        Image t = khmerImg(writer, "បញ្ជីសៀវភៅពេញនិយម (Top 10)", khmerBold.deriveFont(11.5f), Color.BLACK);
        if (t != null) {
            t.scaleToFit(350, 14);
            t.setSpacingAfter(5);
            document.add(t);
        }

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.2f, 6f, 4f, 2f});
        table.setHeaderRows(1);
        table.setSpacingAfter(8);
        table.setKeepTogether(true);

        addHeaderCell(writer, table, "ល.រ");
        addHeaderCell(writer, table, "ចំណងជើងសៀវភៅ");
        addHeaderCell(writer, table, "អ្នកនិពន្ធ");
        addHeaderCell(writer, table, "ចំនួនខ្ចី");

        if (topBooks == null || topBooks.isEmpty()) {
            PdfPCell empty = new PdfPCell();
            empty.setColspan(4);
            empty.setPadding(8);
            empty.setHorizontalAlignment(Element.ALIGN_CENTER);
            empty.setBorderColor(BORDER_LIGHT);

            Image msg = khmerImg(writer, "មិនមានទិន្នន័យ", khmerRegular, Color.BLACK);
            if (msg != null) { msg.scaleToFit(200, 14); empty.addElement(msg); }

            table.addCell(empty);
            document.add(table);
            return;
        }

        int limit = Math.min(topBooks.size(), TOP_BOOKS_LIMIT);
        boolean alt = false;
        int rank = 1;

        for (int i = 0; i < limit; i++) {
            Map<String, Object> b = topBooks.get(i);
            Color bg = alt ? ALT_ROW : Color.WHITE;

            addBodyCell(writer, table, toKhmerNum(rank++), bg, Element.ALIGN_CENTER);
            addBodyCell(writer, table, safeStr(b.get("title")), bg, Element.ALIGN_LEFT);
            addBodyCell(writer, table, safeStr(b.get("author")), bg, Element.ALIGN_LEFT);
            addBodyCell(writer, table, toKhmerNum(b.get("count")), bg, Element.ALIGN_CENTER);

            alt = !alt;
        }

        document.add(table);
    }

    private void addHeaderCell(PdfWriter writer, PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(HEADER_BLUE);
        cell.setBorderColor(Color.WHITE);
        cell.setPadding(6);
        cell.setFixedHeight(26f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        Image img = khmerImg(writer, text, khmerBold.deriveFont(11f), Color.WHITE);
        if (img != null) {
            img.scaleToFit(160, 14);
            img.setAlignment(Element.ALIGN_CENTER);
            cell.addElement(img);
        } else {
            cell.addElement(new Phrase(" "));
        }

        table.addCell(cell);
    }

    private void addBodyCell(PdfWriter writer, PdfPTable table, String text, Color bg, int align) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(bg);
        cell.setBorderColor(BORDER_LIGHT);
        cell.setPadding(4);
        cell.setFixedHeight(22f); // IMPORTANT: row height fixed
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        Image img = khmerImg(writer, text, khmerRegular.deriveFont(10.2f), Color.BLACK);
        if (img != null) {
            img.scaleToFit(260, 14);
            img.setAlignment(align);
            cell.addElement(img);
        } else {
            cell.addElement(new Phrase(" "));
        }

        table.addCell(cell);
    }

    // =========================================
    // Signature (compact)
    // =========================================
    private void addSignatureCompact(Document document, PdfWriter writer) throws Exception {
        PdfPTable sign = new PdfPTable(2);
        sign.setWidthPercentage(100);
        sign.setSpacingBefore(6);
        sign.setKeepTogether(true);

        sign.addCell(signatureCell(writer, " ", " "));
        sign.addCell(signatureCell(writer, "រាជធានីភ្នំពេញ, " + getKhmerDateString(), "ហត្ថលេខា និងឈ្មោះអ្នករៀបចំ"));

        document.add(sign);
    }

    private PdfPCell signatureCell(PdfWriter writer, String dateText, String roleText) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPaddingTop(2);

        Image d = khmerImg(writer, dateText, khmerRegular.deriveFont(10f), TEXT_GRAY);
        if (d != null) { d.scaleToFit(260, 14); d.setAlignment(Element.ALIGN_CENTER); cell.addElement(d); }

        Image r = khmerImg(writer, roleText, khmerBold.deriveFont(11f), Color.BLACK);
        if (r != null) { r.scaleToFit(260, 14); r.setAlignment(Element.ALIGN_CENTER); cell.addElement(r); }

        // only 1 line space (not 2-3 lines)
        cell.addElement(new Paragraph(" "));

        Image dots = khmerImg(writer, "........................................", khmerRegular.deriveFont(10f), Color.BLACK);
        if (dots != null) { dots.scaleToFit(260, 12); dots.setAlignment(Element.ALIGN_CENTER); cell.addElement(dots); }

        return cell;
    }

    private PdfPCell noBorderCell() {
        PdfPCell c = new PdfPCell();
        c.setBorder(Rectangle.NO_BORDER);
        c.setPadding(0);
        return c;
    }

    // =========================================
    // Khmer renderer (Graphics2D -> PdfTemplate -> Image)
    // =========================================
    private Image khmerImg(PdfWriter writer, String text, java.awt.Font font, Color color) {
        if (writer == null || text == null || text.isBlank()) return null;

        try {
            PdfContentByte cb = writer.getDirectContent();

            TextLayout layout = new TextLayout(text, font, frc);
            float w = (float) layout.getAdvance() + 4f;
            float h = (float) (layout.getAscent() + layout.getDescent() + layout.getLeading()) + 2f;

            PdfTemplate template = cb.createTemplate(w, h);
            Graphics2D g2 = template.createGraphics(w, h);

            g2.setColor(color);
            g2.setFont(font);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

            layout.draw(g2, 2f, layout.getAscent() + 1f);
            g2.dispose();

            return Image.getInstance(template);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // =========================================
    // Utils
    // =========================================
    private String safeStr(Object o) { return o == null ? "" : String.valueOf(o); }

    private String toKhmerNum(long num) { return toKhmerNum(String.valueOf(num)); }

    private String toKhmerNum(Object obj) {
        if (obj == null) return "";
        return toKhmerNum(String.valueOf(obj));
    }

    private String toKhmerNum(String num) {
        if (num == null) return "";
        StringBuilder sb = new StringBuilder(num.length());
        for (char c : num.toCharArray()) {
            if (c >= '0' && c <= '9') sb.append((char) (c + 6064));
            else sb.append(c);
        }
        return sb.toString();
    }

    private String getKhmerDateString() {
        Date now = new Date();
        String dd = new SimpleDateFormat("dd").format(now);
        String mm = new SimpleDateFormat("MM").format(now);
        String yyyy = new SimpleDateFormat("yyyy").format(now);

        String monthName = switch (mm) {
            case "01" -> "មករា";
            case "02" -> "កុម្ភៈ";
            case "03" -> "មីនា";
            case "04" -> "មេសា";
            case "05" -> "ឧសភា";
            case "06" -> "មិថុនា";
            case "07" -> "កក្កដា";
            case "08" -> "សីហា";
            case "09" -> "កញ្ញា";
            case "10" -> "តុលា";
            case "11" -> "វិច្ឆិកា";
            case "12" -> "ធ្នូ";
            default -> "";
        };

        return "ថ្ងៃទី " + toKhmerNum(dd) + " ខែ " + monthName + " ឆ្នាំ " + toKhmerNum(yyyy);
    }
}
