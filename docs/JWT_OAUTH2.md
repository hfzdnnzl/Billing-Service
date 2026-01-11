# JWT / OAuth2 Resource Server Implementation

## Overview
This service now supports **JWT Bearer token authentication** using OAuth2 Resource Server pattern. This is a modern, production-ready approach that's more impressive to recruiters than static API keys.

---

## How It Works

### Authentication Flow
```
1. Client (Next.js) obtains JWT from Authorization Server
   ↓
2. Client includes token in request:
   Authorization: Bearer <jwt>
   ↓
3. Billing Service validates JWT signature using public key
   ↓
4. If valid → request proceeds
   If invalid → HTTP 401 Unauthorized
```

### Key Components

#### RSA Keypair
- **Private key** (`src/main/resources/keys/private_key.pem`): Signs JWTs (kept secure, used by auth server)
- **Public key** (`src/main/resources/keys/public_key.pem`): Validates JWT signatures (embedded in resource server)

#### SecurityConfig
- Configures Spring Security as an OAuth2 Resource Server
- Loads public key and creates `JwtDecoder` bean
- Validates JWT signature, expiry, and claims on every request

#### JwtTokenGenerator (Demo Utility)
- Generates test tokens for development/demo
- In production, tokens come from Auth0, Keycloak, AWS Cognito, etc.

---

## Quick Start

### 1. Generate a Test Token
```bash
mvn compile exec:java -Dexec.mainClass="com.lakarra.billing.util.JwtTokenGenerator"
```

This outputs a JWT valid for 15 minutes, formatted like:
```
eyJhbGciOiJSUzI1NiJ9.eyJzY29wZSI6InJlYWQ6cHJpY2VzIHdyaXRlOnBheW1lbnRzIiwiY2xpZW50X2lkIjoibmV4dGpzLWNsaWVudCIsInN1YiI6Im5leHRqcy1jbGllbnQiLCJpc3MiOiJiaWxsaW5nLXNlcnZpY2UiLCJleHAiOjE3MzcyNTE2NzUsImlhdCI6MTczNzI1MDc3NX0.signature...
```

### 2. Use the Token
```bash
# Copy the token from step 1 and use it here:
curl -H "Authorization: Bearer <YOUR_TOKEN>" \
     http://localhost:8080/api/v1/stripe/prices/active
```

### 3. Test Expiry
Wait 15 minutes and try again — you'll get HTTP 401 with:
```json
{
  "error": "invalid_token",
  "error_description": "An error occurred while attempting to decode the Jwt: Jwt expired at..."
}
```

---

## JWT Token Structure

### Header
```json
{
  "alg": "RS256",
  "typ": "JWT"
}
```

### Payload (Claims)
```json
{
  "sub": "nextjs-client",
  "iss": "billing-service",
  "iat": 1737250775,
  "exp": 1737251675,
  "scope": "read:prices write:payments",
  "client_id": "nextjs-client"
}
```

### Signature
RSA-SHA256 signature using private key.

---

## Production Setup

### Using an Authorization Server

Instead of generating tokens locally, integrate with a real auth provider:

#### Option 1: Auth0
```properties
# application.properties
spring.security.oauth2.resourceserver.jwt.issuer-uri=https://your-tenant.auth0.com/
```

#### Option 2: AWS Cognito
```properties
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://cognito-idp.{region}.amazonaws.com/{userPoolId}/.well-known/jwks.json
```

#### Option 3: Keycloak
```properties
spring.security.oauth2.resourceserver.jwt.issuer-uri=https://keycloak.example.com/realms/your-realm
```

### Client Credentials Flow (Next.js → Billing Service)

1. **Next.js server** requests a token:
```javascript
// pages/api/get-token.js (DO NOT expose this endpoint publicly)
const response = await fetch('https://auth-server/oauth/token', {
  method: 'POST',
  headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
  body: new URLSearchParams({
    grant_type: 'client_credentials',
    client_id: process.env.CLIENT_ID,
    client_secret: process.env.CLIENT_SECRET,
    scope: 'read:prices'
  })
});
const { access_token } = await response.json();
```

2. **Next.js calls Billing Service** with token:
```javascript
// pages/api/prices.js
const token = await getToken(); // from step 1
const response = await fetch('http://billing-service:8080/api/v1/stripe/prices/active', {
  headers: { Authorization: `Bearer ${token}` }
});
```

---

## Security Best Practices

### Token Expiry
- **Short-lived access tokens**: 5-15 minutes (reduces exposure window)
- **Refresh tokens**: for longer sessions (stored securely, not in browser localStorage)

### Key Management
- **Never commit private keys** to git
- Add to `.gitignore`:
  ```
  src/main/resources/keys/private_key.pem
  ```
- Use environment variables or secrets managers in production
- Rotate keys periodically (invalidates all old tokens)

### Scope-Based Authorization (Optional Enhancement)
```java
@GetMapping("/api/v1/stripe/prices/active")
@PreAuthorize("hasAuthority('SCOPE_read:prices')")
public ResponseEntity<List<Price>> getActivePrices() {
  // ...
}
```

---

## Advantages Over API Keys

| Feature | API Key | JWT/OAuth2 |
|---------|---------|------------|
| Revocation | Manual rotation required | Automatic expiry, can use blacklist/introspection |
| Granular permissions | No | Yes (scopes/claims) |
| User context | No | Yes (sub, email, etc.) |
| Industry standard | No | Yes (OAuth2 RFC 6749) |
| Recruiter perception | Basic | Professional/Production-ready |
| Scalability | Single shared secret | Distributed validation |

---

## Testing Scenarios

### Valid Token
```bash
TOKEN=$(mvn -q compile exec:java -Dexec.mainClass="com.lakarra.billing.util.JwtTokenGenerator" | grep -A1 "Generated JWT" | tail -1)
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/stripe/prices/active
# → 200 OK with prices
```

### Missing Token
```bash
curl http://localhost:8080/api/v1/stripe/prices/active
# → 401 Unauthorized
```

### Invalid Signature
```bash
curl -H "Authorization: Bearer eyJhbGciOiJSUzI1NiJ9.invalid.signature" \
     http://localhost:8080/api/v1/stripe/prices/active
# → 401 Unauthorized (invalid signature)
```

### Expired Token
```bash
# Use a token from 20 minutes ago
curl -H "Authorization: Bearer <OLD_TOKEN>" \
     http://localhost:8080/api/v1/stripe/prices/active
# → 401 Unauthorized (token expired)
```

---

## Troubleshooting

### Error: "Failed to load JWT public key"
- Ensure `src/main/resources/keys/public_key.pem` exists
- Check file permissions
- Verify PEM format (should start with `-----BEGIN PUBLIC KEY-----`)

### Error: "Jwt expired at..."
- Generate a new token using `JwtTokenGenerator`
- Or increase expiry in `generateToken()` method

### Error: "An error occurred while attempting to decode the Jwt"
- Check token format (should be 3 parts separated by dots)
- Verify private/public key match
- Ensure private key is PKCS#8 format

---

## Next Steps for Portfolio Enhancement

1. **Add scope-based authorization** using `@PreAuthorize`
2. **Integrate with Auth0/Keycloak** for demo
3. **Add token introspection endpoint** for opaque tokens
4. **Implement refresh token flow** (for long-lived sessions)
5. **Add Prometheus metrics** for token validation (success/failure rates)
6. **Document in README.md** with architecture diagram

---

## References

- [Spring Security OAuth2 Resource Server](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/index.html)
- [RFC 6749 - OAuth 2.0](https://datatracker.ietf.org/doc/html/rfc6749)
- [RFC 7519 - JWT](https://datatracker.ietf.org/doc/html/rfc7519)
- [JWT.io](https://jwt.io/) - Decode and verify JWTs
