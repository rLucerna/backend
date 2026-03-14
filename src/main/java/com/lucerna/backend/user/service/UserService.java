package com.lucerna.backend.user.service;

import com.lucerna.backend.common.exception.BusinessException;
import com.lucerna.backend.common.exception.ErrorCode;
import com.lucerna.backend.user.dto.LodgeSummary;
import com.lucerna.backend.user.dto.UserMeResponse;
import com.lucerna.backend.user.entity.User;
import com.lucerna.backend.user.repository.LodgeRepository;
import com.lucerna.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 조회 및 JIT 생성 서비스.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final LodgeRepository lodgeRepository;

    /**
     * keycloakId로 사용자 조회. 없으면 JIT 생성.
     * 기존 유저: lastLoginAt 갱신 + Lodge 조회
     * 신규 유저: User 생성 (Lodge 없음)
     */
    @Transactional
    public UserMeResponse getOrCreateUser(String keycloakId, String email) {
        if (email == null || email.isBlank()) {
            throw new BusinessException(ErrorCode.USER_EMAIL_NOT_FOUND);
        }

        return userRepository.findByKeycloakId(keycloakId)
                .map(this::handleExistingUser)
                .orElseGet(() -> handleNewUser(keycloakId, email));
    }

    private UserMeResponse handleExistingUser(User user) {
        user.updateLastLogin();

        LodgeSummary lodge = lodgeRepository.findByUserId(user.getId())
                .map(LodgeSummary::from)
                .orElse(null);

        return UserMeResponse.of(user, false, lodge);
    }

    private UserMeResponse handleNewUser(String keycloakId, String email) {
        User newUser = User.createNewUser(keycloakId, email);
        userRepository.save(newUser);
        return UserMeResponse.of(newUser, true, null);
    }
}
