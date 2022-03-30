package com.example.pluginapplication.transform

import com.example.pluginapplication.base.BaseAsmTransform
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import com.sun.org.apache.bcel.internal.generic.RETURN

import com.sun.org.apache.bcel.internal.generic.INVOKEVIRTUAL

import com.sun.org.apache.bcel.internal.generic.INVOKESTATIC

import com.sun.org.apache.bcel.internal.generic.ALOAD
import jdk.internal.org.objectweb.asm.Opcodes.*


/**
 * Created by Fuzhicheng on 2022/3/24
 */
class TestAsmTransform :BaseAsmTransform() {
    override fun getClassVisitor(classWriter: ClassWriter): ClassVisitor {
        return object : ClassVisitor(Opcodes.ASM9, classWriter) {
            private var className: String? = ""

            override fun visit(version: Int, access: Int, name: String?, signature: String?, superName: String?, interfaces: Array<out String>?) {
                super.visit(version, access, name, signature, superName, interfaces)
                className = name
                println("className: $name")
            }

            override fun visitMethod(access: Int, name: String?, desc: String?, signature: String?, exceptions: Array<out String>?): MethodVisitor {
                var methodVisitor = super.visitMethod(access, name, desc, signature, exceptions)
                println("method: $name")
                return when {
                    className?.contains("Activity") != true -> methodVisitor
                    "onCreate" != name -> methodVisitor
                    else -> object : MethodVisitor(Opcodes.ASM9, methodVisitor) {
                        // 访问方法操作指令
                        override fun visitMethodInsn(opcode: Int, owner: String?, name: String?, desc: String?, itf: Boolean) {
                            println("opcode: $opcode owner: $owner name: $name desc: $desc")
                            super.visitMethodInsn(opcode, owner, name, desc, itf)

//                            mv.visitVarInsn(Opcodes.ALOAD, 0)
//                            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "com/example/pluginapplication/MainActivity", "showASMToast", "()V", false)
                            mv.visitCode()
                            mv.visitLdcInsn("fuzc")
                            mv.visitLdcInsn("onCreate: \u6267\u884c")
                            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "android/util/Log", "d", "(Ljava/lang/String;Ljava/lang/String;)I", false)
                            mv.visitInsn(Opcodes.POP)


                            mv.visitTypeInsn(NEW, "com/example/pluginapplication/Util");
                            mv.visitInsn(DUP);
                            mv.visitVarInsn(ALOAD, 0);
                            mv.visitMethodInsn(INVOKESPECIAL, "com/example/pluginapplication/Util", "<init>", "(Landroid/content/Context;)V", false);
                            mv.visitMethodInsn(INVOKEVIRTUAL, "com/example/pluginapplication/Util", "showAsmToast", "()V", false);

                            mv.visitEnd()

                        }
                    }
                }
            }

        }
    }
}