//package com.example.deliveryproductservice.config;
//
//import com.example.deliveryproductservice.annotation.CurrentUser;
//import org.springframework.core.MethodParameter;
//import org.springframework.stereotype.Component;
//import org.springframework.web.bind.support.WebDataBinderFactory;
//import org.springframework.web.context.request.NativeWebRequest;
//import org.springframework.web.method.support.HandlerMethodArgumentResolver;
//import org.springframework.web.method.support.ModelAndViewContainer;
//
//@Component
//public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {
//
//    @Override
//    public boolean supportsParameter(MethodParameter parameter) {
//
//        if (parameter.hasParameterAnnotation(CurrentUser.class) &&
//                parameter.getParameterType().equals(Long.class)) {
//            return true;
//        }
//
//        return false;
//    }
//
//    @Override
//    public Object resolveArgument(MethodParameter parameter,
//                                  ModelAndViewContainer mavContainer,
//                                  NativeWebRequest webRequest,
//                                  WebDataBinderFactory binderFactory) {
//
//
//        if (parameter.hasParameterAnnotation(CurrentUser.class)) {
//            String userIdHeader = webRequest.getHeader("X-User-Id");
//            return userIdHeader != null ? Long.parseLong(userIdHeader) : null;
//        }
//
//
//        return null;
//    }
//}

package com.example.deliveryproductservice.config;

import com.example.deliveryproductservice.annotation.CurrentUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * ArgumentResolver для извлечения текущего пользователя из заголовка X-User-Id
 */
@Component
@Slf4j
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean supports = parameter.hasParameterAnnotation(CurrentUser.class)
                && parameter.getParameterType().equals(Long.class);

        if (supports) {
            log.debug("🎯 Supporting @CurrentUser parameter: {}", parameter.getParameterName());
        }

        return supports;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {

        log.debug("🔍 Resolving @CurrentUser parameter");

        try {
            String userIdHeader = webRequest.getHeader("X-User-Id");

            if (userIdHeader != null) {
                Long userId = Long.parseLong(userIdHeader);
                log.debug("✅ Resolved @CurrentUser: {}", userId);
                return userId;
            } else {
                log.warn("⚠️ X-User-Id header not found - user not authenticated?");
                return null;
            }

        } catch (NumberFormatException e) {
            log.error("❌ Invalid X-User-Id header format: {}", webRequest.getHeader("X-User-Id"));
            return null;
        } catch (Exception e) {
            log.error("💥 Error resolving @CurrentUser: {}", e.getMessage(), e);
            return null;
        }
    }
}