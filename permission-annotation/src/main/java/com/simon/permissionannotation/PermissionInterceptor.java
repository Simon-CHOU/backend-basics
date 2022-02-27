package com.simon.permissionannotation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
public class PermissionInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Permission methodAnnotation = handlerMethod.getMethodAnnotation(Permission.class);
        if (methodAnnotation == null) {
            // ... check user
            return true;
        }
        PermissionEnum level = methodAnnotation.level();
        if (PermissionEnum.KID.equals(level)) {
            log.info("I'm  kid");
        }
        if (PermissionEnum.MOM.equals(level)) {
            log.info("I'm  mom");
        }
        if (PermissionEnum.DAD.equals(level)) {
            log.info("I'm  dad");
        }
//        return super.preHandle(request, response, handler);
        return true;
    }
}
