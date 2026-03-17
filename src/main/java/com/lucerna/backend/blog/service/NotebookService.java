package com.lucerna.backend.blog.service;

import com.lucerna.backend.blog.dto.NotebookCreateRequest;
import com.lucerna.backend.blog.dto.NotebookResponse;
import com.lucerna.backend.blog.entity.Notebook;
import com.lucerna.backend.blog.entity.VisibilityStatus;
import com.lucerna.backend.blog.repository.NotebookRepository;
import com.lucerna.backend.common.exception.BusinessException;
import com.lucerna.backend.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service //비즈니스 로직을 처리한다
@RequiredArgsConstructor // Repository를 자동으로 불러와 줌
public class NotebookService {

    private final NotebookRepository notebookRepository;

    @Transactional // 데이터베이스에 뭔가 저장하거나 수정할 때 꼭 붙이는 안전장치
    public NotebookResponse createNotebook(NotebookCreateRequest request) {

        // 1. 중복 검사 (비즈니스 로직의 핵심!)
        // " 오두막에 똑같은 이름의 수첩이 이미 있는지 확인"
        boolean isDuplicate = notebookRepository.existsByLodgeIdAndName(request.getLodgeId(), request.getName());

        if (isDuplicate) {
            throw new BusinessException(ErrorCode.NOTEBOOK_DUPLICATE_NAME);
        }

        // 2. 문제없으면 DTO 상자에 있는 데이터를 꺼내서 Entity, Notebook으로 조립!
        Notebook notebook = Notebook.builder()
                .lodgeId(request.getLodgeId())
                .name(request.getName())
                .colorCode(request.getColorCode())
                .displayOrder(request.getDisplayOrder())
                .visibilityStatus(request.getVisibilityStatus())
                .collectionSum(0) // 새 수첩이니까 수집 합은 0
                .build();

        // 3. Repository에 DB에 저장하라고 명령
        Notebook savedNotebook = notebookRepository.save(notebook);

        // 4. 저장 완료된 Entity를 다시 Response DTO 상자에 담아서 준비
        return NotebookResponse.from(savedNotebook);
    }

    @Transactional(readOnly = true)
    public List<NotebookResponse> getNotebooks(Long lodgeId) {
        // 1. DB에서 '비공개(PRIVATE)'가 아닌 수첩만 가져오기
        List<Notebook> notebooks = notebookRepository.findAllByLodgeIdAndVisibilityStatusNot(lodgeId, VisibilityStatus.PRIVATE);

        // 2. from 메서드를 써서 Entity를 DTO로 변환
        return notebooks.stream()
                .map(NotebookResponse::from)
                .toList();
    }

}