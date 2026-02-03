package financial.dart.service;

import financial.dart.domain.UpcomingIpo;
import financial.dart.section.service.CorpSectionMainXmlService;
import financial.dart.vector.dto.SimilarListedCorpResult;
import financial.dart.vector.dto.UpcomingIpoSimilarResponse;
import financial.dart.vector.service.SimilarListedCorpSearchService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UpcomingIpoSimilarService {

    private final UpcomingIpoService upcomingIpoService;
    private final CorpSectionMainXmlService corpSectionMainXmlService;
    private final SimilarListedCorpSearchService similarSearchService;

    public UpcomingIpoSimilarService(
            UpcomingIpoService upcomingIpoService,
            CorpSectionMainXmlService corpSectionMainXmlService,
            SimilarListedCorpSearchService similarSearchService
    ) {
        this.upcomingIpoService = upcomingIpoService;
        this.corpSectionMainXmlService = corpSectionMainXmlService;
        this.similarSearchService = similarSearchService;
    }

    // UpcomingIpoSimilarResponse
    public List<Long> findSimilar(Long upcomingIpoId) {
        UpcomingIpo ipo = upcomingIpoService.getById(upcomingIpoId);

        String rceptNo = ipo.getRceptNo();
        if (rceptNo == null || rceptNo.isBlank()) {
            rceptNo = upcomingIpoService.fetchRceptNoByDetailUrl(ipo.getDetailUrl());
            if (rceptNo != null && !rceptNo.isBlank()) {
                ipo.updateRceptNo(rceptNo);
                upcomingIpoService.save(ipo);
            }
        }

        if (rceptNo == null || rceptNo.isBlank()) {
//            return new UpcomingIpoSimilarResponse("", List.of());
            return List.of();
        }

        var sections = corpSectionMainXmlService.fetchSectionsByRcpNo(rceptNo);
        String businessOverview = sections.businessOverview();

        String industrySummary = summarizeForIndustry(businessOverview);
        upcomingIpoService.updateIndustryIfEmpty(ipo, industrySummary);

        List<SimilarListedCorpResult> similar = similarSearchService.search(businessOverview, 10);

        return similar.stream()
                .map(SimilarListedCorpResult::corpId)
                .toList();
//        return new UpcomingIpoSimilarResponse(businessOverview, similar);
    }

    private String summarizeForIndustry(String text) {
        if (text == null) return "";
        String cleaned = text.replaceAll("\\s+", " ").trim();
        cleaned = cleaned.replaceAll("^\\d+\\s*\\.\\s*사업의\\s*개요\\s*", "");
        if (cleaned.isBlank()) return "";

        int idx = cleaned.indexOf("다.");
        if (idx > 0 && idx < 200) {
            return cleaned.substring(0, idx + 2).trim();
        }

        int maxLen = Math.min(cleaned.length(), 120);
        String snippet = cleaned.substring(0, maxLen).trim();
        if (cleaned.length() > maxLen) snippet = snippet + "...";
        return snippet;
    }
}
