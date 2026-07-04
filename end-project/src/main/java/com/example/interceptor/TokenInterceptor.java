package com.example.interceptor;

import com.example.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class TokenInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String requestURI = request.getRequestURI();

        if (requestURI.contains("/api/auth/login")){
            log.info("登录请求，放行");
            return true;
        }

        // 注册相关公开接口放行
        if (requestURI.contains("/api/auth/register") ||
            requestURI.contains("/api/auth/majors") ||
            requestURI.contains("/api/auth/enterprises")){
            log.info("公开接口请求，放行");
            return true;
        }

        String token = request.getHeader("token");

        if (token == null || token.isEmpty()){
            log.info("令牌为空，不放行");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        try {
            JwtUtils.parseToken(token);
        }catch (Exception e){
            log.info("令牌错误");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        log.info("令牌合法");
        return true;
    }
}
