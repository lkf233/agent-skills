package org.lkf.agent.rag.processing;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentBySentenceSplitter;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service("txt")
public class TxtDocumentProcessingStrategy extends AbstractDocumentProcessingStrategy {

    @Override
    public String strategy() {
        return "txt";
    }

    @Override
    protected String extractText(byte[] fileBytes) {
        return new String(fileBytes, StandardCharsets.UTF_8);
    }

    @Override
    public List<String> process(byte[] fileBytes) {
        String text = extractText(fileBytes);
        DocumentBySentenceSplitter splitter = new DocumentBySentenceSplitter(500, 50);
        return toSegmentTexts(splitter.split(Document.from(text)));
    }
}
