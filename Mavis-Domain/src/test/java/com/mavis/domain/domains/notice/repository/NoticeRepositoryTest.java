package com.mavis.domain.domains.notice.repository;

import com.mavis.domain.domains.notice.domain.Notice;
import com.mavis.domain.support.RepositoryTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class NoticeRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private NoticeRepository noticeRepository;

    @Test
    void 삭제되지_않은_공지사항을_id로_조회한다() {
        Notice notice = Notice.builder()
                .title("테스트 공지")
                .content("내용")
                .build();
        Notice saved = noticeRepository.save(notice);

        Optional<Notice> result = noticeRepository.findByIdAndIsDeletedFalse(saved.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("테스트 공지");
    }

    @Test
    void 삭제된_공지사항은_조회되지_않는다() {
        Notice notice = Notice.builder()
                .title("삭제된 공지")
                .content("내용")
                .build();
        Notice saved = noticeRepository.save(notice);
        saved.delete();
        noticeRepository.save(saved);

        Optional<Notice> result = noticeRepository.findByIdAndIsDeletedFalse(saved.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void 삭제되지_않은_공지사항_목록을_페이징_조회한다() {
        noticeRepository.save(Notice.builder().title("공지1").content("내용1").build());
        noticeRepository.save(Notice.builder().title("공지2").content("내용2").build());
        Notice deleted = Notice.builder().title("삭제공지").content("내용3").build();
        deleted.delete();
        noticeRepository.save(deleted);

        Page<Notice> result = noticeRepository.findAllByIsDeletedFalse(PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(2);
    }
}
