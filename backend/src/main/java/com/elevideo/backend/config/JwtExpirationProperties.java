package com.elevideo.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "security.jwt")
public class JwtExpirationProperties {

    private String issuer;
    private String secret;
    private Expiration expiration;

    @Getter
    @Setter
    public static class Expiration {
        private long authentication;
        private long emailVerification;
        private long passwordReset;
        private long pythonService;
    }
}
