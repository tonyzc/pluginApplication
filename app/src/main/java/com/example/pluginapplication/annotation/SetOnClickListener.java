package com.example.pluginapplication.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited //是否可以被子类继承父类注解，声明后才可以被继承，仅对类有效
public @interface SetOnClickListener {

    int id();

    String methodName();
}
