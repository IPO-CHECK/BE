package financial.dart.dto;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpcomingDto {

    private Long upcomingIpoId;
    private Long corpId;
    private String corpCode;
    private String corpName;
    private String market; // 시장 구분
    private String industry; // 업종
    private String underwriter; // 주관사
    private String subDate; // 청약일
    private String listDate; // 상장일
    private String status; // 진행상태
    private String price; // 공모가
}
