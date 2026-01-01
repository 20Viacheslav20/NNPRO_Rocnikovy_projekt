package com.tsystem.service;
import com.tsystem.model.user.User;
import com.tsystem.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtService {

    private static final long JWT_EXPIRATION_MS = 8 * 60 * 60 * 1000; // 8 hours

    private static String PRIVATE_KEY;
    private static String PUBLIC_KEY;
    private final UserRepository userRepository;

    @Value("${jwt.private-key}")
    public void setPrivateKey(String privateKey) {
        PRIVATE_KEY = privateKey;
    }

    @Value("${jwt.public-key}")
    public void setPublicKey(String publicKey) {
        PUBLIC_KEY = publicKey;
    }

    public String extractUsername(String jwt) {
        return extractClaim(jwt, Claims::getSubject);
    }

    public <T> T extractClaim(String jwt, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(jwt);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        if (!(userDetails instanceof User)) {
            throw new IllegalArgumentException("UserDetails must be instance of User");
        }

        User user = (User) userDetails;

        claims.put("userId", user.getId());
        claims.put("name", user.getName());
        claims.put("surname", user.getSurname());
        claims.put("role", user.getRole().name());
        claims.put("permissions",
                user.getAuthorities().stream()
                        .map(a -> a.getAuthority())
                        .toList()
        );
        claims.put("tokenVersion", user.getTokenVersion());

        return generateToken(claims, userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION_MS))
                .signWith(getPrivateSigningKey(), SignatureAlgorithm.RS512)
                .compact();
    }

    public boolean isTokenValid(String jwt, UserDetails userDetails) {
        final String username = extractUsername(jwt);
        Claims claims = Jwts.parser()
                .setSigningKey(getPublicVerifyingKey())
                .parseClaimsJws(jwt)
                .getBody();

        String userIdStr = claims.get("userId", String.class);
        UUID userId = UUID.fromString(userIdStr);
        Integer tokenVersion = claims.get("tokenVersion", Integer.class);

        // Получаем текущую версию из БД
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Проверяем, совпадает ли версия
        return  !user.isBlocked()
                &&(tokenVersion.equals(user.getTokenVersion()))
                && (username.equals(userDetails.getUsername()))
                && !isTokenExpired(jwt);
    }

    private boolean isTokenExpired(String jwt) {
        return extractExpiration(jwt).before(new Date());
    }

    private Date extractExpiration(String jwt) {
        return extractClaim(jwt, Claims::getExpiration);
    }

    private Claims extractAllClaims(String jwt) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getPublicVerifyingKey())
                .build()
                .parseClaimsJws(jwt)
                .getBody();
    }

    /**
     * Получает приватный ключ для подписи токена
     */
    private PrivateKey getPrivateSigningKey() {
        try {
            byte[] keyBytes = Decoders.BASE64.decode(PRIVATE_KEY);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load private key", e);
        }
    }

    /**
     * Получает публичный ключ для проверки токена
     */
    private PublicKey getPublicVerifyingKey() {
        try {
            byte[] keyBytes = Decoders.BASE64.decode(PUBLIC_KEY);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load public key", e);
        }
    }

    public String getTokenDetails(String jwt) {
        Claims claims = extractAllClaims(jwt);

        StringBuilder sb = new StringBuilder();
        sb.append("Token details:\n");
        sb.append("Subject (username): ").append(claims.getSubject()).append("\n");
        sb.append("UserID: ").append(claims.get("userId")).append("\n");
        sb.append("Issued at: ").append(claims.getIssuedAt()).append("\n");
        sb.append("Expiration: ").append(claims.getExpiration()).append("\n");
        sb.append("All claims: ").append(claims).append("\n");
        return sb.toString();
    }
}