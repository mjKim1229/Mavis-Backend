package com.mavis.api.product.service;

import com.mavis.api.product.dto.GetProductPreviewResponse;
import com.mavis.api.product.dto.GetProductResponse;
import com.mavis.api.product.dto.SubCategoryVO;
import com.mavis.common.enums.ProductSubCategory;
import com.mavis.domain.domains.product.domain.ProductImageType;
import com.mavis.domain.domains.product.implement.ProductReader;
import com.mavis.api.product.implement.ProductTotalViewManager;
import com.mavis.common.enums.EnumMapper;
import com.mavis.common.enums.ProductCategory;
import com.mavis.domain.domains.product.domain.Product;
import com.mavis.domain.domains.product.domain.ProductColor;
import com.mavis.domain.domains.product.domain.ProductImage;
import com.mavis.domain.domains.product.repository.ProductRepository;
import com.mavis.domain.domains.product.vo.ColorVO;
import com.mavis.domain.domains.product.vo.ProductNoticeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final EnumMapper enumMapper;
    private final ProductReader productReader;
    private final ProductRepository productRepository;
    private final ProductTotalViewManager productTotalViewManager;

    @Transactional
    public GetProductResponse getProductById(Long id) {
        Product product = productReader.readById(id);
        List<ColorVO> colors = productReader.readProductColors(product.getId());
        List<String> mainImageUrls = extractImageUrl(product, ProductImageType.MAIN);
        List<String> productImages = extractImageUrl(product, ProductImageType.PRODUCT);
        List<String> detailImages = extractImageUrl(product, ProductImageType.DETAIL);
        productTotalViewManager.increaseTotalView(product);
        return GetProductResponse.from(product, colors, mainImageUrls, productImages, detailImages);
    }

    private static List<String> extractImageUrl(Product product, ProductImageType productImageType) {
        return product.getImages().stream()
                .filter(productImage -> productImage.getImageType() == productImageType)
                .map(ProductImage::getImageUrl)
                .toList();
    }

    public List<SubCategoryVO> getProductTotalCategory() {
        return Arrays.stream(ProductCategory.values())
                .map(category ->
                        SubCategoryVO.from(
                                category,
                                !category.isBlankSubCategory() ? List.of() : enumMapper.toEnumVoList(category.getSubCategories())
                        )
                )
                .toList();
    }

    public ProductNoticeResponse getProductNotice(Long productId) {
        return productReader.readProductNotice(productId);
    }

    public List<GetProductPreviewResponse> getThisWeekPopularProducts(Pageable pageable) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        List<Product> products = productRepository.getWeeklyBestProducts(weekStart, weekEnd, pageable);

        return products.stream()
                .map(product -> {
                            String previewImage = getPreviewImage(product);
                            List<String> colors = extractColors(product);
                            return GetProductPreviewResponse.from(product, colors, previewImage);
                        }
                ).toList();
    }

    private static List<String> extractColors(Product product) {
        return product.getColors().stream()
                .map(ProductColor::getColor)
                .toList();
    }

    private static String getPreviewImage(Product product) {
        return product.getImages().stream()
                .map(ProductImage::getImageUrl)
                .findFirst()
                .orElse(null);
    }

    public List<GetProductPreviewResponse> getRecentCreatedProducts(Pageable pageable) {
        List<Product> products = productRepository.getRecentCreatedProducts(pageable);
        return products.stream()
                .map(product -> {
                    List<String> colors = extractColors(product);
                    String previewImage = getPreviewImage(product);
                    return GetProductPreviewResponse.from(product, colors, previewImage);
                }).toList();
    }

    public List<GetProductPreviewResponse> getProductPreviewResponseBySubCategory(ProductCategory productCategory, ProductSubCategory subCategory, Pageable pageable) {
        List<Product> products = productRepository.getProductsByCategory(productCategory, subCategory, pageable);
        return products.stream()
                .map(product -> {
                    List<String> colors = extractColors(product);
                    String previewImage = getPreviewImage(product);
                    return GetProductPreviewResponse.from(product, colors, previewImage);
                }).toList();
    }

    public List<GetProductPreviewResponse> getClearanceProduct(Pageable pageable) {
        List<Product> products = productRepository.getClearanceProduct(pageable);
        return products.stream()
                .map(product -> {
                    List<String> colors = extractColors(product);
                    String previewImage = getPreviewImage(product);
                    return GetProductPreviewResponse.from(product, colors, previewImage);
                }).toList();
    }
}
