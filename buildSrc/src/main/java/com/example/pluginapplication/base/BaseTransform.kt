package com.example.pluginapplication.base

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.ide.common.internal.WaitableExecutor
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

/**
 * Created by Fuzhicheng on 2022/3/24
 */
abstract class BaseTransform : Transform(){
    private val waitableExecutor = WaitableExecutor.useGlobalSharedThreadPool()


    override fun transform(transformInvocation: TransformInvocation) {
        super.transform(transformInvocation)
        // 非增量编译必须先清除之前所有的输出, 否则 transformDexArchiveWithDexMergerForDebug
        if (!transformInvocation.isIncremental) {
            transformInvocation.outputProvider.deleteAll()
        }
        val outputProvider = transformInvocation.outputProvider

        transformInvocation.inputs.forEach { input ->
            //处理jar
            input.jarInputs.forEach { jarInput ->
                //对jar做transform处理
                forEachJarInput(jarInput)
                waitableExecutor.execute {
                    val dest = outputProvider.getContentLocation(jarInput.file.absolutePath, jarInput.contentTypes, jarInput.scopes, Format.JAR)
                    if (transformInvocation.isIncremental) {
                        handleIncrementalJarInput(jarInput, dest)
                    } else {
                        handleNonIncrementalJarInput(jarInput, dest)
                    }
                }
            }

            //处理class
            input.directoryInputs.forEach { directoryInput ->
                //对class做transform处理
                forEachDirectoryInput(directoryInput)
                waitableExecutor.execute {
                    val dest = outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
                    if (transformInvocation.isIncremental) {
                        handleIncrementalDirectoryInput(directoryInput, dest)
                    } else {
                        handleNonIncrementalDirectoryInput(directoryInput.file)
                        FileUtils.copyDirectory(directoryInput.file, dest)
                    }
                }
            }

        }

        // 保证所有任务全部执行完毕再执行后续transform, 传参true表示: 如果其中一个Task抛出异常时终止其他task
        waitableExecutor.waitForTasksWithQuickFail<Any>(true)
    }

    /**
     * 增量处理类修改
     */
    private fun handleIncrementalDirectoryInput(directoryInput: DirectoryInput, dest: File) {
        val srcDirPath = directoryInput.file.absolutePath
        val destDirPath = dest.absolutePath
        directoryInput.changedFiles.forEach { (inputFile, status) ->
            val destFilePath = inputFile.absolutePath.replace(srcDirPath, destDirPath)
            val destFile = File(destFilePath)
            when (status) {
                Status.NOTCHANGED -> {
                }
                Status.ADDED -> {
                    handleNonIncrementalDirectoryInput(inputFile)
                    FileUtils.copyFile(inputFile, destFile)
                }
                Status.REMOVED -> {
                    if (destFile.exists()) {
                        FileUtils.forceDelete(destFile)
                    }
                }
                Status.CHANGED -> {
                    // 如果状态是改变, 说明有历史缓存, 应该先删掉, 再写入我们本次生成的
                    if (dest.exists()) {
                        FileUtils.forceDelete(dest)
                    }
                    handleNonIncrementalDirectoryInput(inputFile)
                    FileUtils.copyFile(inputFile, destFile)
                }
                else -> {
                }
            }
        }
    }

    /**
     * 非增量处理类修改, 可以把 new bytes 直接写回原文件
     * 注意: 必须递归到file, 不能处理路径
     */
    private fun handleNonIncrementalDirectoryInput(inputFile: File) {
        if (inputFile.isDirectory) {
            inputFile.listFiles()?.forEach {
                handleNonIncrementalDirectoryInput(it)
            }
        } else {
            handleSingleFile(inputFile)
        }
    }

    /**
     * 处理单个路径下的单个文件
     */
    private fun handleSingleFile(inputFile: File) {
        println("filePath: "+inputFile.absolutePath + " --fileName: "+inputFile.name)

        if (inputFile.absolutePath.contains(Regex("com.example"))) {
            val inputStream = FileInputStream(inputFile)
            val oldBytes = IOUtils.readBytes(inputStream)
            inputStream.close()

            val newBytes = handleFileBytes(oldBytes, getPackageNameFromPath(inputFile.canonicalPath))
            // 注意!! 实例化FileOutputStream时会清除掉原文件内容!!!!
            val outputStream = FileOutputStream(inputFile)
            outputStream.write(newBytes)
            outputStream.close()
        }
    }

    /**
     * 增量处理JarInput
     */
    private fun handleIncrementalJarInput(jarInput: JarInput, dest: File) {
        when (jarInput.status) {
            Status.NOTCHANGED -> {
            }
            Status.ADDED -> {
                handleNonIncrementalJarInput(jarInput, dest)
            }
            Status.REMOVED -> {
                if (dest.exists()) {
                    FileUtils.forceDelete(dest)
                }
            }
            Status.CHANGED -> {
                // 如果状态是改变, 说明有历史缓存, 应该先删掉, 再写入我们本次生成的
                if (dest.exists()) {
                    FileUtils.forceDelete(dest)
                }
                handleNonIncrementalJarInput(jarInput, dest)
            }
            else -> {
            }
        }
    }


    /**
     * 非增量处理JarInput
     * 两种方式
     * 1. 解压缩, 修改完后再重新压缩
     * 2. 直接通过JarFile进行遍历, 先写入一个新文件中, 再替换原jar
     */
    private fun handleNonIncrementalJarInput(jarInput: JarInput, dest: File) {
        val oldPath = jarInput.file.absolutePath
        val oldJarFile = JarFile(jarInput.file)

        val newPath = oldPath.substring(0, oldPath.lastIndexOf(".")) + ".bak"
        val newFile = File(newPath)
        val newJarOutputStream = JarOutputStream(FileOutputStream(newFile))

        oldJarFile.entries().iterator().forEach {
            newJarOutputStream.putNextEntry(ZipEntry(it.name))
            val inputStream = oldJarFile.getInputStream(it)
            // 修改逻辑 com.youcii.advanced.classname
            if (Regex("com.example").matches(it.name)) {
                val oldBytes = IOUtils.readBytes(inputStream)
                newJarOutputStream.write(handleFileBytes(oldBytes, it.name))
            }
            // 不做改动, 原版复制
            else {
                IOUtils.copy(inputStream, newJarOutputStream)
            }
            newJarOutputStream.closeEntry()
            inputStream.close()
        }

        newJarOutputStream.close()
        oldJarFile.close()

        jarInput.file.delete()
        newFile.renameTo(jarInput.file)

        FileUtils.copyFile(jarInput.file, dest)
    }

    /**
     * 例: /xxxx/xxx/xxx/com/youcii/advanced/xxxx.class
     */
    private fun getPackageNameFromPath(path: String): String {
        // 处理成: youcii/advanced/xxxx.class
        val packagePath = path.substringAfterLast(File.separator + "com" + File.separator)
        println("packagePath: "+ packagePath)
        // 处理成: [youcii, advanced, xxxx.class]
        val packageArray = packagePath.split(File.separator)
        var packageName = "com."
        for (t in packageArray) {
            packageName += "$t."
        }
        println("getPackageNameFromPath: "+packageName.substringBefore(".class."))
        // 当前为 com.youcii.advanced.R.class.
        // 需要去除.class.
        return packageName.substringBefore(".class.")
    }


    override fun getName(): String {
        return javaClass.name
    }

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> {
        return TransformManager.CONTENT_CLASS
    }

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    override fun isIncremental(): Boolean {
        return true
    }

    open fun forEachDirectoryInput(directoryInput: DirectoryInput) {}
    open fun forEachJarInput(jarInput: JarInput) {}
    abstract fun handleFileBytes(oldBytes: ByteArray, className: String): ByteArray
}