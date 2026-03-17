package com.example.ChatApplication.Security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {


    @Value("${application.security.jwt.secret-key}")
    private String secretKey;
    @Value("${application.security.jwt.expiration}")
    private String expirationTime;

    public String GenerateToken(UserDetails userDetail){
        return GenerateToken(new HashMap<>(),userDetail);
    }

    public  String GenerateToken(HashMap<String, Object> extraClaims, UserDetails userDetail) {
        return buildToken(
                extraClaims,
                userDetail,
                Long.parseLong(expirationTime)
        );
    }

    private String buildToken(Map<String, Object> extraClaims,
                              UserDetails userDetail,
                              long expirationTime) {
        var authorities = userDetail.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        return Jwts.builder()
                .setClaims(extraClaims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+expirationTime))
                .setSubject(userDetail.getUsername())
                .claim("authorities",authorities)
                .signWith(getSigningKey())
                .compact();
    }

    public boolean isTokenValid(String jwt, UserDetails userDetails) {
       String username=extractUsername(jwt);
       return (username.equals(userDetails.getUsername()) && !isTokenExpired(jwt));
    }

    private boolean isTokenExpired(String jwt) {
        return extractExpiration(jwt).before(new Date());
    }

    private Date extractExpiration(String jwt) {
        return extractClaim(jwt, Claims::getExpiration);
    }

    public String extractUsername(String jwt) {
        return extractClaim(jwt, Claims::getSubject);
    }

    private <T> T extractClaim(String jwt, Function<Claims,T> claimsResolver) {
        Claims claims=extractAllClaims(jwt);
       return  claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String jwt) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(jwt)
                .getBody();
    }

    private Key getSigningKey() {
        byte[] KeyBytes= Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(KeyBytes);
    }
}
