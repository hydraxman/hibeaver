#HiBeaver

![cute animals always busy in building their river dam](https://github.com/BryanSharp/hibeaver/blob/master/beaver.jpeg?raw=true)

Beaver means 河狸 in Chinese, cute animals always busy in building their great river dam.

HiBeaver is an Android plugin for modifying your library jars byte code.

Dress them as if they are naked! Yeah~

This plugin has been uploaded to jcenter. You can use this by adding the following code to your buildScripts:

    classpath 'com.bryansharp:HiBeaver:1.2.2'

and then add this to you app build scripts:

    apply plugin: 'hiBeaver'
    import com.bryansharp.gradle.hibeaver.utils.MethodLogAdapter
    import org.objectweb.asm.ClassVisitor
    import org.objectweb.asm.MethodVisitor
    import org.objectweb.asm.Opcodes
    //or you can import like bellow:
    //import org.objectweb.asm.*
    hiBeaver {
        //this will determine the name of this hibeaver transform, no practical use.
        hiBeaverModifyName = 'myHibeaverTest'
        //turn this on to make it print help content, default value is true
        showHelp = true
        //this flag will decide whether the log of the modifying process be printed or not, default value is false
        keepQuiet = false
        //this is a kit feature of the plugin, set it true to see the time consume of this build
        watchTimeConsume = false
    
        //this is the most important part
        modifyMatchMaps = [
                'com.xiaomi.mipush.sdk.u': [
                        // you can use javap -s command to get the description of one method
                        ['methodName': 'a', 'methodDesc': '(Lorg/apache/thrift/a;Lcom/xiaomi/xmpush/thrift/a;ZLcom/xiaomi/xmpush/thrift/r;)V', 'adapter': {
                            ClassVisitor cv, int access, String name, String desc, String signature, String[] exceptions ->
                                MethodVisitor methodVisitor = cv.visitMethod(access, name, desc, signature, exceptions);
                                MethodVisitor adapter = new MethodLogAdapter(methodVisitor) {
                                    @Override
                                    void visitCode() {
                                        super.visitCode();
                                        methodVisitor.visitVarInsn(Opcodes.ALOAD, 1);
                                        methodVisitor.visitVarInsn(Opcodes.ALOAD, 2);
                                        methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, MethodLogAdapter.className2Path("bruce.com.testhibeaver.MainActivity"), "hookXM", "(Ljava/lang/Object;Ljava/lang/Object;)V");
                                    }
                                }
                                return adapter;
                        }]
                ],
                'okhttp3.internal.http.HttpEngine': [
                        ['methodName': '<init>', 'methodDesc': null, 'adapter': {
                            ClassVisitor cv, int access, String name, String desc, String signature, String[] exceptions ->
                                MethodVisitor methodVisitor = cv.visitMethod(access, name, desc, signature, exceptions);
                                MethodVisitor adapter = new MethodLogAdapter(methodVisitor) {
                                    @Override
                                    void visitCode() {
                                        super.visitCode();
                                        methodVisitor.visitVarInsn(Opcodes.ALOAD, 1);
                                        methodVisitor.visitVarInsn(Opcodes.ALOAD, 2);
                                        methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, "bruce/com/testhibeaver/MainActivity", "hookXM", "(Ljava/lang/Object;Ljava/lang/Object;)V");
                                    }
                                }
                                return adapter;
                        }]
                ],
        ]
    }

You can also see the content above in the build log outputs.

There is also a demo showing how to use it. You can either get it through git submodule and add a settings.gradle file to include the module, or get it by checking out [hiBeaverDemo](https://github.com/BryanSharp/hiBeaverDemo).

This plugin is also uploaded to jitpack. You can use jitpack version by adding the following code to your buildScripts:

    classpath 'com.github.BryanSharp:hibeaver:1.2.1'

using jitpack version, its Maven Repo is needed, add this as well:

    maven { url 'https://jitpack.io' }

Hope you can enjoy it! Any comment and suggestion is welcomed.