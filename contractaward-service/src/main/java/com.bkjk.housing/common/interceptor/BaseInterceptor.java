package com.bkjk.housing.common.interceptor;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import java.lang.annotation.Annotation;
import java.util.Objects;

public class BaseInterceptor extends HandlerInterceptorAdapter {

    protected <A extends Annotation> A getAnnotation(final Object handler, final Class<A> annotation) {
        final HandlerMethod handlerMethod = (HandlerMethod) handler;
        A annotationInstance = handlerMethod.getBeanType().getAnnotation(annotation);
        if (Objects.nonNull(annotationInstance)) {
            return annotationInstance;
        }
        annotationInstance = handlerMethod.getMethod().getAnnotation(annotation);
        return annotationInstance;
    }
    
}