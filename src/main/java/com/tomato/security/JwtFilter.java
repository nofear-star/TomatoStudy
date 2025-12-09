package com.tomato.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT 过滤器：验证 token 并将用户信息放入 SecurityContext
 */
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final TokenBlacklistService blacklist;

    public JwtFilter(JwtUtil jwtUtil, TokenBlacklistService blacklist) {
        this.jwtUtil = jwtUtil;
        this.blacklist = blacklist;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // request.getRequestURI() 包含 context-path，所以需要检查 /api/auth/ 或 /auth/
        // 同时允许访问上传的文件资源，不需要JWT认证
        if (path == null) {
            return false;
        }
        return path.startsWith("/api/auth/") 
            || path.startsWith("/auth/")
            || path.startsWith("/api/uploads/")
            || path.startsWith("/uploads/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 先清除之前的认证信息
        SecurityContextHolder.clearContext();
        
        String header = request.getHeader("Authorization");
        String path = request.getRequestURI();
        // request.getRequestURI() 包含 context-path，所以需要检查 /api/auth/ 或 /auth/
        // 同时允许访问上传的文件资源，不需要JWT认证
        boolean isPublicPath = path != null && (
            path.startsWith("/api/auth/") 
            || path.startsWith("/auth/")
            || path.startsWith("/api/uploads/")
            || path.startsWith("/uploads/")
        );
        
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7).trim();
            
            if (token.isEmpty()) {
                if (!isPublicPath) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"success\":false,\"message\":\"Token 不能为空\"}");
                    return;
                }
                filterChain.doFilter(request, response);
                return;
            }
            
            // 检查 token 是否被拉黑
            if (blacklist.isBlacklisted(token)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"success\":false,\"message\":\"Token 已失效，请重新登录\"}");
                return;
            }
            
            // 解析 token 并设置认证信息到 SecurityContext
            try {
                Claims claims = io.jsonwebtoken.Jwts.parserBuilder()
                        .setSigningKey(jwtUtil.getSigningKey())
                        .build()
                        .parseClaimsJws(token)
                        .getBody();
                
                Long userId = Long.valueOf(claims.getSubject());
                
                // 创建认证对象并放入 SecurityContext
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userId, // principal 使用 userId
                        null,   // credentials 不需要
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                );
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (io.jsonwebtoken.ExpiredJwtException e) {
                // Token 已过期
                SecurityContextHolder.clearContext();
                if (!isPublicPath) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"success\":false,\"message\":\"Token 已过期，请重新登录\"}");
                    return;
                }
            } catch (io.jsonwebtoken.JwtException | IllegalArgumentException e) {
                // Token 无效
                SecurityContextHolder.clearContext();
                if (!isPublicPath) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"success\":false,\"message\":\"Token 无效: " + e.getMessage() + "\"}");
                    return;
                }
            } catch (Exception e) {
                // 其他错误
                SecurityContextHolder.clearContext();
                if (!isPublicPath) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"success\":false,\"message\":\"Token 验证失败: " + e.getMessage() + "\"}");
                    return;
                }
            }
        } else if (!isPublicPath) {
            // 对于受保护的资源，如果没有提供 token，返回 401
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"message\":\"缺少 Authorization 头，格式应为: Bearer <token>\"}");
            return;
        }
        
        filterChain.doFilter(request, response);
    }
}

