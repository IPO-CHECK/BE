package com.koscom.team5.ipo_check.controller;

import com.koscom.team5.ipo_check.domain.ListedCorp;
import com.koscom.team5.ipo_check.repository.ListedCorpRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"}, allowedHeaders = "*")
public class ListedCorpApiController {

    private static final int DEFAULT_SIZE = 10;

    private final ListedCorpRepository listedCorpRepository;

    public ListedCorpApiController(ListedCorpRepository listedCorpRepository) {
        this.listedCorpRepository = listedCorpRepository;
    }

    /**
     * 상장기업 목록 조회 (페이징, 한 페이지 10개)
     */
    @GetMapping("/listed-corps")
    public ResponseEntity<Map<String, Object>> getListedCorps(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        size = size <= 0 ? DEFAULT_SIZE : Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, size);
        Page<ListedCorp> result = listedCorpRepository.findAll(pageable);

        Map<String, Object> body = new HashMap<>();
        body.put("content", result.getContent());
        body.put("totalPages", result.getTotalPages());
        body.put("totalElements", result.getTotalElements());
        body.put("number", result.getNumber());
        body.put("size", result.getSize());
        body.put("first", result.isFirst());
        body.put("last", result.isLast());

        return ResponseEntity.ok(body);
    }
}
