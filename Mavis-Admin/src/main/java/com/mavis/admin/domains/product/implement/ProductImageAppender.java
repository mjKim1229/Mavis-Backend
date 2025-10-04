package com.mavis.admin.domains.product.implement;

import com.mavis.domain.domains.product.domain.Product;
import com.mavis.domain.domains.product.domain.ProductImage;
import com.mavis.domain.domains.product.domain.ProductImageType;
import com.mavis.domain.domains.product.repository.ProductImageRepository;
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

    public void saveImages(List<MultipartFile> mainImages, List<MultipartFile> detailImages, Product product) {
        List<String> uploadedMainImages = mainImages.stream()
                .map(s3FileUploader::uploadImageToS3)
                .toList();
        List<ProductImage> mainProductImages = mapProductImagesInOrder(uploadedMainImages, product, ProductImageType.MAIN);
        productImageRepository.saveAll(mainProductImages);

        List<String> uploadedDetailImages = detailImages.stream()
                .map(s3FileUploader::uploadImageToS3)
                .toList();
        List<ProductImage> detailProductImages = mapProductImagesInOrder(uploadedDetailImages, product, ProductImageType.DETAIL);
        productImageRepository.saveAll(detailProductImages);
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
