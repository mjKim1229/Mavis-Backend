package com.mavis.api.notice.controller;

import com.mavis.api.support.ControllerTestSupport;
import com.mavis.domain.domains.notice.domain.Notice;
import com.mavis.domain.domains.notice.repository.NoticeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NoticeControllerTest extends ControllerTestSupport {

    @Autowired
    private NoticeRepository noticeRepository;

    @Test
    void 공지사항_상세_조회_성공() throws Exception {
        Notice notice = noticeRepository.save(Notice.builder()
                .title("테스트 공지")
                .content("내용입니다")
                .build());

        mockMvc.perform(get("/v1/api/notice/{noticeId}", notice.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("테스트 공지"))
                .andExpect(jsonPath("$.data.content").value("내용입니다"));
    }

    @Test
    void 존재하지_않는_공지사항_조회시_404() throws Exception {
        mockMvc.perform(get("/v1/api/notice/{noticeId}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void 공지사항_목록_조회_성공() throws Exception {
        noticeRepository.save(Notice.builder().title("공지1").content("내용1").build());
        noticeRepository.save(Notice.builder().title("공지2").content("내용2").build());

        mockMvc.perform(get("/v1/api/notice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.content").isArray());
    }
}
