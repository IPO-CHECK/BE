package financial.dart.controller;

import financial.dart.vector.service.ListedCorpVectorIndexService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ListedCorpVectorAdminController {

    private final ListedCorpVectorIndexService service;


    public ListedCorpVectorAdminController(ListedCorpVectorIndexService service) {
        this.service = service;
    }

    // 전체 상장기업 인덱싱
    @PostMapping("/admin/vector/listed-corps/index")
    public void indexListed() {
        service.indexAll();
    }

    // 특정 기업만 인덱싱 (디버깅용)
    @PostMapping("/admin/vector/listed-corps/index/{corpCode}")
    public ListedCorpVectorIndexService.IndexResult indexOne(
            @PathVariable String corpCode
    ) {
        return service.indexOneByCorpCodeOrStockCode(corpCode);
    }
}