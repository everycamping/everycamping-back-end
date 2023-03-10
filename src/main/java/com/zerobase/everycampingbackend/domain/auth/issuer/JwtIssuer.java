package com.zerobase.everycampingbackend.domain.auth.issuer;


import com.zerobase.everycampingbackend.domain.auth.dto.JwtDto;
import com.zerobase.everycampingbackend.domain.auth.dto.UserVo;
import com.zerobase.everycampingbackend.domain.auth.util.Aes256Util;
import com.zerobase.everycampingbackend.exception.CustomException;
import com.zerobase.everycampingbackend.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtIssuer {

    private static final String SECRET_KEY = "secretKey";
    private static final String REFRESH_SUBJECT = "REFRESH";
    public static final long EXPIRE_TIME = 1000 * 60 * 5;
    public static final long REFRESH_EXPIRE_TIME = 1000 * 60 * 60;
    public static final String KEY_ROLES = "roles";

    @Bean
    PasswordEncoder getPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    public JwtDto createToken(String userEmail, Long id, String role) {
        Claims claims = Jwts.claims().setSubject(Aes256Util.encrypt(userEmail))
            .setId(Aes256Util.encrypt(id.toString()));
        claims.put(KEY_ROLES, role);

        Date now = new Date();

        String accessToken = Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(new Date(now.getTime() + EXPIRE_TIME))
            .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
            .compact();

        claims.setSubject(REFRESH_SUBJECT);

        String refreshToken = Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(new Date(now.getTime() + EXPIRE_TIME * 2))
            .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
            .compact();

        return JwtDto.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .build();
    }

    public UserVo getUserVo(Claims claims) {
        return new UserVo(
            Long.parseLong(Objects.requireNonNull(Aes256Util.decrypt(claims.getId()))),
            Aes256Util.decrypt(claims.getSubject()));
    }

    public Claims getClaims(String token) {
        Claims claims;
        try {
            claims = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            claims = e.getClaims();
        } catch (Exception e) {
            throw new CustomException(ErrorCode.TOKEN_NOT_VALID);
        }
        return claims;
    }

}
