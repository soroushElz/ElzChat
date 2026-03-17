package com.example.ChatApplication.auth;

import com.example.ChatApplication.Email.EmailService;
import com.example.ChatApplication.Email.EmailTemplateName;
import com.example.ChatApplication.Exception.ActivationTokenException;
import com.example.ChatApplication.Exception.ChangePasswordTokenException;
import com.example.ChatApplication.Exception.OperationNotPermittedException;
import com.example.ChatApplication.Exception.TokenRefreshException;
import com.example.ChatApplication.Role.RoleRepository;
import com.example.ChatApplication.Security.JwtService;
import com.example.ChatApplication.Security.RefreshToken;
import com.example.ChatApplication.Security.RefreshTokenService;
import com.example.ChatApplication.user.*;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;


    @Value("${application.mailing.activation-url}")
    private String activationUrl;

    @Value("${application.mailing.forget-password-url}")
    private String forgetPasswordUrl;


    public void register(RegistrationRequest registrationRequest) throws MessagingException {
        var UserRole=roleRepository.findByName("USER")
                .orElseThrow(()-> new IllegalStateException("ROLE USER was not initiated"));
        String email= registrationRequest.getEmail();

        Optional<User> userOptional= userRepository.findByEmail(email);

        if(userOptional.isPresent()){
           Boolean enabled;
            enabled = userOptional.get().isEnabled();
            if (!enabled){
           userRepository.deleteByEmail(email);
           }else {
                throw new OperationNotPermittedException("username is Already Taken");
            }
        }
        var user=User.builder()
                .email(registrationRequest.getEmail())
                .password(passwordEncoder.encode(registrationRequest.getPassword()))
                .firstname(registrationRequest.getFirstname())
                .lastname(registrationRequest.getLastname())
                .roles(List.of(UserRole))
                .build();
        userRepository.save(user);
        sendValidationEmail(user,TOKEN_TYPE.ACCOUNT_ACTIVATION_TOKEN,activationUrl,
                "Account Activation",
                EmailTemplateName.ACTIVATE_ACCOUNT);
    }

    private void sendValidationEmail(User user,TOKEN_TYPE type,String redirectUri,
                                     String subject,EmailTemplateName templateName) throws MessagingException {
        var newToken = generateAndSaveActivationToken(user,type);

        emailService.sendEmail(
                user.getEmail(),
                user.getName(),
                templateName,
                redirectUri,
                newToken,
                subject
        );
    }

    private String generateAndSaveActivationToken(User user, TOKEN_TYPE type) {

        String generatedToken= generateActivationCode(6);

            var token= Token.builder()
                    .token(generatedToken)
                    .createdAt(LocalDateTime.now())
                    .tokenType(type)
                    .expiresAt(LocalDateTime.now().plusMinutes(15))
                    .user(user)
                    .build();

            tokenRepository.save(token);

            return generatedToken;

    }

    private String generateActivationCode(int length) {
        String characters="0123456789";
        StringBuilder sb= new StringBuilder();
        SecureRandom secureRandom = new SecureRandom();
        for (int i=0 ; i<length ;i++){
            int randomIndex=secureRandom.nextInt(characters.length());
            sb.append(characters.charAt(randomIndex));
        }
        return sb.toString();
    }
    
    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) {
         var auth= authenticationManager.authenticate(
                                        new UsernamePasswordAuthenticationToken (
                                                authenticationRequest.getEmail(),
                                                authenticationRequest.getPassword()));

     var Claims=new HashMap<String,Object>();
     var user=(User)auth.getPrincipal();
     Claims.put("fullname",user.getName());
     var jwt=jwtService.GenerateToken(Claims,(User)auth.getPrincipal());
      RefreshToken refreshToken= refreshTokenService.CreateRefreshToken(Math.toIntExact(user.getId()));
     return AuthenticationResponse.builder()
             .accessToken(jwt)
             .refreshToken(refreshToken.getToken())
             .build();

    }

    @Transactional
    public void activateAccount(String token) throws MessagingException,ActivationTokenException{
        Token savedToken= tokenRepository.findByToken(token)
                .orElseThrow(() -> new ActivationTokenException("Invalid Token"));
        if(LocalDateTime.now().isAfter(savedToken.getExpiresAt())){
            sendValidationEmail(savedToken.getUser(),TOKEN_TYPE.ACCOUNT_ACTIVATION_TOKEN,activationUrl,
                    "Account Activation",
                    EmailTemplateName.ACTIVATE_ACCOUNT);
            throw new ActivationTokenException("Activation token has expired. A new token has been send to the same email address");
        }

        var user=userRepository.findById(savedToken.getUser().getId())
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found"));
        user.setEnabled(true);
        userRepository.save(user);

        savedToken.setValidatedAt(LocalDateTime.now());
        tokenRepository.save(savedToken);

    }

    public AuthenticationResponse refreshToken(TokenRefreshRequest refreshRequest) throws TokenRefreshException{
        String  requestRefreshToken = refreshRequest.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token= jwtService.GenerateToken(user);
                    return  AuthenticationResponse.builder()
                            .refreshToken(requestRefreshToken)
                            .accessToken(token)
                            .build();
                })

                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken,
                        "Refresh token is not in database!"));
    }

    public void resetForgottenPassword(String email) throws MessagingException {
        User user=userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found"));

        sendValidationEmail(user,TOKEN_TYPE.RESET_PASSWORD_TOKEN,forgetPasswordUrl,
                "change password",
                EmailTemplateName.CHANGE_PASSWORD);

    }

    public void changePassword(String token,String newPassword) throws  MessagingException,ChangePasswordTokenException{
        Token savedToken= tokenRepository.findByToken(token)
                .orElseThrow(() -> new ChangePasswordTokenException("Invalid Token"));

        if(!savedToken.getTokenType().toString().equals(TOKEN_TYPE.RESET_PASSWORD_TOKEN.toString())){
            throw new ChangePasswordTokenException("token type isn't correct");
        }

        if(savedToken.getValidatedAt() != null){
            throw new ChangePasswordTokenException("token has been used ");
        }

        if(LocalDateTime.now().isAfter(savedToken.getExpiresAt())){
            sendValidationEmail(savedToken.getUser(),TOKEN_TYPE.RESET_PASSWORD_TOKEN,forgetPasswordUrl,
                    "change password",
                    EmailTemplateName.CHANGE_PASSWORD);
            throw new ChangePasswordTokenException("token has expired. A new token has been send to the same email address");
        }
        var user=userRepository.findById(savedToken.getUser().getId())
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found"));
        user.setPassword(newPassword);
        userRepository.save(user);

        savedToken.setValidatedAt(LocalDateTime.now());
        tokenRepository.save(savedToken);

    }

}
