package com.lucerna.backend.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("keycloak")
public class KeycloakProperties {

    private String authServerUrl;
    private String realm;
    private String clientId;
    private String tokenUri;
    private String logoutUri;
    private String revokeUri;
    private String adminClientId;
    private String adminClientSecret;
}
