package org.lkf.agent.rag.processing;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

@Service("word")
public class WordDocumentProcessingStrategy extends AbstractDocumentProcessingStrategy {

    @Override
    public String strategy() {
        return "word";
    }

    @Override
    protected String extractText(byte[] fileBytes) throws Exception {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(fileBytes);
             XWPFDocument document = new XWPFDocument(inputStream)) {
            StringBuilder builder = new StringBuilder();
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                if (paragraph.getText() != null && !paragraph.getText().trim().isEmpty()) {
                    builder.append(paragraph.getText()).append("\n");
                }
            }
            for (XWPFTable table : document.getTables()) {
                table.getRows().forEach(row -> {
                    StringBuilder rowBuilder = new StringBuilder();
                    row.getTableCells().forEach(cell -> rowBuilder.append(cell.getText()).append(" | "));
                    builder.append(rowBuilder).append("\n");
                });
            }
            return builder.toString();
        }
    }

    @Override
    public List<String> process(byte[] fileBytes) throws Exception {
        List<String> blocks = new ArrayList<>();
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(fileBytes);
             XWPFDocument document = new XWPFDocument(inputStream)) {
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                String text = paragraph.getText();
                if (text != null && !text.trim().isEmpty()) {
                    blocks.add(text.trim());
                }
            }
            for (XWPFTable table : document.getTables()) {
                table.getRows().forEach(row -> {
                    StringBuilder rowBuilder = new StringBuilder();
                    row.getTableCells().forEach(cell -> {
                        if (rowBuilder.length() > 0) {
                            rowBuilder.append(" | ");
                        }
                        rowBuilder.append(cell.getText() == null ? "" : cell.getText().trim());
                    });
                    String rowText = rowBuilder.toString().trim();
                    if (!rowText.isEmpty()) {
                        blocks.add("表格行: " + rowText);
                    }
                });
            }
        }
        return splitByBlocks(blocks, 1000, 120);
    }
}
