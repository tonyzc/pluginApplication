package com.example.pluginapplication;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.ASM9;

public class TestClassAdapter extends ClassVisitor {
    private String className;

    public TestClassAdapter(ClassVisitor classVisitor){
        super(ASM9,classVisitor);
    }


    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.className = name;
    }

    //扫描到类字段回调
    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        return super.visitField(access, name, descriptor, signature, value);
    }

    //扫描到类方法回调
    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
//        return mv == null?null:new TestMethodAdapter(mv);

        if(className.contains("MainActivity")){
            return new TestMethodAdapter(mv);
        }else {
            return mv;
        }
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
        cv.visitField(Opcodes.ACC_PUBLIC,"age", Type.getDescriptor(int.class), null, null);

    }
}
