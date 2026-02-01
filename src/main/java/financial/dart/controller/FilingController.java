package financial.dart.controller;

import financial.dart.domain.CorpFiling;
import financial.dart.service.FilingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/corps")
public class FilingController {

    private final FilingService filingService;

    public FilingController(FilingService filingService) {
        this.filingService = filingService;
    }

    // 온디맨드로 DART 호출 + 저장
    @PostMapping("/{corpCode}/filing/latest")
    public ResponseEntity<?> fetchLatest(@PathVariable String corpCode) {
        CorpFiling saved = filingService.fetchAndSaveLatestBusinessReport(corpCode);
        return ResponseEntity.ok(saved);
    }

    // 저장된 캐시 조회
    @GetMapping("/{corpCode}/filing/latest")
    public ResponseEntity<?> getLatest(@PathVariable String corpCode) {
        CorpFiling cached = filingService.getCachedLatest(corpCode);
        return ResponseEntity.ok(cached);
    }
}