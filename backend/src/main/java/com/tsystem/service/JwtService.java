package com.tsystem.service;

import com.tsystem.model.user.User;

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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


@Service
@RequiredArgsConstructor

public class JwtService {

    private static String SECRET_KEY;

    @Value("${jwt.secret-key}")
    public void setSecretKey(String secretKey) {
        SECRET_KEY = secretKey;
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
        // to check if userDetails is instance of User
        if (!(userDetails instanceof User)) {
            throw new IllegalArgumentException("UserDetails must be instance of User");
        }
        User user = (User) userDetails;
        // check if userId is not null
        if (user.getId() == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        claims.put("userId", user.getId());
        claims.put("name", user.getName());
        claims.put("surname", user.getSurname());

        System.out.println("Generating token with claims: " + claims); // Логирование
        return generateToken(claims, userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000))
                .signWith(getSignInKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public boolean isTokenValid(String jwt, UserDetails userDetails) {
        final String username = extractUsername(jwt);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(jwt);
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
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(jwt)
                .getBody();

    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
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