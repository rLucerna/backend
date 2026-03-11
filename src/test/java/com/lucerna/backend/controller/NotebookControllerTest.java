package com.lucerna.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucerna.backend.blog.entity.Notebook;
import com.lucerna.backend.blog.entity.VisibilityStatus;
import com.lucerna.backend.blog.repository.NotebookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.hasSize;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // 🔥 아주 중요! 테스트가 끝나면 DB에 넣었던 데이터를 깔끔하게 롤백(삭제)해 줍니다.
class NotebookControllerTest {

    @Autowired
    MockMvc mockMvc; // 포스트맨 역할을 대신해 줄 가짜 클라이언트

    @Autowired
    ObjectMapper objectMapper; // 객체를 JSON으로 바꿔주는 마법사

    @Autowired
    NotebookRepository notebookRepository; // DB 창고 관리자

    @BeforeEach
    void setUp(){
        notebookRepository.deleteAllInBatch();//DB 수첩 창고 싹 비우기
    }

    @Test
    @DisplayName("1. 수첩 생성 시 정상적으로 201 응답을 반환한다.")
    void createNotebook_Success() throws Exception {
        // given (준비): 보낼 JSON 데이터 만들기 (단순 문자열로 짰습니다)
        String jsonBody = """
                {
                  "lodgeId": 1,
                  "name": "새로운 테스트 수첩",
                  "colorCode": "#000000",
                  "displayOrder": 1,
                  "visibilityStatus": "ALL"
                }
                """;

        // when & then (실행 & 검증): /api/v1/notebooks 로 POST 날리고 결과 확인
        mockMvc.perform(post("/api/v1/notebooks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isCreated()) // 201 상태 코드가 나와야 패스!
                .andExpect(jsonPath("$.name").value("새로운 테스트 수첩")); // 이름이 똑같이 들어가야 패스!
    }

    @Test
    @DisplayName("2. 수첩 생성 시 이름이 중복되면 409 에러를 반환한다.")
    void createNotebook_DuplicateName_Throws409() throws Exception {
        // given (준비): DB에 이미 수첩을 하나 억지로 넣어둡니다.
        Notebook existingNotebook = Notebook.builder()
                .lodgeId(1L)
                .name("중복될 수첩 이름")
                .colorCode("#FFFFFF")
                .visibilityStatus(VisibilityStatus.ALL)
                .build();
        notebookRepository.save(existingNotebook);

        // 똑같은 이름("중복될 수첩 이름")으로 새 수첩을 만들려고 시도합니다.
        String jsonBody = """
                {
                  "lodgeId": 1,
                  "name": "중복될 수첩 이름",
                  "colorCode": "#111111",
                  "displayOrder": 2,
                  "visibilityStatus": "ALL"
                }
                """;

        // when & then (실행 & 검증): 409 Conflict 에러가 터져야 패스!
        mockMvc.perform(post("/api/v1/notebooks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isConflict()); // 409(Conflict)가 나오기를 기대함!
    }

    @Test
    @DisplayName("3. 수첩 목록 조회 시 비공개(PRIVATE) 수첩은 필터링되어 보이지 않는다.")
    void getNotebooks_FilterPrivate() throws Exception {
        // given (준비): DB에 롯지 1번으로 공개(ALL) 수첩 1개, 비공개(PRIVATE) 수첩 1개를 넣습니다.
        notebookRepository.save(Notebook.builder().lodgeId(1L).name("공개 수첩").visibilityStatus(VisibilityStatus.ALL).build());
        notebookRepository.save(Notebook.builder().lodgeId(1L).name("비공개 수첩").visibilityStatus(VisibilityStatus.PRIVATE).build());

        // when & then (실행 & 검증): GET 요청 날리기
        mockMvc.perform(get("/api/v1/notebooks")
                        .param("lodgeId", "1"))
                .andExpect(status().isOk()) // 200 성공해야 함
                .andExpect(jsonPath("$", hasSize(1))) // 2개를 넣었지만 응답(리스트) 사이즈는 1개여야 패스! (비공개가 빠졌으니까)
                .andExpect(jsonPath("$[0].name").value("공개 수첩")); // 그 1개의 이름은 '공개 수첩'이어야 패스!
    }
}