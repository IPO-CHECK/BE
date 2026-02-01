package financial.dart.section.controller;

import financial.dart.section.service.CorpSectionMainXmlService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dart/sections")
public class CorpSectionController {

    private final CorpSectionMainXmlService service;

    public CorpSectionController(CorpSectionMainXmlService service) {
        this.service = service;
    }

    @GetMapping("/by-rcpno/{rcpNo}")
    public Response fetch(@PathVariable String rcpNo) {
        var pair = service.fetchSectionsByRcpNo(rcpNo);

        return new Response(
                rcpNo,
                pair.businessOverview().length(),
                pair.productService().length(),
                pair.businessOverview(),
                pair.productService()
        );
    }

    public record Response(
            String rcpNo,
            int businessOverviewLength,
            int productServiceLength,
            String businessOverview,
            String productService
    ) {}
}