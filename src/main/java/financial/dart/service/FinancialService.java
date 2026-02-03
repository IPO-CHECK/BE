package financial.dart.service;

import financial.dart.domain.Corporation;
import financial.dart.domain.DartConstants;
import financial.dart.domain.Financial;
import financial.dart.dto.FinancialsDto;
import financial.dart.repository.CorporationRepository;
import financial.dart.repository.FinancialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class FinancialService {

    private final FinancialRepository financialRepository;
    private final CorporationRepository corporationRepository;
    private final RestTemplate restTemplate;

    @Value("${dart.api-key}")
    private String apiKey;

    public FinancialsDto getFinancials(Long corpId) {
        List<Financial> financials = financialRepository.findByCorporationIdOrderByBsnsYearAsc(corpId);
        List<String> staticQuarters = List.of("22(1~4Q)", "23(1~4Q)", "24(1~4Q)", "25(1~3Q)");
        return FinancialsDto.builder()
                .quarters(staticQuarters)
                .revenue(getRevenueList(financials))
                .opProfit(getOpProfitList(financials))
                .netIncome(getNetIncomeList(financials))
                .build();
    }

    private List<Long> getRevenueList(List<Financial> financials) {
        List<Long> revenues = new ArrayList<>();
        for (Financial f : financials) {
            revenues.add(toMillion(f.getRevenue()));
        }
        return revenues;
    }

    private List<Long> getOpProfitList(List<Financial> financials) {
        List<Long> opProfits = new ArrayList<>();
        for (Financial f : financials) {
            opProfits.add(toMillion(f.getOp()));
        }
        return opProfits;
    }

    private List<Long> getNetIncomeList(List<Financial> financials) {
        List<Long> netIncomes = new ArrayList<>();
        for (Financial f : financials) {
            netIncomes.add(toMillion(f.getNi()));
        }
        return netIncomes;
    }


    // 가장 최근 분기 재무제표 조회
    public Financial findByCorporationId(Long corpId) {
        return financialRepository.findByCorporationId(corpId).orElse(null);
    }

    public Financial findByFinancialId(Long financialId) {
        return financialRepository.findById(financialId).orElse(null);
    }

    /**
     * 매출액, 자산총계, 자본총계 0.2배 ~ 5배 이내
     */
    public List<Financial> findSimilarCorporations(List<Long> corpIds, Financial financial, String year, int quarter) {
        return financialRepository.findByCorporationId(corpIds,
                financial.getRevenue(), financial.getTotalAssets(), financial.getTotalEquity(), year, convertToCode(quarter));
    }

    /**
     * 특정 기업의 특정 연도 1~4분기 데이터를 모두 수집하여 저장
     */
    public void syncQuarterlyData(String year) {
        List<Corporation> corporations = corporationRepository.findCorps();

        int success = 0;
        int fail = 0;
        int count = 1;

        for (Corporation corp : corporations) {
            try {
                singleSyncQuarterlyData(corp, year);
                success++;
                Thread.sleep(1000);
                log.info("진행도: {}/{} 완료", count++, corporations.size());
            } catch (Exception e) {
                fail++;
                log.error("실패 [{}]: {}", corp.getCorpName(), e.getMessage());
            }
        }
        log.info("배치 종료 - 성공: {}, 실패: {}", success, fail);
    }

    public void singleSyncQuarterlyData(Corporation corp, String year) {
        List<Map<String, String>> q1List = callApi(corp.getCorpCode(), year, "11013"); // 1분기
        List<Map<String, String>> q2List = callApi(corp.getCorpCode(), year, "11012"); // 반기
        List<Map<String, String>> q3List = callApi(corp.getCorpCode(), year, "11014"); // 3분기
        List<Map<String, String>> annualList = callApi(corp.getCorpCode(), year, "11011"); // 사업보고서(연간)

        List<Financial> batchList = new ArrayList<>();

        // 2. [1분기, 2분기, 3분기] 데이터 생성 (단순 매핑)
        if (!q1List.isEmpty()) batchList.add(mapToEntity(q1List, corp, year, "11013"));
        if (!q2List.isEmpty()) batchList.add(mapToEntity(q2List, corp, year, "11012"));
        if (!q3List.isEmpty()) batchList.add(mapToEntity(q3List, corp, year, "11014"));

        // 3. [4분기] 데이터 생성 (핵심 로직: 연간 - 3분기누적)
        if (!annualList.isEmpty() && !q3List.isEmpty()) {
            Financial q4Entity = calculateQ4(annualList, q3List, corp, year);
            batchList.add(q4Entity);
        }

        // 4. DB 일괄 저장
        if (!batchList.isEmpty()) {
            financialRepository.saveAll(batchList);
            log.info("처리 완료: {} ({})", corp.getCorpName(), corp.getCorpCode());
        } else {
            log.warn("데이터 없음: {} ({})", corp.getCorpName(), corp.getCorpCode());
        }

    }

    // --- [내부 로직 1] 일반 분기(1,2,3Q) 매핑 ---
    private Financial mapToEntity(List<Map<String, String>> list, Corporation corp, String year, String reportCode) {
        return Financial.builder()
                .corporation(corp)
                .bsnsYear(year)
                .reprtCode(reportCode)
                // IS (손익): thstrm_amount(당기 3개월치) 사용
                .revenue(getValue(list, DartConstants.REV, "thstrm_amount"))
                .op(getValue(list, DartConstants.OP, "thstrm_amount"))
                .ni(getValue(list, DartConstants.NI, "thstrm_amount"))
                .grossProfit(getValue(list, DartConstants.GP, "thstrm_amount"))
                .financeCosts(getValue(list, DartConstants.FIN_COST, "thstrm_amount"))
                // BS (재무): thstrm_amount(현재 잔액) 사용
                .totalAssets(getValue(list, DartConstants.ASSETS, "thstrm_amount"))
                .totalLiabilities(getValue(list, DartConstants.LIAB, "thstrm_amount"))
                .totalEquity(getValue(list, DartConstants.EQUITY, "thstrm_amount"))
                .capStock(getValue(list, DartConstants.CAP_STOCK, "thstrm_amount"))
                .curAssets(getValue(list, DartConstants.CUR_ASSETS, "thstrm_amount"))
                .curLiabilities(getValue(list, DartConstants.CUR_LIAB, "thstrm_amount"))
                // Prev (성장성): frmtrm_amount(전년 동기) 사용
                .prevRevenue(getValue(list, DartConstants.REV, "frmtrm_amount"))
                .prevOp(getValue(list, DartConstants.OP, "frmtrm_amount"))
                .prevNi(getValue(list, DartConstants.NI, "frmtrm_amount"))
                .prevTotalAssets(getValue(list, DartConstants.ASSETS, "frmtrm_amount"))
                .build();
    }

    // --- [내부 로직 2] 4분기 순수 데이터 계산 (연간 - 3Q누적) ---
    private Financial calculateQ4(List<Map<String, String>> annualList, List<Map<String, String>> q3List, Corporation corp, String year) {
        // BS(재무상태표)는 누적이 아니므로 연말 기준 잔액을 그대로 씁니다.
        // IS(손익계산서)만 뺍니다.

        long revAnnual = getValue(annualList, DartConstants.REV, "thstrm_amount");
        long revQ3Cum = getValue(q3List, DartConstants.REV, "thstrm_add_amount"); // 3Q 누적

        long opAnnual = getValue(annualList, DartConstants.OP, "thstrm_amount");
        long opQ3Cum = getValue(q3List, DartConstants.OP, "thstrm_add_amount");

        long niAnnual = getValue(annualList, DartConstants.NI, "thstrm_amount");
        long niQ3Cum = getValue(q3List, DartConstants.NI, "thstrm_add_amount");

        long gpAnnual = getValue(annualList, DartConstants.GP, "thstrm_amount");
        long gpQ3Cum = getValue(q3List, DartConstants.GP, "thstrm_add_amount");

        long fcAnnual = getValue(annualList, DartConstants.FIN_COST, "thstrm_amount");
        long fcQ3Cum = getValue(q3List, DartConstants.FIN_COST, "thstrm_add_amount");

        long prevRevQ4 = getValue(annualList, DartConstants.REV, "frmtrm_amount")
                - getValue(q3List, DartConstants.REV, "frmtrm_add_amount");

        long prevOpQ4 = getValue(annualList, DartConstants.OP, "frmtrm_amount")
                - getValue(q3List, DartConstants.OP, "frmtrm_add_amount");

        long prevNiQ4 = getValue(annualList, DartConstants.NI, "frmtrm_amount")
                - getValue(q3List, DartConstants.NI, "frmtrm_add_amount");

        return Financial.builder()
                .corporation(corp)
                .bsnsYear(year)
                .reprtCode("11011") // 편의상 11011을 4분기 코드로 사용 (또는 별도 코드 정의)
                // 계산된 4분기 순수 값
                .revenue(revAnnual - revQ3Cum)
                .op(opAnnual - opQ3Cum)
                .ni(niAnnual - niQ3Cum)
                .grossProfit(gpAnnual - gpQ3Cum)
                .financeCosts(fcAnnual - fcQ3Cum)
                // BS는 연말 잔액 그대로
                .totalAssets(getValue(annualList, DartConstants.ASSETS, "thstrm_amount"))
                .totalLiabilities(getValue(annualList, DartConstants.LIAB, "thstrm_amount"))
                .totalEquity(getValue(annualList, DartConstants.EQUITY, "thstrm_amount"))
                .capStock(getValue(annualList, DartConstants.CAP_STOCK, "thstrm_amount"))
                .curAssets(getValue(annualList, DartConstants.CUR_ASSETS, "thstrm_amount"))
                .curLiabilities(getValue(annualList, DartConstants.CUR_LIAB, "thstrm_amount"))
                .prevRevenue(prevRevQ4)
                .prevOp(prevOpQ4)
                .prevNi(prevNiQ4)
                .prevTotalAssets(getValue(annualList, DartConstants.ASSETS, "frmtrm_amount"))
                .build();
    }

    // --- [유틸] JSON 리스트에서 특정 계정의 금액 추출 ---
    private Long getValue(List<Map<String, String>> list, String accountId, String fieldName) {
        for (Map<String, String> item : list) {
            if (accountId.equals(item.get("account_id"))) {
                String val = item.get(fieldName);
                if (val == null || val.trim().isEmpty() || "-".equals(val)) return 0L;
                return Long.parseLong(val.replace(",", ""));
            }
        }
        return 0L; // 못 찾으면 0 반환
    }

    // --- [유틸] API 호출 ---
    private List<Map<String, String>> callApi(String corpCode, String year, String reportCode) {
        List<Map<String, String>> result = fetch(corpCode, year, reportCode, "CFS");
        if (result.isEmpty()) {
            result = fetch(corpCode, year, reportCode, "OFS");
        }
        return result;
    }

    // 중복 제거를 위한 내부 호출 메서드
    private List<Map<String, String>> fetch(String corpCode, String year, String reportCode, String div) {
        String url = "https://opendart.fss.or.kr/api/fnlttSinglAcntAll.json?crtfc_key=" + apiKey
                + "&corp_code=" + corpCode
                + "&bsns_year=" + year
                + "&reprt_code=" + reportCode
                + "&fs_div=" + div;
        try {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
            }
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.get("list") != null) {
                return (List<Map<String, String>>) response.get("list");
            }
        } catch (Exception e) {
            log.warn("API 호출 실패 [{}]: {}-{} ({})", div, corpCode, reportCode, e.getMessage());
        }
        return Collections.emptyList();
    }

    private void sleep() {
        try {
            Thread.sleep(300); // 300~400ms
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String convertToCode(int quarter) {
        if (quarter == 1) return "11013";
        else if (quarter == 2) return "11012";
        else if (quarter == 3) return "11014";
        else return "11011";
    }

    private Long toMillion(Long won) {
        if (won == null) return 0L;
        // 1,000,000으로 나누어 '백만 원' 단위로 변환
        return won / 1_000_000L;
    }
}
