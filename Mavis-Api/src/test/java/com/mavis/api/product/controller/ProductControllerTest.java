package com.mavis.api.product.controller;

import com.mavis.api.support.ControllerTestSupport;
import com.mavis.common.enums.ProductSubCategory;
import com.mavis.domain.domains.product.domain.Product;
import com.mavis.domain.domains.product.domain.ProductColor;
import com.mavis.domain.domains.product.domain.ProductImage;
import com.mavis.domain.domains.product.domain.ProductImageType;
import com.mavis.domain.domains.product.domain.ProductNotice;
import com.mavis.domain.domains.product.repository.ProductColorRepository;
import com.mavis.domain.domains.product.repository.ProductImageRepository;
import com.mavis.domain.domains.product.repository.ProductNoticeRepository;
import com.mavis.domain.domains.product.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProductControllerTest extends ControllerTestSupport {

    @Autowired private ProductRepository productRepository;
    @Autowired private ProductColorRepository productColorRepository;
    @Autowired private ProductImageRepository productImageRepository;
    @Autowired private ProductNoticeRepository productNoticeRepository;
    @Autowired private EntityManager em;

    private Product saveProduct(String name, int price, ProductSubCategory subCategory, boolean clearance) {
        return productRepository.save(Product.builder()
                .name(name)
                .price(price)
                .subCategory(subCategory)
                .isClearance(clearance)
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

    private void saveNotice(Product product) {
        productNoticeRepository.save(ProductNotice.builder()
                .precaution("주의사항")
                .shippingInfo("배송정보")
                .returnRequest("반품신청")
                .returnProcess("반품처리")
                .product(product)
                .build());
    }

    @Nested
    class 단일_상품_조회 {

        @Test
        void 조회_성공_JSON_필드_및_이미지_orderNum_정렬_검증() throws Exception {
            Product product = saveProduct("테스트상품", 50000, ProductSubCategory.INNERWEAR, false);
            saveColor(product, "블랙");
            // 일부러 역순 저장 → @OrderBy("orderNum ASC")로 정렬되어 나와야 함
            saveImage(product, ProductImageType.MAIN, "https://s3.test/main.jpg", 0);
            saveImage(product, ProductImageType.PRODUCT, "https://s3.test/p2.jpg", 2);
            saveImage(product, ProductImageType.PRODUCT, "https://s3.test/p0.jpg", 0);
            saveImage(product, ProductImageType.PRODUCT, "https://s3.test/p1.jpg", 1);
            em.flush();
            em.clear();

            mockMvc.perform(get("/v1/api/products/{id}", product.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(product.getId()))
                    .andExpect(jsonPath("$.data.name").value("테스트상품"))
                    .andExpect(jsonPath("$.data.price").value(50000))
                    .andExpect(jsonPath("$.data.colors[0].name").value("블랙"))
                    .andExpect(jsonPath("$.data.mainImages[0]").value("https://s3.test/main.jpg"))
                    .andExpect(jsonPath("$.data.productImages[0]").value("https://s3.test/p0.jpg"))
                    .andExpect(jsonPath("$.data.productImages[1]").value("https://s3.test/p1.jpg"))
                    .andExpect(jsonPath("$.data.productImages[2]").value("https://s3.test/p2.jpg"));
        }

        @Test
        void 존재하지_않는_상품_조회시_404() throws Exception {
            mockMvc.perform(get("/v1/api/products/{id}", 999999L))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class 최근_등록_상품_조회 {

        @Test
        void 조회_성공_previewImage와_colors_매핑_검증() throws Exception {
            Product product = saveProduct("최근상품", 30000, ProductSubCategory.COTTON, false);
            saveColor(product, "블랙");
            saveColor(product, "화이트");
            saveImage(product, ProductImageType.MAIN, "https://s3.test/main.jpg", 0);
            saveImage(product, ProductImageType.DETAIL, "https://s3.test/detail.jpg", 0);
            em.flush();
            em.clear();

            mockMvc.perform(get("/v1/api/products/recent"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.data[0].id").value(product.getId()))
                    .andExpect(jsonPath("$.data[0].name").value("최근상품"))
                    .andExpect(jsonPath("$.data[0].previewImage").value("https://s3.test/main.jpg"))
                    .andExpect(jsonPath("$.data[0].colors").isArray())
                    .andExpect(jsonPath("$.data[0].colors.length()").value(2));
        }

        @Test
        void 메인이미지_없으면_previewImage_null() throws Exception {
            saveProduct("이미지없는상품", 10000, ProductSubCategory.COTTON, false);
            em.flush();
            em.clear();

            mockMvc.perform(get("/v1/api/products/recent"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.data[0].previewImage").doesNotExist());
        }

        @Test
        void 삭제된_상품은_미포함() throws Exception {
            saveProduct("활성상품", 10000, ProductSubCategory.COTTON, false);
            Product deleted = saveProduct("삭제상품", 20000, ProductSubCategory.COTTON, false);
            deleted.delete();
            productRepository.save(deleted);
            em.flush();
            em.clear();

            mockMvc.perform(get("/v1/api/products/recent"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.data[0].name").value("활성상품"));
        }

        @Test
        void 상품_없으면_빈_목록() throws Exception {
            mockMvc.perform(get("/v1/api/products/recent"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }
    }

    @Nested
    class 카테고리별_상품_조회 {

        @Test
        void subCategory_미지정시_카테고리_하위_전체_조회() throws Exception {
            // FASHION = {ACCESSORY, INNERWEAR}
            saveProduct("악세상품", 10000, ProductSubCategory.ACCESSORY, false);
            saveProduct("이너상품", 20000, ProductSubCategory.INNERWEAR, false);
            // 다른 카테고리(FABRIC)
            saveProduct("면상품", 30000, ProductSubCategory.COTTON, false);
            em.flush();
            em.clear();

            mockMvc.perform(get("/v1/api/products/category/products")
                            .param("productCategory", "FASHION"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(2));
        }

        @Test
        void subCategory_지정시_해당_하위만_조회() throws Exception {
            saveProduct("악세상품", 10000, ProductSubCategory.ACCESSORY, false);
            saveProduct("이너상품", 20000, ProductSubCategory.INNERWEAR, false);
            em.flush();
            em.clear();

            mockMvc.perform(get("/v1/api/products/category/products")
                            .param("productCategory", "FASHION")
                            .param("subCategory", "ACCESSORY"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.data[0].name").value("악세상품"));
        }

        @Test
        void previewImage와_colors_매핑_검증() throws Exception {
            Product product = saveProduct("이너상품", 20000, ProductSubCategory.INNERWEAR, false);
            saveColor(product, "블랙");
            saveImage(product, ProductImageType.MAIN, "https://s3.test/main.jpg", 0);
            em.flush();
            em.clear();

            mockMvc.perform(get("/v1/api/products/category/products")
                            .param("productCategory", "FASHION"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].previewImage").value("https://s3.test/main.jpg"))
                    .andExpect(jsonPath("$.data[0].colors[0]").value("블랙"));
        }

        @Test
        void 매칭되는_상품_없으면_빈_목록() throws Exception {
            saveProduct("면상품", 30000, ProductSubCategory.COTTON, false);
            em.flush();
            em.clear();

            mockMvc.perform(get("/v1/api/products/category/products")
                            .param("productCategory", "FASHION"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }
    }

    @Nested
    class 클리어런스_상품_조회 {

        @Test
        void 클리어런스_상품만_조회() throws Exception {
            saveProduct("클리어런스상품", 10000, ProductSubCategory.COTTON, true);
            saveProduct("일반상품", 20000, ProductSubCategory.COTTON, false);
            em.flush();
            em.clear();

            mockMvc.perform(get("/v1/api/products/clearance"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.data[0].name").value("클리어런스상품"))
                    .andExpect(jsonPath("$.data[0].isClearance").value(true));
        }
    }

    @Nested
    class 상품_검색 {

        @Test
        void 키워드_부분일치_검색() throws Exception {
            saveProduct("여름 원피스", 10000, ProductSubCategory.COTTON, false);
            saveProduct("겨울 코트", 20000, ProductSubCategory.COTTON, false);
            em.flush();
            em.clear();

            mockMvc.perform(get("/v1/api/products/search")
                            .param("keyword", "원피스"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.data[0].name").value("여름 원피스"));
        }

        @Test
        void 대소문자_무시_검색() throws Exception {
            saveProduct("Summer Dress", 10000, ProductSubCategory.COTTON, false);
            em.flush();
            em.clear();

            mockMvc.perform(get("/v1/api/products/search")
                            .param("keyword", "summer"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(1));
        }

        @Test
        void 일치_없으면_빈_목록() throws Exception {
            saveProduct("여름 원피스", 10000, ProductSubCategory.COTTON, false);
            em.flush();
            em.clear();

            mockMvc.perform(get("/v1/api/products/search")
                            .param("keyword", "없는키워드"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }
    }

    @Nested
    class 상품_공지_조회 {

        @Test
        void 조회_성공() throws Exception {
            Product product = saveProduct("상품", 10000, ProductSubCategory.COTTON, false);
            saveNotice(product);
            em.flush();
            em.clear();

            mockMvc.perform(get("/v1/api/products/{id}/notice", product.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.precaution").value("주의사항"))
                    .andExpect(jsonPath("$.data.shippingInfo").value("배송정보"));
        }

        @Test
        void 공지_없으면_404() throws Exception {
            Product product = saveProduct("상품", 10000, ProductSubCategory.COTTON, false);
            em.flush();
            em.clear();

            mockMvc.perform(get("/v1/api/products/{id}/notice", product.getId()))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class 상품_카테고리_조회 {

        @Test
        void 전체_카테고리_목록_반환() throws Exception {
            mockMvc.perform(get("/v1/api/products/category"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(4));
        }
    }
}
