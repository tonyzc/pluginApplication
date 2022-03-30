package com.example.pluginapplication;

import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.ASM9;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class TestMethodAdapter extends MethodVisitor implements Opcodes{
    public TestMethodAdapter(MethodVisitor methodVisitor) {
        super(ASM9, methodVisitor);
    }


    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        System.out.println("== TestMethodVisitor, owner = " + owner + ", name = " + name);

//        mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
//        mv.visitLdcInsn("CALL " + name);
//        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
//
//        mv.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
//
//        mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
//        mv.visitLdcInsn("RETURN " + name);
//        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);



        //方法执行之前打印
        mv.visitLdcInsn("before method exec");
        mv.visitLdcInsn(" [ASM 测试] method in " + owner + " ,name=" + name);
        mv.visitMethodInsn(INVOKESTATIC,
                "android/util/Log", "i", "(Ljava/lang/String;Ljava/lang/String;)I", false);
        mv.visitInsn(POP);

        mv.visitMethodInsn(opcode, owner, name, descriptor, isInterface);

        //方法执行之后打印
        mv.visitLdcInsn(" after method exec");
        mv.visitLdcInsn(" method in " + owner + " ,name=" + name);
        mv.visitMethodInsn(INVOKESTATIC,
                "android/util/Log", "i", "(Ljava/lang/String;Ljava/lang/String;)I", false);
        mv.visitInsn(POP);
    }

    @Override
    public void visitEnd() {
        super.visitEnd();

    }
}
