package financial.dart.vector.service;

import java.util.ArrayList;
import java.util.List;

public class TextChunker {

    /**
     * 간단/안정 버전:
     * - 글자수 기준으로 자름 (한국어에 꽤 잘 맞습니다)
     * - 문단 경계(\n\n) 우선, 없으면 강제 split
     */
    public static List<String> chunkByChars(String text, int chunkSize, int overlap) {
        if (text == null) return List.of();
        String normalized = text.trim();
        if (normalized.isEmpty()) return List.of();

        List<String> chunks = new ArrayList<>();

        int start = 0;
        int n = normalized.length();

        while (start < n) {
            int end = Math.min(n, start + chunkSize);

            // 가능하면 문단/문장 경계로 끊기
            int cut = findBestCut(normalized, start, end);
            if (cut <= start) cut = end;

            String piece = normalized.substring(start, cut).trim();
            if (!piece.isEmpty()) chunks.add(piece);

            start = Math.max(cut - overlap, cut); // overlap 적용
            if (start == cut) {
                // overlap이 0인 경우
                start = cut;
            }
        }

        return chunks;
    }

    private static int findBestCut(String s, int start, int end) {
        // 1) 문단 경계 우선
        int p = s.lastIndexOf("\n\n", end);
        if (p >= start + 100) return p;

        // 2) 줄바꿈
        p = s.lastIndexOf('\n', end);
        if (p >= start + 100) return p;

        // 3) 문장 경계(한국어/영문)
        p = lastIndexOfAny(s, end, "다.", "요.", ". ", "。", "…", "!");
        if (p >= start + 100) return p + 1;

        return -1;
    }

    private static int lastIndexOfAny(String s, int fromIndex, String... tokens) {
        int best = -1;
        for (String t : tokens) {
            int idx = s.lastIndexOf(t, fromIndex);
            if (idx > best) best = idx;
        }
        return best;
    }
}