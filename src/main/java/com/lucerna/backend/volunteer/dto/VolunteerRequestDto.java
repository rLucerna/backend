package com.lucerna.backend.volunteer.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VolunteerRequestDto {
    // 필수
    private String progrmBgnde;       // 봉사 시작일자 (예: 20231226)

    // 선택
    private String progrmEndde;       // 봉사 종료일자
    private String adultPosblAt;      // 성인 가능 여부 (Y/N)
    private String yngbgsPosblAt;     // 청소년 가능 여부 (Y/N)
    private Integer numOfRows = 10;   // 한 페이지 결과 수 (기본값 10)
    private Integer pageNo = 1;       // 페이지 번호 (기본값 1)
    private String keyword;           // 검색 키워드
    private String schCateGu = "all"; // 검색 카테고리
    private String schSido;           // 시/도 코드
    private String schSign1;          // 시/군/구 코드
    private String actBeginTm;        // 봉사 시작 시간
    private String actEndTm;          // 봉사 종료 시간
    private String noticeBgnde;       // 모집 시작일
    private String noticeEndde;       // 모집 종료일
    private String actPlace;          // 봉사 장소
    private String nanmmbyNm;         // 모집 기관
}
