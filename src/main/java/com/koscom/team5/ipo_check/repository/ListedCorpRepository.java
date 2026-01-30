package com.koscom.team5.ipo_check.repository;

import com.koscom.team5.ipo_check.domain.ListedCorp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ListedCorpRepository extends JpaRepository<ListedCorp, Long> {

    boolean existsByStockCode(String stockCode);

    @Query("select l.stockCode from ListedCorp l")
    List<String> findAllStockCodes();
}
