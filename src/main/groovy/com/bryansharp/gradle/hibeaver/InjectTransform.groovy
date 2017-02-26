package com.bryansharp.gradle.hibeaver

import com.android.annotations.NonNull
import com.android.annotations.Nullable
import com.android.build.api.transform.*
import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.pipeline.TransformManager
import com.bryansharp.gradle.hibeaver.utils.DataHelper
import com.bryansharp.gradle.hibeaver.utils.Log
import com.bryansharp.gradle.hibeaver.utils.ModifyClassUtil
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.gradle.api.Project

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

/**
 * Created by MrBu(bsp0911932@163.com) on 2016/5/7.
 *
 * @author bushaopeng
 *         Project: FirstGradle
 *         introduction:
 */
public class InjectTransform extends Transform {
    static AppExtension android
    static HashSet<String> targetClasses = [];
    private static Project project;

    public InjectTransform(Project project) {
        InjectTransform.project = project
    }

    @Override
    String getName() {
        return "HiBeaver"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    public void transform(
            @NonNull Context context,
            @NonNull Collection<TransformInput> inputs,
            @NonNull Collection<TransformInput> referencedInputs,
            @Nullable TransformOutputProvider outputProvider,
            boolean isIncremental) throws IOException, TransformException, InterruptedException {
        Log.info "==============hiBeaver ${project.hiBeaver.hiBeaverModifyName + ' '}transform enter=============="
        android = project.extensions.getByType(AppExtension)
//        String flavorAndBuildType = context.name.split("For")[1]
//        Log.info("flavorAndBuildType ${flavorAndBuildType}")
        targetClasses = [];
        Map<String, List<Map<String, Object>>> modifyMatchMaps = project.hiBeaver.modifyMatchMaps;
        targetClasses.addAll(modifyMatchMaps.keySet());
        /**
         * 获取所有依赖的classPaths
         */
        def classPaths = []
        String buildTypes
        String productFlavors
        inputs.each { TransformInput input ->
            input.directoryInputs.each { DirectoryInput directoryInput ->
                classPaths.add(directoryInput.file.absolutePath)
                buildTypes = directoryInput.file.name
                productFlavors = directoryInput.file.parentFile.name
            }
            input.jarInputs.each { JarInput jarInput ->
                classPaths.add(jarInput.file.absolutePath)
                Log.info("项目包含的jar包：${jarInput.file.absolutePath}");
            }
        }

        def paths = [android.bootClasspath.get(0).absolutePath/*, injectClassPath*/]
        paths.addAll(classPaths)
        /**遍历输入文件*/
        inputs.each { TransformInput input ->
            /**
             * 遍历jar
             * JarInput和DirectoryInput两个接口都继承自QualifiedContent这个接口
             * 他们的scope属性（枚举，类型为QualifiedContent.Scope）表明这个Input所属的类型可见源码注释
             * @see QualifiedContent.Scope
             */
            input.jarInputs.each { JarInput jarInput ->
                String destName = jarInput.file.name;
                /** 重名名输出文件,因为可能同名,会覆盖*/
                def hexName = DigestUtils.md5Hex(jarInput.file.absolutePath).substring(0, 8);
                if (destName.endsWith(".jar")) {
                    destName = destName.substring(0, destName.length() - 4);
                }
                /** 获得输出文件*/
                File dest = outputProvider.getContentLocation(destName + "_" + hexName, jarInput.contentTypes, jarInput.scopes, Format.JAR);
                def modifiedJar = null;
                if (isJarNeedModify(jarInput.file)) {
                    modifiedJar = modifyJarFile(jarInput.file, context.getTemporaryDir());
                }
                if (modifiedJar == null) {
                    modifiedJar = jarInput.file;
                } else {
                    saveModifiedJarForCheck(modifiedJar);
                }
                FileUtils.copyFile(modifiedJar, dest);
            }
            /**
             * 遍历目录
             * 目前对目录不做处理，直接复制
             */
            input.directoryInputs.each { DirectoryInput directoryInput ->
                File dest = outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY);
                Log.info("dest dir  ${dest.absolutePath}")
                FileUtils.copyDirectory(directoryInput.file, dest);
            }
        }
    }

    private static void saveModifiedJarForCheck(File optJar) {
        File dir = DataHelper.ext.hiBeaverDir;
        File checkJarFile = new File(dir, optJar.getName());
        if (checkJarFile.exists()) {
            checkJarFile.delete();
        }
        FileUtils.copyFile(optJar, checkJarFile);
    }

    static boolean shouldModifyClass(String className) {
        if (project.hiBeaver.enableModify) {
            return targetClasses.contains(className)
        } else {
            return false;
        }
    }

    /**
     * 植入代码
     * @param buildDir 是项目的build class目录,就是我们需要注入的class所在地
     * @param lib 这个是hackdex的目录,就是AntilazyLoad类的class文件所在地
     */
    public static File modifyJarFile(File jarFile, File tempDir) {
        if (jarFile) {
            /** 设置输出到的jar */
            def hexName = DigestUtils.md5Hex(jarFile.absolutePath).substring(0, 8);
            def optJar = new File(tempDir, hexName + jarFile.name)
            JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(optJar));
            /**
             * 读取原jar
             */
            def file = new JarFile(jarFile);
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
                    className = entryName.replace("/", ".").replace(".class", "")
                    if (shouldModifyClass(className)) {
                        modifiedClassBytes = ModifyClassUtil.modifyClasses(className, sourceClassBytes, project.hiBeaver.modifyMatchMaps.get(className));
                    }
                }
                if (modifiedClassBytes == null) {
                    jarOutputStream.write(sourceClassBytes);
                } else {
                    jarOutputStream.write(modifiedClassBytes);
                }
                jarOutputStream.closeEntry();
            }
            Log.info("${hexName} is modified");
            jarOutputStream.close();
            file.close();
            return optJar;
        }
        return null;
    }
    /**
     * 该jar文件是否包含需要修改的类
     * @param jarFile
     * @return
     */
    public static boolean isJarNeedModify(File jarFile) {
        if (jarFile) {
            boolean modified = false;
            /**
             * 读取原jar
             */
            def file = new JarFile(jarFile);
            Enumeration enumeration = file.entries();
            while (enumeration.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) enumeration.nextElement();
                String entryName = jarEntry.getName();
                String className
                if (entryName.endsWith(".class")) {
                    className = entryName.replace("/", ".").replace(".class", "")
                    if (shouldModifyClass(className)) {
                        modified = true;
                    }
                }
            }
            file.close();
            return modified;
        }
    }

    private static void writeStreamWithBuffer(InputStream inputStream, OutputStream out) {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }
}
