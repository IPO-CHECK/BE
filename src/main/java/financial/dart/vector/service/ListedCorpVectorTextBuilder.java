package financial.dart.vector.service;

import financial.dart.domain.ListedCorp;
import org.springframework.stereotype.Component;

@Component
public class ListedCorpVectorTextBuilder {

    public String build(ListedCorp corp) {
        String industry = safe(corp.getIndustry());
        String products = safe(corp.getMainProducts());

        // 너무 길면 컷 (선택)
        if (products.length() > 3000) {
            products = products.substring(0, 3000);
        }

        StringBuilder sb = new StringBuilder();
        if (!industry.isBlank()) {
            sb.append("[INDUSTRY]\n").append(industry.trim()).append("\n\n");
        }
        if (!products.isBlank()) {
            sb.append("[MAIN_PRODUCTS]\n").append(products.trim()).append("\n");
        }
        return sb.toString().trim();
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}