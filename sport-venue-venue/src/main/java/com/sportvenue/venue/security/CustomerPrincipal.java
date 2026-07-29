package com.sportvenue.venue.security;

import lombok.Getter;

/**
 * C 端登录主体（与 B 端 User 分离）
 */
@Getter
public class CustomerPrincipal {

    private final Long id;
    private final Long merchantId;
    private final String nickname;

    public CustomerPrincipal(Long id, Long merchantId, String nickname) {
        this.id = id;
        this.merchantId = merchantId;
        this.nickname = nickname;
    }
}
