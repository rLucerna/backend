package com.lucerna.backend.user.repository;

import com.lucerna.backend.user.entity.Lodge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LodgeRepository extends JpaRepository<Lodge, Long> {
    Optional<Lodge> findByUserId(Long userId);
}
