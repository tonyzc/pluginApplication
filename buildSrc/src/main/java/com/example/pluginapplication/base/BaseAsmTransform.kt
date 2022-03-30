package com.example.pluginapplication.base

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes

/**
 * Created by Fuzhicheng on 2022/3/24
 */
abstract class BaseAsmTransform:BaseTransform() {

    override fun handleFileBytes(oldBytes: ByteArray, className: String): ByteArray {
        return try {
            val classReader = ClassReader(oldBytes)
            val classWriter = ClassWriter(ClassWriter.COMPUTE_MAXS)
            val classVisitor = getClassVisitor(classWriter)
            classReader.accept(classVisitor, Opcodes.ASM9)
            classWriter.toByteArray()
        } catch (e: ArrayIndexOutOfBoundsException) {
            oldBytes
        } catch (e: IllegalArgumentException) {
            oldBytes
        }
    }

    abstract fun getClassVisitor(classWriter: ClassWriter): ClassVisitor
}