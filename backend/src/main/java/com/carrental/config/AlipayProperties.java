package com.carrental.config;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

@Data
@Component
@ConfigurationProperties(prefix = "app.alipay")
public class AlipayProperties {
    private final DefaultResourceLoader resourceLoader = new DefaultResourceLoader();

    private boolean enabled;
    private String gatewayUrl;
    private String appId;
    private String merchantPrivateKey;
    private String merchantPrivateKeyLocation;
    private String alipayPublicKey;
    private String alipayPublicKeyLocation;
    private String notifyUrl;
    private String returnUrl;
    private String charset;
    private String signType;

    public String getMerchantPrivateKey() {
        return resolveKey(merchantPrivateKey, merchantPrivateKeyLocation);
    }

    public String getAlipayPublicKey() {
        return resolveKey(alipayPublicKey, alipayPublicKeyLocation);
    }

    private String resolveKey(String inlineValue, String location) {
        if (inlineValue != null && !inlineValue.isBlank()) {
            return normalizeKey(inlineValue);
        }
        if (location == null || location.isBlank()) {
            return inlineValue;
        }
        try {
            Resource resource = resourceLoader.getResource(location);
            if (!resource.exists()) {
                return inlineValue;
            }
            String content = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            return normalizeKey(content);
        } catch (IOException ex) {
            return inlineValue;
        }
    }

    private String normalizeKey(String content) {
        if (content == null) {
            return null;
        }
        String trimmed = content.trim();
        if (trimmed.contains("BEGIN RSA PRIVATE KEY")) {
            return convertPkcs1PemToPkcs8(trimmed);
        }
        return stripPemMarkers(trimmed);
    }

    private String stripPemMarkers(String content) {
        return content
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("-----BEGIN RSA PRIVATE KEY-----", "")
            .replace("-----END RSA PRIVATE KEY-----", "")
            .replaceAll("\\s+", "");
    }

    private String convertPkcs1PemToPkcs8(String pkcs1Pem) {
        byte[] pkcs1 = Base64.getDecoder().decode(stripPemMarkers(pkcs1Pem));
        byte[] pkcs8 = wrapPkcs1InPkcs8(pkcs1);
        return Base64.getEncoder().encodeToString(pkcs8);
    }

    private byte[] wrapPkcs1InPkcs8(byte[] pkcs1) {
        try {
            ByteArrayOutputStream inner = new ByteArrayOutputStream();
            inner.write(encodeIntegerZero());
            inner.write(encodeSequence(new byte[] {
                0x06, 0x09, 0x2A, (byte) 0x86, 0x48, (byte) 0x86, (byte) 0xF7, 0x0D, 0x01, 0x01, 0x01,
                0x05, 0x00
            }));
            inner.write(encodeOctetString(pkcs1));
            return encodeSequence(inner.toByteArray());
        } catch (IOException ex) {
            throw new IllegalStateException("failed to convert PKCS#1 private key to PKCS#8", ex);
        }
    }

    private byte[] encodeIntegerZero() {
        return new byte[] {0x02, 0x01, 0x00};
    }

    private byte[] encodeSequence(byte[] value) throws IOException {
        return encodeDer((byte) 0x30, value);
    }

    private byte[] encodeOctetString(byte[] value) throws IOException {
        return encodeDer((byte) 0x04, value);
    }

    private byte[] encodeDer(byte tag, byte[] value) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write(tag);
        writeLength(output, value.length);
        output.write(value);
        return output.toByteArray();
    }

    private void writeLength(ByteArrayOutputStream output, int length) throws IOException {
        if (length < 0x80) {
            output.write(length);
            return;
        }
        int temp = length;
        int byteCount = 0;
        byte[] buffer = new byte[4];
        while (temp > 0) {
            buffer[byteCount++] = (byte) (temp & 0xFF);
            temp >>= 8;
        }
        output.write(0x80 | byteCount);
        for (int i = byteCount - 1; i >= 0; i--) {
            output.write(buffer[i]);
        }
    }
}
