package org.lkf.agent.rag.processing;

import java.util.List;

public interface DocumentProcessingStrategy {

    String strategy();

    List<String> process(byte[] fileBytes) throws Exception;
}
