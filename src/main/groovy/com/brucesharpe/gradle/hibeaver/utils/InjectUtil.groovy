package com.brucesharpe.gradle.hibeaver.utils

import com.brucesharpe.gradle.hibeaver.PatchProduceParams
import javassist.*
import org.apache.commons.codec.digest.DigestUtils

import java.util.jar.JarOutputStream
/**
 * Created by MrBu(bsp0911932@163.com) on 2016/5/10.
 *
 * @author bushaopeng
 * Project: FirstGradle
 * introduction:
 */
public class InjectUtil {
    private static boolean classPathInited = false;

    public static String getConstructionCode() {
        return "\n\t\t\tandroid.util.Log.i(\"INJECT\",bruce.Inject.class.toString());";
//		return "if (java.lang.Boolean.FALSE.booleanValue()) {\n\t\t\tSystem.out.println(bruce.Inject.class);\n\t\t}";
    }

    public static String injectConstructionClasses(String className, String buildDir, JarOutputStream stream,String oldHash) {
        Log.info "正在注入类${className}"
        String hash = null;
        try {
            CtClass ctClass = ClassPool.getDefault().get(className);
            CtConstructor[] conts = ctClass.getDeclaredConstructors();
            CtConstructor ctConstructor;
            StringBuffer buffer2;
            if ((conts == null) || (conts.length == 0)) {
                ctConstructor = new CtConstructor(new CtClass[0], ctClass);
                buffer2 = new StringBuffer();
                buffer2.append("{\n\n")
                        .append(getConstructionCode())
                        .append("}");

                ctConstructor.setBody(buffer2.toString());
                ctClass.addConstructor(ctConstructor);
            } else {
                ctConstructor = conts[0];
                boolean succ = false;
                try {
                    ctConstructor.getParameterTypes();
                    ctConstructor.insertBeforeBody(getConstructionCode());
                    succ = true;
                } catch (NotFoundException e) {
                    succ = false;
                }
                if (!succ) {
                    ctConstructor = new CtConstructor(new CtClass[0], ctClass);
                    buffer2 = new StringBuffer();
                    buffer2.append("{\n\n")
                            .append(getConstructionCode())
                            .append("}");
                    ctConstructor.setBody(buffer2.toString());
                    ctClass.addConstructor(ctConstructor);
                }
            }
            byte[] bytes = ctClass.toBytecode();
            hash = DigestUtils.shaHex(bytes);
            if((!oldHash)||!oldHash.equals(hash)){
                patchToClass(ctClass)
            }
            if (stream == null) {
                ctClass.writeFile(buildDir);
            } else {
                stream.write(bytes);
            }
            ctClass.detach();
        } catch (Exception e) {
            e.printStackTrace();
        }
        DataHelper.ext.classMap.put(className, hash)
        return hash;
    }

    public static void initClassPathList(List<String> classPathList) {
        try {
            if (!classPathInited) {
                for (String classDir : classPathList) {
                    File cp = new File(classDir);
                    ClassPath mClassPath = ClassPool.getDefault()
                            .insertClassPath(cp.getAbsolutePath().replaceAll("\\\\", "/"));
                    Log.info("add classpath : " + cp.getAbsolutePath());
                }
                classPathInited = true;
            }
        } catch (NotFoundException e1) {
            e1.printStackTrace();
        }
    }

    public static void patchToClass(CtClass source) {
        PatchProduceParams patchProduceParams = DataHelper.ext.producePatch;
        if (patchProduceParams.producePatchEnable) {
            Log.info "发生变化的类：${source.name}"
            File patchTmpDir = DataHelper.ext.patchTempDir
            source.writeFile(patchTmpDir.absolutePath)
        }
    }
}