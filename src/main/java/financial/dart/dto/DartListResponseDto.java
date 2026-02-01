package financial.dart.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record DartListResponseDto(
        String status,
        String message,
        @JsonProperty("page_no") Integer pageNo,
        @JsonProperty("page_count") Integer pageCount,
        @JsonProperty("total_count") Integer totalCount,
        @JsonProperty("total_page") Integer totalPage,
        List<Item> list
) {
    public record Item(
            @JsonProperty("corp_code") String corpCode,
            @JsonProperty("corp_name") String corpName,
            @JsonProperty("stock_code") String stockCode,
            @JsonProperty("corp_cls") String corpCls,
            @JsonProperty("report_nm") String reportNm,
            @JsonProperty("rcept_no") String rceptNo,
            @JsonProperty("flr_nm") String flrNm,
            @JsonProperty("rcept_dt") String rceptDt,
            String rm
    ) {}
}