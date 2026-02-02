package financial.dart.controller;

import financial.dart.domain.UpcomingIpo;
import financial.dart.service.UpcomingIpoService;
import financial.dart.service.UpcomingIpoSimilarService;
import financial.dart.vector.dto.UpcomingIpoSimilarResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/upcoming-ipo")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"}, allowedHeaders = "*")
public class UpcomingIpoController {

    private final UpcomingIpoService upcomingIpoService;
    private final UpcomingIpoSimilarService upcomingIpoSimilarService;

    public UpcomingIpoController(
            UpcomingIpoService upcomingIpoService,
            UpcomingIpoSimilarService upcomingIpoSimilarService
    ) {
        this.upcomingIpoService = upcomingIpoService;
        this.upcomingIpoSimilarService = upcomingIpoSimilarService;
    }

    @PostMapping("/refresh")
    public ResponseEntity<List<UpcomingIpo>> refresh() {
        return ResponseEntity.ok(upcomingIpoService.refreshFrom38());
    }

    @GetMapping
    public ResponseEntity<List<UpcomingIpo>> list() {
        return ResponseEntity.ok(upcomingIpoService.listAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UpcomingIpo> get(@PathVariable Long id) {
        return ResponseEntity.ok(upcomingIpoService.getById(id));
    }

    @GetMapping("/{id}/similar")
    public ResponseEntity<UpcomingIpoSimilarResponse> similar(@PathVariable Long id) {
        return ResponseEntity.ok(upcomingIpoSimilarService.findSimilar(id));
    }
}
