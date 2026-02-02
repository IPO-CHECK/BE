package financial.dart.vector.domain;

import financial.dart.domain.ListedCorp;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ListedCorpVector {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listed_corp_id", nullable = false)
    private ListedCorp listedCorp;

    @Column(nullable = false)
    private Integer chunkIndex;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String vector; // ✅ JSON 문자열로 저장

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String text;   // chunk 원문도 같이 저장하면 디버깅/검증 쉬움
}