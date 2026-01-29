package com.lakarra.billing.util;

import io.jsonwebtoken.Jwts;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility to generate JWT tokens for testing/demo purposes.
 * Run this class to generate a token that can be used with the Billing Service API.
 * In production, tokens would be issued by an Authorization Server (Auth0, Keycloak, AWS Cognito, etc.)
 */
public class JwtTokenGenerator {

    public static void main(String[] args) {
        try {
            String token = generateToken("nextjs-client", 15); // 15 minute expiry
            
            System.out.println("=".repeat(80));
            System.out.println("Generated JWT Token (valid for 15 minutes):");
            System.out.println("=".repeat(80));
            System.out.println(token);
            System.out.println("=".repeat(80));
            System.out.println("\nUse this token to call the API:");
            System.out.println("\ncurl -H \"Authorization: Bearer " + token + "\" \\");
            System.out.println("     http://localhost:8080/api/v1/stripe/prices/active");
            System.out.println("\nToken Details:");
            System.out.println("  Subject: nextjs-client");
            System.out.println("  Issuer: billing-service");
            System.out.println("  Expiry: 15 minutes from now");
            System.out.println("  Scope: read:prices");
            System.out.println("=".repeat(80));
            
        } catch (Exception e) {
            System.err.println("Error generating JWT token: " + e.getMessage());
        }
    }

    /**
     * Generate a JWT token with the specified subject and expiry.
     * 
     * @param subject The subject (client/user identifier)
     * @param expiryMinutes Token validity in minutes
     * @return Signed JWT token
     */
    public static String generateToken(String subject, int expiryMinutes) throws Exception {
        PrivateKey privateKey = loadPrivateKey();
        
        Instant now = Instant.now();
        Instant expiry = now.plus(expiryMinutes, ChronoUnit.MINUTES);
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("scope", "read:prices write:payments");
        claims.put("client_id", subject);
        
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuer("billing-service")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    /**
     * Load the private key from src/main/resources/keys/private_key.pem
     */
    private static PrivateKey loadPrivateKey() throws Exception {
        String keyPath = "src/main/resources/keys/private_key.pem";
        String privateKeyPEM = new String(Files.readAllBytes(Paths.get(keyPath)))
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----BEGIN RSA PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("-----END RSA PRIVATE KEY-----", "")
            .replaceAll("\\s", "");
        
        byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }
}
