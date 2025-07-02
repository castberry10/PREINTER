package com.interviewee.preinter.document;

import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.metadata.Metadata;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;


@Service
public class DocumentService {
    private final Parser parser = new AutoDetectParser();
    private final BodyContentHandler handler = new BodyContentHandler(-1);  // 크기 무제한
    private final Metadata metadata = new Metadata();

    public String extractText(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            parser.parse(is, handler, metadata, new ParseContext());
            return handler.toString();
        } catch (Exception e) {
            throw new RuntimeException("문서 파싱 실패: " + e.getMessage(), e);
        }
    }
}