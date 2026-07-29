package com.sportvenue.venue.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 简单 AES 加密（本地/演示）；生产应换 KMS。
 */
@Component
public class SecretCrypto {

    private final byte[] keyBytes;

    public SecretCrypto(@Value("${saas.crypto.key:sportvenue-saas-key16}") String key) {
        String normalized = key == null ? "sportvenue-saas-key16" : key;
        // AES-128：取/补齐 16 字节
        byte[] raw = normalized.getBytes(StandardCharsets.UTF_8);
        byte[] k = new byte[16];
        System.arraycopy(raw, 0, k, 0, Math.min(raw.length, 16));
        this.keyBytes = k;
    }

    public String encrypt(String plain) {
        if (!StringUtils.hasText(plain)) {
            return null;
        }
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(keyBytes, "AES"));
            return Base64.getEncoder().encodeToString(cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("加密失败", e);
        }
    }

    public String decrypt(String enc) {
        if (!StringUtils.hasText(enc)) {
            return null;
        }
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(keyBytes, "AES"));
            return new String(cipher.doFinal(Base64.getDecoder().decode(enc)), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("解密失败", e);
        }
    }

    public boolean hasSecret(String enc) {
        return StringUtils.hasText(enc);
    }
}
