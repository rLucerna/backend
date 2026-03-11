package com.lucerna.backend.blog.service;

import com.lucerna.backend.blog.dto.NotebookCreateRequest;
import com.lucerna.backend.blog.dto.NotebookResponse;
import com.lucerna.backend.blog.entity.Notebook;
import com.lucerna.backend.blog.entity.VisibilityStatus;
import com.lucerna.backend.blog.repository.NotebookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service // "나는 비즈니스 로직을 처리하는 주방장이야!" 선언
@RequiredArgsConstructor // 창고지기(Repository)를 자동으로 불러와 줌
public class NotebookService {

    private final NotebookRepository notebookRepository;

    @Transactional // 데이터베이스에 뭔가 저장하거나 수정할 때 꼭 붙이는 안전장치
    public NotebookResponse createNotebook(NotebookCreateRequest request) {

        // 1. 중복 검사 (비즈니스 로직의 핵심!)
        // "창고지기야, 이 오두막에 똑같은 이름의 수첩이 이미 있는지 확인해 봐!"
        boolean isDuplicate = notebookRepository.existsByLodgeIdAndName(request.getLodgeId(), request.getName());

        if (isDuplicate) {
            // 똑같은 이름이 있으면 요리(생성) 중단하고 에러 던지기! (나중에 409 Custom Exception으로 바꿀 예정)
            throw new IllegalArgumentException("이미 존재하는 수첩 이름입니다.");
        }

        // 2. 문제없으면 DTO 상자에 있는 데이터를 꺼내서 Entity 식재료(Notebook)로 조립!
        Notebook notebook = Notebook.builder()
                .lodgeId(request.getLodgeId())
                .name(request.getName())
                .colorCode(request.getColorCode())
                .displayOrder(request.getDisplayOrder())
                .visibilityStatus(request.getVisibilityStatus())
                .collectionSum(0) // 새 수첩이니까 수집 합은 0
                .build();

        // 3. 창고지기에게 DB에 저장하라고 명령!
        Notebook savedNotebook = notebookRepository.save(notebook);

        // 4. 저장 완료된 Entity를 다시 예쁜 Response DTO 상자에 담아서 서빙 준비!
        return NotebookResponse.from(savedNotebook);
    }

    @Transactional(readOnly = true)
    public List<NotebookResponse> getNotebooks(Long lodgeId) {
        // 1. DB에서 '비공개(PRIVATE)'가 아닌 수첩만 가져오기
        List<Notebook> notebooks = notebookRepository.findAllByLodgeIdAndVisibilityStatusNot(lodgeId, VisibilityStatus.PRIVATE);

        // 2. 성빈님이 만든 from 메서드를 써서 Entity를 DTO로 예쁘게 변환!
        return notebooks.stream()
                .map(NotebookResponse::from) // 🔥 바로 이 부분!
                .toList();
    }

}