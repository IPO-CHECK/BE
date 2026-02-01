package financial.dart.service;

import financial.dart.client.DartClient;
import financial.dart.domain.CorpFiling;
import financial.dart.dto.DartListResponseDto;
import financial.dart.repository.CorpFilingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;

@Service
public class FilingService {

    private final DartClient dartClient;
    private final CorpFilingRepository corpFilingRepository;

    public FilingService(DartClient dartClient, CorpFilingRepository corpFilingRepository) {
        this.dartClient = dartClient;
        this.corpFilingRepository = corpFilingRepository;
    }

    @Transactional
    public CorpFiling fetchAndSaveLatestBusinessReport(String corpCode) {
        DartListResponseDto resp = dartClient.fetchBusinessReportList(corpCode);

        if (resp == null) {
            throw new IllegalStateException("DART list.json 응답이 null 입니다. corpCode=" + corpCode);
        }
        if (!"000".equals(resp.status())) {
            throw new IllegalStateException("DART API 오류: status=" + resp.status() + ", message=" + resp.message());
        }
        if (resp.list() == null || resp.list().isEmpty()) {
            throw new IllegalStateException("DART 사업보고서 목록이 비어있습니다. corpCode=" + corpCode);
        }

        // pblntf_ty=A & detail=A001로 가져오더라도 report_nm에 사업보고서 외 공시가 섞일 수 있어,
        // 최종적으로 report_nm이 "사업보고서"인 것만 골라 최신 rcept_dt 선택
        DartListResponseDto.Item latest = resp.list().stream()
                .filter(it -> it.reportNm() != null && it.reportNm().startsWith("사업보고서"))
                .max(Comparator.comparing(DartListResponseDto.Item::rceptDt))
                .orElseThrow(() -> new IllegalStateException("사업보고서 항목이 없습니다. corpCode=" + corpCode));

        CorpFiling filing = corpFilingRepository
                .findTopByCorpCodeOrderByRceptDtDesc(corpCode)
                .filter(existing -> existing.getRceptNo().equals(latest.rceptNo()))
                .orElseGet(() -> new CorpFiling(corpCode, latest.rceptNo()));

        filing.setReportNm(latest.reportNm());
        filing.setRceptDt(latest.rceptDt());
        filing.setRm(latest.rm());

        return corpFilingRepository.save(filing);
    }

    @Transactional(readOnly = true)
    public CorpFiling getCachedLatest(String corpCode) {
        return corpFilingRepository.findTopByCorpCodeOrderByRceptDtDesc(corpCode)
                .orElseThrow(() -> new IllegalStateException("캐시된 최신 사업보고서가 없습니다. corpCode=" + corpCode));
    }
}