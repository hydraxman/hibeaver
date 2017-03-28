package com.bryansharp.gradle.hibeaver.utils

import org.objectweb.asm.*

/**
 * Created by bryansharp(bsp0911932@163.com) on 2016/5/10.
 *
 * @author bryansharp
 * Project: FirstGradle
 * introduction:
 */
public class ModifyClassUtil {

    public
    static byte[] modifyClasses(String className, byte[] srcByteCode, Object container) {
        List<Map<String, Object>> methodMatchMaps = getList(container);
        byte[] classBytesCode = null;
        if (methodMatchMaps) {
            try {
                Log.info("====start modifying ${className}====");
                classBytesCode = modifyClass(srcByteCode, methodMatchMaps);
                Log.info("====revisit modified ${className}====");
                onlyVisitClassMethod(classBytesCode, methodMatchMaps);
                Log.info("====finish modifying ${className}====");
                return classBytesCode;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (classBytesCode == null) {
            classBytesCode = srcByteCode;
        }
        return classBytesCode;
    }

    static List<Map<String, Object>> getList(Object container) {
        if (container instanceof List) {
            return container;
        } else if (container instanceof Map) {
            return (List<Map<String, Object>>) container.get(Const.KEY_MODIFYMETHODS);
        }
        return null;
    }

    private
    static byte[] modifyClass(byte[] srcClass, List<Map<String, Object>> modifyMatchMaps) throws IOException {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        ClassVisitor adapter = new MethodFilterClassVisitor(classWriter, modifyMatchMaps);
        ClassReader cr = new ClassReader(srcClass);
        //cr.accept(visitor, ClassReader.SKIP_DEBUG);
        cr.accept(adapter, 0);
        return classWriter.toByteArray();
    }

    private
    static void onlyVisitClassMethod(byte[] srcClass, List<Map<String, Object>> modifyMatchMaps) throws IOException {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        MethodFilterClassVisitor visitor = new MethodFilterClassVisitor(classWriter, modifyMatchMaps);
        visitor.onlyVisit = true;
        ClassReader cr = new ClassReader(srcClass);
        cr.accept(visitor, 0);
    }

    static class MethodFilterClassVisitor extends ClassVisitor implements Opcodes {
//        private String className;
        private List<Map<String, Object>> methodMatchMaps;
        public boolean onlyVisit = false;

        public MethodFilterClassVisitor(
                final ClassVisitor cv, List<Map<String, Object>> methodMatchMaps) {
            super(Opcodes.ASM4, cv);
//            this.className = className;
            this.methodMatchMaps = methodMatchMaps;
        }

        @Override
        void visitEnd() {
            Log.logEach('* visitEnd *');
            super.visitEnd()
        }

        @Override
        void visitAttribute(Attribute attribute) {
            Log.logEach('* visitAttribute *', attribute, attribute.type, attribute.metaClass, attribute.metaPropertyValues, attribute.properties);
            super.visitAttribute(attribute)
        }

        @Override
        AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            Log.logEach('* visitAnnotation *', desc, visible);
            return super.visitAnnotation(desc, visible)
        }

        @Override
        void visitInnerClass(String name, String outerName,
                             String innerName, int access) {
            Log.logEach('* visitInnerClass *', name, outerName, innerName, Log.accCode2String(access));
            super.visitInnerClass(name, outerName, innerName, access)
        }

        @Override
        void visitOuterClass(String owner, String name, String desc) {
            Log.logEach('* visitOuterClass *', owner, name, desc);
            super.visitOuterClass(owner, name, desc)
        }

        @Override
        void visitSource(String source, String debug) {
            Log.logEach('* visitSource *', source, debug);
            super.visitSource(source, debug)
        }

        @Override
        FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
            Log.logEach('* visitField *', Log.accCode2String(access), name, desc, signature, value);
            return super.visitField(access, name, desc, signature, value)
        }

        @Override
        public void visit(int version, int access, String name,
                          String signature, String superName, String[] interfaces) {
            Log.logEach('* visit *', Log.accCode2String(access), name, signature, superName, interfaces);
            super.visit(version, access, name, signature, superName, interfaces);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name,
                                         String desc, String signature, String[] exceptions) {
            MethodVisitor myMv = null;
            if (!onlyVisit) {
                Log.logEach("* visitMethod *", Log.accCode2String(access), name, desc, signature, exceptions);
            }
            methodMatchMaps.each {
                Map<String, Object> map ->
                    String metName = map.get(Const.KEY_METHODNAME);
                    String metMatchType = map.get(Const.KEY_METHODMATCHTYPE);
                    String methodDesc = map.get(Const.KEY_METHODDESC);
                    if (Util.isPatternMatch(metName, metMatchType, name)) {
                        Closure visit = map.get(Const.KEY_ADAPTER);
                        if (visit != null) {
                            //methodDesc 不设置，为空，即代表对methodDesc不限制
                            if (methodDesc != null) {
                                if (Util.isPatternMatch(methodDesc, metMatchType, desc)) {
                                    if (onlyVisit) {
                                        myMv = new MethodLogAdapter(cv.visitMethod(access, name, desc, signature, exceptions));
                                    } else {
                                        try {
                                            myMv = visit(cv, access, name, desc, signature, exceptions);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            myMv = null
                                        }
                                    }
                                }
                            } else {
                                try {
                                    myMv = visit(cv, access, name, desc, signature, exceptions);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    myMv = null
                                }
                            }
                        }
                    }
            }
            if (myMv != null) {
                if (onlyVisit) {
                    Log.logEach("* revisitMethod *", Log.accCode2String(access), name, desc, signature);
                }
                return myMv;
            } else {
                return cv.visitMethod(access, name, desc, signature, exceptions);
            }
        }

    }

}