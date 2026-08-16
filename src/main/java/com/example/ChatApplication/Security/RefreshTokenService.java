package com.example.ChatApplication.Security;

import com.example.ChatApplication.Exception.TokenRefreshException;
import com.example.ChatApplication.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {
    @Value("${application.security.jwt.refresh-token.expiration}")
    private  Long refreshTokenDurationMs;
    private  UserRepository userRepository;
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    public RefreshTokenService(UserRepository userRepository,RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.refreshTokenRepository=refreshTokenRepository;
    }

    public Optional<RefreshToken> findByToken(String token){
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken CreateRefreshToken(Long userId){
        RefreshToken refreshToken=RefreshToken.builder()
                .user(userRepository.findById(userId).get())
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                .build();

        refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }

    public RefreshToken verifyExpiration(RefreshToken token) throws TokenRefreshException {
        if(token.getExpiryDate().compareTo(Instant.now())<0){
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(token.getToken(),"Refresh token was expired. Please make a new signin request");
        }
        return token;
    }


    @Transactional
    public int deleteByUserId(Long userId){
       return  refreshTokenRepository.deleteByUser(userRepository.findById(userId).get());
    }
}

