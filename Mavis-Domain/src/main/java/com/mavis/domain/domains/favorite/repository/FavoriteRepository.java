package com.mavis.domain.domains.favorite.repository;

import com.mavis.domain.domains.favorite.domain.Favorite;
import com.mavis.domain.domains.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    Page<Favorite> findByUserAndIsDeletedFalse(User user, Pageable pageable);
    Optional<Favorite> findByIdAndIsDeletedFalse(Long id);
}
