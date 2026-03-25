package com.carrental.config;

import com.carrental.security.AuthInterceptor;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
    private final AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
            .addPathPatterns("/api/**")
            .excludePathPatterns(
                "/api/auth/login",
                "/api/auth/register",
                "/api/public/**",
                "/api/pay/alipay/notify",
                "/api/pay/alipay/return",
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html"
            );
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOriginPatterns("*")
            .allowedMethods("*")
            .allowedHeaders("*")
            .allowCredentials(false);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get(System.getProperty("user.dir"), "uploads");
        registry.addResourceHandler("/uploads/**")
            .addResourceLocations(uploadPath.toUri().toString());
    }
}
