package financial.dart.ListedCorp;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.xml.stream.*;
import java.io.InputStream;
import java.util.*;

@Component
public class CorpCodeXmlParser {

    public Map<String, List<CorpCodeRow>> parseToNameMap(Resource xmlResource) throws Exception {
        Map<String, List<CorpCodeRow>> map = new HashMap<>();

        XMLInputFactory factory = XMLInputFactory.newInstance();

        try (InputStream is = xmlResource.getInputStream()) {
            XMLStreamReader r = factory.createXMLStreamReader(is);

            String current = null;
            String corpCode = null;
            String corpName = null;
            String stockCode = null;
            String modifyDate = null;

            while (r.hasNext()) {
                int evt = r.next();

                if (evt == XMLStreamConstants.START_ELEMENT) {
                    current = r.getLocalName();
                }
                else if (evt == XMLStreamConstants.CHARACTERS) {
                    if (current == null) continue;

                    String txt = r.getText();
                    if (txt == null) continue;
                    txt = txt.trim();
                    if (txt.isEmpty()) continue;

                    switch (current) {
                        case "corp_code" -> corpCode = txt;
                        case "corp_name" -> corpName = txt;
                        case "stock_code" -> stockCode = txt;
                        case "modify_date" -> modifyDate = txt;
                    }
                }
                else if (evt == XMLStreamConstants.END_ELEMENT) {
                    String end = r.getLocalName();

                    // ✅ OpenDART CORPCODE.xml에서 한 기업은 <list> 단위
                    if ("list".equals(end)) {
                        if (corpCode != null && corpName != null) {
                            String key = NameNormalizer.norm(corpName);
                            CorpCodeRow row = new CorpCodeRow(
                                    corpCode,
                                    corpName,
                                    stockCode == null ? "" : stockCode.trim(),
                                    modifyDate == null ? "" : modifyDate.trim()
                            );
                            map.computeIfAbsent(key, k -> new ArrayList<>()).add(row);
                        }

                        // 다음 엔트리를 위해 초기화
                        corpCode = corpName = stockCode = modifyDate = null;
                    }

                    current = null;
                }
            }

            r.close();
        }

        return map;
    }
}