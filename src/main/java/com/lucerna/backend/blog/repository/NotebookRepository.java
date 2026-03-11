package com.lucerna.backend.blog.repository;

import com.lucerna.backend.blog.entity.Notebook;
import com.lucerna.backend.blog.entity.VisibilityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotebookRepository extends JpaRepository<Notebook, Long> {
    //1. 수첩 생성 시 이름 중복 체크용
    boolean existsByLodgeIdAndName(Long lodgeId, String name);

    //2. 새 코드: 수첩 목록 조회 시 비공개 필터링용
    List<Notebook> findAllByLodgeIdAndVisibilityStatusNot(Long lodgeId, VisibilityStatus visibilityStatus);
}
