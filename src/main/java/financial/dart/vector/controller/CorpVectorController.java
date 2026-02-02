package financial.dart.vector.controller;

import financial.dart.vector.dto.IndexResultDto;
import financial.dart.vector.service.CorpVectorIndexService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/vector")
public class CorpVectorController {

    private final CorpVectorIndexService corpVectorIndexService;

    public CorpVectorController(CorpVectorIndexService corpVectorIndexService) {
        this.corpVectorIndexService = corpVectorIndexService;
    }

    @PostMapping("/index/{corpCode}")
    public IndexResultDto indexLatest(@PathVariable String corpCode) {
        return corpVectorIndexService.indexLatest(corpCode);
    }
}