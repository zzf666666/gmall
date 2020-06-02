package com.atguigu.gmall.index.config;

import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GmallCache {

    String prefix() default "";

    String lock() default "lock";

    int timeout() default 60;

    int random() default 0;
}
