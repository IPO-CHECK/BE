package ListedCorp;

public class NameNormalizer {

    private NameNormalizer() {}

    public static String norm(String s) {
        if (s == null) return "";
        String x = s.trim();

        // 주식회사 표기 제거
        x = x.replace("㈜", "")
                .replace("(주)", "")
                .replace("주식회사", "")
                .replace("주)", "");

        // 괄호 내용 제거 (필요 없으면 주석 처리)
        x = x.replaceAll("\\(.*?\\)", "");

        // 특수문자 공백 치환
        x = x.replaceAll("[^0-9A-Za-z가-힣]", " ");
        x = x.replaceAll("\\s+", " ").trim();

        // 영문 포함 대비
        return x.toUpperCase();
    }
}