package ListedCorp;

public record ListedRow(
        String corpName,
        String market,
        String stockCode,
        String industry,
        String mainProducts,
        String listedDateRaw,
        String fiscalMonth,
        String ceoName,
        String homepage,
        String region
) {}