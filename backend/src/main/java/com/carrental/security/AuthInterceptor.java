package com.carrental.security;

import com.carrental.common.BusinessException;
import com.carrental.util.JwtUtils;
import io.jsonwebtoken.Claims;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {
    private final JwtUtils jwtUtils;

    @Override
    public boolean preHandle(jakarta.servlet.http.HttpServletRequest request,
                             jakarta.servlet.http.HttpServletResponse response,
                             Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (!StringUtils.hasText(token) || !token.startsWith("Bearer ")) {
            throw new BusinessException(401, "please login first");
        }

        Claims claims = jwtUtils.parseToken(token.substring(7));
        AuthUser authUser = new AuthUser(
            claims.get("userId", Long.class),
            claims.getSubject(),
            claims.get("role", String.class)
        );
        AuthContext.set(authUser);

        RoleAllowed roleAllowed = handlerMethod.getMethodAnnotation(RoleAllowed.class);
        if (roleAllowed == null) {
            roleAllowed = handlerMethod.getBeanType().getAnnotation(RoleAllowed.class);
        }
        if (roleAllowed != null && Arrays.stream(roleAllowed.value()).noneMatch(authUser.getRole()::equals)) {
            throw new BusinessException(403, "permission denied");
        }
        return true;
    }

    @Override
    public void afterCompletion(jakarta.servlet.http.HttpServletRequest request,
                                jakarta.servlet.http.HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        AuthContext.clear();
    }
}
