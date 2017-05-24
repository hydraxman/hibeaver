package com.bryansharp.gradle.hibeaver

import com.android.annotations.NonNull
import com.android.annotations.Nullable
import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.bryansharp.gradle.hibeaver.entity.MethodInfo
import com.bryansharp.gradle.hibeaver.utils.DataHelper
import com.bryansharp.gradle.hibeaver.utils.EraserUtil
import com.bryansharp.gradle.hibeaver.utils.ModifyClassUtil
import com.bryansharp.gradle.hibeaver.utils.Util
import groovy.io.FileType
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
/**
 * Created by bryansharp(bsp0911932@163.com) on 2016/5/7.
 *
 * @author bryansharp
 *         Project: FirstGradle
 *         introduction:
 */
public class EraserTransform extends Transform {

    @Override
    String getName() {
        return "HiBeaverEraser"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<QualifiedContent.Scope> getScopes() {
        if (DataHelper.ext.projectType == DataHelper.TYPE_APP) {
            return TransformManager.SCOPE_FULL_PROJECT
        } else if (DataHelper.ext.projectType == DataHelper.TYPE_LIB) {
            return TransformManager.SCOPE_FULL_LIBRARY
        }
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
        /**遍历输入文件*/
        inputs.each { TransformInput input ->
            input.jarInputs.each { JarInput jarInput ->
                String destName = jarInput.file.name;
                if (destName.endsWith(".jar")) {
                    destName = destName.substring(0, destName.length() - 4);
                }
                /** 获得输出文件*/
                File dest = outputProvider.getContentLocation(destName, jarInput.contentTypes, jarInput.scopes, Format.JAR);
                def modifiedJar = jarInput.file;
                FileUtils.copyFile(modifiedJar, dest);
            }
            /**
             * 遍历目录
             */
            input.directoryInputs.each { DirectoryInput directoryInput ->
                File dest = outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY);
//                Log.info("dest dir  ${dest.absolutePath}")
                File dir = directoryInput.file
                if (dir) {
                    dir.traverse(type: FileType.FILES, nameFilter: ~/.*\.class/) {
                        File classFile ->
                            collectMethodInfos(classFile);
                    }
//                    dir.traverse(type: FileType.FILES, nameFilter: ~/.*\.class/) {
//                        File classFile ->
//                            File modified = modifyClassFile(dir, classFile, context.getTemporaryDir());
//                            if (modified != null) {
//                                //key为相对路径
//                                modifyMap.put(classFile.absolutePath.replace(dir.absolutePath, ""), modified);
//                            }
//                    }
                    FileUtils.copyDirectory(directoryInput.file, dest);
//                    modifyMap.entrySet().each {
//                        Map.Entry<String, File> en ->
//                            File target = new File(dest.absolutePath + en.getKey());
//                            Log.info(target.getAbsolutePath());
//                            if (target.exists()) {
//                                target.delete();
//                            }
//                            FileUtils.copyFile(en.getValue(), target);
//                            saveModifiedJarForCheck(en.getValue());
//                            en.getValue().delete();
//                    }
                }
            }
        }
    }

    public static File modifyClassFile(File dir, File classFile, File tempDir) {
        File modified;
        try {
            String className = Util.path2Classname(classFile.absolutePath.replace(dir.absolutePath + File.separator, ""));
            Map<String, Object> modifyMatchMaps = Util.getHiBeaver().modifyMatchMaps
            byte[] sourceClassBytes = IOUtils.toByteArray(new FileInputStream(classFile));
            String key = Util.shouldModifyClass(className)
            if (key != null) {
                byte[] modifiedClassBytes = ModifyClassUtil.modifyClasses(className, sourceClassBytes, modifyMatchMaps.get(key));
                if (modifiedClassBytes) {
                    modified = new File(tempDir, className.replace('.', '') + '.class')
                    if (modified.exists()) {
                        modified.delete();
                    }
                    modified.createNewFile()
                    new FileOutputStream(modified).write(modifiedClassBytes)
                }
            }
        } catch (Exception e) {
        }
        return modified;

    }

    public static List<MethodInfo> collectMethodInfos(File classFile) {
        def infos = []
        byte[] sourceClassBytes = IOUtils.toByteArray(new FileInputStream(classFile));
        return EraserUtil.collectMethods(sourceClassBytes);

    }
}
