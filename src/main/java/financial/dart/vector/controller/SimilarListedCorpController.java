package financial.dart.vector.controller;

import financial.dart.vector.dto.SimilarListedCorpRequest;
import financial.dart.vector.dto.SimilarListedCorpResult;
import financial.dart.vector.service.SimilarListedCorpSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SimilarListedCorpController {

    private final SimilarListedCorpSearchService service;

    public SimilarListedCorpController(SimilarListedCorpSearchService service) {
        this.service = service;
    }

    @PostMapping("/api/vector/similar-listed")
    public ResponseEntity<List<SimilarListedCorpResult>> similarListed(
            @RequestBody SimilarListedCorpRequest request
    ) {
        List<SimilarListedCorpResult> results = service.search(
                request == null ? null : request.businessOverview(),
                request == null ? null : request.topK()
        );
        return ResponseEntity.ok(results);
    }
}
