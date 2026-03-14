package com.lucerna.backend.volunteer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucerna.backend.volunteer.dto.VolunteerRequestDto;
import com.lucerna.backend.volunteer.dto.VolunteerResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VolunteerService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${volunteer.api.key}")
    private String serviceKey;

    @Value("${volunteer.api.base-url}")
    private String baseUrl;

    public VolunteerResponseDto getVolunteers(VolunteerRequestDto request) {
        String raw = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("http")
                        .host(baseUrl)
                        .path("/openapi/service/rest/VolunteerPartcptnService/getVltrPartcptnItem")
                        .queryParam("_type", "json")
                        .queryParam("serviceKey", serviceKey)
                        .queryParam("progrmBgnde", request.getProgrmBgnde())
                        .queryParam("numOfRows", request.getNumOfRows())
                        .queryParam("pageNo", request.getPageNo())
                        .queryParam("schCateGu", request.getSchCateGu())
                        .queryParamIfPresent("progrmEndde", Optional.ofNullable(request.getProgrmEndde()))
                        .queryParamIfPresent("keyword", Optional.ofNullable(request.getKeyword()))
                        .queryParamIfPresent("schSido", Optional.ofNullable(request.getSchSido()))
                        .queryParamIfPresent("schSign1", Optional.ofNullable(request.getSchSign1()))
                        .queryParamIfPresent("adultPosblAt", Optional.ofNullable(request.getAdultPosblAt()))
                        .queryParamIfPresent("yngbgsPosblAt", Optional.ofNullable(request.getYngbgsPosblAt()))
                        .queryParamIfPresent("actBeginTm", Optional.ofNullable(request.getActBeginTm()))
                        .queryParamIfPresent("actEndTm", Optional.ofNullable(request.getActEndTm()))
                        .queryParamIfPresent("actPlace", Optional.ofNullable(request.getActPlace()))
                        .queryParamIfPresent("nanmmbyNm", Optional.ofNullable(request.getNanmmbyNm()))
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
            VolunteerResponseDto.ApiResponse apiResponse =
                    objectMapper.readValue(raw, VolunteerResponseDto.ApiResponse.class);

            VolunteerResponseDto result = new VolunteerResponseDto();
            result.setResultCode(apiResponse.getResponse().getHeader().getResultCode());
            result.setResultMsg(apiResponse.getResponse().getHeader().getResultMsg());
            result.setNumOfRows(apiResponse.getResponse().getBody().getNumOfRows());
            result.setPageNo(apiResponse.getResponse().getBody().getPageNo());
            result.setTotalCount(apiResponse.getResponse().getBody().getTotalCount());

            Object itemsObj = apiResponse.getResponse().getBody().getItems();
            if (itemsObj instanceof java.util.Map) {
                java.util.Map<?, ?> itemsMap = (java.util.Map<?, ?>) itemsObj;
                Object itemObj = itemsMap.get("item");
                if (itemObj instanceof List) {
                    result.setVolunteers(objectMapper.convertValue(
                            itemObj,
                            objectMapper.getTypeFactory().constructCollectionType(
                                    List.class, VolunteerResponseDto.VolunteerItem.class)));
                } else if (itemObj != null) {
                    result.setVolunteers(List.of(objectMapper.convertValue(
                            itemObj, VolunteerResponseDto.VolunteerItem.class)));
                }
            }

            return result;
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {  // ← 이렇게 변경!
            throw new RuntimeException("응답 파싱 실패", e);
        }
    }
}