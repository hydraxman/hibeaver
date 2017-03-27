package com.bryansharp.gradle.hibeaver.utils

import com.bryansharp.gradle.hibeaver.InjectTransform
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.IOUtils

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

/**
 * Created by bryansharp(bsp0911932@163.com) on 2016/5/10.
 *
 * @author bryansharp
 * Project: FirstGradle
 * introduction:
 */
public class ModifyFiles {


    public static void modify(Map<String, Map<String, Object>> taskMap) {
        final File tempDir = DataHelper.ext.hiBeaverTempDir;
        taskMap.entrySet().forEach({
            entry ->
                String path = entry.getKey();
                Map<String, Object> map = entry.getValue();
                Util.initTargetClasses(map)
                File targetFile = new File(path)
                def type = isSupportFile(targetFile)
                switch (type) {
                    case Const.TY_AAR:
                        modifyAar(targetFile, map);
                        break;
                    case Const.TY_JAR:
                        modifyJar(targetFile, map, tempDir, false);
                        break;
                }
        })
    }

    public static JarFile unzipEntryToTemp(JarEntry element, JarFile zipFile) {
        def stream = zipFile.getInputStream(element);
        def array = IOUtils.toByteArray(stream);
        String hex = DigestUtils.md5Hex(element.getName());
        final File tempDir = DataHelper.ext.hiBeaverTempDir;
        File targetFile = new File(tempDir, hex + ".jar");
        if (targetFile.exists()) {
            targetFile.delete()
        } else {
            targetFile.createNewFile()
        }
        new FileOutputStream(targetFile).write(array)
        return new JarFile(targetFile)
    }

    public static File modifyJar(File jarFile, Map<String, Object> modifyMatchMaps, File tempDir, boolean nameHex) {
        /**
         * 读取原jar
         */
        def file = new JarFile(jarFile);
        /** 设置输出到的jar */
        def hexName = "";
        if (nameHex) {
            hexName = DigestUtils.md5Hex(jarFile.absolutePath).substring(0, 8);
        }
        def outputJar = new File(tempDir, hexName + jarFile.name)
        JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(outputJar));
        Enumeration enumeration = file.entries();
        while (enumeration.hasMoreElements()) {
            JarEntry jarEntry = (JarEntry) enumeration.nextElement();
            InputStream inputStream = file.getInputStream(jarEntry);

            String entryName = jarEntry.getName();
            String className

            ZipEntry zipEntry = new ZipEntry(entryName);

            jarOutputStream.putNextEntry(zipEntry);

            byte[] modifiedClassBytes = null;
            byte[] sourceClassBytes = IOUtils.toByteArray(inputStream);
            if (entryName.endsWith(".class")) {
                className = Util.path2Classname(entryName)
                String key = Util.shouldModifyClass(className)
                if (modifyMatchMaps != null && key != null) {
                    modifiedClassBytes = ModifyClassUtil.modifyClasses(className, sourceClassBytes, modifyMatchMaps.get(key));
                }
            }
            if (modifiedClassBytes == null) {
                jarOutputStream.write(sourceClassBytes);
            } else {
                jarOutputStream.write(modifiedClassBytes);
            }
            jarOutputStream.closeEntry();
        }
//            Log.info("${hexName} is modified");
        jarOutputStream.close();
        file.close();
        return outputJar;
    }

    public static int modifyAar(File targetFile, Map<String, Object> map) {
        final File hiBeaverDir = DataHelper.ext.hiBeaverDir;
        final File tempDir = DataHelper.ext.hiBeaverTempDir;
        JarFile zipFile = new JarFile(targetFile);
        Enumeration<JarEntry> entries = zipFile.entries();

        def outputAar = new File(hiBeaverDir, targetFile.name)
        if (outputAar.exists()) {
            outputAar.delete()
        }

        JarOutputStream outputAarStream = new JarOutputStream(new FileOutputStream(outputAar))
        while (entries.hasMoreElements()) {
            JarEntry element = entries.nextElement();
            def name = element.getName();
            if (name.endsWith(".jar")) {
                JarFile innerJar = unzipEntryToTemp(element, zipFile);
                def outJar = modifyJar(innerJar, map, tempDir, true);
                outputAarStream.write(IOUtils.toByteArray(new FileInputStream(outJar)))
            } else {
                outputAarStream.write(IOUtils.toByteArray(zipFile.getInputStream(element)))
            }
        }
    }

    public static int isSupportFile(File targetFile) {
        def name = targetFile.getName();
        if (name.endsWith(".jar")) {
            return Const.TY_JAR;
        } else if (name.endsWith(".aar")) {
            return Const.TY_AAR;
        }
        return -1;
    }
}