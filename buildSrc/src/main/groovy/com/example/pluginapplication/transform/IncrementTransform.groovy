package com.example.pluginapplication.transform

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import org.apache.commons.io.FileUtils
import com.example.pluginapplication.TestClassAdapter
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter

class IncrementTransform extends Transform {

    IncrementTransform(Project project) {
//        project.getExtensions().create()
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation)
        println("******    欢迎使用 IncrementTransform   ******")

        boolean isIncremental = transformInvocation.isIncremental()
        //消费型输入，可以从中获取jar包和class文件夹路径。需要输出给下一个任务
        Collection<TransformInput> inputs = transformInvocation.getInputs()
        //引用型输入，无需输出。
        Collection<TransformInput> referencedInputs = transformInvocation.getReferencedInputs()
//OutputProvider管理输出路径，如果消费型输入为空，你会发现OutputProvider == null
        TransformOutputProvider outputProvider = transformInvocation.getOutputProvider()

        //如果非增量，则清空旧的输出内容
        if(!isIncremental){
            outputProvider.deleteAll()
        }

        for (TransformInput input : inputs) {
            for(JarInput jarInput : input.getJarInputs()) {
                Status status = jarInput.getStatus()

                File dest = outputProvider.getContentLocation(
                        jarInput.getName(),
                        jarInput.getContentTypes(),
                        jarInput.getScopes(),
                        Format.JAR)

                if(isIncremental){
                    switch (status){
                        case NOTCHANGED:
                            break
                        case ADDED:
                        case CHANGED:
                            transformJar(jarInput.getFile(), dest)
                            break
                        case REMOVED:
                            if(dest.exists()){
                                FileUtils.forceDelete(dest)
                            }
                            break
                    }
                }else {
                    transformJar(jarInput.getFile(), dest)
                }

            }

            for(DirectoryInput directoryInput : input.getDirectoryInputs()) {
                File dest = outputProvider.getContentLocation(
                        directoryInput.getName(),
                        directoryInput.getContentTypes(),
                        directoryInput.getScopes(),
                        Format.DIRECTORY)


                //将class文件及目录复制到dest路径
                FileUtils.copyDirectory(directoryInput.getFile(), dest)

                transformDir(directoryInput.getFile(), dest)
            }

        }

    }

    private static void transformJar(File input, File dest) {
        println("=== transformJar ===")
        FileUtils.copyFile(input, dest)
    }

    private static void transformDir(File input, File dest) {
        if (dest.exists()) {
            FileUtils.forceDelete(dest)
        }
        FileUtils.forceMkdir(dest)
        String srcDirPath = input.getAbsolutePath()
        String destDirPath = dest.getAbsolutePath()
        println("=== transform dir = " + srcDirPath + ", " + destDirPath)

        for (File file : input.listFiles()) {
            String destFilePath = file.absolutePath.replace(srcDirPath, destDirPath)
            File destFile = new File(destFilePath)
            if (file.isDirectory()) {
                transformDir(file, destFile)
            } else if (file.isFile()) {
                FileUtils.touch(destFile)
                transformSingleFile(file, destFile)
            }
        }
    }

    private static void transformSingleFile(File input, File dest) {
        println("=== transformSingleFile ===")
        weave(input.getAbsolutePath(), dest.getAbsolutePath())
    }

    private static void weave(String inputPath, String outputPath) {
        try {
            FileInputStream is = new FileInputStream(inputPath)
            ClassReader cr = new ClassReader(is)
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS)
            TestClassAdapter adapter = new TestClassAdapter(cw)
            cr.accept(adapter, ClassReader.EXPAND_FRAMES)
            FileOutputStream fos = new FileOutputStream(outputPath)
            fos.write(cw.toByteArray())
            fos.close()
        } catch (IOException e) {
            e.printStackTrace()
        }
    }

    //transform任务名字（用于尾部拼接）
    @Override
    String getName() {
        return "IncrementalTransform"
    }

    //    Transform需要处理的类型
    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    //transform作用域,要处理所有class字节码，Scope我们一般使用TransformManager.SCOPE_FULL_PROJECT
    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    //增量编译开关
    @Override
    boolean isIncremental() {
        return false
    }
}
