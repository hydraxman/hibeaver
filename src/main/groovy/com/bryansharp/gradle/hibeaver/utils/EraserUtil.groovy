package com.bryansharp.gradle.hibeaver.utils

import com.bryansharp.gradle.hibeaver.entity.MethodInfo
import org.objectweb.asm.*

/**
 * Created by bryansharp(bsp0911932@163.com) on 2016/5/10.
 *
 * @author bryansharp
 * Project: FirstGradle
 * introduction:
 */
public class EraserUtil {

    public
    static List<MethodInfo> collectMethods(byte[] srcByteCode) {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        MethodCollectClassVisitor adapter = new MethodCollectClassVisitor(classWriter);
        ClassReader cr = new ClassReader(srcByteCode);
        //cr.accept(visitor, ClassReader.SKIP_DEBUG);
        cr.accept(adapter, 0);
        return adapter.getMethods();
    }

    static class MethodCollectClassVisitor extends ClassVisitor implements Opcodes {
        private List<MethodInfo> methods = [];

        public MethodCollectClassVisitor(
                final ClassVisitor cv) {
            super(Opcodes.ASM4, cv);
        }

        @Override
        FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
//            Log.logEach('* visitField *', Log.accCode2String(access), name, desc, signature, value);
            return super.visitField(access, name, desc, signature, value)
        }

        @Override
        public MethodVisitor visitMethod(int access, String name,
                                         String desc, String signature, String[] exceptions) {
//            Log.logEach("* visitMethod *", Log.accCode2String(access), name, desc, signature, exceptions);
            return new MethodVisitor(Opcodes.ASM4, cv.visitMethod(access, name, desc, signature, exceptions)) {
                @Override
                AnnotationVisitor visitAnnotation(String annDesc, boolean visible) {
                    //annDesc 形如：Lbruce/com/testhibeaver/Erase;
                    Log.info("---- visitAnnotation ------------>" + annDesc + "," + visible)

                    return new AnnotationVisitor(Opcodes.ASM4, super.visitAnnotation(annDesc, visible)) {
                        @Override
                        void visit(String annKeyName, Object value) {
                            super.visit(annKeyName, value)
                        }
                    }
                }
            };
        }

        public List<MethodInfo> getMethods() {
            return methods;
        }


    }

}