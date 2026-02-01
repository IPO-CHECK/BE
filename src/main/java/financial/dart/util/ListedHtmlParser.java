package financial.dart.ListedCorp;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

@Component
public class ListedHtmlParser {

    /**
     * 상장법인목록.html은 EUC-KR 인코딩인 케이스가 많습니다.
     */
    public List<ListedRow> parse(Resource htmlResource) throws Exception {
        Document doc = Jsoup.parse(htmlResource.getInputStream(), Charset.forName("EUC-KR").name(), "");
        Elements rows = doc.select("table.bbs_tb tr");

        List<ListedRow> out = new ArrayList<>();
        for (int i = 1; i < rows.size(); i++) { // 0=헤더
            Elements tds = rows.get(i).select("td");
            if (tds.size() < 10) continue;

            // 컬럼 순서: 회사명/시장구분/종목코드/업종/주요제품/상장일/결산월/대표자명/홈페이지/지역
            out.add(new ListedRow(
                    tds.get(0).text().trim(),
                    tds.get(1).text().trim(),
                    tds.get(2).text().trim(),
                    tds.get(3).text().trim(),
                    tds.get(4).text().trim(),
                    tds.get(5).text().trim(),
                    tds.get(6).text().trim(),
                    tds.get(7).text().trim(),
                    tds.get(8).text().trim(),
                    tds.get(9).text().trim()
            ));
        }
        return out;
    }
}