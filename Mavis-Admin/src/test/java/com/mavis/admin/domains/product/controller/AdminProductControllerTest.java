package com.mavis.admin.domains.product.controller;

import com.mavis.admin.domains.product.dto.CreateProductRequest;
import com.mavis.admin.domains.product.dto.ProductImageVO;
import com.mavis.admin.domains.product.dto.ProductNoticeVO;
import com.mavis.admin.domains.product.dto.UpdateProductClearanceRequest;
import com.mavis.admin.domains.product.dto.UpdateProductRequest;
import com.mavis.admin.support.ControllerTestSupport;
import com.mavis.common.enums.ProductSubCategory;
import com.mavis.domain.domains.admin.domain.Admin;
import com.mavis.domain.domains.admin.repository.AdminRepository;
import com.mavis.domain.domains.product.domain.Product;
import com.mavis.domain.domains.product.domain.ProductColor;
import com.mavis.domain.domains.product.domain.ProductImage;
import com.mavis.domain.domains.product.domain.ProductImageType;
import com.mavis.domain.domains.product.domain.ProductNotice;
import com.mavis.domain.domains.product.repository.ProductColorRepository;
import com.mavis.domain.domains.product.repository.ProductImageRepository;
import com.mavis.domain.domains.product.repository.ProductNoticeRepository;
import com.mavis.domain.domains.product.repository.ProductRepository;
import com.mavis.infrastructure.image.ImageDirectory;
import com.mavis.infrastructure.image.S3FileUploader;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.multipart.MultipartFile;

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AdminProductControllerTest extends ControllerTestSupport {

    @MockitoBean
    S3FileUploader s3FileUploader;

    @Autowired private AdminRepository adminRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private ProductNoticeRepository productNoticeRepository;
    @Autowired private ProductColorRepository productColorRepository;
    @Autowired private ProductImageRepository productImageRepository;
    @Autowired private EntityManager em;

    private Admin savedAdmin;

    @BeforeEach
    void setUp() {
        savedAdmin = adminRepository.save(Admin.builder()
                .username("admin")
                .password("password")
                .build());
    }

    private Product saveProduct(String name, int price) {
        return productRepository.save(Product.builder()
                .name(name)
                .price(price)
                .subCategory(ProductSubCategory.COTTON)
                .isClearance(false)
                .build());
    }

    private void saveNotice(Product product) {
        productNoticeRepository.save(ProductNotice.builder()
                .precaution("주의사항")
                .shippingInfo("배송정보")
                .returnRequest("반품신청")
                .returnProcess("반품처리")
                .product(product)
                .build());
    }

    private void saveColor(Product product, String color) {
        productColorRepository.save(ProductColor.of(product, color));
    }

    private void saveImage(Product product, ProductImageType type, String url, int order) {
        productImageRepository.save(ProductImage.builder()
                .product(product)
                .imageType(type)
                .imageUrl(url)
                .orderNum(order)
                .build());
    }

    private MockMultipartFile jsonPart(String name, Object value) throws Exception {
        return new MockMultipartFile(name, "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(value));
    }

    private MockMultipartFile imagePart(String name) {
        return new MockMultipartFile(name, "test.jpg", MediaType.IMAGE_JPEG_VALUE, "fake-image".getBytes());
    }

    private MockMultipartFile imagePart(String name, String filename) {
        return new MockMultipartFile(name, filename, MediaType.IMAGE_JPEG_VALUE, "fake-image".getBytes());
    }

    // S3 업로드 결과를 원본 파일명 기반 URL로 반환 → orderNum 매칭 검증용
    private void mockS3ReturnsFilenameUrl() {
        given(s3FileUploader.uploadImageToS3(any(), any(ImageDirectory.class)))
                .willAnswer(invocation -> {
                    MultipartFile file = invocation.getArgument(0);
                    return "https://s3.test/" + file.getOriginalFilename();
                });
    }

    private List<ProductImage> findImagesByType(ProductImageType type) {
        List<ProductImage> all = productImageRepository.findAll();
        return all.stream()
                .filter(image -> image.getImageType() == type)
                .filter(image -> !image.isDeleted())
                .sorted(Comparator.comparingInt(ProductImage::getOrderNum))
                .toList();
    }

    private ProductNoticeVO noticeVO() {
        return new ProductNoticeVO("주의사항", "배송정보", "반품신청", "반품처리");
    }

    @Nested
    class 상품_단건_조회 {

        @Test
        void 조회_성공_JSON_필드_전체_검증() throws Exception {
            Product product = saveProduct("테스트상품", 50000);
            saveNotice(product);
            saveColor(product, "블랙");
            saveImage(product, ProductImageType.MAIN, "https://s3.test/main.jpg", 0);
            saveImage(product, ProductImageType.PRODUCT, "https://s3.test/product.jpg", 0);
            saveImage(product, ProductImageType.DETAIL, "https://s3.test/detail.jpg", 0);
            em.flush();
            em.clear();

            mockMvc.perform(get("/v1/api/product/{id}", product.getId())
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(product.getId()))
                    .andExpect(jsonPath("$.data.name").value("테스트상품"))
                    .andExpect(jsonPath("$.data.price").value(50000))
                    .andExpect(jsonPath("$.data.isClearance").value(false))
                    .andExpect(jsonPath("$.data.subCategory").value("COTTON"))
                    .andExpect(jsonPath("$.data.colors[0].name").value("블랙"))
                    .andExpect(jsonPath("$.data.mainImages[0]").value("https://s3.test/main.jpg"))
                    .andExpect(jsonPath("$.data.productImages[0]").value("https://s3.test/product.jpg"))
                    .andExpect(jsonPath("$.data.detailImages[0]").value("https://s3.test/detail.jpg"))
                    .andExpect(jsonPath("$.data.productNoticeVO.precaution").value("주의사항"))
                    .andExpect(jsonPath("$.data.productNoticeVO.shippingInfo").value("배송정보"));
        }

        @Test
        void 이미지가_orderNum_오름차순으로_정렬되어_반환() throws Exception {
            Product product = saveProduct("테스트상품", 50000);
            saveNotice(product);
            saveColor(product, "블랙");
            saveImage(product, ProductImageType.MAIN, "https://s3.test/main.jpg", 0);
            // 일부러 orderNum 역순으로 저장
            saveImage(product, ProductImageType.PRODUCT, "https://s3.test/p2.jpg", 2);
            saveImage(product, ProductImageType.PRODUCT, "https://s3.test/p0.jpg", 0);
            saveImage(product, ProductImageType.PRODUCT, "https://s3.test/p1.jpg", 1);
            em.flush();
            em.clear();

            mockMvc.perform(get("/v1/api/product/{id}", product.getId())
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.productImages[0]").value("https://s3.test/p0.jpg"))
                    .andExpect(jsonPath("$.data.productImages[1]").value("https://s3.test/p1.jpg"))
                    .andExpect(jsonPath("$.data.productImages[2]").value("https://s3.test/p2.jpg"));
        }

        @Test
        void 존재하지_않는_상품_조회시_404() throws Exception {
            mockMvc.perform(get("/v1/api/product/{id}", 999999L)
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isNotFound());
        }

        @Test
        void 비인증_요청시_401() throws Exception {
            mockMvc.perform(get("/v1/api/product/{id}", 1L))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class 상품_목록_조회 {

        @Test
        void 조회_성공_최신순() throws Exception {
            saveProduct("상품1", 10000);
            saveProduct("상품2", 20000);
            em.flush();
            em.clear();

            mockMvc.perform(get("/v1/api/product")
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].name").value("상품2"))
                    .andExpect(jsonPath("$.data[1].name").value("상품1"));
        }

        @Test
        void 삭제된_상품은_미포함() throws Exception {
            saveProduct("활성상품", 10000);
            Product deleted = saveProduct("삭제상품", 20000);
            deleted.delete();
            productRepository.save(deleted);
            em.flush();
            em.clear();

            mockMvc.perform(get("/v1/api/product")
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.data[0].name").value("활성상품"));
        }

        @Test
        void 상품_없을때_빈_목록() throws Exception {
            mockMvc.perform(get("/v1/api/product")
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        void 비인증_요청시_401() throws Exception {
            mockMvc.perform(get("/v1/api/product"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class 상품_등록 {

        @Test
        void 등록_성공_상품_공지_색상_저장() throws Exception {
            given(s3FileUploader.uploadImageToS3(any(), any(ImageDirectory.class)))
                    .willReturn("https://s3.test/uploaded.jpg");

            CreateProductRequest request = new CreateProductRequest(
                    "신상품", 30000, ProductSubCategory.NYLON,
                    List.of("블랙", "화이트"), false, noticeVO());

            mockMvc.perform(multipart("/v1/api/product")
                            .file(jsonPart("request", request))
                            .file(imagePart("mainImages"))
                            .file(imagePart("productImages"))
                            .file(imagePart("detailImages"))
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.productId").isNumber());

            em.flush();
            em.clear();

            Pageable pageable = Pageable.unpaged();
            List<Product> products = productRepository.findByIsDeletedFalseOrderByIdDesc(pageable);
            assertThat(products).hasSize(1);
            Product saved = products.get(0);
            assertThat(saved.getName()).isEqualTo("신상품");
            assertThat(productNoticeRepository.findByProduct(saved)).isPresent();
        }

        @Test
        void 여러_이미지_등록시_orderNum_0부터_순서대로_부여() throws Exception {
            mockS3ReturnsFilenameUrl();

            CreateProductRequest request = new CreateProductRequest(
                    "신상품", 30000, ProductSubCategory.NYLON,
                    List.of("블랙"), false, noticeVO());

            mockMvc.perform(multipart("/v1/api/product")
                            .file(jsonPart("request", request))
                            .file(imagePart("mainImages", "main.jpg"))
                            .file(imagePart("productImages", "p0.jpg"))
                            .file(imagePart("productImages", "p1.jpg"))
                            .file(imagePart("productImages", "p2.jpg"))
                            .file(imagePart("detailImages", "d0.jpg"))
                            .file(imagePart("detailImages", "d1.jpg"))
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isOk());

            em.flush();
            em.clear();

            List<ProductImage> productImages = findImagesByType(ProductImageType.PRODUCT);
            assertThat(productImages).hasSize(3);
            assertThat(productImages).extracting(ProductImage::getOrderNum).containsExactly(0, 1, 2);
            assertThat(productImages).extracting(ProductImage::getImageUrl)
                    .containsExactly("https://s3.test/p0.jpg", "https://s3.test/p1.jpg", "https://s3.test/p2.jpg");

            List<ProductImage> detailImages = findImagesByType(ProductImageType.DETAIL);
            assertThat(detailImages).extracting(ProductImage::getOrderNum).containsExactly(0, 1);

            List<ProductImage> mainImages = findImagesByType(ProductImageType.MAIN);
            assertThat(mainImages).hasSize(1);
            assertThat(mainImages.get(0).getOrderNum()).isEqualTo(0);
        }

        @Test
        void 메인이미지가_1개가_아니면_400() throws Exception {
            CreateProductRequest request = new CreateProductRequest(
                    "신상품", 30000, ProductSubCategory.NYLON,
                    List.of("블랙"), false, noticeVO());

            mockMvc.perform(multipart("/v1/api/product")
                            .file(jsonPart("request", request))
                            .file(imagePart("mainImages"))
                            .file(imagePart("mainImages"))
                            .file(imagePart("productImages"))
                            .file(imagePart("detailImages"))
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void 비인증_요청시_401() throws Exception {
            CreateProductRequest request = new CreateProductRequest(
                    "신상품", 30000, ProductSubCategory.NYLON,
                    List.of("블랙"), false, noticeVO());

            mockMvc.perform(multipart("/v1/api/product")
                            .file(jsonPart("request", request))
                            .file(imagePart("mainImages"))
                            .file(imagePart("productImages"))
                            .file(imagePart("detailImages")))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class 상품_수정 {

        @Test
        void 수정_성공_이름_가격_카테고리_변경() throws Exception {
            Product product = saveProduct("기존상품", 10000);
            saveNotice(product);
            em.flush();
            em.clear();

            UpdateProductRequest request = new UpdateProductRequest(
                    "수정상품", 99000, ProductSubCategory.POLY,
                    List.of(), List.of(), List.of(), List.of(), noticeVO());

            mockMvc.perform(multipart("/v1/api/product/{id}", product.getId())
                            .file(jsonPart("request", request))
                            .with(req -> { req.setMethod("PUT"); return req; })
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isOk());

            em.flush();
            em.clear();

            Product updated = productRepository.findById(product.getId()).orElseThrow();
            assertThat(updated.getName()).isEqualTo("수정상품");
            assertThat(updated.getPrice()).isEqualTo(99000);
            assertThat(updated.getSubCategory()).isEqualTo(ProductSubCategory.POLY);
        }

        @Test
        void 기존_이미지_유지하고_새_이미지는_maxOrder_뒤에_추가() throws Exception {
            Product product = saveProduct("기존상품", 10000);
            saveNotice(product);
            saveImage(product, ProductImageType.PRODUCT, "https://s3.test/keep.jpg", 0);
            em.flush();
            em.clear();

            mockS3ReturnsFilenameUrl();

            // keep.jpg 유지(VO 포함) + 새 이미지 1개 추가
            UpdateProductRequest request = new UpdateProductRequest(
                    "기존상품", 10000, ProductSubCategory.COTTON,
                    List.of(),
                    List.of(),
                    List.of(new ProductImageVO(0, "https://s3.test/keep.jpg")),
                    List.of(),
                    noticeVO());

            mockMvc.perform(multipart("/v1/api/product/{id}", product.getId())
                            .file(jsonPart("request", request))
                            .file(imagePart("productImages", "new.jpg"))
                            .with(req -> { req.setMethod("PUT"); return req; })
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isOk());

            em.flush();
            em.clear();

            List<ProductImage> productImages = findImagesByType(ProductImageType.PRODUCT);
            assertThat(productImages).hasSize(2);
            assertThat(productImages).extracting(ProductImage::getImageUrl)
                    .containsExactly("https://s3.test/keep.jpg", "https://s3.test/new.jpg");
            assertThat(productImages).extracting(ProductImage::getOrderNum)
                    .containsExactly(0, 1);
        }

        @Test
        void 중간_이미지_삭제후_새_이미지_추가시_maxOrder는_삭제분_포함해_계산() throws Exception {
            Product product = saveProduct("기존상품", 10000);
            saveNotice(product);
            saveImage(product, ProductImageType.PRODUCT, "https://s3.test/i1.jpg", 1);
            saveImage(product, ProductImageType.PRODUCT, "https://s3.test/i2.jpg", 2);
            saveImage(product, ProductImageType.PRODUCT, "https://s3.test/i3.jpg", 3);
            em.flush();
            em.clear();

            mockS3ReturnsFilenameUrl();

            // i1, i2 유지 / i3 삭제(VO 제외) / 새 이미지 2개 추가
            UpdateProductRequest request = new UpdateProductRequest(
                    "기존상품", 10000, ProductSubCategory.COTTON,
                    List.of(),
                    List.of(),
                    List.of(
                            new ProductImageVO(1, "https://s3.test/i1.jpg"),
                            new ProductImageVO(2, "https://s3.test/i2.jpg")),
                    List.of(),
                    noticeVO());

            mockMvc.perform(multipart("/v1/api/product/{id}", product.getId())
                            .file(jsonPart("request", request))
                            .file(imagePart("productImages", "new1.jpg"))
                            .file(imagePart("productImages", "new2.jpg"))
                            .with(req -> { req.setMethod("PUT"); return req; })
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isOk());

            em.flush();
            em.clear();

            // i3(3) 삭제됐지만 maxOrder=3 기준 → 새 이미지 4,5 (3번 자리는 gap)
            List<ProductImage> productImages = findImagesByType(ProductImageType.PRODUCT);
            assertThat(productImages).hasSize(4);
            assertThat(productImages).extracting(ProductImage::getImageUrl)
                    .containsExactly(
                            "https://s3.test/i1.jpg",
                            "https://s3.test/i2.jpg",
                            "https://s3.test/new1.jpg",
                            "https://s3.test/new2.jpg");
            assertThat(productImages).extracting(ProductImage::getOrderNum)
                    .containsExactly(1, 2, 4, 5);
        }

        @Test
        void 요청에_없는_기존_이미지는_soft_delete() throws Exception {
            Product product = saveProduct("기존상품", 10000);
            saveNotice(product);
            saveImage(product, ProductImageType.PRODUCT, "https://s3.test/keep.jpg", 0);
            saveImage(product, ProductImageType.PRODUCT, "https://s3.test/remove.jpg", 1);
            em.flush();
            em.clear();

            // keep.jpg만 유지, remove.jpg는 요청에서 제외 → 삭제 대상, 새 파일 없음
            UpdateProductRequest request = new UpdateProductRequest(
                    "기존상품", 10000, ProductSubCategory.COTTON,
                    List.of(),
                    List.of(),
                    List.of(new ProductImageVO(0, "https://s3.test/keep.jpg")),
                    List.of(),
                    noticeVO());

            mockMvc.perform(multipart("/v1/api/product/{id}", product.getId())
                            .file(jsonPart("request", request))
                            .with(req -> { req.setMethod("PUT"); return req; })
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isOk());

            em.flush();
            em.clear();

            List<ProductImage> productImages = findImagesByType(ProductImageType.PRODUCT);
            assertThat(productImages).hasSize(1);
            assertThat(productImages.get(0).getImageUrl()).isEqualTo("https://s3.test/keep.jpg");
        }

        @Test
        void 존재하지_않는_상품_수정시_404() throws Exception {
            UpdateProductRequest request = new UpdateProductRequest(
                    "수정상품", 99000, ProductSubCategory.POLY,
                    List.of(), List.of(), List.of(), List.of(), noticeVO());

            mockMvc.perform(multipart("/v1/api/product/{id}", 999999L)
                            .file(jsonPart("request", request))
                            .with(req -> { req.setMethod("PUT"); return req; })
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isNotFound());
        }

        @Test
        void 비인증_요청시_401() throws Exception {
            UpdateProductRequest request = new UpdateProductRequest(
                    "수정상품", 99000, ProductSubCategory.POLY,
                    List.of(), List.of(), List.of(), List.of(), noticeVO());

            mockMvc.perform(multipart("/v1/api/product/{id}", 1L)
                            .file(jsonPart("request", request))
                            .with(req -> { req.setMethod("PUT"); return req; }))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class 상품_삭제 {

        @Test
        void 삭제_성공_soft_delete() throws Exception {
            Product product = saveProduct("삭제대상", 10000);
            em.flush();
            em.clear();

            mockMvc.perform(delete("/v1/api/product/{id}", product.getId())
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isOk());

            em.flush();
            em.clear();

            Product deleted = productRepository.findById(product.getId()).orElseThrow();
            assertThat(deleted.isDeleted()).isTrue();
        }

        @Test
        void 존재하지_않는_상품_삭제시_404() throws Exception {
            mockMvc.perform(delete("/v1/api/product/{id}", 999999L)
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isNotFound());
        }

        @Test
        void 비인증_요청시_401() throws Exception {
            mockMvc.perform(delete("/v1/api/product/{id}", 1L))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class 상품_클리어런스_수정 {

        @Test
        void 클리어런스_true로_변경_성공() throws Exception {
            Product product = saveProduct("상품", 10000);
            em.flush();
            em.clear();

            UpdateProductClearanceRequest request = new UpdateProductClearanceRequest(true);

            mockMvc.perform(patch("/v1/api/product/{id}/clearance", product.getId())
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            em.flush();
            em.clear();

            Product updated = productRepository.findById(product.getId()).orElseThrow();
            assertThat(updated.isClearance()).isTrue();
        }

        @Test
        void 비인증_요청시_401() throws Exception {
            UpdateProductClearanceRequest request = new UpdateProductClearanceRequest(true);

            mockMvc.perform(patch("/v1/api/product/{id}/clearance", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }
}
