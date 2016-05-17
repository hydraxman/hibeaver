package com.brucesharpe.gradle.hibeaver

import com.android.build.api.transform.*
import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.pipeline.TransformManager
import com.brucesharpe.gradle.hibeaver.utils.*
import groovy.io.FileType
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.gradle.api.Project

import javax.xml.crypto.dsig.TransformException
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
    static String mLib = "";
    def static applicationName
    static AppExtension android

    private static Project project;

    public InjectTransform(Project project) {
        InjectTransform.project = project
        mLib = "${project.projectDir.absolutePath}${File.separator}hackClass"
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
    void transform(com.android.build.api.transform.Context context, Collection<TransformInput> inputs, Collection<TransformInput> referencedInputs, TransformOutputProvider outputProvider, boolean isIncremental)
            throws IOException, TransformException, InterruptedException {
        if (project.hiBeaver.enablePatchInject) {
            Log.info "HiBeaver patch enabled"
        } else {
            Log.info "HiBeaver patch disabled"
            return
        }
        android = project.extensions.getByType(AppExtension)
        String flavorAndBuildType = context.name.split("For")[1]
        Log.info("flavorAndBuildType ${flavorAndBuildType}")
        /**
         * 拿到application的name
         */
//        def processManifestTask = project.tasks.findByName("process${flavorAndBuildType.capitalize()}Manifest")
//        File manifestFile = processManifestTask.inputs.files.files[0]

//        processManifestTask.inputs.files.files.each{
//            Log.info "mmmmmm="+it.absolutePath
//        }
//        Log.info "找到manifest:${manifestFile.absolutePath},是否存在 ${manifestFile.exists()}"
//        if (!manifestFile.exists()) {
//            Log.info "没有找到manifest文件，无法完成注入"
//            return
//        }
        File manifestFile = android.sourceSets.getByName("main").manifest.srcFile
        applicationName = getApplicationName(manifestFile)
        Log.info "applicationName ${applicationName}"

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
            }
        }
        /**
         * 取出inject.jar
         */
        def injectJarFile = new File(context.getTemporaryDir(), "inject.jar")
        injectJarFile.createNewFile()
        def output = new FileOutputStream(injectJarFile)
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("inject.jar")
        writeStreamWithBuffer(inputStream, output)
        output.close()
        inputStream.close()
        def injectClassPath = injectJarFile.absolutePath
        def paths = [android.bootClasspath.get(0).absolutePath, injectClassPath]
        paths.addAll(classPaths)
        InjectUtil.initClassPathList(paths)
        DataHelper.ext.classMap = [:]
        /**遍历输入文件*/
        inputs.each { TransformInput input ->
            /**遍历jar*/
            input.jarInputs.each { JarInput jarInput ->
                if ([QualifiedContent.Scope.PROJECT,
                     QualifiedContent.Scope.SUB_PROJECTS].containsAll(jarInput.scopes)) {
                    String destName = jarInput.name;
                    /** 重名名输出文件,因为可能同名,会覆盖*/
                    def hexName = DigestUtils.md5Hex(jarInput.file.absolutePath);
                    if (destName.endsWith(".jar")) {
                        destName = destName.substring(0, destName.length() - 4);
                    }
                    /** 获得输出文件*/
                    File dest = outputProvider.getContentLocation(destName + "_" + hexName, jarInput.contentTypes, jarInput.scopes, Format.JAR);
                    def optJar = injectJarFiles(jarInput.file, context.getTemporaryDir())
                    FileUtils.copyFile(optJar, dest);
                    optJar.delete()
                }
            }
            /**遍历目录*/
            input.directoryInputs.each { DirectoryInput directoryInput ->
                if ([QualifiedContent.Scope.PROJECT,
                     QualifiedContent.Scope.SUB_PROJECTS].containsAll(directoryInput.scopes)){
                    /**获得产物的目录*/
                    File dest = outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY);
                    injectAllClassFiles(directoryInput.file);
                    /**处理完后拷到目标文件*/
                    FileUtils.copyDirectory(directoryInput.file, dest);
                }
            }
        }
        saveInjectedClasses(flavorAndBuildType);
        producePatch()
        injectJarFile.delete()
    }

    def static saveInjectedClasses(String flavorAndBuildType) {
        Map classMap = DataHelper.ext.classMap
        File f = new File(DataHelper.ext.patchDir, "patchableClassesHash.txt");
        if (f.exists()) {
            f.delete()
        } else {
            f.createNewFile()
        }
        f.append("以下类经过注入，可以进行热修复，此文件用于校验类的改变，请不要修改:\n", 'utf-8')
        classMap.each {
            f.append("${it.key}${PluginUtil.DIVIDER}${it.value}\n")
        }
        //写完后保存到ouputs目录
        android.applicationVariants.all {
            variant ->
                if (variant.name.toLowerCase().equals(flavorAndBuildType.toLowerCase())) {
                    variant.outputs.each {
                        output ->
                            FileUtils.copyFile(f, new File(output.outputFile.parentFile, f.name))
                    }
                }
        }
    }

    def static producePatch() {
        if (DataHelper.ext.producePatch.producePatchEnable) {
            DexUtil.dex(project, DataHelper.ext.patchTempDir)
        }
    }
    /**
     * 植入代码
     * @param buildDir 是项目的build class目录,就是我们需要注入的class所在地
     * @param lib 这个是hackdex的目录,就是AntilazyLoad类的class文件所在地
     */
    public static void injectThisClass(File classFile, String buildDir) {
        def className = classFile.absolutePath.replace("${buildDir}${File.separator}", "").replace(File.separator, ".").replace(".class", "")
        def classSimpleName = classFile.name
        if (shouldInjectClass(classSimpleName, className)) {
            InjectUtil.injectConstructionClasses(className, buildDir, null, getOldClassHash(className))
        }
    }

    static boolean shouldInjectClass(String classSimpleName, String className) {
        def shouldInject = true
        if (classSimpleName.startsWith("R")) {
            shouldInject = false
        }
        if (classSimpleName.startsWith("BuildConfig")) {
            shouldInject = false
        }
        if (isApplication(className)) {
            shouldInject = false
        }
        if (!shouldInject) {
            Log.info "忽略类：${className}"
        }
        shouldInject
    }

    static boolean isApplication(String className) {
        if (className.contains(applicationName)) return true
        false
    }
    /**
     * 植入代码
     * @param buildDir 是项目的build class目录,就是我们需要注入的class所在地
     * @param lib 这个是hackdex的目录,就是AntilazyLoad类的class文件所在地
     */
    public static void injectAllClassFiles(File classDir) {
        def dir = new File(classDir.absolutePath);
        dir.traverse(type: FileType.FILES, nameFilter: ~/.*\.class/) {
            File file ->
                injectThisClass(file, classDir.absolutePath)
        }
    }
    /**
     * 植入代码
     * @param buildDir 是项目的build class目录,就是我们需要注入的class所在地
     * @param lib 这个是hackdex的目录,就是AntilazyLoad类的class文件所在地
     */
    public static String getOldClassHash(String className) {
        if (DataHelper.ext.oldClassHashMap) {
            DataHelper.ext.oldClassHashMap.get(className)
        }
    }
    /**
     * 植入代码
     * @param buildDir 是项目的build class目录,就是我们需要注入的class所在地
     * @param lib 这个是hackdex的目录,就是AntilazyLoad类的class文件所在地
     */
    public static File injectJarFiles(File jarFile, File tempDir) {
        if (jarFile) {
            def optJar = new File(tempDir, jarFile.name)
            def file = new JarFile(jarFile);
            Enumeration enumeration = file.entries();
            JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(optJar));
            while (enumeration.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) enumeration.nextElement();
                String entryName = jarEntry.getName();
                String className
                ZipEntry zipEntry = new ZipEntry(entryName);
                InputStream inputStream = file.getInputStream(jarEntry);
                jarOutputStream.putNextEntry(zipEntry);
                if (entryName.endsWith(".class")) {
                    className = entryName.replace("/", ".").replace(".class", "")
                    String classSimpleName = entryName.substring(className.lastIndexOf("."))
                    if (shouldInjectClass(classSimpleName, className)) {
                        InjectUtil.injectConstructionClasses(className, null, jarOutputStream, getOldClassHash(className))
                    } else {
                        jarOutputStream.write(IOUtils.toByteArray(inputStream));
                    }
                } else {
                    jarOutputStream.write(IOUtils.toByteArray(inputStream));
                }
                jarOutputStream.closeEntry();

            }
            jarOutputStream.close();
            file.close();
            return optJar;
        }
    }
    /**
     *
     * @param manifestFile manifest文件
     * @return Application类的pathName
     */
    public static String getApplicationName(File manifestFile) {
        def manifest = new XmlParser().parse(manifestFile)
        //获取android命名空间下的所有tag名字
        def androidTag = new groovy.xml.Namespace("http://schemas.android.com/apk/res/android", 'android')
        String appName = manifest.application[0].attribute(androidTag.name)
        if (appName.startsWith(".")) {
            appName = manifest.attribute("package") + appName
        }
        return appName;
    }

    private static void writeStreamWithBuffer(InputStream inputStream, OutputStream out) {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }
}
