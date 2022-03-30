package com.example.compiler;

import com.example.annotation.BindView;
import com.example.annotation.BindViews;
import com.example.annotation.Data;
import com.google.auto.service.AutoService;
import com.squareup.javawriter.JavaWriter;

import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

@AutoService(Processor.class)
public class CustomProcessor extends AbstractProcessor {
    private Messager messager;//用于日志
    private Elements elementUtils;
    private Filer filer;//用于生成Java或class文件

    //指定支持的注解类型
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        for (Class<? extends Annotation> annotation : getSupportedAnnotations()) {
            types.add(annotation.getCanonicalName());
        }
        return types;
    }

    private Set<Class<? extends Annotation>> getSupportedAnnotations() {
        Set<Class<? extends Annotation>> annotations = new LinkedHashSet<>();
        annotations.add(BindView.class);
        annotations.add(BindViews.class);
        annotations.add(Data.class);
        return annotations;
    }

    //支持的Java版本
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        messager = processingEnvironment.getMessager();
        elementUtils = processingEnvironment.getElementUtils();
        filer = processingEnvironment.getFiler();
    }

    //处理入口
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        messager.printMessage(Diagnostic.Kind.NOTE, "-----开始执行注解处理器");
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(Data.class);
        String className = null;//需要处理的类名
        Element element = null;//需要处理的对象

        for (Element e : elements) {
            if (e.getKind() == ElementKind.CLASS && e instanceof TypeElement) {
                TypeElement t = (TypeElement) e;
                className = t.getQualifiedName().toString();
                element = t;
                break;
            }
        }

        try {
            //返回类内的所有节点
            List<? extends Element> enclosedElements = element.getEnclosedElements();
            // 保存字段的集合
            Map<TypeMirror, Name> fieldMap = new HashMap<>();
            for (Element ele : enclosedElements) {
                if(ele.getKind() == ElementKind.FIELD){
                    //字段的类型
                    TypeMirror typeMirror = ele.asType();
                    //字段的名称
                    Name simpleName = ele.getSimpleName();
                    fieldMap.put(typeMirror, simpleName);
                }
            }

            //生成Java源文件
            JavaFileObject sourceFile = filer.createSourceFile(getClassName(className));
            //写入代码到文件
            createSourceFile(className, fieldMap, sourceFile.openWriter());
            //手动编译
//            doCompile(sourceFile.toUri().getPath());

        } catch (Exception e) {
            e.printStackTrace();
        }


        return false;
    }

    private void createSourceFile(String className, Map<TypeMirror, Name> fieldMap, Writer openWriter) throws Exception{

        // 生成源代码
        JavaWriter jw = new JavaWriter(openWriter);
        jw.emitPackage(getPackage(className));
        jw.beginType(getClassName(className), "class", EnumSet.of(Modifier.PUBLIC));
        for (Map.Entry<TypeMirror, Name> map : fieldMap.entrySet()) {
            String type = map.getKey().toString();
            String name = map.getValue().toString();
            //字段
            jw.emitField(type, name, EnumSet.of(Modifier.PRIVATE));
        }
        for (Map.Entry<TypeMirror, Name> map : fieldMap.entrySet()) {
            String type = map.getKey().toString();
            String name = map.getValue().toString();
            //getter
            jw.beginMethod(type, "get" + humpString(name), EnumSet.of(Modifier.PUBLIC))
                    .emitStatement("return " + name)
                    .endMethod();
            //setter
            jw.beginMethod("void", "set" + humpString(name), EnumSet.of(Modifier.PUBLIC), type, "arg")
                    .emitStatement("this." + name + " = arg")
                    .endMethod();
        }
        jw.endType().close();

    }

    /**
     * 编译文件
     * @param path
     * @throws IOException
     */
    private void doCompile(String path) throws IOException {
        //拿到编译器
        JavaCompiler complier = ToolProvider.getSystemJavaCompiler();
        //文件管理者
        StandardJavaFileManager fileMgr =
                complier.getStandardFileManager(null, null, null);
        //获取文件
        Iterable units = fileMgr.getJavaFileObjects(path);
        //编译任务
        JavaCompiler.CompilationTask t = complier.getTask(null, fileMgr, null, null, null, units);
        //进行编译
        t.call();
        fileMgr.close();
    }

    /**
     * 驼峰命名
     *
     * @param name
     * @return
     */
    private String humpString(String name) {
        String result = name;
        if (name.length() == 1) {
            result = name.toUpperCase();
        }
        if (name.length() > 1) {
            result = name.substring(0, 1).toUpperCase() + name.substring(1);
        }
        return result;
    }

    /**
     * 读取包名
     * @param name
     * @return
     */
    private String getPackage(String name) {
        String result;
        if (name.contains(".")) {
            result = name.substring(0, name.lastIndexOf("."));
        }else {
            result = "";
        }
        return result;
    }


    /**
     * 读取类名，类全名命名规则为Package.className
     *
     * @param name
     * @return
     */
    private String getClassName(String name) {
        String result = name;
        if (name.contains(".")) {
            //截取最后一个.后面的字符串
            result = name.substring(name.lastIndexOf(".") + 1)+"Inject";
        }
        return result;
    }


}
