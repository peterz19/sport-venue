package com.sportvenue.venue.util;

import com.sportvenue.common.exception.BusinessException;
import com.sportvenue.venue.entity.User;
import com.sportvenue.venue.security.CustomerPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static User requireCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            throw new BusinessException(401, "未登录或登录已失效");
        }
        return user;
    }

    /** B 端商户上下文：仅老板/店员，不含 ADMIN */
    public static Long requireMerchantId() {
        User user = requireCurrentUser();
        if (user.getMerchantId() == null) {
            throw new BusinessException(403, "当前账号未绑定商户");
        }
        if (user.getUserType() != User.UserType.B_MERCHANT
                && user.getUserType() != User.UserType.B_STAFF) {
            throw new BusinessException(403, "无商户操作权限");
        }
        return user.getMerchantId();
    }

    public static CustomerPrincipal requireCustomer() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomerPrincipal p)) {
            throw new BusinessException(401, "未登录或登录已失效");
        }
        return p;
    }

    public static Long requireCustomerMerchantId() {
        return requireCustomer().getMerchantId();
    }

    public static Long requireCustomerId() {
        return requireCustomer().getId();
    }

    public static Long currentUserId() {
        return requireCurrentUser().getId();
    }

    public static String currentOperatorName() {
        User user = requireCurrentUser();
        if (user.getRealName() != null && !user.getRealName().isBlank()) {
            return user.getRealName();
        }
        return user.getUsername();
    }

    public static boolean isOwner() {
        User user = requireCurrentUser();
        return user.getUserType() == User.UserType.B_MERCHANT;
    }

    public static void requireOwner() {
        if (!isOwner()) {
            throw new BusinessException(403, "仅老板可执行此操作");
        }
    }
}
