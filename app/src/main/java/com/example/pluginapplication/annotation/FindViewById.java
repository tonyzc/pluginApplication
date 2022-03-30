package com.example.pluginapplication.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD) //针对成员变量
@Retention(RetentionPolicy.RUNTIME)
public @interface FindViewById {

    // 使用value命名，则使用的时候可以忽略，否则使用时就得把参数名加上
    int value();
}
