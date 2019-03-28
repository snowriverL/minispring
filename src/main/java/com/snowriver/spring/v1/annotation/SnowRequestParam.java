package com.snowriver.spring.v1.annotation;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SnowRequestParam {

    String value() default "";
}