package com.example.deliveryproductservice.config;

import com.example.deliveryproductservice.annotation.CurrentUser;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {

        if (parameter.hasParameterAnnotation(CurrentUser.class) &&
                parameter.getParameterType().equals(Long.class)) {
            return true;
        }

        return false;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {


        if (parameter.hasParameterAnnotation(CurrentUser.class)) {
            String userIdHeader = webRequest.getHeader("X-User-Id");
            return userIdHeader != null ? Long.parseLong(userIdHeader) : null;
        }


        return null;
    }
}