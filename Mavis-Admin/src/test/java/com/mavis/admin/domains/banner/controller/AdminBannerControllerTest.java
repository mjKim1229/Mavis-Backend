package com.mavis.admin.domains.banner.controller;

import com.mavis.admin.domains.banner.dto.BannerImageVO;
import com.mavis.admin.domains.banner.dto.UpdateBannerRequest;
import com.mavis.admin.support.ControllerTestSupport;
import com.mavis.domain.domains.admin.domain.Admin;
import com.mavis.domain.domains.admin.repository.AdminRepository;
import com.mavis.domain.domains.banner.domain.BannerImage;
import com.mavis.domain.domains.banner.repository.BannerImageRepository;
import com.mavis.infrastructure.image.ImageDirectory;
import com.mavis.infrastructure.image.S3FileUploader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminBannerControllerTest extends ControllerTestSupport {

    @MockitoBean
    S3FileUploader s3FileUploader;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private BannerImageRepository bannerImageRepository;

    private Admin savedAdmin;

    @BeforeEach
    void setUp() {
        savedAdmin = adminRepository.save(Admin.builder()
                .username("admin")
                .password("password")
                .build());
    }

    private MockMultipartFile jsonPart(String name, Object value) throws Exception {
        return new MockMultipartFile(name, "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(value));
    }

    private MockMultipartFile imagePart(String name) {
        return new MockMultipartFile(name, "test.jpg", MediaType.IMAGE_JPEG_VALUE, "fake-image".getBytes());
    }

    @Nested
    class 배너_목록_조회 {

        @Test
        void 배너_없을때_빈_리스트_반환() throws Exception {
            mockMvc.perform(get("/v1/admin/banners")
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        void 배너_있을때_sortOrder_순_반환() throws Exception {
            bannerImageRepository.save(BannerImage.builder().imageUrl("https://s3.test/banner/b.jpg").sortOrder(1).build());
            bannerImageRepository.save(BannerImage.builder().imageUrl("https://s3.test/banner/a.jpg").sortOrder(0).build());

            mockMvc.perform(get("/v1/admin/banners")
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].sortOrder").value(0))
                    .andExpect(jsonPath("$.data[1].sortOrder").value(1));
        }

        @Test
        void 삭제된_배너는_반환_안됨() throws Exception {
            BannerImage deleted = BannerImage.builder().imageUrl("https://s3.test/banner/old.jpg").sortOrder(0).build();
            deleted.delete();
            bannerImageRepository.save(deleted);
            bannerImageRepository.save(BannerImage.builder().imageUrl("https://s3.test/banner/live.jpg").sortOrder(1).build());

            mockMvc.perform(get("/v1/admin/banners")
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.data[0].imageUrl").value("https://s3.test/banner/live.jpg"));
        }

        @Test
        void 비인증_요청시_401() throws Exception {
            mockMvc.perform(get("/v1/admin/banners"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class 배너_수정 {

        @Test
        void 새_이미지_추가_성공() throws Exception {
            given(s3FileUploader.uploadImageToS3(any(), any(ImageDirectory.class)))
                    .willReturn("https://s3.test/banner/new.jpg");

            UpdateBannerRequest request = new UpdateBannerRequest(List.of());
            MockMultipartFile requestPart = jsonPart("request", request);
            MockMultipartFile image = imagePart("images");

            mockMvc.perform(multipart("/v1/admin/banners")
                            .file(requestPart)
                            .file(image)
                            .with(req -> { req.setMethod("PUT"); return req; })
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isOk());

            List<BannerImage> saved = bannerImageRepository.findAllByIsDeletedFalseOrderBySortOrderAsc();
            assertThat(saved).hasSize(1);
            assertThat(saved.get(0).getImageUrl()).isEqualTo("https://s3.test/banner/new.jpg");
        }

        @Test
        void 기존_이미지_유지하고_새_이미지_추가() throws Exception {
            BannerImage existing = bannerImageRepository.save(
                    BannerImage.builder().imageUrl("https://s3.test/banner/keep.jpg").sortOrder(0).build());

            given(s3FileUploader.uploadImageToS3(any(), any(ImageDirectory.class)))
                    .willReturn("https://s3.test/banner/new.jpg");

            UpdateBannerRequest request = new UpdateBannerRequest(
                    List.of(new BannerImageVO(0, "https://s3.test/banner/keep.jpg")));
            MockMultipartFile requestPart = jsonPart("request", request);
            MockMultipartFile image = imagePart("images");

            mockMvc.perform(multipart("/v1/admin/banners")
                            .file(requestPart)
                            .file(image)
                            .with(req -> { req.setMethod("PUT"); return req; })
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isOk());

            List<BannerImage> saved = bannerImageRepository.findAllByIsDeletedFalseOrderBySortOrderAsc();
            assertThat(saved).hasSize(2);
            assertThat(saved.stream().map(BannerImage::getImageUrl))
                    .contains("https://s3.test/banner/keep.jpg", "https://s3.test/banner/new.jpg");
        }

        @Test
        void keepImages_미포함_기존_이미지_soft_delete() throws Exception {
            bannerImageRepository.save(
                    BannerImage.builder().imageUrl("https://s3.test/banner/old.jpg").sortOrder(0).build());

            UpdateBannerRequest request = new UpdateBannerRequest(List.of());
            MockMultipartFile requestPart = jsonPart("request", request);

            mockMvc.perform(multipart("/v1/admin/banners")
                            .file(requestPart)
                            .with(req -> { req.setMethod("PUT"); return req; })
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isOk());

            List<BannerImage> active = bannerImageRepository.findAllByIsDeletedFalseOrderBySortOrderAsc();
            assertThat(active).isEmpty();
        }

        @Test
        void 비인증_요청시_401() throws Exception {
            UpdateBannerRequest request = new UpdateBannerRequest(List.of());
            MockMultipartFile requestPart = jsonPart("request", request);

            mockMvc.perform(multipart("/v1/admin/banners")
                            .file(requestPart)
                            .with(req -> { req.setMethod("PUT"); return req; }))
                    .andExpect(status().isUnauthorized());
        }
    }
}
