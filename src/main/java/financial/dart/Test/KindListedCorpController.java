package financial.dart.Test;

import financial.dart.Test.ImportResultDto;
import financial.dart.Test.KindCorpDownloadRequestDto;
import financial.dart.Test.KindListedCorpImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/kind/corps")
public class KindListedCorpController {

    private final KindListedCorpImportService importService;

    public KindListedCorpController(KindListedCorpImportService importService) {
        this.importService = importService;
    }

    /**
     * POST /api/kind/corps/import
     * - KIND에서 상장법인목록 "엑셀(실제 HTML)" 다운로드
     * - 파싱 후 DB 적재
     */
    @GetMapping("/import")
    public ResponseEntity<ImportResultDto> importCorps(@RequestBody(required = false) KindCorpDownloadRequestDto reqDto) {
        ImportResultDto result = importService.downloadParseAndSave(reqDto);
        return ResponseEntity.ok(result);
    }
}