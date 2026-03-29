package org.lkf.agent.rag.processing;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentBySentenceSplitter;
import dev.langchain4j.data.segment.TextSegment;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public abstract class AbstractDocumentProcessingStrategy implements DocumentProcessingStrategy {

    @Override
    public List<String> process(byte[] fileBytes) throws Exception {
        String text = extractText(fileBytes);
        Document document = Document.from(text);
        DocumentBySentenceSplitter splitter = new DocumentBySentenceSplitter(600, 100);
        return toSegmentTexts(splitter.split(document));
    }

    protected abstract String extractText(byte[] fileBytes) throws Exception;

    protected List<String> toSegmentTexts(List<TextSegment> textSegments) {
        List<String> segments = new ArrayList<>();
        for (TextSegment textSegment : textSegments) {
            String content = textSegment.text();
            if (content != null && !content.trim().isEmpty()) {
                segments.add(content.trim());
            }
        }
        return segments;
    }

    protected List<String> splitByBlocks(List<String> blocks, int maxChars, int overlapChars) {
        List<String> segments = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        for (String block : blocks) {
            if (block == null) {
                continue;
            }
            String content = block.trim();
            if (content.isEmpty()) {
                continue;
            }
            if (builder.length() == 0) {
                builder.append(content);
                continue;
            }
            if (builder.length() + content.length() + 1 <= maxChars) {
                builder.append("\n").append(content);
                continue;
            }
            String finished = builder.toString().trim();
            if (!finished.isEmpty()) {
                segments.add(finished);
            }
            String overlap = tail(finished, overlapChars);
            builder.setLength(0);
            if (!overlap.isEmpty()) {
                builder.append(overlap).append("\n");
            }
            builder.append(content);
        }
        String last = builder.toString().trim();
        if (!last.isEmpty()) {
            segments.add(last);
        }
        return segments;
    }

    protected List<String> splitByRegex(String text, String regex, int maxChars, int overlapChars) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }
        String[] pieces = Pattern.compile(regex).split(text);
        List<String> blocks = new ArrayList<>();
        for (String piece : pieces) {
            if (piece != null && !piece.trim().isEmpty()) {
                blocks.add(piece.trim());
            }
        }
        return splitByBlocks(blocks, maxChars, overlapChars);
    }

    private String tail(String content, int overlapChars) {
        if (content == null || content.isEmpty() || overlapChars <= 0) {
            return "";
        }
        if (content.length() <= overlapChars) {
            return content;
        }
        return content.substring(content.length() - overlapChars);
    }
}
