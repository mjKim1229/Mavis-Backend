package com.mavis.admin.domains.product.service;

import com.mavis.admin.domains.product.dto.*;
import com.mavis.admin.domains.product.implement.ProductColorAppender;
import com.mavis.admin.domains.product.implement.ProductImageAppender;
import com.mavis.domain.domains.product.domain.*;
import com.mavis.domain.domains.product.implement.ProductReader;
import com.mavis.domain.domains.product.repository.ProductImageRepository;
import com.mavis.domain.domains.product.repository.ProductNoticeRepository;
import com.mavis.domain.domains.product.repository.ProductRepository;
import com.mavis.domain.domains.product.vo.ColorVO;
import com.mavis.infrastructure.image.ImageDirectory;
import com.mavis.infrastructure.image.S3FileUploader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminProductService {

    private final ProductColorAppender productColorAppender;
    private final ProductRepository productRepository;
    private final ProductNoticeRepository productNoticeRepository;
    private final ProductImageAppender productImageAppender;
    private final ProductImageRepository productImageRepository;
    private final ProductReader productReader;
    private final S3FileUploader s3FileUploader;


    @Transactional
    public GetProductResponse getProductById(Long id) {
        Product product = productReader.readById(id);
        List<ColorVO> colors = productReader.readProductColors(product.getId());
        List<String> mainImageUrls = extractImageUrl(product, ProductImageType.MAIN);
        List<String> productImages = extractImageUrl(product, ProductImageType.PRODUCT);
        List<String> detailImages = extractImageUrl(product, ProductImageType.DETAIL);
        ProductNotice productNotice = product.getProductNotice();
        ProductNoticeVO productNoticeVO = new ProductNoticeVO(productNotice.getPrecaution(), productNotice.getShippingInfo(), productNotice.getReturnRequest(), productNotice.getReturnProcess());
        return GetProductResponse.from(product, colors, mainImageUrls, productImages, detailImages, productNoticeVO);
    }

    @Transactional(readOnly = true)
    public List<GetProductPreviewResponse> getProductList(Pageable pageable) {
        List<Product> products = productRepository.findByIsDeletedFalseOrderByIdDesc(pageable);

        return products.stream()
                .map(product -> {
                            String previewImage = getPreviewImage(product);
                            List<String> colors = extractColors(product);
                            return GetProductPreviewResponse.from(product, colors, previewImage);
                        }
                ).toList();
    }

    private static String getPreviewImage(Product product) {
        return product.getImages().stream()
                .map(ProductImage::getImageUrl)
                .findFirst()
                .orElse(null);
    }

    private static List<String> extractColors(Product product) {
        return product.getColors().stream()
                .map(ProductColor::getColor)
                .toList();
    }

    @Transactional
    public CreateProductResponse createProduct(CreateProductRequest request, List<MultipartFile> mainImages, List<MultipartFile> productImages, List<MultipartFile> detailImages) {
        Product product = request.toProduct();
        Product savedProduct = productRepository.save(product);

        ProductNotice productNotice = request.toProductNotice(savedProduct);
        productNoticeRepository.save(productNotice);

        productColorAppender.saveProductColors(request.colors(), savedProduct);
        productImageAppender.saveImages(mainImages, productImages, detailImages, savedProduct);
        return new CreateProductResponse(product.getId());
    }

    @Transactional
    public void updateProduct(Long productId, UpdateProductRequest request, List<MultipartFile> mainImages, List<MultipartFile> productImages, List<MultipartFile> detailImages) {
        //product
        Product product = productReader.readById(productId);
        product.update(request.name(), request.price(), request.subCategory());

        //notice
        ProductNotice productNotice = product.getProductNotice();
        ProductNoticeVO notice = request.notice();
        productNotice.update(notice.precaution(), notice.shippingInfo(), notice.returnRequest(), notice.returnProcess());

        //color
        List<String> requestColors = request.colors();
        List<String> recentColors = extractColors(product);

        List<String> toAddColors = requestColors.stream()
                .filter(color -> !recentColors.contains(color))
                .toList();
        productColorAppender.saveProductColors(toAddColors, product);

        List<ProductColor> recentProductColors = product.getColors();
        recentProductColors.stream()
                .filter(color -> !requestColors.contains(color.getColor()))
                .forEach(ProductColor::delete);

        Map<ProductImageType, List<ProductImage>> recentProductImageMap = product.getImages().stream()
                .filter(image -> !image.isDeleted())
                .collect(Collectors.groupingBy(ProductImage::getImageType));

        Map<String, ProductImage> recentProductImageUrlMap = product.getImages().stream()
                .filter(image -> !image.isDeleted())
                .collect(Collectors.toMap(
                        ProductImage::getImageUrl,
                        Function.identity(),
                        (existing, replacement) -> existing // 중복 시 기존 값 유지
                ));

        updateImageByType(recentProductImageMap, ProductImageType.MAIN, request.mainImages(), mainImages, recentProductImageUrlMap, product);
        updateImageByType(recentProductImageMap, ProductImageType.PRODUCT, request.productImages(), productImages, recentProductImageUrlMap, product);
        updateImageByType(recentProductImageMap, ProductImageType.DETAIL, request.detailImages(), detailImages, recentProductImageUrlMap, product);

    }

    private void updateImageByType(Map<ProductImageType, List<ProductImage>> recentImageMap, ProductImageType productImageType, List<ProductImageVO> requestImages, List<MultipartFile> newImages, Map<String, ProductImage> recentProductImageUrlMap, Product product) {
        List<ProductImage> recentProductImages = recentImageMap.getOrDefault(productImageType, List.of());

        List<String> requestImageUrls = requestImages.stream()
                .map(ProductImageVO::imageUrl)
                .toList();

        recentProductImages.stream()
                .filter(image -> !requestImageUrls.contains(image.getImageUrl()))
                .forEach(ProductImage::delete);

        for (ProductImageVO requestImage : requestImages) {
            ProductImage productImage = recentProductImageUrlMap.get(requestImage.imageUrl());
            if (productImage != null) {
                productImage.update(requestImage.order());
            }
        }

        if (newImages == null || newImages.isEmpty()) {
            return;
        }

        int maxOrder = recentProductImages.stream()
                .map(ProductImage::getOrderNum)
                .max(Integer::compareTo)
                .orElse(-1); // 기존 이미지 없으면 -1

        for (int i = 0; i < newImages.size(); i++) {
            MultipartFile multipartFile = newImages.get(i);
            if (multipartFile == null || multipartFile.isEmpty()) {
                continue;
            }
            String uploadImageUrl = s3FileUploader.uploadImageToS3(multipartFile, ImageDirectory.PRODUCT);

            ProductImage productImage = ProductImage.builder()
                    .product(product)
                    .imageType(productImageType)
                    .orderNum(maxOrder + 1 + i)
                    .imageUrl(uploadImageUrl)
                    .build();

            productImageRepository.save(productImage);
        }
    }

    @Transactional
    public void deleteProduct(Long productId) {
        Product product = productReader.readById(productId);
        product.delete();
    }

    @Transactional
    public void updateProductClearance(Long productId, UpdateProductClearanceRequest request) {
        Product product = productReader.readById(productId);
        product.updateClearance(request.isClearance());
    }

    private List<String> extractImageUrl(Product product, ProductImageType imageType) {
        return product.getImages().stream()
                .filter(image -> image.getImageType().equals(imageType))
                .map(ProductImage::getImageUrl)
                .toList();
    }
}
