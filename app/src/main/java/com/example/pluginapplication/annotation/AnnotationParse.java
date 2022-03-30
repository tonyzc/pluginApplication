package com.example.pluginapplication.annotation;

import android.app.Activity;
import android.view.View;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class AnnotationParse {

    public static void parse(final Activity activity){
        try {
        //获取成员变量
        Field[] fields = activity.getClass().getDeclaredFields();
        Method[] methods = activity.getClass().getDeclaredMethods();

        FindViewById findIdAnnotation;
        SetOnClickListener clickListenerAnnotation;

        for (Field field : fields){
            Annotation[] annotations = field.getAnnotations();
            for (Annotation annotation:annotations){
                if(annotation instanceof FindViewById){
                    findIdAnnotation = field.getAnnotation(FindViewById.class);
                    field.setAccessible(true);
                    int id = findIdAnnotation.value();
                    View view = activity.findViewById(id);
                    if(view!=null){
                        field.set(activity,view);
                    }else {
                        throw new Exception("view id " +id+" is invalid");
                    }
                }else if(annotation instanceof SetOnClickListener){
                    clickListenerAnnotation = field.getAnnotation(SetOnClickListener.class);
                    field.setAccessible(true);
                    int id = clickListenerAnnotation.id();
                    String methodName = clickListenerAnnotation.methodName();
                    View view = (View) field.get(activity);
                    if(view  == null){
                        //如果该对象未初始化，则重新初始化,并对activity中的该变量进行赋值
                        view = activity.findViewById(id);
                        if(view!=null){
                            field.set(activity,view);
                        }else {
                            throw new Exception("view id " +id+" is invalid");
                        }
                    }

                    for(final Method method:methods){
                        //找到对应注解名字的方法
                        if(method.getName().equals(methodName)){
                            view.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    try {
                                        method.setAccessible(true);
                                        method.invoke(activity);
                                    } catch (IllegalAccessException e) {
                                        e.printStackTrace();
                                    } catch (InvocationTargetException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            break;
                        }
                    }

                }
            }

        }

        }catch (Exception e) {
            e.printStackTrace();
        }

    }


}
