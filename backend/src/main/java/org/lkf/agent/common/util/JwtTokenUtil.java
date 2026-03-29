package org.lkf.agent.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.lkf.agent.common.exception.BusinessException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * JWT 工具类。
 */
public final class JwtTokenUtil {

    /**
     * JWT 密钥。
     */
    private static final String SECRET_KEY_TEXT = "agent-chat-jwt-secret-key-for-v1-please-change";

    /**
     * 令牌有效期（秒）。
     */
    private static final long EXPIRE_SECONDS = 24 * 60 * 60;

    /**
     * 密钥对象。
     */
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET_KEY_TEXT.getBytes(StandardCharsets.UTF_8));

    /**
     * 私有构造器。
     */
    private JwtTokenUtil() {
    }

    /**
     * 生成访问令牌。
     *
     * @param username 用户名
     * @return 访问令牌
     */
    public static String generateToken(String username) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(EXPIRE_SECONDS)))
                .signWith(SECRET_KEY)
                .compact();
    }

    /**
     * 解析用户名。
     *
     * @param token 访问令牌
     * @return 用户名
     * @throws BusinessException 令牌无效时抛出
     */
    public static String parseUsername(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(SECRET_KEY).build().parseSignedClaims(token).getPayload();
            return claims.getSubject();
        } catch (Exception exception) {
            throw new BusinessException(401, "令牌无效或已过期");
        }
    }
}
