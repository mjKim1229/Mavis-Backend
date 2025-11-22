package com.mavis.admin.domains.product.service;

import com.mavis.admin.domains.product.dto.*;
import com.mavis.admin.domains.product.implement.ProductColorAppender;
import com.mavis.admin.domains.product.implement.ProductImageAppender;
import com.mavis.domain.domains.product.domain.Product;
import com.mavis.domain.domains.product.domain.ProductColor;
import com.mavis.domain.domains.product.domain.ProductImage;
import com.mavis.domain.domains.product.domain.ProductNotice;
import com.mavis.domain.domains.product.implement.ProductReader;
import com.mavis.domain.domains.product.repository.ProductColorRepository;
import com.mavis.domain.domains.product.repository.ProductImageRepository;
import com.mavis.domain.domains.product.repository.ProductNoticeRepository;
import com.mavis.domain.domains.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminProductService {

    private final ProductColorAppender productColorAppender;
    private final ProductRepository productRepository;
    private final ProductNoticeRepository productNoticeRepository;
    private final ProductImageAppender productImageAppender;
    private final ProductReader productReader;
    private final ProductColorRepository productColorRepository;

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
    public void updateProduct(Long productId, UpdateProductRequest request) {
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
        List<ProductColor> toDeleteColors = recentProductColors.stream()
                .filter(color -> !requestColors.contains(color.getColor()))
                .toList();
        productColorRepository.deleteAll(toDeleteColors);
    }

    @Transactional
    public void deleteProduct(Long productId) {
        Product product = productReader.readById(productId);
        product.delete();
    }
}
