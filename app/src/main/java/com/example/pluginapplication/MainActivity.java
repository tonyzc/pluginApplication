package com.example.pluginapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pluginapplication.annotation.AnnotationParse;
import com.example.pluginapplication.annotation.FindViewById;
import com.example.pluginapplication.annotation.SetOnClickListener;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.logging.Logger;

public class MainActivity extends Activity {

    @FindViewById(R.id.jumpBtn)
    @SetOnClickListener(id = R.id.jumpBtn, methodName = "click")
    private TextView jumpBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AnnotationParse.parse(this);
        jumpBtn.setText("点击xiu");
//        new Util(this).showAsmToast();
        //运行时注解
//        getAllAnnotationView();
    }

    private void showASMToast(){
        Toast.makeText(this,"来自MainActivity Asm toast",Toast.LENGTH_LONG);
    }


    private void click(){
        Toast.makeText(this, "来自运行时注解click方法", Toast.LENGTH_SHORT).show();
    }

    private void getAllAnnotationView() {
        Annotation[] annotations = this.getClass().getDeclaredAnnotations();
        Log.d("Main", "getAllAnnotationView: 个数"+annotations.length+" 注解数组："+annotations.toString());

        //获取成员变量
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields){
            try {
                if(field.getAnnotations()!=null){
                    //是否标注了指定注解
                    if(field.isAnnotationPresent(FindViewById.class)){
                        field.setAccessible(true);
                        FindViewById annotation = field.getAnnotation(FindViewById.class);
                        //找到注解id，findViewById()找到的view赋值给field
                        field.set(this,findViewById(annotation.value()));
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }
    }
}
