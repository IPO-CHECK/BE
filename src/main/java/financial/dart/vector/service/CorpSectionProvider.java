package financial.dart.vector.service;

public interface CorpSectionProvider {
    ExtractedSections fetchLatestByCorpCode(String corpCode);

    record ExtractedSections(
            String corpCode,
            String rcpNo,
            String businessOverview,
            String productService
    ) {}
}