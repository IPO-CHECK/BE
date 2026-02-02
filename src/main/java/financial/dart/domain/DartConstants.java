package financial.dart.domain;

public class DartConstants {
    // 손익계산서 (IS)
    public static final String REV = "ifrs-full_Revenue";                // 매출액
    public static final String OP = "dart_OperatingIncomeLoss";     // 영업이익
    public static final String NI = "ifrs-full_ProfitLoss";              // 당기순이익
    public static final String GP = "ifrs-full_GrossProfit";             // 매출총이익
    public static final String FIN_COST = "ifrs-full_FinanceCosts";    // 금융비용
    // 재무상태표 (BS)
    public static final String ASSETS = "ifrs-full_Assets";              // 자산총계
    public static final String LIAB = "ifrs-full_Liabilities";           // 부채총계
    public static final String EQUITY = "ifrs-full_Equity";              // 자본총계
    public static final String CAP_STOCK = "ifrs-full_IssuedCapital";    // 자본금
    public static final String CUR_ASSETS = "ifrs-full_CurrentAssets";   // 유동자산
    public static final String CUR_LIAB = "ifrs-full_CurrentLiabilities";// 유동부채
}
