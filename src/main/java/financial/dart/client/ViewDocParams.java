//package financial.dart.client;
//
///**
// * DART viewer.do 호출용 파라미터 DTO
// */
//public record ViewDocParams(
//        String rcpNo,
//        String dcmNo,
//        String eleId,
//        String offset,
//        String length,
//        String dtd
//) {
//    /** 문서 전체 조회용 (관례: offset=0, length=0) */
//    public ViewDocParams withFullDoc() {
//        return new ViewDocParams(
//                this.rcpNo,
//                this.dcmNo,
//                this.eleId,
//                "0",
//                "0",
//                this.dtd
//        );
//    }
//}