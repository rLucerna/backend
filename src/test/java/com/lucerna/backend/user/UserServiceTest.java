package com.lucerna.backend.user;

import com.lucerna.backend.common.exception.BusinessException;
import com.lucerna.backend.common.exception.ErrorCode;
import com.lucerna.backend.user.dto.UserMeResponse;
import com.lucerna.backend.user.entity.Lodge;
import com.lucerna.backend.user.entity.User;
import com.lucerna.backend.user.repository.LodgeRepository;
import com.lucerna.backend.user.repository.UserRepository;
import com.lucerna.backend.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LodgeRepository lodgeRepository;

    private static final String KEYCLOAK_ID = "kc-sub-123";
    private static final String EMAIL = "test@example.com";

    @Test
    @DisplayName("기존 유저 조회 시 isNewUser=false, save 미호출")
    void existingUser_returnsIsNewUserFalse() {
        User user = createUserWithId(1L);
        given(userRepository.findByKeycloakId(KEYCLOAK_ID)).willReturn(Optional.of(user));
        given(lodgeRepository.findByUserId(1L)).willReturn(Optional.empty());

        UserMeResponse response = userService.getOrCreateUser(KEYCLOAK_ID, EMAIL);

        assertThat(response.isNewUser()).isFalse();
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("기존 유저 조회 시 lastLoginAt 갱신")
    void existingUser_updatesLastLogin() {
        User user = createUserWithId(1L);
        given(userRepository.findByKeycloakId(KEYCLOAK_ID)).willReturn(Optional.of(user));
        given(lodgeRepository.findByUserId(1L)).willReturn(Optional.empty());

        userService.getOrCreateUser(KEYCLOAK_ID, EMAIL);

        assertThat(user.getLastLoginAt()).isNotNull();
    }

    @Test
    @DisplayName("기존 유저 + Lodge 있음 → Lodge 데이터 포함")
    void existingUser_withLodge_includesLodgeData() {
        User user = createUserWithId(1L);
        Lodge lodge = createLodge(10L, 1L, "산책하는 고양이");
        given(userRepository.findByKeycloakId(KEYCLOAK_ID)).willReturn(Optional.of(user));
        given(lodgeRepository.findByUserId(1L)).willReturn(Optional.of(lodge));

        UserMeResponse response = userService.getOrCreateUser(KEYCLOAK_ID, EMAIL);

        assertThat(response.lodge()).isNotNull();
        assertThat(response.lodge().nickname()).isEqualTo("산책하는 고양이");
    }

    @Test
    @DisplayName("기존 유저 + Lodge 없음 → lodge=null")
    void existingUser_withoutLodge_lodgeIsNull() {
        User user = createUserWithId(1L);
        given(userRepository.findByKeycloakId(KEYCLOAK_ID)).willReturn(Optional.of(user));
        given(lodgeRepository.findByUserId(1L)).willReturn(Optional.empty());

        UserMeResponse response = userService.getOrCreateUser(KEYCLOAK_ID, EMAIL);

        assertThat(response.lodge()).isNull();
    }

    @Test
    @DisplayName("신규 유저 JIT 생성 → save 호출, isNewUser=true")
    void newUser_jitCreation_savesAndReturnsNewUser() {
        given(userRepository.findByKeycloakId(KEYCLOAK_ID)).willReturn(Optional.empty());
        given(userRepository.save(any(User.class))).willAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            setField(saved, "id", 1L);
            return saved;
        });

        UserMeResponse response = userService.getOrCreateUser(KEYCLOAK_ID, EMAIL);

        assertThat(response.isNewUser()).isTrue();
        assertThat(response.lodge()).isNull();
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("신규 유저 status=ACTIVE")
    void newUser_statusIsActive() {
        given(userRepository.findByKeycloakId(KEYCLOAK_ID)).willReturn(Optional.empty());
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        UserMeResponse response = userService.getOrCreateUser(KEYCLOAK_ID, EMAIL);

        assertThat(response.status()).isEqualTo(com.lucerna.backend.user.entity.UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("email이 null이면 USER_EMAIL_NOT_FOUND 예외")
    void nullEmail_throwsException() {
        assertThatThrownBy(() -> userService.getOrCreateUser(KEYCLOAK_ID, null))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.USER_EMAIL_NOT_FOUND);
    }

    // === 헬퍼 메서드 ===

    private User createUserWithId(Long id) {
        User user = User.createNewUser(KEYCLOAK_ID, EMAIL);
        setField(user, "id", id);
        return user;
    }

    private Lodge createLodge(Long id, Long userId, String nickname) {
        Lodge lodge = newInstance(Lodge.class);
        setField(lodge, "id", id);
        setField(lodge, "userId", userId);
        setField(lodge, "nickname", nickname);
        return lodge;
    }

    @SuppressWarnings("unchecked")
    private <T> T newInstance(Class<T> clazz) {
        try {
            var constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
