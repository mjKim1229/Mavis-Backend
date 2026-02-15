package com.mavis.domain.domains.notice.repository;

import com.mavis.domain.domains.notice.domain.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
}
