package com.mavis.domain.domains.banner.repository;

import com.mavis.domain.domains.banner.domain.BannerImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BannerImageRepository extends JpaRepository<BannerImage, Long> {
    List<BannerImage> findAllByIsDeletedFalseOrderBySortOrderAsc();
}
