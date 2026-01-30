//package financial.dart.Test.scheduler;
//
//import financial.dart.Test.ImportResultDto;
//import financial.dart.Test.KindListedCorpImportService;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//@Component
//public class KindListedCorpScheduler {
//
//    private static final Logger log = LoggerFactory.getLogger(KindListedCorpScheduler.class);
//
//    private final KindListedCorpImportService importService;
//
//    public KindListedCorpScheduler(KindListedCorpImportService importService) {
//        this.importService = importService;
//    }
//
//    // ✅ 매일 00:00 (Asia/Seoul 기준)
//    @Scheduled(cron = "*/5 * * * * *", zone = "Asia/Seoul")
//    public void runDailyImport() {
//        log.info("[KIND Import] start");
//        ImportResultDto result = importService.downloadParseAndSave(null);
//        log.info("[KIND Import] done - parsed={}, inserted={}, skipped={}",
//                result.parsedRows, result.insertedRows, result.skippedRows);
//    }
//}