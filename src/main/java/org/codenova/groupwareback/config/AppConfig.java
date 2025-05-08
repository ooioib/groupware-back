package org.codenova.groupwareback.config;

import org.codenova.groupwareback.interceptor.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration // 설정 클래스임을 나타냄 (스프링이 자동으로 읽어들임)
public class AppConfig implements WebMvcConfigurer {

    // JWT 인증을 담당할 인터셉터 주입
    @Autowired
    private AuthInterceptor authInterceptor;

    // 인터셉터 등록 설정
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // authInterceptor를 적용할 URL 패턴 지정
        // 패턴 표기는 ant 표기를 따르고 있고,
        // 여러 패턴을 동시에 지정하고 싶으면 , 로 여러개 지정하면 된다.
        registry.addInterceptor(authInterceptor)
                .addPathPatterns(
                        "/api/private/**",   // /api/private 이하 모든 요청
                        "/api/board/**",     // /api/board 이하 모든 요청
                        "/api/note/**",      // /api/note 이하 모든 요청
                        "/api/chat/**"
                );
    }
}

