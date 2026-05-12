package com.yt.evaluation_system.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 * 用于生成和解析 JSON Web Token，替代原有的 UUID + Redis 方案。
 * JWT 是自包含令牌，Token 本身携带 userId 和 role 信息，
 * 微服务解析 Token 即可获得用户身份，无需查询 Redis。
 */
public class JwtUtils {

    // 签名密钥（所有微服务共享，因为都引用 common 模块）
    private static final String SECRET_KEY = "CampusFoodRadar2026SecretKey!@#$%^&*";

    // Token 有效期：7 天（单位：毫秒）
    private static final long EXPIRATION = 7 * 24 * 60 * 60 * 1000L;

    /**
     * 生成 JWT Token
     *
     * @param userId 用户 ID
     * @param role   用户角色 (0=普通用户, 1=商家, 2=管理员)
     * @return JWT 字符串
     */
    public static String generateToken(Long userId, Integer role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId.toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    /**
     * 解析 JWT Token，获取 Claims
     *
     * @param token JWT 字符串
     * @return Claims 对象，解析失败返回 null
     */
    public static Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从 JWT Token 中提取用户 ID
     *
     * @param token JWT 字符串
     * @return 用户 ID，解析失败返回 null
     */
    public static Long getUserId(String token) {
        Claims claims = parseToken(token);
        if (claims == null) return null;
        Object userId = claims.get("userId");
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        }
        if (userId instanceof Long) {
            return (Long) userId;
        }
        return Long.valueOf(userId.toString());
    }

    /**
     * 从 JWT Token 中提取用户角色
     *
     * @param token JWT 字符串
     * @return 用户角色，解析失败返回 null
     */
    public static Integer getRole(String token) {
        Claims claims = parseToken(token);
        if (claims == null) return null;
        Object role = claims.get("role");
        if (role instanceof Integer) {
            return (Integer) role;
        }
        return Integer.valueOf(role.toString());
    }

    /**
     * 校验 Token 是否有效（未过期且可解析）
     *
     * @param token JWT 字符串
     * @return true=有效, false=无效或已过期
     */
    public static boolean isValid(String token) {
        return parseToken(token) != null;
    }
}
