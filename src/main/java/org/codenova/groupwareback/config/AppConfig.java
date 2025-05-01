package org.codenova.groupwareback.config;

import org.codenova.groupwareback.interceptor.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AppConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 패턴 표기는 ant 표기를 따르고 있고,
        // 여러 패턴을 동시에 지정하고 싶으면 , 로 여러개 지정하면 된다.
        registry.addInterceptor(authInterceptor).addPathPatterns("/api/private/**", "/api/public/**");
    }
}

