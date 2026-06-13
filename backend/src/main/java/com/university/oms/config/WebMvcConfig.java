package com.university.oms.config;

import com.university.oms.security.AuthInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;

/**
 * Spring MVC配置类，注册拦截器和字符编码过滤器
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    /** 认证拦截器，用于校验用户登录状态 */
    private final AuthInterceptor authInterceptor;

    public WebMvcConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    /** 注册认证拦截器，拦截所有/api/**路径的请求 */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor).addPathPatterns("/api/**");
    }

    /** 配置UTF-8字符编码过滤器，强制请求和响应使用UTF-8编码 */
    @Bean
    public CharacterEncodingFilter characterEncodingFilter() {
        CharacterEncodingFilter filter = new CharacterEncodingFilter(StandardCharsets.UTF_8.name());
        filter.setForceRequestEncoding(true);
        filter.setForceResponseEncoding(true);
        return filter;
    }
}
