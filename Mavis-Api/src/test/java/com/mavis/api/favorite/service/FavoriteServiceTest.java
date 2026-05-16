package com.mavis.api.favorite.service;

import com.mavis.api.auth.implement.UserReader;
import com.mavis.api.favorite.implement.FavoriteReader;
import com.mavis.domain.domains.favorite.exception.FavoriteAlreadyExistsException;
import com.mavis.domain.domains.favorite.repository.FavoriteRepository;
import com.mavis.domain.domains.product.domain.Product;
import com.mavis.domain.domains.product.implement.ProductReader;
import com.mavis.domain.domains.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @InjectMocks
    private FavoriteService favoriteService;

    @Mock
    private UserReader userReader;
    @Mock
    private FavoriteRepository favoriteRepository;
    @Mock
    private ProductReader productReader;
    @Mock
    private FavoriteReader favoriteReader;

    @Test
    void 이미_즐겨찾기에_추가된_상품이면_FavoriteAlreadyExistsException을_던진다() {
        Long productId = 1L;
        User user = User.builder().id(1L).build();
        Product product = Product.builder().id(productId).build();

        given(userReader.getCurrentUser()).willReturn(user);
        given(productReader.readById(productId)).willReturn(product);
        given(favoriteRepository.existsByUserAndProductIdAndIsDeletedFalse(user, productId)).willReturn(true);

        assertThatThrownBy(() -> favoriteService.createFavorite(productId))
                .isInstanceOf(FavoriteAlreadyExistsException.class);

        verify(favoriteRepository, never()).save(any());
    }

    @Test
    void 즐겨찾기에_없는_상품이면_정상_저장된다() {
        Long productId = 1L;
        User user = User.builder().id(1L).build();
        Product product = Product.builder().id(productId).build();

        given(userReader.getCurrentUser()).willReturn(user);
        given(productReader.readById(productId)).willReturn(product);
        given(favoriteRepository.existsByUserAndProductIdAndIsDeletedFalse(user, productId)).willReturn(false);

        favoriteService.createFavorite(productId);

        verify(favoriteRepository).save(any());
    }
}
