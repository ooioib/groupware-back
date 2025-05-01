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

/*
    HandlerInterceptor :
    Spring 기반의 Web Application 에서 제공되는 HandlerInterceptor는 컨트롤러 실행 전후에 끼어들어 , 특정 작업을 수행할 수 있는 역할은 한다.
    공통작업 넣고 싶을때 보통 사용하게 된다.
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {
    @Value("${secret}")
    private String secret;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        System.out.println("AuthInterceptor.preHandle() called");
        if(request.getMethod().equals("OPTIONS")) {
            return true;
        }

        String authorization = request.getHeader("Authorization");
        if(authorization == null ||  !authorization.startsWith("Bearer ")) {
            response.sendError(401);
            return false;
        }
        String token = authorization.replace("Bearer ", "");
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret)).withIssuer("groupware").build();
            DecodedJWT jwt  =verifier.verify(token);
            String subject = jwt.getSubject();   // 토큰주인 (로그인 사원의 아이디)
            request.setAttribute("subject", subject);   // request 객체에 추가로 세팅을 시켜서 컨트롤러가 작동되게 유도

            return true;
        }catch(Exception e) {
            response.sendError(401);
            return false;
        }
    }
}