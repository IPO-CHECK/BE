package financial.dart.controller;

import financial.dart.service.ListedUniverseImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/import")
public class AdminImportController {

    private final ListedUniverseImportService importService;

    public AdminImportController(ListedUniverseImportService importService) {
        this.importService = importService;
    }

    @PostMapping("/listed-universe")
    public ResponseEntity<?> importListedUniverse() throws Exception {
        var result = importService.importFromClasspath();
        return ResponseEntity.ok(result);
    }
}