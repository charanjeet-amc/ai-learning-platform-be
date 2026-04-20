package com.ailearning.platform.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses PDF and DOCX files into structured Markdown content,
 * uploading inline images to S3.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentParserService {

    private final S3StorageService s3StorageService;

    /**
     * Result of parsing a document — Markdown text plus extracted structure hints.
     */
    public record ParsedDocument(
            String markdown,
            List<Section> sections
    ) {}

    public record Section(
            int level,       // 1 = Module, 2 = Topic, 3 = Concept
            String title,
            String content   // Markdown content under this heading
    ) {}

    /**
     * Parse a file (PDF or DOCX) into structured Markdown.
     */
    public ParsedDocument parse(MultipartFile file, String courseSlug) throws IOException {
        String filename = Objects.requireNonNull(file.getOriginalFilename()).toLowerCase();
        if (filename.endsWith(".docx")) {
            return parseDocx(file, courseSlug);
        } else if (filename.endsWith(".pdf")) {
            return parsePdf(file);
        } else {
            throw new IllegalArgumentException("Unsupported file type. Only PDF and DOCX are supported.");
        }
    }

    // ─── DOCX Parsing ──────────────────────────────────────────

    private ParsedDocument parseDocx(MultipartFile file, String courseSlug) throws IOException {
        try (InputStream is = file.getInputStream();
             XWPFDocument doc = new XWPFDocument(is)) {

            StringBuilder fullMd = new StringBuilder();
            List<Section> sections = new ArrayList<>();
            StringBuilder currentContent = new StringBuilder();
            String currentTitle = null;
            int currentLevel = 0;
            int imageCounter = 0;

            for (var element : doc.getBodyElements()) {
                if (element instanceof XWPFParagraph para) {
                    String style = para.getStyleID();
                    int headingLevel = getHeadingLevel(style, para);

                    if (headingLevel > 0 && headingLevel <= 3) {
                        // Save previous section
                        if (currentTitle != null) {
                            sections.add(new Section(currentLevel, currentTitle, currentContent.toString().trim()));
                        }
                        currentTitle = para.getText().trim();
                        currentLevel = headingLevel;
                        currentContent = new StringBuilder();
                        String prefix = "#".repeat(headingLevel) + " ";
                        fullMd.append("\n").append(prefix).append(currentTitle).append("\n\n");
                    } else {
                        // Regular paragraph — check for images
                        String text = extractParagraphMarkdown(para, courseSlug, imageCounter);
                        if (text.contains("![")) {
                            imageCounter++;
                        }
                        fullMd.append(text).append("\n\n");
                        currentContent.append(text).append("\n\n");
                    }

                } else if (element instanceof XWPFTable table) {
                    String tableMd = convertTableToMarkdown(table);
                    fullMd.append(tableMd).append("\n\n");
                    currentContent.append(tableMd).append("\n\n");
                }
            }

            // Save last section
            if (currentTitle != null) {
                sections.add(new Section(currentLevel, currentTitle, currentContent.toString().trim()));
            }

            // If no headings found, create a single section from all content
            if (sections.isEmpty()) {
                sections.add(new Section(1, "Content", fullMd.toString().trim()));
            }

            return new ParsedDocument(fullMd.toString().trim(), sections);
        }
    }

    private int getHeadingLevel(String styleId, XWPFParagraph para) {
        if (styleId == null) return 0;
        String lower = styleId.toLowerCase();
        // Word styles: Heading1, Heading2, Heading3 or heading 1, heading 2
        Pattern p = Pattern.compile("heading\\s*(\\d)", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(lower);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        // Check for bold + large font as H1 fallback
        if (!para.getRuns().isEmpty()) {
            XWPFRun firstRun = para.getRuns().get(0);
            if (firstRun.isBold() && firstRun.getFontSizeAsDouble() != null && firstRun.getFontSizeAsDouble() >= 16) {
                return 1;
            }
            if (firstRun.isBold() && firstRun.getFontSizeAsDouble() != null && firstRun.getFontSizeAsDouble() >= 13) {
                return 2;
            }
        }
        return 0;
    }

    private String extractParagraphMarkdown(XWPFParagraph para, String courseSlug, int imageCounter) {
        StringBuilder md = new StringBuilder();

        // Check for bulleted/numbered list
        if (para.getNumIlvl() != null) {
            md.append("- ");
        }

        for (XWPFRun run : para.getRuns()) {
            // Handle embedded images
            for (XWPFPicture pic : run.getEmbeddedPictures()) {
                try {
                    XWPFPictureData picData = pic.getPictureData();
                    String ext = picData.suggestFileExtension();
                    String imgName = courseSlug + "-img-" + imageCounter + "." + ext;
                    String url = s3StorageService.uploadBytes(
                            picData.getData(), "courses/" + courseSlug, imgName);
                    md.append("![").append(pic.getDescription() != null ? pic.getDescription() : "image")
                      .append("](").append(url).append(")");
                } catch (IOException e) {
                    log.warn("Failed to upload embedded image: {}", e.getMessage());
                    md.append("[image upload failed]");
                }
            }

            String text = run.getText(0);
            if (text == null) continue;

            // Apply inline formatting
            if (run.isBold() && run.isItalic()) {
                md.append("***").append(text).append("***");
            } else if (run.isBold()) {
                md.append("**").append(text).append("**");
            } else if (run.isItalic()) {
                md.append("*").append(text).append("*");
            } else if (run.getUnderline() != UnderlinePatterns.NONE) {
                md.append("_").append(text).append("_");
            } else {
                md.append(text);
            }
        }

        // Check for hyperlinks
        for (XWPFHyperlink link : para.getDocument().getHyperlinks()) {
            // hyperlinks are handled inline by runs; this is a fallback
        }

        return md.toString();
    }

    private String convertTableToMarkdown(XWPFTable table) {
        StringBuilder md = new StringBuilder();
        List<XWPFTableRow> rows = table.getRows();
        if (rows.isEmpty()) return "";

        // Header row
        XWPFTableRow header = rows.get(0);
        md.append("| ");
        for (XWPFTableCell cell : header.getTableCells()) {
            md.append(cell.getText().trim()).append(" | ");
        }
        md.append("\n|");
        for (int i = 0; i < header.getTableCells().size(); i++) {
            md.append(" --- |");
        }
        md.append("\n");

        // Data rows
        for (int i = 1; i < rows.size(); i++) {
            md.append("| ");
            for (XWPFTableCell cell : rows.get(i).getTableCells()) {
                md.append(cell.getText().trim()).append(" | ");
            }
            md.append("\n");
        }

        return md.toString();
    }

    // ─── PDF Parsing ────────────────────────────────────────────

    private ParsedDocument parsePdf(MultipartFile file) throws IOException {
        try (PDDocument doc = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String rawText = stripper.getText(doc);

            // Convert raw text into sections by detecting heading patterns
            List<Section> sections = new ArrayList<>();
            StringBuilder fullMd = new StringBuilder();
            String[] lines = rawText.split("\\n");

            StringBuilder currentContent = new StringBuilder();
            String currentTitle = null;
            int currentLevel = 0;

            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) {
                    currentContent.append("\n");
                    fullMd.append("\n");
                    continue;
                }

                // Heuristic: short uppercase or title-case lines are headings
                int detectedLevel = detectHeadingLevel(trimmed);
                if (detectedLevel > 0) {
                    if (currentTitle != null) {
                        sections.add(new Section(currentLevel, currentTitle, currentContent.toString().trim()));
                    }
                    currentTitle = trimmed;
                    currentLevel = detectedLevel;
                    currentContent = new StringBuilder();
                    fullMd.append("\n").append("#".repeat(detectedLevel)).append(" ").append(trimmed).append("\n\n");
                } else {
                    currentContent.append(trimmed).append("\n");
                    fullMd.append(trimmed).append("\n");
                }
            }

            if (currentTitle != null) {
                sections.add(new Section(currentLevel, currentTitle, currentContent.toString().trim()));
            }

            if (sections.isEmpty()) {
                sections.add(new Section(1, "Content", fullMd.toString().trim()));
            }

            return new ParsedDocument(fullMd.toString().trim(), sections);
        }
    }

    private int detectHeadingLevel(String line) {
        // Skip very long lines — headings are typically short
        if (line.length() > 80) return 0;
        // Skip lines that look like sentences (end with period etc.)
        if (line.matches(".*[.,:;!?]$")) return 0;

        // ALL CAPS — likely major heading
        if (line.equals(line.toUpperCase()) && line.length() > 2 && line.matches(".*[A-Z].*")) {
            return 1;
        }
        // Title Case and short — likely sub heading
        if (isTitleCase(line) && line.length() < 60) {
            return 2;
        }
        return 0;
    }

    private boolean isTitleCase(String text) {
        String[] words = text.split("\\s+");
        if (words.length < 2) return false;
        int capitalizedWords = 0;
        for (String word : words) {
            if (!word.isEmpty() && Character.isUpperCase(word.charAt(0))) {
                capitalizedWords++;
            }
        }
        return capitalizedWords >= words.length * 0.6;
    }
}
