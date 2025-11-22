package com.mavis.admin.domains.product.service;

import com.mavis.admin.domains.product.dto.*;
import com.mavis.admin.domains.product.implement.ProductColorAppender;
import com.mavis.admin.domains.product.implement.ProductImageAppender;
import com.mavis.domain.domains.product.domain.*;
import com.mavis.domain.domains.product.implement.ProductReader;
import com.mavis.domain.domains.product.repository.ProductColorRepository;
import com.mavis.domain.domains.product.repository.ProductImageRepository;
import com.mavis.domain.domains.product.repository.ProductNoticeRepository;
import com.mavis.domain.domains.product.repository.ProductRepository;
import com.mavis.infrastructure.image.S3FileUploader;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminProductService {

    private final ProductColorAppender productColorAppender;
    private final ProductRepository productRepository;
    private final ProductNoticeRepository productNoticeRepository;
    private final ProductImageAppender productImageAppender;
    private final ProductImageRepository productImageRepository;
    private final ProductReader productReader;
    private final ProductColorRepository productColorRepository;
    private final S3FileUploader s3FileUploader;


    @Transactional(readOnly = true)
    public List<GetProductResponse> getProductList(Pageable pageable) {
        List<Product> products = productRepository.findByIsDeletedFalseOrderByIdDesc(pageable);

        return products.stream()
                .map(product -> {
                            String previewImage = getPreviewImage(product);
                            List<String> colors = extractColors(product);
                            return GetProductResponse.from(product, colors, previewImage);
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
                .collect(Collectors.groupingBy(ProductImage::getImageType));

        Map<String, ProductImage> recentProductImageUrlMap = product.getImages().stream()
                .collect(Collectors.toMap(
                        ProductImage::getImageUrl,
                        Function.identity(),
                        (existing, replacement) -> existing // 중복 시 기존 값 유지
                ));

        updateImageByType(recentProductImageMap, ProductImageType.MAIN, request.mainImages(), mainImages, recentProductImageUrlMap);
        updateImageByType(recentProductImageMap, ProductImageType.PRODUCT, request.productImages(), productImages, recentProductImageUrlMap);
        updateImageByType(recentProductImageMap, ProductImageType.DETAIL, request.detailImages(), detailImages, recentProductImageUrlMap);

    }

    private void updateImageByType(Map<ProductImageType, List<ProductImage>> recentImageMap, ProductImageType productImageType, List<ProductImageVO> requestImages, List<MultipartFile> newImages, Map<String, ProductImage> recentProductImageUrlMap) {
        List<ProductImage> recentProductImages = recentImageMap.get(productImageType);
        List<String> requestImageUrls = requestImages.stream()
                .map(ProductImageVO::imageUrl)
                .toList();

        recentProductImages.stream()
                .filter(image -> !requestImageUrls.contains(image.getImageUrl()))
                .forEach(ProductImage::delete);

        int index = 0;
        for (ProductImageVO requestImage : requestImages) {
            if (requestImage.imageUrl() == null) {
                MultipartFile multipartFile = newImages.get(index);
                String uploadImageUrl = s3FileUploader.uploadImageToS3(multipartFile);
                Integer order = requestImage.order();
                ProductImage productImage = ProductImage.builder()
                        .imageType(productImageType)
                        .orderNum(order)
                        .imageUrl(uploadImageUrl)
                        .build();
                productImageRepository.save(productImage);
            } else {
                ProductImage productImage = recentProductImageUrlMap.get(requestImage.imageUrl());
                productImage.update(requestImage.order());
            }
            index++;
        }
    }

    @Transactional
    public void deleteProduct(Long productId) {
        Product product = productReader.readById(productId);
        product.delete();
    }
}
