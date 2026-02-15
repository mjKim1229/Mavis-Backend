package com.mavis.domain.domains.notice.repository;

import com.mavis.domain.domains.notice.domain.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    Optional<Notice> findByIdAndIsDeletedFalse(Long id);
    Page<Notice> findAllByIsDeletedFalse(Pageable pageable);
}
