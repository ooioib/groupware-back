package org.codenova.groupwareback.interceptor;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

// 요청 전에 JWT 인증을 검사하는 인터셉터
@Component
public class AuthInterceptor implements HandlerInterceptor {

    // JWT 검증용 비밀키 (application.yml에 정의된 값 주입)
    @Value("${secret}")
    private String secret;

    // 컨트롤러 실행 전에 실행되는 메서드
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        System.out.println("AuthInterceptor.preHandle() called");

        // OPTIONS 요청은 검사 없이 통과
        if (request.getMethod().equals("OPTIONS")) {
            return true;
        }

        // Authorization 헤더 확인
        String authorization = request.getHeader("Authorization");

        // 헤더가 없거나 형식이 틀리면 인증 실패 (401 Unauthorized)
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            response.sendError(401);
            return false;
        }

        // 토큰 추출
        String token = authorization.replace("Bearer ", "");

        // JWT 검증기를 생성 (HMAC256 + 비밀 키, 발급자 일치 여부 포함)
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                    .withIssuer("groupware")
                    .build();

            // 토큰 검증 및 디코딩
            DecodedJWT jwt = verifier.verify(token);

            // JWT에 저장된 사용자 식별자(subject) 추출
            String subject = jwt.getSubject();

            // 사용자 ID를 request 객체에 저장
            request.setAttribute("subject", subject);

            // 인증 성공 시 요청 계속 진행
            return true;

        } catch (Exception e) {
            // 검증 실패 시 401
            response.sendError(401);
            return false;
        }
    }
}
