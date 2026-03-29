package org.lkf.agent.rag.processing;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service("markdown")
public class MarkdownDocumentProcessingStrategy extends AbstractDocumentProcessingStrategy {

    @Override
    public String strategy() {
        return "markdown";
    }

    @Override
    protected String extractText(byte[] fileBytes) {
        return new String(fileBytes, StandardCharsets.UTF_8);
    }

    @Override
    public List<String> process(byte[] fileBytes) {
        String text = extractText(fileBytes);
        List<String> blocks = new ArrayList<>();
        String[] lines = text.split("\n");
        String currentHeading = "";
        StringBuilder paragraph = new StringBuilder();
        for (String line : lines) {
            String trimmed = line == null ? "" : line.trim();
            if (trimmed.startsWith("#")) {
                flushParagraph(blocks, currentHeading, paragraph);
                currentHeading = trimmed;
                continue;
            }
            if (trimmed.isEmpty()) {
                flushParagraph(blocks, currentHeading, paragraph);
                continue;
            }
            if (paragraph.length() > 0) {
                paragraph.append("\n");
            }
            paragraph.append(trimmed);
        }
        flushParagraph(blocks, currentHeading, paragraph);
        return splitByBlocks(blocks, 900, 120);
    }

    private void flushParagraph(List<String> blocks, String heading, StringBuilder paragraph) {
        String content = paragraph.toString().trim();
        if (!content.isEmpty()) {
            if (heading != null && !heading.isEmpty()) {
                blocks.add(heading + "\n" + content);
            } else {
                blocks.add(content);
            }
        }
        paragraph.setLength(0);
    }
}
