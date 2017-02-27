#HiBeaver

![cute animals always busy in building their river dam](https://github.com/BryanSharp/hibeaver/blob/master/beaver.jpeg?raw=true)

Beaver means 河狸 in Chinese, cute animals always busy in building their cute river dam.

HiBeaver is an Android plugin for modifying your library jars byte code.

Dress them as if they are naked! Yeah~

This plugin has been uploaded to jcenter. You can use this by adding the following code to your buildScripts:

    classpath 'com.bryansharp:HiBeaver:1.2.2'

[Link to Jcenter](https://bintray.com/bsp0911932/maven/HiBeaver)

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
                'the classname of which you want to modify': [
                        // you can use javap -s command to get the description of one method
                        ['methodName': 'name of the method', 'methodDesc': 'method description', 'adapter': {
                            ClassVisitor cv, int access, String name, String desc, String signature, String[] exceptions ->
                                //return the following part to check the method byte code
                                return new MethodLogAdapter(cv.visitMethod(access, name, desc, signature, exceptions));
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
