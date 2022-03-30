package com.example.pluginapplication;

import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.Format;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.api.transform.TransformOutputProvider;
import com.android.build.gradle.internal.pipeline.TransformManager;
import com.android.utils.FileUtils;

import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

public class DemoTransform extends Transform {
    Project project;

    public DemoTransform(Project project) {
        this.project = project;
    }

    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation);
        System.out.println("******       欢迎使用 DemoTransform  1.0.3编译插件     ******");
        System.out.println("project name:"+project.getName()+"  displayName:"+project.getDisplayName() +"  desc:"+project.getDescription());


        boolean isIncremental = transformInvocation.isIncremental();
        //消费型输入，可以从中获取jar包和class文件夹路径。需要输出给下一个任务
        Collection<TransformInput> inputs = transformInvocation.getInputs();
        //引用型输入，无需输出。
        Collection<TransformInput> referencedInputs = transformInvocation.getReferencedInputs();
//OutputProvider管理输出路径，如果消费型输入为空，你会发现OutputProvider == null
        TransformOutputProvider outputProvider = transformInvocation.getOutputProvider();

        //先处理jar文件
        for (TransformInput input : inputs) {
            for(JarInput jarInput : input.getJarInputs()) {
                System.out.println("jar= " + jarInput.getName());
                File dest = outputProvider.getContentLocation(
                        jarInput.getFile().getAbsolutePath(),
                        jarInput.getContentTypes(),
                        jarInput.getScopes(),
                        Format.JAR);
                //to do some transform


                // 将修改过的字节码copy到dest，就可以实现编译期间干预字节码的目的了
                FileUtils.copyFile(jarInput.getFile(), dest);
            }

            //再处理class
            for(DirectoryInput directoryInput : input.getDirectoryInputs()) {
                if(directoryInput.getFile().isDirectory()){
                    for (File file : FileUtils.getAllFiles(directoryInput.getFile())) {
                        System.out.println("directoryInput--"+file.getName());
                    }
                }


                File dest = outputProvider.getContentLocation(
                        directoryInput.getName(),
                        directoryInput.getContentTypes(),
                        directoryInput.getScopes(),
                        Format.DIRECTORY);
                //建立文件夹
                FileUtils.mkdirs(dest);

                //to do some transform

                //将class文件及目录复制到dest路径
                FileUtils.copyDirectory(directoryInput.getFile(), dest);
            }

        }

    }

    //transform任务名字（用于尾部拼接）
    @Override
    public String getName() {
        return "DemoTransform";
    }

    //    Transform需要处理的类型
    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    //transform作用域,要处理所有class字节码，Scope我们一般使用TransformManager.SCOPE_FULL_PROJECT
    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT;
    }

    //增量编译开关,true只有增量编译时才回生效
    @Override
    public boolean isIncremental() {
        return true;
    }
}
