package financial.dart.vector.service;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SimpleChunker {

    // 초기값: 1000자 기준
    private static final int MAX_CHARS = 1000;

    public List<String> chunk(String text) {
        if (text == null || text.isBlank()) return List.of();

        // 짧으면 1개로
        if (text.length() <= MAX_CHARS) return List.of(text);

        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + MAX_CHARS, text.length());

            // 가능하면 줄바꿈/문장 경계에서 끊기
            int cut = findCut(text, start, end);
            chunks.add(text.substring(start, cut).trim());
            start = cut;
        }
        return chunks;
    }

    private int findCut(String text, int start, int end) {
        for (int i = end; i > start; i--) {
            char c = text.charAt(i - 1);
            if (c == '\n' || c == '.' || c == '。') return i;
        }
        return end;
    }
}