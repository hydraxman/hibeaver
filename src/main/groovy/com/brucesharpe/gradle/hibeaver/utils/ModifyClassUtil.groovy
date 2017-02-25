package com.brucesharpe.gradle.hibeaver.utils

import org.objectweb.asm.*
/**
 * Created by MrBu(bsp0911932@163.com) on 2016/5/10.
 *
 * @author bushaopeng
 * Project: FirstGradle
 * introduction:
 */
public class ModifyClassUtil {

    public
    static byte[] modifyClasses(String className, byte[] classByteCode, List<Map<String, Object>> modifyMatchMaps) {
        byte[] classBytesCode = null;
        try {
            Log.info("====start modifying ${className}====");
            classBytesCode = modifyClass(classByteCode, className, modifyMatchMaps);
            Log.info("====finish modifying ${className}====");
            return classBytesCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return classBytesCode;
    }


    private
    static byte[] modifyClass(byte[] srcClass, String className, List<Map<String, Object>> modifyMatchMaps) throws IOException {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        ClassAdapter adapter = new MethodFilterClassAdapter(classWriter, className, modifyMatchMaps);
        ClassReader cr = new ClassReader(srcClass);
        cr.accept(adapter, ClassReader.SKIP_DEBUG);
        return classWriter.toByteArray();
    }

    static class MethodFilterClassAdapter extends ClassAdapter implements Opcodes {
        private String className;
        private List<Map<String, Object>> modifyMatchMaps;

        public MethodFilterClassAdapter(
                final ClassVisitor cv, String className, List<Map<String, Object>> modifyMatchMaps) {
            super(cv);
            this.className = className;
            this.modifyMatchMaps = modifyMatchMaps;
        }

        @Override
        public void visit(int version, int access, String name,
                          String signature, String superName, String[] interfaces) {
            if (cv != null) {
                cv.visit(version, access, name, signature, superName, interfaces);
            }
        }

        @Override
        public MethodVisitor visitMethod(int access, String name,
                                         String desc, String signature, String[] exceptions) {
            MethodVisitor myMv = null;
            Log.logEach("*visitMethod*${className}*", access, name, desc, signature);
            modifyMatchMaps.each {
                Map<String, Object> map ->
                    String targetClassName = map.get('class').toString();
                    if (className.equals(targetClassName)) {
                        String metName = map.get('methodName');
                        String methodDesc = map.get('methodDesc');
                        if (name.equals(metName)) {
                            Closure visit = map.get('adapter');
                            if (methodDesc != null) {
                                if (methodDesc.equals(desc)) {
                                    myMv = visit(cv, access, name, desc, signature, exceptions);
                                }
                            } else {
                                myMv = visit(cv, access, name, desc, signature, exceptions);
                            }
                        }
                    }
            }
            if (myMv != null) {
                return myMv;
            } else {
                return cv.visitMethod(access, name, desc, signature, exceptions);
            }
        }

    }

}