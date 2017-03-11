#HiBeaver

![cute animals always busy in building their river dam](https://github.com/BryanSharp/hibeaver/blob/master/beaver.jpeg?raw=true)

###简介

HiBeaver是一个用于进行Android字节码插桩的Gradle插件，可以用来实现Android轻量级AOP设计。结合强大的Java ASM字节码修改工具和Gradle Transform API，HiBeaver可以实现在Android应用编译阶段，依据使用者的配置，对工程内所包含的Java字节码进行修改，从而支持使用者仅通过Gradle配置对字节码进行代码注入和AOP设计，或对项目依赖的Jar包内的代码增加Hook节点。

Beaver，即河狸，是一种日日忙碌于在自己栖息河流上修建和装修大坝的可爱小动物。河狸的堤坝虽说不上像三峡那样“高峡出平湖”，却也为自然和生态做出了暖男般的贡献。

###快速上手

该插件已经上传到Jcenter,可直接引用如下：

    classpath 'com.bryansharp:HiBeaver:1.2.3'

[Link to Jcenter](https://bintray.com/bsp0911932/maven/HiBeaver)

然后在工程的build.gradle里加入如下片段（或通过其他.gradle引入）：

    import com.bryansharp.gradle.hibeaver.utils.MethodLogAdapter
    import org.objectweb.asm.ClassVisitor
    import org.objectweb.asm.MethodVisitor
    import org.objectweb.asm.Opcodes
    //或者通过下面这行统一引入
    //import org.objectweb.asm.*
    hiBeaver {
        //下面这个参数仅仅影响log输出，为本次修改命名，无实际意义
        hiBeaverModifyName = 'myHibeaverTest'
        //设置为true可以显示帮助内容，默认为true
        showHelp = true
        //keepQuiet默认为false,为true时不会有字节码修改的log输出，建议为false
        keepQuiet = false
        //下面的参数设置为true时会输出工程编译耗时信息
        watchTimeConsume = false

        //重头戏是配置下面的参数：modifyMatchMaps
        //基础配置结构形如： ['class':[[:],[:]],'class':[[:],[:]]], type is Map<String, List<Map<String, Object>>>
        //高级配置结构形如: 
        //[
        //   'classMatchPattern':
        //     [
        //        'classMatchType':'wildcard',
        //        'modifyMethods':[ [:], [:] ]
        //     ]
        //     ,
        //   'classMatchPattern':
        //     [
        //        'classMatchType':'regEx',
        //        'modifyMethods':[ [:], [:] ]
        //     ]
        //]
        modifyMatchMaps = [
                //this is the basic version
                'classname of which to be modified': [
                        // 用javap -s 命令来查看类中方法的description
                        // adapter 的值为一个closure
                        ['methodName': 'the name of the method', 'methodDesc': 'javap -s to get the description', 'adapter': {
                            //以下closure中的参数不可以改变顺序，缺一不可
                            ClassVisitor cv, int access, String name, String desc, String signature, String[] exceptions ->
                                //return null to modify nothing
                                return null;
                        }]
                        ,
                        ['methodName': 'the name of the method2', 'methodDesc': 'javap -s to get the description', 'adapter': {
                            ClassVisitor cv, int access, String name, String desc, String signature, String[] exceptions ->
                                return null;
                        }]
                ]
                ,
                //高级配置，可以设置类的批量匹配
                '*Activity'                       : [
                        //匹配规则为三种类型之一: all,regEx,wildcard
                        //默认是all
                        //wildcard匹配仅支持*通配符，*代表任意长度（>0）的任意字符
                        'classMatchType': 'wildcard',
                        'modifyMethods' : [
                                //methodMatchType会同时对methodName和methodDesc的匹配生效
                                //methodDesc设置为空代表对methodDesc不进行限制
                                ['methodName': 'on**', 'methodMatchType': 'wildcard', 'methodDesc': null, 'adapter': {
                                    ClassVisitor cv, int access, String name, String desc, String signature, String[] exceptions ->
                                        MethodVisitor methodVisitor = cv.visitMethod(access, name, desc, signature, exceptions);
                                        MethodVisitor adapter = new MethodLogAdapter(methodVisitor) {
                                            @Override
                                            void visitCode() {
                                                super.visitCode();
                                                methodVisitor.visitLdcInsn(desc);
                                                methodVisitor.visitLdcInsn(name);
                                                methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, "bruce/com/testhibeaver/MainActivity", "hookXM", "(Ljava/lang/Object;Ljava/lang/Object;)V");
                                            }
                                        }
                                        return adapter;
                                }]
                        ]
                ]
                ,
                //regEx即正则表达式匹配
                '.*D[a-zA-Z]*Receiver'                       : [
                        'classMatchType': 'regEx',
                        'modifyMethods' : [
                                ['methodName': 'on**', 'methodMatchType': 'wildcard', 'methodDesc': null, 'adapter': {
                                    ClassVisitor cv, int access, String name, String desc, String signature, String[] exceptions ->
                                        MethodVisitor methodVisitor = cv.visitMethod(access, name, desc, signature, exceptions);
                                        MethodVisitor adapter = new MethodLogAdapter(methodVisitor) {
                                            @Override
                                            void visitCode() {
                                                super.visitCode();
                                                methodVisitor.visitLdcInsn(desc);
                                                methodVisitor.visitLdcInsn(name);
                                                methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, "bruce/com/testhibeaver/MainActivity", "hookXM", "(Ljava/lang/Object;Ljava/lang/Object;)V");
                                            }
                                        }
                                        return adapter;
                                }]
                        ]
                ]
        ]
    }

本repo项目中还包含一个submodule，里面有本插件的demo，可以使用git submodule来进行初始化，然后在项目根目录加入settings.gradle并编辑（include ':submodule:app'）来包含这个子项目（是一个app demo）。

玩的愉快！有任何问题和bug请提issue，欢迎参与到本项目的完善中！

##English Version

By applying the regular expression and wildcard features, HiBeaver now has been upgraded to an Android lightweight AOP design tool.

Beaver means 河狸 in Chinese, cute animals always busy in building their cute river dam.

Basically, HiBeaver is an Android plugin for modifying your java byte code during the building of your package.

This plugin has been uploaded to jcenter. You can use this by adding the following code to your buildScripts:

    classpath 'com.bryansharp:HiBeaver:1.2.3'

[Link to Jcenter](https://bintray.com/bsp0911932/maven/HiBeaver)

and then add this to you app build scripts:

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
        //basic structure is like ['class':[[:],[:]],'class':[[:],[:]]], type is Map<String, List<Map<String, Object>>>
        //advanced structure is like: ['classMatchPattern':['classMatchType':'wildcard','modifyMethods':[[:],[:]]],'classMatchPattern':['classMatchType':'regEx','modifyMethods':[[:],[:]]]]
        modifyMatchMaps = [
                //this is the basic version
                'classname of which to be modified': [
                        // you can use javap -s command to get the description of one method
                        // the adapter is a closure
                        ['methodName': 'the name of the method', 'methodDesc': 'javap -s to get the description', 'adapter': {
                            //the below args cannot be changed, to copy them entirely with nothing changed is recommended
                            ClassVisitor cv, int access, String name, String desc, String signature, String[] exceptions ->
                                //return null to modify nothing
                                return null;
                        }]
                        ,
                        ['methodName': 'the name of the method2', 'methodDesc': 'javap -s to get the description', 'adapter': {
                            ClassVisitor cv, int access, String name, String desc, String signature, String[] exceptions ->
                                return null;
                        }]
                ]
                ,
                //the latter ones are advanced cases
                '*Activity'                       : [
                        //the value of classMatchType can either be one of the three: all,regEx,wildcard
                        //default value is all
                        'classMatchType': 'wildcard',
                        'modifyMethods' : [
                                //methodMatchType会同时对methodName和methodDesc的匹配生效
                                //methodDesc设置为空代表对methodDesc不进行限制
                                ['methodName': 'on**', 'methodMatchType': 'wildcard', 'methodDesc': null, 'adapter': {
                                    ClassVisitor cv, int access, String name, String desc, String signature, String[] exceptions ->
                                        MethodVisitor methodVisitor = cv.visitMethod(access, name, desc, signature, exceptions);
                                        MethodVisitor adapter = new MethodLogAdapter(methodVisitor) {
                                            @Override
                                            void visitCode() {
                                                super.visitCode();
                                                methodVisitor.visitLdcInsn(desc);
                                                methodVisitor.visitLdcInsn(name);
                                                methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, "bruce/com/testhibeaver/MainActivity", "hookXM", "(Ljava/lang/Object;Ljava/lang/Object;)V");
                                            }
                                        }
                                        return adapter;
                                }]
                        ]
                ]
                ,
                '.*D[a-zA-Z]*Receiver'                       : [
                        'classMatchType': 'regEx',
                        'modifyMethods' : [
                                ['methodName': 'on**', 'methodMatchType': 'wildcard', 'methodDesc': null, 'adapter': {
                                    ClassVisitor cv, int access, String name, String desc, String signature, String[] exceptions ->
                                        MethodVisitor methodVisitor = cv.visitMethod(access, name, desc, signature, exceptions);
                                        MethodVisitor adapter = new MethodLogAdapter(methodVisitor) {
                                            @Override
                                            void visitCode() {
                                                super.visitCode();
                                                methodVisitor.visitLdcInsn(desc);
                                                methodVisitor.visitLdcInsn(name);
                                                methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, "bruce/com/testhibeaver/MainActivity", "hookXM", "(Ljava/lang/Object;Ljava/lang/Object;)V");
                                            }
                                        }
                                        return adapter;
                                }]
                        ]
                ]
        ]
    }

You can also see the content above in the build log outputs.

There is also a demo showing how to use it. You can either get it through git submodule and add a settings.gradle file to include the module, or get it by checking out [hiBeaverDemo](https://github.com/BryanSharp/hiBeaverDemo).

Hope you can enjoy it! Any comment and suggestion is welcomed.
