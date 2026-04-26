package com.mavis.admin.domains.product.implement;

import com.mavis.domain.domains.product.domain.Product;
import com.mavis.domain.domains.product.domain.ProductImage;
import com.mavis.domain.domains.product.domain.ProductImageType;
import com.mavis.domain.domains.product.exception.MainImageRequiredException;
import com.mavis.domain.domains.product.repository.ProductImageRepository;
import com.mavis.infrastructure.image.ImageDirectory;
import com.mavis.infrastructure.image.S3FileUploader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
public class ProductImageAppender {
    private final ProductImageRepository productImageRepository;
    private final S3FileUploader s3FileUploader;

    public void saveImages(List<MultipartFile> mainImages, List<MultipartFile> productImages, List<MultipartFile> detailImages, Product product) {
        if (mainImages == null || mainImages.size() != 1) {
            throw MainImageRequiredException.EXCEPTION;
        }
        //메인
        List<String> uploadedMainImages = mainImages.stream()
                .map(image -> s3FileUploader.uploadImageToS3(image, ImageDirectory.PRODUCT))
                .toList();
        List<ProductImage> mainProductImages = mapProductImagesInOrder(uploadedMainImages, product, ProductImageType.MAIN);
        productImageRepository.saveAll(mainProductImages);

        //상품
        List<String> uploadedProductImages = productImages.stream()
                .map(image -> s3FileUploader.uploadImageToS3(image, ImageDirectory.PRODUCT))
                .toList();
        List<ProductImage> productImageEntities = mapProductImagesInOrder(uploadedProductImages, product, ProductImageType.PRODUCT);
        productImageRepository.saveAll(productImageEntities);

        //상품 상세
        List<String> detailProductImageUrls = detailImages.stream()
                .map(image -> s3FileUploader.uploadImageToS3(image, ImageDirectory.PRODUCT))
                .toList();
        List<ProductImage> productDetailImages = mapProductImagesInOrder(detailProductImageUrls, product, ProductImageType.DETAIL);
        productImageRepository.saveAll(productDetailImages);
    }

    private static List<ProductImage> mapProductImagesInOrder(List<String> uploadedImages, Product product, ProductImageType productImageType) {
        return IntStream.range(0, uploadedImages.size())
                .mapToObj(orderNum -> ProductImage.builder()
                        .product(product)
                        .imageType(productImageType)
                        .imageUrl(uploadedImages.get(orderNum))
                        .orderNum(orderNum)
                        .build()
                ).toList();
    }
}
