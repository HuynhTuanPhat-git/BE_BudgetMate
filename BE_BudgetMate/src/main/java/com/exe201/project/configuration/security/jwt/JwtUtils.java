package com.exe201.project.configuration.security.jwt;

import com.exe201.project.entity.User;
import com.exe201.project.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Component
@Slf4j
public class JwtUtils {

    @Value("${budgetmate.app.jwtSecret}")
    private String jwtSecret;
    @Value("${budgetmate.app.jwtExpirationMs}")
    private int jwtExpirationMs;
    @Autowired
    private UserRepository userRepository;
    // Biến static để lưu giá trị jwtSecret cho các phương thức static
    private static String staticJwtSecret;

    // Phương thức khởi tạo sau khi bean được tạo, gán giá trị từ jwtSecret sang staticJwtSecret
    @PostConstruct
    public void init() {
        staticJwtSecret = jwtSecret;
    }

    private Key key(){
        return Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(jwtSecret));
    }

    public boolean validateToken(String jwt){
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parse(jwt);
            return true;
        }catch (MalformedJwtException e){
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    public String getEmailFromJwtToken(String jwt){
        return Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(jwt).getBody().getSubject();
    }

    public String generateTokenFromUsername(String email){
        User user = userRepository.findByEmail(email).get();
        return Jwts.builder()
                .setSubject(email)
                .setIssuer("budget-mate-system")
                .claim("userId", user.getId())
                .claim("role", user.getRole().getName())
                .signWith(key(), SignatureAlgorithm.HS256)
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + jwtExpirationMs))
                .setId(String.valueOf(UUID.randomUUID()))
                .compact();
    }

    public String generateJwtToken(UserDetailsImpl userPrincipal){
        return generateTokenFromUsername(userPrincipal.getUsername());
    }

    public String generateVerificationToken(String username){
        return Jwts.builder()
                .setSubject(username)
                .setIssuer("budget-mate-system")
                .signWith(key(), SignatureAlgorithm.HS256)
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().toInstant().plus(1, ChronoUnit.DAYS).toEpochMilli()))
                .setId(String.valueOf(UUID.randomUUID()))
                .compact();
    }

    public String generateResetPasswordToken(String email){
        return Jwts.builder()
                .setSubject(email)
                .setIssuer("budget-mate-system")
                .signWith(key(), SignatureAlgorithm.HS256)
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().toInstant().plus(1, ChronoUnit.DAYS).toEpochMilli()))
                .setId(String.valueOf(UUID.randomUUID()))
                .compact();
    }

    public Date getExpDateFromToken(String token){
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody().getExpiration();
    }

    public static String getRoleFromJwtToken(String token) {
        // Giải mã token và lấy phần body chứa các claims
        Claims claims = Jwts.parser()
                .setSigningKey(staticJwtSecret)
                .parseClaimsJws(token)
                .getBody();
        return claims.get("role", String.class);
    }

    public static String getSubFromJwtToken(String token) {
        // Giải mã token và lấy phần body chứa các claims
        Claims claims = Jwts.parser()
                .setSigningKey(staticJwtSecret)
                .parseClaimsJws(token)
                .getBody();
        return claims.get("sub", String.class);
    }
}
