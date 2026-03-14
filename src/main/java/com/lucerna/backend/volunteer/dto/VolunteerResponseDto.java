package com.lucerna.backend.volunteer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class VolunteerResponseDto {
    private String resultCode;    // 결과 코드 (0000 = 성공)
    private String resultMsg;     // 결과 메시지
    private int numOfRows;        // 한 페이지 결과 수
    private int pageNo;           // 현재 페이지 번호
    private int totalCount;       // 전체 데이터 수
    private List<VolunteerItem> volunteers;  // 봉사활동 목록

    @Getter
    @Setter
    public static class VolunteerItem {
        private String progrmRegistNo;  // 프로그램 등록 번호
        private String progrmSj;        // 프로그램 제목
        private String nanmmbyNm;       // 모집 기관명
        private String progrmBgnde;     // 봉사 시작일
        private String progrmEndde;     // 봉사 종료일
        private String actPlace;        // 봉사 장소
        private String actBeginTm;      // 봉사 시작 시간
        private String actEndTm;        // 봉사 종료 시간
        private String noticeBgnde;     // 모집 시작일
        private String noticeEndde;     // 모집 종료일
        private String adultPosblAt;    // 성인 가능 여부
        private String yngbgsPosblAt;   // 청소년 가능 여부
        private String sidoCd;          // 시/도 코드
        private String gugunCd;         // 시/군/구 코드
        private String srvcClCode;      // 서비스 분류
        private String url;             // 상세 페이지 URL
    }
    @Getter @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ApiResponse {
        private ApiResponseWrapper response;
    }

    @Getter @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ApiResponseWrapper {
        private ApiHeader header;
        private ApiBody body;
    }

    @Getter @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ApiHeader {
        private String resultCode;
        private String resultMsg;
    }

    @Getter @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ApiBody {
        private Object items;
        private int numOfRows;
        private int pageNo;
        private int totalCount;
    }
}
