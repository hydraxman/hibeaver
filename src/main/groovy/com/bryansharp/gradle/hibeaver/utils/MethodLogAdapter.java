package com.bryansharp.gradle.hibeaver.utils;

import org.objectweb.asm.Attribute;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Created by bryansharp on 17/2/15.
 */

public class MethodLogAdapter extends MethodVisitor {

    public MethodLogAdapter(MethodVisitor mv) {
        super(Opcodes.ASM4, mv);
    }

    /**
     * turn "com.bryansharp.util" to "com/bryansharp/util"
     *
     * @param classname full class name
     * @return class path
     */
    public static String className2Path(String classname) {
        return classname.replace('.', '/');
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
        Log.logEach("visitMethodInsn", Log.getOpName(opcode), owner, name, desc);
        super.visitMethodInsn(opcode, owner, name, desc);
    }

    @Override
    public void visitAttribute(Attribute attribute) {
        Log.logEach("visitAttribute", attribute);
        super.visitAttribute(attribute);
    }

    @Override
    public void visitEnd() {
        Log.info("visitEnd");
        super.visitEnd();
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        Log.logEach("visitFieldInsn", Log.getOpName(opcode), owner, name, desc);
        super.visitFieldInsn(opcode, owner, name, desc);
    }

    @Override
    public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
        Log.logEach("visitFrame", type, local, nLocal, nStack, stack);
        super.visitFrame(type, nLocal, local, nStack, stack);
    }

    @Override
    public void visitLabel(Label label) {
        Log.logEach("visitLabel", label);
        super.visitLabel(label);
    }

    @Override
    public void visitLineNumber(int line, Label label) {
        Log.logEach("visitLineNumber", line, label);
        super.visitLineNumber(line, label);
    }

    @Override
    public void visitIincInsn(int var, int increment) {
        Log.logEach("visitIincInsn", var, increment);
        super.visitIincInsn(var, increment);
    }

    @Override
    public void visitIntInsn(int i, int i1) {
        Log.logEach("visitIntInsn", i, i1);
        super.visitIntInsn(i, i1);
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        Log.logEach("visitMaxs", maxStack, maxLocals);
        super.visitMaxs(maxStack, maxLocals);
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
        Log.logEach("visitVarInsn", Log.getOpName(opcode), var);
        super.visitVarInsn(opcode, var);
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        Log.logEach("visitJumpInsn", Log.getOpName(opcode), label);
        super.visitJumpInsn(opcode, label);
    }

    @Override
    public void visitLdcInsn(Object o) {
        Log.logEach("visitLdcInsn", o);
        super.visitLdcInsn(o);
    }

    @Override
    public void visitLookupSwitchInsn(Label label, int[] ints, Label[] labels) {
        Log.logEach("visitLookupSwitchInsn", label, ints, labels);
        super.visitLookupSwitchInsn(label, ints, labels);
    }

    @Override
    public void visitMultiANewArrayInsn(String s, int i) {
        Log.logEach("visitMultiANewArrayInsn", s, i);
        super.visitMultiANewArrayInsn(s, i);
    }

    @Override
    public void visitTableSwitchInsn(int i, int i1, Label label, Label[] labels) {
        Log.logEach("visitTableSwitchInsn", i, i1, label, labels);
        super.visitTableSwitchInsn(i, i1, label, labels);
    }

    @Override
    public void visitTryCatchBlock(Label label, Label label1, Label label2, String s) {
        Log.logEach("visitTryCatchBlock", label, label1, label2, s);
        super.visitTryCatchBlock(label, label1, label2, s);
    }

    @Override
    public void visitTypeInsn(int opcode, String s) {
        Log.logEach("visitTypeInsn", Log.getOpName(opcode), s);
        super.visitTypeInsn(opcode, s);
    }

    @Override
    public void visitCode() {
        Log.info("visitCode");
        super.visitCode();
    }

    @Override
    public void visitLocalVariable(String s, String s1, String s2, Label label, Label label1, int i) {
        Log.logEach("visitLocalVariable", s, s1, s2, label, label1, i);
        super.visitLocalVariable(s, s1, s2, label, label1, i);
    }

    @Override
    public void visitInsn(int opcode) {
        Log.logEach("visitInsn", Log.getOpName(opcode));
        super.visitInsn(opcode);
    }
}
