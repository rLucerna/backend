package com.lucerna.backend.blog.controller;

import com.lucerna.backend.blog.dto.NotebookCreateRequest;
import com.lucerna.backend.blog.dto.NotebookResponse;
import com.lucerna.backend.blog.service.NotebookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // "여기는 화면(HTML)이 아니라 데이터(JSON)만 반환하는 API 전용 입구입니다!" 선언
@RequestMapping("/api/v1/notebooks") // 이 입구의 기본 주소는 '/api/v1/notebooks'로 통일!
@RequiredArgsConstructor
public class NotebookController {

    private final NotebookService notebookService; // 웨이터가 주방장(Service)을 부르기 위해 대기 중

    // POST 방식의 요청이 오면 이 메서드가 실행됩니다.
    @PostMapping
    public ResponseEntity<NotebookResponse> createNotebook(@RequestBody NotebookCreateRequest request) {

        // 1. 웨이터(Controller)가 손님(Postman)의 주문서(@RequestBody)를 받아서 주방장(Service)에게 전달!
        NotebookResponse response = notebookService.createNotebook(request);

        // 2. 주방장이 요리를 성공적으로 마치면, 상태 코드 '201 Created(생성됨)'와 함께 완성된 수첩 데이터를 서빙!
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<NotebookResponse>> getNotebooks(@RequestParam Long lodgeId) {
        List<NotebookResponse> responses = notebookService.getNotebooks(lodgeId);
        return ResponseEntity.ok(responses);
    }
}