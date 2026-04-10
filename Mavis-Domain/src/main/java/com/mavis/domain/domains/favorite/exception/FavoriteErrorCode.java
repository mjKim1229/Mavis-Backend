package com.mavis.domain.domains.favorite.exception;

import com.mavis.common.dto.ErrorReason;
import com.mavis.common.exception.BaseErrorCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum FavoriteErrorCode implements BaseErrorCode {
    FAVORITE_NOT_FOUND(404, "존재하지 않는 즐겨찾기입니다.", "FAVORITE_404_1"),
    UNAUTHORIZED_FAVORITE(403, "본인의 즐겨찾기만 삭제할 수 있습니다.", "FAVORITE_403_1");

    private final Integer status;
    private final String message;
    private final String code;

    @Override
    public ErrorReason getErrorReason() {
        return new ErrorReason(status, message, code);
    }
}
